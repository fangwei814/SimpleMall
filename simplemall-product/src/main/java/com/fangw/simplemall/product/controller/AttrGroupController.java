package com.fangw.simplemall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.R;
import com.fangw.simplemall.product.entity.AttrEntity;
import com.fangw.simplemall.product.entity.AttrGroupEntity;
import com.fangw.simplemall.product.service.AttrGroupService;
import com.fangw.simplemall.product.service.AttrService;
import com.fangw.simplemall.product.service.CategoryService;
import com.fangw.simplemall.product.vo.AttrGroupRelationVo;

/**
 * 属性分组
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrService attrService;

    /**
     * 删除属性与分组的关联关系
     * 
     * @param vos
     *            接受一个我们自定义的vo数组
     * @return
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos) {
        attrService.deleteRelation(vos);
        return R.ok();
    }

    /**
     * 获取属性分组的关联的所有属性
     * 
     * @param attrgroupId
     * @return
     */
    /// product/attrgroup/{attrgroupId}/attr/relation
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", entities);
    }

    /**
     * 根据catelogId和key进行分页查询
     * 
     * @param params
     * @param catelogId
     * @return
     */
    @GetMapping("/list/{catelogId}")
    public R listByCatelogId(@RequestParam Map<String, Object> params, @PathVariable Long catelogId) {
        return R.ok().put("page", attrGroupService.queryPage(params, catelogId));
    }

    /**
     * 获取属性分组详情
     * 
     * @param attrGroupId
     * @return
     */
    @GetMapping("/info/{attrGroupId}")
    @Transactional
    public R infoByAttrGroupId(@PathVariable Long attrGroupId) {
        // 先找到对应的数据
        AttrGroupEntity groupEntity = attrGroupService.getById(attrGroupId);

        // 然后再找三级分类的路径
        if (Objects.isNull(groupEntity)) {
            return R.error("Not find.");
        }
        Long[] path = categoryService.findCatelogPath(groupEntity.getCatelogId());
        groupEntity.setCatelogPath(path);

        return R.ok().put("attrGroup", path);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrGroupService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    // @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
