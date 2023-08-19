package com.fangw.simplemall.product.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.product.dao.CategoryDao;
import com.fangw.simplemall.product.entity.CategoryEntity;
import com.fangw.simplemall.product.service.CategoryService;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

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
        }).sorted((menu1, menu2) -> {
            return (Objects.isNull(menu1.getSort()) ? 0 : menu1.getSort())
                - (Objects.isNull(menu2.getSort()) ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
    }

    private List<CategoryEntity> getChildren(CategoryEntity cur, List<CategoryEntity> allList) {
        // 对传入的cur，找到他的所有孩子，形成多维数组
        return allList.stream().filter(item -> {
            // 对每个列表中的元素，找父id是当前id，也就是找孩子
            return cur.getCatId().equals(item.getParentCid());
        }).peek(item -> {
            // 递归调用每一层，形成树结构
            item.setChildren(getChildren(item, allList));
        }).sorted((menu1, menu2) -> {
            // 注意递归底部会有空指针，需要判断
            return (Objects.isNull(menu1.getSort()) ? 0 : menu1.getSort())
                - (Objects.isNull(menu2.getSort()) ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
    }
}