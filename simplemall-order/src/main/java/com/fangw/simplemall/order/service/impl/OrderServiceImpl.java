package com.fangw.simplemall.order.service.impl;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.constant.OrderConstant;
import com.fangw.common.exception.NoStockException;
import com.fangw.common.to.OrderTo;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.common.utils.R;
import com.fangw.common.vo.MemberRespVo;
import com.fangw.simplemall.order.dao.OrderDao;
import com.fangw.simplemall.order.entity.OrderEntity;
import com.fangw.simplemall.order.entity.OrderItemEntity;
import com.fangw.simplemall.order.enums.OrderStatusEnum;
import com.fangw.simplemall.order.feign.CartFeignService;
import com.fangw.simplemall.order.feign.MemberFeignService;
import com.fangw.simplemall.order.feign.ProductFeignService;
import com.fangw.simplemall.order.feign.WareFeignService;
import com.fangw.simplemall.order.interceptor.LoginUserInterceptor;
import com.fangw.simplemall.order.service.OrderItemService;
import com.fangw.simplemall.order.service.OrderService;
import com.fangw.simplemall.order.to.OrderCreateTo;
import com.fangw.simplemall.order.vo.*;

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    private static ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();
    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private CartFeignService cartFeignService;
    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(new Query<OrderEntity>().getPage(params), new QueryWrapper<OrderEntity>());

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();

        // 获取主线程的请求域
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 1、远程查询所有的地址列表
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // 将主线程的请求域放在该线程的域中
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> addressVos = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddressVos(addressVos);
        }, executor);

        // 2、远程查询购物车所有选中的购物项
        // 直接远程调用会请求头丢失cookie，在config里面添加拦截器包装回去
        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            // 将主线程的请求域放在该线程的域中
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            // 批量查询商品项库存
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R skuHasStock = wareFeignService.getSkuHasStock(collect);
            if (skuHasStock.getCode() == 0) {
                List<SkuStockVo> data = skuHasStock.getData(new TypeReference<List<SkuStockVo>>() {});
                if (Objects.nonNull(data)) {
                    Map<Long, Boolean> map =
                        data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                    confirmVo.setStocks(map);
                }
            }
        }, executor);

        // 3、查询用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        // 4、其他数据自动计算
        // 5、防重令牌
        String token = UUID.randomUUID().toString().replace("_", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token);
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(addressFuture, cartFuture).join();
        return confirmVo;
    }

    @Override
    @Transactional
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        // 在当前线程共享 OrderSubmitVo
        confirmVoThreadLocal.set(vo);
        SubmitOrderResponseVo response = new SubmitOrderResponseVo();

        // 拿到当前用户
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();

        // 1.验证令牌
        // 先验证，验证成功就删除令牌
        // 因为如果先完成业务再删除令牌，完成业务和删除不是原子的，可能打断然后多次完成业务
        String script =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();

        // 原子验证和删除令牌
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
            Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        if (Objects.isNull(result) || result == 0L) {
            // 令牌验证失败
            response.setCode(1);
            return response;
        } else {
            // 令牌验证成功
            // 2.创建订单
            OrderCreateTo order = createOrder();

            // 3.验证价格
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 4.金额对比成功，保存订单
                saveOrder(order);

                // 5.库存锁定，只要有异常就回滚订单数据
                // 订单号，所有订单项(skuId,skuName,num)
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);

                // 远程锁库存
                // 为了高并发，库存服务自己回滚，可以发消息给库存服务
                // 库存服务本身也可以使用自动解锁模式，采用消息队列的死信路由实现
                R r = wareFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    // 锁成功了
                    response.setOrder(order.getOrder());

                    // todo: 远程服务扣减积分
                    // 订单创建成功发送消息给mq
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());

                    response.setCode(0);
                    return response;
                } else {
                    // 锁定失败
                    throw new NoStockException((String)r.get("msg"));
                }

            } else {
                // 金额对比失败
                response.setCode(2);
                return response;
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return getOne(new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderSn, orderSn));
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        // 1、查询当前这个订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if (Objects.equals(orderEntity.getStatus(), OrderStatusEnum.CREATE_NEW.getCode())) {
            // 2、关单
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);

            // 3、发给MQ一个
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderEntity);
        }
    }

    /**
     * 保存订单、订单项数据
     *
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);

    }

    /**
     * 创建订单
     *
     * @return
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();

        // 1.生成一个订单号
        String orderSn = IdWorker.getTimeId();

        // 2.构建一个订单
        OrderEntity orderEntity = buildOrder(orderSn);
        orderCreateTo.setOrder(orderEntity);

        // 3.获取所有的订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);
        orderCreateTo.setOrderItems(itemEntities);

        // 4.计算价格积分等相关信息
        computePrice(orderEntity, itemEntities);

        return orderCreateTo;
    }

    /**
     * 计算价格
     *
     * @param orderEntity
     * @param itemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");

        // 1、订单的总额，叠加每一个订单项的总额信息
        for (OrderItemEntity entity : itemEntities) {
            total = total.add(entity.getRealAmount());
            coupon = coupon.add(entity.getCouponAmount());
            integration = integration.add(entity.getIntegrationAmount());
            promotion = promotion.add(entity.getPromotionAmount());
            gift = gift.add(new BigDecimal(entity.getGiftIntegration()));
            growth = growth.add(new BigDecimal(entity.getGiftGrowth()));
        }
        // 订单总额
        orderEntity.setTotalAmount(total);
        // 应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setPromotionAmount(promotion);
        // 设置积分等信息
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setDeleteStatus(0); // 0 未删除
    }

    /**
     * 构建所有订单项数据
     *
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (Objects.nonNull(currentUserCartItems) && !currentUserCartItems.isEmpty()) {
            return currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
        }

        return null;
    }

    /**
     * 构建某个订单项
     *
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();

        // 1、订单信息：订单号 v
        // 2、商品的spu信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {});
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());

        // 3、商品的sku信息 v
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        itemEntity.setSkuQuantity(cartItem.getCount());
        itemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";"));

        // 4、优惠信息【不做】
        // 5、积分信息
        itemEntity
            .setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        itemEntity.setGiftIntegration(
            cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());

        // 6、订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        // 当前订单项的实际金额 总额-各种优惠
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = orign.subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getCouponAmount())
            .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);
        return itemEntity;
    }

    /**
     * 构建一个OrderSn的订单
     *
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        MemberRespVo respVp = LoginUserInterceptor.loginUser.get();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(respVp.getId());

        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();

        // 1、获取运费 和 收货信息
        R r = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = r.getData(new TypeReference<FareVo>() {});

        // 2、设置运费
        entity.setFreightAmount(fareResp.getFare());

        // 3、设置收货人信息
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverPhone(fareResp.getAddress().getPhone());

        // 4、设置订单的相关状态信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        // 5、默认取消信息
        entity.setAutoConfirmDay(7);
        return entity;
    }

}