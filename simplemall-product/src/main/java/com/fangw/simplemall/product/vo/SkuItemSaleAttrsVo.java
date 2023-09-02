package com.fangw.simplemall.product.vo;

import lombok.Data;

// 销售属性组合
@Data
public class SkuItemSaleAttrsVo {
    private Long attrId;
    private String attrName;
    private String attrValues;
}
