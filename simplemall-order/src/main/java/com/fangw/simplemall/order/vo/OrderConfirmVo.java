package com.fangw.simplemall.order.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class OrderConfirmVo {

    /**
     * 收货地址，ums_member_receive_address 表
     */
    @Setter
    @Getter
    List<MemberAddressVo> addressVos;

    /**
     * 所有选中的购物车项
     */
    @Setter
    @Getter
    List<OrderItemVo> items;

    // 发票记录。。。

    /**
     * 优惠券信息
     */
    @Setter
    @Getter
    Integer integration;
    /**
     * 是否有库存
     */
    @Setter
    @Getter
    Map<Long, Boolean> stocks;

    /**
     * 防重令牌
     */
    @Setter
    @Getter
    String OrderToken;

    /**
     * @return 订单总额 所有选中商品项的价格 * 其数量
     */
    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    /**
     * 应付价格
     */
    // BigDecimal pryPrice;
    public BigDecimal getPryPrice() {
        return getTotal();
    }

    public Integer getCount() {
        Integer i = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                i += item.getCount();
            }
        }
        return i;
    }
}
