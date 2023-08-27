package com.fangw.simplemall.product.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.product.dao.BrandDao;
import com.fangw.simplemall.product.dao.CategoryBrandRelationDao;
import com.fangw.simplemall.product.dao.CategoryDao;
import com.fangw.simplemall.product.entity.BrandEntity;
import com.fangw.simplemall.product.entity.CategoryBrandRelationEntity;
import com.fangw.simplemall.product.entity.CategoryEntity;
import com.fangw.simplemall.product.service.CategoryBrandRelationService;

@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity>
    implements CategoryBrandRelationService {
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private BrandDao brandDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(new Query<CategoryBrandRelationEntity>().getPage(params),
            new QueryWrapper<CategoryBrandRelationEntity>());

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveDetail(CategoryBrandRelationEntity entity) {
        // 通过id查找对象
        CategoryEntity category = categoryDao.selectById(entity.getCatelogId());
        BrandEntity brand = brandDao.selectById(entity.getBrandId());

        // 设置名字
        entity.setCatelogName(category.getName());
        entity.setBrandName(brand.getName());

        // 入库
        save(entity);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        // 构造更新条件
        LambdaUpdateWrapper<CategoryBrandRelationEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CategoryBrandRelationEntity::getBrandId, brandId);
        wrapper.set(CategoryBrandRelationEntity::getBrandName, name);

        update(wrapper);
    }

    @Override
    public void updateCatelog(Long catId, String name) {
        // 构造更新条件
        LambdaUpdateWrapper<CategoryBrandRelationEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CategoryBrandRelationEntity::getCatelogId, catId);
        wrapper.set(CategoryBrandRelationEntity::getCatelogName, name);

        update(wrapper);
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        // 1.先查出所有品牌id
        List<CategoryBrandRelationEntity> relationEntities = list(new LambdaUpdateWrapper<CategoryBrandRelationEntity>()
            .eq(CategoryBrandRelationEntity::getCatelogId, catId));

        // 2.查出所有品牌详细信息
        return relationEntities.stream().map(relationEntity -> {
            // 查询所有的品牌信息
            return brandDao.selectById(relationEntity.getBrandId());
        }).collect(Collectors.toList());
    }
}