package com.fangw.simplemall.product.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.product.dao.AttrGroupDao;
import com.fangw.simplemall.product.entity.AttrEntity;
import com.fangw.simplemall.product.entity.AttrGroupEntity;
import com.fangw.simplemall.product.service.AttrGroupService;
import com.fangw.simplemall.product.service.AttrService;
import com.fangw.simplemall.product.vo.AttrGroupWithAttrsVo;
import com.fangw.simplemall.product.vo.SpuItemAttrGroupVo;

@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page =
            this.page(new Query<AttrGroupEntity>().getPage(params), new QueryWrapper<AttrGroupEntity>());

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        // 1.构造查询条件
        String key = (String)params.get("key");
        LambdaQueryWrapper<AttrGroupEntity> wrapper = new LambdaQueryWrapper<>();

        // 2.判断是否有key
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(obj -> {
                obj.eq(AttrGroupEntity::getAttrGroupId, key).or().like(AttrGroupEntity::getAttrGroupName, key);
            });
        }

        // 3.判断是否有catelogId，0表示没有
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);

            // 封装
            return new PageUtils(page);
        } else {
            wrapper.eq(AttrGroupEntity::getCatelogId, catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);

            // 封装
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        // 1.查询所有的属性分组
        List<AttrGroupEntity> groupEntities =
            list(new LambdaQueryWrapper<AttrGroupEntity>().eq(AttrGroupEntity::getCatelogId, catelogId));

        // 2.封装vo
        return groupEntities.stream().map(attrGroupEntity -> {
            // 查询属性
            List<AttrEntity> attrList = attrService.getRelationAttr(attrGroupEntity.getAttrGroupId());

            // vo
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(attrGroupEntity, attrGroupWithAttrsVo);
            attrGroupWithAttrsVo.setAttrs(attrList);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        // spuId查类别id
        // 类别id查attrgroupid
        // attrgroup通过关联关系查attr
        // attr查product_attr_value
        return baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
    }

}