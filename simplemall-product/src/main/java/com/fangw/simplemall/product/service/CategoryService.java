package com.fangw.simplemall.product.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.product.entity.CategoryEntity;
import com.fangw.simplemall.product.vo.Catalog2Vo;

/**
 * 商品三级分类
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 用树形结构展示全部分类 本质上是一次全表查询
     *
     * @return
     */
    List<CategoryEntity> listWithTree();

    /**
     * 删除菜单，这边会检查是否被别引用
     * 
     * @param list
     */
    void removeMenuByIds(List<Long> list);

    /**
     * 获取节点路径
     * 
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    /**
     * 级联更新，别的表涉及了名字也要同步更新
     *
     * @param category
     */
    void updateDetail(CategoryEntity category);

    /**
     * 获取一级分类
     * 
     * @return
     */
    List<CategoryEntity> getLevel1Categorys();

    /**
     * 通过redis获取
     * 
     * @return
     */
    Map<String, List<Catalog2Vo>> getCatalogJsonFromRedis();

    /**
     * 获取分类vo
     * 
     * @return
     */
    Map<String, List<Catalog2Vo>> getCatalogJsonFromDbUsingCache();
}
