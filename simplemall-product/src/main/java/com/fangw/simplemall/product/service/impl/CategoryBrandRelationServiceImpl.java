package com.fangw.simplemall.product.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

}