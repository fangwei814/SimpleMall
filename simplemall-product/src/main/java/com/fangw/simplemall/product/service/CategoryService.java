package com.fangw.simplemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

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
     * 用树形结构展示全部分类
     * 本质上是一次全表查询
     *
     * @return
     */
    List<CategoryEntity> listWithTree();
}

