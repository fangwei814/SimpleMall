package com.fangw.simplemall.product.service.impl;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.product.dao.SkuInfoDao;
import com.fangw.simplemall.product.entity.SkuInfoEntity;
import com.fangw.simplemall.product.service.SkuInfoService;

@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

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

}