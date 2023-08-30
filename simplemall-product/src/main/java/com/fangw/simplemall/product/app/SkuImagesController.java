package com.fangw.simplemall.product.app;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.R;
import com.fangw.simplemall.product.entity.SkuImagesEntity;
import com.fangw.simplemall.product.service.SkuImagesService;

/**
 * sku图片
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
@RestController
@RequestMapping("product/skuimages")
public class SkuImagesController {
    @Autowired
    private SkuImagesService skuImagesService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:skuimages:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = skuImagesService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("product:skuimages:info")
    public R info(@PathVariable("id") Long id) {
        SkuImagesEntity skuImages = skuImagesService.getById(id);

        return R.ok().put("skuImages", skuImages);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:skuimages:save")
    public R save(@RequestBody SkuImagesEntity skuImages) {
        skuImagesService.save(skuImages);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:skuimages:update")
    public R update(@RequestBody SkuImagesEntity skuImages) {
        skuImagesService.updateById(skuImages);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:skuimages:delete")
    public R delete(@RequestBody Long[] ids) {
        skuImagesService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
