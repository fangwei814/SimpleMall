package com.fangw.simplemall.order.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.common.vo.MemberRespVo;
import com.fangw.simplemall.order.dao.OrderDao;
import com.fangw.simplemall.order.entity.OrderEntity;
import com.fangw.simplemall.order.feign.CartFeignService;
import com.fangw.simplemall.order.feign.MemberFeignService;
import com.fangw.simplemall.order.interceptor.LoginUserInterceptor;
import com.fangw.simplemall.order.service.OrderService;
import com.fangw.simplemall.order.vo.MemberAddressVo;
import com.fangw.simplemall.order.vo.OrderConfirmVo;
import com.fangw.simplemall.order.vo.OrderItemVo;

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private CartFeignService cartFeignService;
    @Autowired
    private ThreadPoolExecutor executor;

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
        }, executor);

        // 3、查询用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        // todo: 4、其他数据自动计算
        // todo: 5、防重令牌

        CompletableFuture.allOf(addressFuture, cartFuture);
        return confirmVo;
    }

}