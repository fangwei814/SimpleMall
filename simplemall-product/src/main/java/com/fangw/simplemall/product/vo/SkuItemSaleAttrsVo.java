package com.fangw.simplemall.product.vo;

import java.util.List;

import lombok.Data;

// 销售属性组合
@Data
public class SkuItemSaleAttrsVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
