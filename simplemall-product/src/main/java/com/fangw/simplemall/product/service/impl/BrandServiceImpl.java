package com.fangw.simplemall.product.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.product.dao.BrandDao;
import com.fangw.simplemall.product.entity.BrandEntity;
import com.fangw.simplemall.product.service.BrandService;
import com.fangw.simplemall.product.service.CategoryBrandRelationService;

@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 获取参数，添加查询条件
        String key = (String)params.get("key");
        LambdaQueryWrapper<BrandEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(key)) {
            wrapper.eq(BrandEntity::getBrandId, key).or().like(BrandEntity::getName, key);
        }

        IPage<BrandEntity> page = this.page(new Query<BrandEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void updateDetail(BrandEntity brand) {
        // 更新自己
        this.updateById(brand);

        // 取出brand名字信息
        Long brandId = brand.getBrandId();
        String name = brand.getName();

        // 更新别的表
        if (StringUtils.isNotBlank(name)) {
            categoryBrandRelationService.updateBrand(brandId, name);

            // todo:其他表
        }
    }

    @Override
    public List<BrandEntity> getBrandsByIds(List<Long> brandIds) {
        return list(new LambdaQueryWrapper<BrandEntity>().in(BrandEntity::getBrandId, brandIds));
    }

}