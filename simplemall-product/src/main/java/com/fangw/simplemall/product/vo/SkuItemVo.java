package com.fangw.simplemall.product.vo;

import java.util.List;

import com.fangw.simplemall.product.entity.SkuImagesEntity;
import com.fangw.simplemall.product.entity.SkuInfoEntity;
import com.fangw.simplemall.product.entity.SpuInfoDescEntity;

import lombok.Data;

@Data
public class SkuItemVo {
    // 1.sku基本信息 pms_sku_info
    private SkuInfoEntity info;

    private boolean hasStock = true;

    // 2.sku的图片信息 pms_sku_images
    private List<SkuImagesEntity> images;

    // 3.获取spu的销售属性组合
    private List<SkuItemSaleAttrsVo> saleAttr;

    // 4.获取spu的介绍 pms_sku_info_desc
    private SpuInfoDescEntity desp;

    // 5.获取spu的规格参数信息
    private List<SpuItemAttrGroupVo> groupAttrs;
    // 6、当前商品的秒杀优惠信息
    SeckillInfoVo seckillInfo;
}
