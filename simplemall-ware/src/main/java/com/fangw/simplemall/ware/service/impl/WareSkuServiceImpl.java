package com.fangw.simplemall.ware.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.exception.NoStockException;
import com.fangw.common.to.OrderTo;
import com.fangw.common.to.mq.StockDetailTo;
import com.fangw.common.to.mq.StockLockedTo;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.common.utils.R;
import com.fangw.common.vo.SkuHasStockVo;
import com.fangw.simplemall.ware.dao.WareSkuDao;
import com.fangw.simplemall.ware.entity.WareOrderTaskDetailEntity;
import com.fangw.simplemall.ware.entity.WareOrderTaskEntity;
import com.fangw.simplemall.ware.entity.WareSkuEntity;
import com.fangw.simplemall.ware.feign.OrderFeignService;
import com.fangw.simplemall.ware.feign.ProductFeignService;
import com.fangw.simplemall.ware.service.WareOrderTaskDetailService;
import com.fangw.simplemall.ware.service.WareOrderTaskService;
import com.fangw.simplemall.ware.service.WareSkuService;
import com.fangw.simplemall.ware.vo.OrderItemVo;
import com.fangw.simplemall.ware.vo.OrderVo;
import com.fangw.simplemall.ware.vo.WareSkuLockVo;

import lombok.Data;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    private WareSkuDao wareSkuDao;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private WareOrderTaskService orderTaskService;
    @Autowired
    private WareOrderTaskDetailService orderTaskDetailService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private OrderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /*
        wareId: 123,//仓库id
        skuId: 123//商品id
         */
        LambdaQueryWrapper<WareSkuEntity> wrapper = new LambdaQueryWrapper<>();

        String wareId = (String)params.get("wareId");
        if (StringUtils.isNotBlank(wareId)) {
            wrapper.eq(WareSkuEntity::getWareId, wareId);
        }

        String skuId = (String)params.get("skuId");
        if (StringUtils.isNotBlank(skuId)) {
            wrapper.eq(WareSkuEntity::getSkuId, skuId);
        }

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 如果没有记录就新增
        LambdaQueryWrapper<WareSkuEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WareSkuEntity::getWareId, wareId).eq(WareSkuEntity::getSkuId, skuId);
        long count = count(wrapper);
        if (count == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);

            // sku名字
            try {
                R ret = productFeignService.info(skuId);
                Map<String, Object> skuInfo = (Map<String, Object>)ret.get("skuInfo");
                if (ret.getCode() == 0) {
                    wareSkuEntity.setSkuName((String)skuInfo.get("skuName"));
                }
            } catch (Exception e) {

            }
            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        // 根据skuIds查询总的库存量
        return skuIds.stream().map(skuId -> {
            // 每个id查找库存量
            Long count = baseMapper.getSkuStock(skuId);

            // 设置vo
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(!Objects.isNull(count) && count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean orderLockStock(WareSkuLockVo vo) {
        // 保存库存工作单的详情，用来追溯
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(taskEntity);

        // 1、每个商品在哪个库存里有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHashStock> collect = locks.stream().map(item -> {
            SkuWareHashStock stock = new SkuWareHashStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            // 查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareIdHashSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        // 2、锁定库存
        for (SkuWareHashStock hashStock : collect) {
            boolean skuStocked = false;
            Long skuId = hashStock.getSkuId();
            List<Long> wareIds = hashStock.getWareId();
            if (wareIds == null || wareIds.isEmpty()) {
                // 没有任何仓库有这个商品的库存
                throw new NoStockException(skuId);
            }

            // 如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给mq
            // 如果有商品锁定失败，那么前面所定的就回滚
            for (Long wareId : wareIds) {
                // 成功就返回1，否则就返回0
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hashStock.getNum());
                if (count == 1) {
                    skuStocked = true;

                    // 告诉mq库存锁定成功
                    // 保存库存工作单详情
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null, skuId, "",
                        hashStock.getNum(), taskEntity.getId(), wareId, 1);
                    orderTaskDetailService.save(entity);
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(entity, stockDetailTo);

                    // 只发id不行，防止回滚以后找不到数据
                    lockedTo.setDetailTo(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
                    break;
                } else {
                    // 当前仓库锁失败，重试下一个仓库

                }
            }
            if (!skuStocked) {
                // 当前商品所有仓库都没有锁住，其他商品也不需要锁了，直接返回没有库存了
                throw new NoStockException(skuId);
            }
        }

        // 3、运行到这，全部都是锁定成功的
        return true;
    }

    @Override
    public void unlockStock(StockLockedTo to) {
        StockDetailTo detail = to.getDetailTo();
        Long detailId = detail.getId();

        /*
         * 1、查询数据库关于这个订单的锁库存消息
         *    有，证明库存锁定成功了。
         *      1、没有这个订单。必须解锁
         *      2、有这个订单。不是解锁库存。
         *          订单状态：已取消：解锁库存
         *          订单状态：没取消：不能解锁
         *    没有，库存锁定失败了，库存回滚了。这种情况无需解锁
         */

        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailId);
        if (byId != null) {
            Long id = to.getId(); // 库存工作单的Id，拿到订单号
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn(); // 根据订单号查询订单的状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                // 订单数据返回成功
                OrderVo data = r.getData(new TypeReference<OrderVo>() {});
                if (data == null || data.getStatus() == 4) {
                    // 订单不存在、订单已经被取消了，才能解锁库存
                    if (byId.getLockStatus() == 1) {
                        // 当前库存工作单详情，状态1 已锁定但是未解锁才可以解锁
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                } else {
                    // 消息拒绝以后重新放到队列里面，让别人继续消费解锁
                    throw new RuntimeException("远程服务失败");
                }
            }
        } else {
            // 无需解锁
        }
    }

    @Override
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        // 查一下最新库存的状态，防止重复解锁库存
        WareOrderTaskEntity task = orderTaskService.getOrderTeskByOrderSn(orderSn);
        Long taskId = task.getId();
        // 按照工作单找到所有 没有解锁的库存，进行解锁
        List<WareOrderTaskDetailEntity> entities =
            orderTaskDetailService.list(new LambdaQueryWrapper<WareOrderTaskDetailEntity>()
                .eq(WareOrderTaskDetailEntity::getTaskId, taskId).eq(WareOrderTaskDetailEntity::getLockStatus, 1));
        // 进行解锁
        for (WareOrderTaskDetailEntity entity : entities) {
            unLockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
        }
    }

    private void unLockStock(Long skuId, Long wareId, Integer num, Long detailId) {
        // 库存解锁
        wareSkuDao.unlockStock(skuId, wareId, num);
        // 更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(detailId);
        entity.setLockStatus(2);// 变为已解锁
        orderTaskDetailService.updateById(entity);
    }

    @Data
    class SkuWareHashStock {
        private Long skuId; // skuid
        private Integer num; // 锁定件数
        private List<Long> wareId; // 锁定仓库id
    }

}