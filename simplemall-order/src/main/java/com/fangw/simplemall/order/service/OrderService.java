package com.fangw.simplemall.order.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.order.entity.OrderEntity;
import com.fangw.simplemall.order.vo.OrderConfirmVo;
import com.fangw.simplemall.order.vo.OrderSubmitVo;
import com.fangw.simplemall.order.vo.SubmitOrderResponseVo;

/**
 * 订单
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:48:03
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 订单确认页数据获取
     *
     * @return
     */
    OrderConfirmVo confirmOrder();

    /**
     * 下单操作
     * 
     * @param vo
     * @return
     */
    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    /**
     * 通过订单号获取订单的详情信息
     * 
     * @param orderSn
     * @return
     */
    OrderEntity getOrderByOrderSn(String orderSn);

    /**
     * 关闭订单
     * 
     * @param entity
     */
    void closeOrder(OrderEntity entity);
}
