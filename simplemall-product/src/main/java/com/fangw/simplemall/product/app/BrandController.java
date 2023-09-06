package com.fangw.simplemall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.R;
import com.fangw.common.valid.UpdateGroup;
import com.fangw.common.valid.UpdateStatusGroup;
import com.fangw.simplemall.product.entity.BrandEntity;
import com.fangw.simplemall.product.service.BrandService;

/**
 * 品牌
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    @GetMapping("/infos")
    public R info(@RequestParam("brandId") List<Long> brandIds) {
        List<BrandEntity> brand = brandService.getBrandsByIds(brandIds);

        return R.ok().put("brand", brand);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    // @RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    // @RequiresPermissions("product:brand:save")
    public R save(@Valid @RequestBody BrandEntity brand/*, BindingResult result*/) {
        // if (result.hasErrors()) {
        // // 1.获取校验的错误结果
        // Map<String, String> map = new HashMap<>();
        // result.getFieldErrors().forEach(item -> {
        // String message = item.getDefaultMessage();
        // String field = item.getField();
        // map.put(message, field);
        // });
        // return R.error(400, "提交的数据不合法").put("data", map);
        // } else {
        brandService.save(brand);
        return R.ok();
        // }
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand) {
        brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改状态
     */
    @PostMapping("/update/status")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand) {
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
