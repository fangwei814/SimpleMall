package com.fangw.simplemall.product.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.product.dao.SkuInfoDao;
import com.fangw.simplemall.product.entity.SkuImagesEntity;
import com.fangw.simplemall.product.entity.SkuInfoEntity;
import com.fangw.simplemall.product.entity.SpuInfoDescEntity;
import com.fangw.simplemall.product.service.*;
import com.fangw.simplemall.product.vo.SkuItemSaleAttrsVo;
import com.fangw.simplemall.product.vo.SkuItemVo;
import com.fangw.simplemall.product.vo.SpuItemAttrGroupVo;

@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService saleAttrValueService;
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private AttrGroupService attrGroupService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page =
            this.page(new Query<SkuInfoEntity>().getPage(params), new QueryWrapper<SkuInfoEntity>());

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        /*
        key: '华为',//检索关键字
        catelogId: 0,
        brandId: 0,
        min: 0,
        max: 0
         */
        LambdaQueryWrapper<SkuInfoEntity> wrapper = new LambdaQueryWrapper<>();

        // 1.key
        String key = (String)params.get("key");
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(obj -> {
                obj.eq(SkuInfoEntity::getSkuId, key).or().like(SkuInfoEntity::getSkuName, key);
            });
        }

        // 2.catelogId
        String catelogId = (String)params.get("catelogId");
        if (StringUtils.isNotBlank(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq(SkuInfoEntity::getCatalogId, catelogId);
        }

        // 3.brandId
        String brandId = (String)params.get("brandId");
        if (StringUtils.isNotBlank(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq(SkuInfoEntity::getBrandId, brandId);
        }

        // 4.min
        String min = (String)params.get("min");
        if (StringUtils.isNotBlank(min)) {
            wrapper.ge(SkuInfoEntity::getPrice, min);
        }

        // 5.max
        String max = (String)params.get("max");
        if (StringUtils.isNotBlank(max)) {
            // 判断max是否大于0
            BigDecimal bigMax = new BigDecimal(max);
            if (bigMax.compareTo(new BigDecimal("0")) == 1) {
                wrapper.le(SkuInfoEntity::getPrice, max);
            }
        }

        IPage<SkuInfoEntity> page = page(new Query<SkuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return list(new LambdaQueryWrapper<SkuInfoEntity>().eq(SkuInfoEntity::getSpuId, spuId));
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();

        // 1.sku基本信息
        SkuInfoEntity info = getById(skuId);
        skuItemVo.setInfo(info);

        // 2.图片信息
        // 查询pms_sku_images
        List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
        skuItemVo.setImages(images);

        // 3.销售属性
        // pms_sku_sale_attr_value pms_sku_info
        List<SkuItemSaleAttrsVo> saleAttrsVos = saleAttrValueService.getSaleAttrsBySpuId(info.getSpuId());
        skuItemVo.setSaleAttr(saleAttrsVos);

        // 4.介绍
        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(info.getSpuId());
        skuItemVo.setDesp(spuInfoDescEntity);

        // 5.规格参数
        // 通过category找到所有attrgroup，通过attrgroup和relation查到所有attr，通过product_attr_value查到具体的值
        List<SpuItemAttrGroupVo> attrGroupVos =
            attrGroupService.getAttrGroupWithAttrsBySpuId(info.getSpuId(), info.getCatalogId());
        skuItemVo.setGroupAttrs(attrGroupVos);

        return skuItemVo;
    }

}