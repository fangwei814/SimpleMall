package com.fangw.simplemall.product.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.product.dao.CategoryDao;
import com.fangw.simplemall.product.entity.CategoryEntity;
import com.fangw.simplemall.product.service.CategoryBrandRelationService;
import com.fangw.simplemall.product.service.CategoryService;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page =
            this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<CategoryEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1.全表查询列出所有的分类
        List<CategoryEntity> allList = list();

        // 2.根据分类的从属关系组装
        // 如果父id为0表示是一级分类
        return allList.stream().filter(item -> item.getParentCid() == 0).peek(item -> {
            item.setChildren(getChildren(item, allList));
        }).sorted(Comparator.comparingInt(menu -> (Objects.isNull(menu.getSort()) ? 0 : menu.getSort())))
            .collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> list) {
        // todo:这边还没有判断被哪些引用

        baseMapper.deleteBatchIds(list);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        // 递归寻找父亲的id
        List<Long> res = new ArrayList<>();
        findParentPath(catelogId, res);

        // 逆序
        Collections.reverse(res);

        // 返回
        return res.toArray(new Long[0]);
    }

    @Override
    @Transactional
    public void updateDetail(CategoryEntity category) {
        // 更新自己
        updateById(category);

        // 取出信息
        Long catId = category.getCatId();
        String name = category.getName();

        // 更新他表
        if (StringUtils.isNotBlank(name)) {
            // todo:其他表
            categoryBrandRelationService.updateCatelog(catId, name);
        }
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 1.将当前节点添加到路径
        paths.add(catelogId);

        // 2.获取当前节点的父亲
        CategoryEntity entity = getById(catelogId);
        if (entity.getParentCid() != 0) {
            findParentPath(entity.getParentCid(), paths);
        }

        return paths;
    }

    private List<CategoryEntity> getChildren(CategoryEntity cur, List<CategoryEntity> allList) {
        // 对传入的cur，找到他的所有孩子，形成多维数组
        return allList.stream().filter(item -> {
            // 对每个列表中的元素，找父id是当前id，也就是找孩子
            return cur.getCatId().equals(item.getParentCid());
        }).peek(item -> {
            // 递归调用每一层，形成树结构
            item.setChildren(getChildren(item, allList));
        }).sorted(Comparator.comparingInt(menu -> (Objects.isNull(menu.getSort()) ? 0 : menu.getSort())))
            .collect(Collectors.toList());
    }
}