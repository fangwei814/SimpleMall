package com.fangw.simplemall.product.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.product.entity.BrandEntity;
import com.fangw.simplemall.product.entity.CategoryBrandRelationEntity;

/**
 * 品牌分类关联
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存，但是没有名字，需要通过id找到名字
     * 
     * @param entity
     */
    void saveDetail(CategoryBrandRelationEntity entity);

    /**
     * 更新品牌信息
     * 
     * @param brandId
     * @param name
     */
    void updateBrand(Long brandId, String name);

    /**
     * 更新分类信息
     * 
     * @param catId
     * @param name
     */
    void updateCatelog(Long catId, String name);

    /**
     * 查询分类对应的所有品牌信息
     * 
     * @param catId
     * @return
     */
    List<BrandEntity> getBrandsByCatId(Long catId);
}
