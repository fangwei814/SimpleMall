package com.fangw.simplemall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.R;
import com.fangw.simplemall.product.entity.BrandEntity;
import com.fangw.simplemall.product.entity.CategoryBrandRelationEntity;
import com.fangw.simplemall.product.service.CategoryBrandRelationService;
import com.fangw.simplemall.product.vo.BrandVo;

/**
 * 品牌分类关联
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * /product/categorybrandrelation/brands/list
     *
     * 1、Controller：处理请求，接受和校验数据 2、Service接受controller传来的数据，进行业务处理 3、Controller接受Service处理完的数据，封装页面指定的vo
     */
    @GetMapping("/brands/list")
    public R relationBrandsList(@RequestParam(value = "catId", required = true) Long catId) {
        // 1.查关联表
        List<BrandEntity> vos = categoryBrandRelationService.getBrandsByCatId(catId);

        // 2.封装vo
        List<BrandVo> collect = vos.stream().map(vo -> {
            BrandVo brandVo = new BrandVo();
            brandVo.setBrandName(vo.getName());
            brandVo.setBrandId(vo.getBrandId());
            return brandVo;
        }).collect(Collectors.toList());

        return R.ok().put("data", collect);
    }

    @PostMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity entity) {
        // 传过来的brandId和catelogId被封装成entity
        categoryBrandRelationService.saveDetail(entity);

        return R.ok("Save succeed");
    }

    /**
     * 查询指定分类id的所有关联信息
     * 
     * @param params
     * @return
     */
    @GetMapping("/catelog/list")
    public R catelogList(@RequestParam Map<String, Object> params) {
        // 取出brandId然后查询
        LambdaQueryWrapper<CategoryBrandRelationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryBrandRelationEntity::getBrandId, params.get("brandId"));
        List<CategoryBrandRelationEntity> list = categoryBrandRelationService.list(wrapper);

        if (Objects.nonNull(list)) {
            return R.ok().put("data", list);
        } else {
            return R.error("Not find category brand relation");
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
