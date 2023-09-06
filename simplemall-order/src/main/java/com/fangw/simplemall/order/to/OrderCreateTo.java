package com.fangw.simplemall.order.to;

import java.math.BigDecimal;
import java.util.List;

import com.fangw.simplemall.order.entity.OrderEntity;
import com.fangw.simplemall.order.entity.OrderItemEntity;

import lombok.Data;

@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    /** 订单计算的应付价格 **/
    private BigDecimal payPrice;

    /** 运费 **/
    private BigDecimal fare;

}
