package com.fangw.simplemall.order.vo;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class OrderItemVo {
    /**
     * 商品Id
     */
    private Long skuId;
    /**
     * 商品标题
     */
    private String title;
    /**
     * 商品图片
     */
    private String image;
    /**
     * 商品套餐信
     */
    private List<String> skuAttr;
    /**
     * 商品价格
     */
    private BigDecimal price;
    /**
     * 数量
     */
    private Integer count;
    /**
     * 小计价格
     */
    private BigDecimal totalPrice;
}
