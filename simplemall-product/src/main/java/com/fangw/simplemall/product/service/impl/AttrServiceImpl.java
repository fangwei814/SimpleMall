package com.fangw.simplemall.product.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.product.dao.AttrAttrgroupRelationDao;
import com.fangw.simplemall.product.dao.AttrDao;
import com.fangw.simplemall.product.dao.AttrGroupDao;
import com.fangw.simplemall.product.dao.CategoryDao;
import com.fangw.simplemall.product.entity.AttrAttrgroupRelationEntity;
import com.fangw.simplemall.product.entity.AttrEntity;
import com.fangw.simplemall.product.entity.AttrGroupEntity;
import com.fangw.simplemall.product.entity.CategoryEntity;
import com.fangw.simplemall.product.service.AttrService;
import com.fangw.simplemall.product.vo.AttrRespVo;
import com.fangw.simplemall.product.vo.AttrVo;

@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), new QueryWrapper<AttrEntity>());

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveAttr(AttrVo attr) {
        // 保存属性
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        save(attrEntity);

        // 保存分组信息
        if (Objects.nonNull(attr.getAttrGroupId())) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    @Transactional
    public PageUtils queryBaseAttrPage(Map<String, Object> params, String attrType, Long catelogId) {
        // 1.先分页查询当前符合条件的所有
        LambdaQueryWrapper<AttrEntity> attrWrapper = new LambdaQueryWrapper<>();
        if (catelogId != 0) {
            attrWrapper.eq(AttrEntity::getCatelogId, catelogId);
        }

        // key
        String key = (String)params.get("key");
        if (StringUtils.isNotBlank(key)) {
            attrWrapper.and(obj -> {
                obj.eq(AttrEntity::getAttrId, key).or().like(AttrEntity::getAttrName, key);
            });
        }

        // 分页查询
        IPage<AttrEntity> page = page(new Query<AttrEntity>().getPage(params), attrWrapper);
        PageUtils pageUtils = new PageUtils(page);

        // 2.构造返回的Vo
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> collect = records.stream().map(attrEntity -> {
            // 创建一个vo，复制
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);

            // 查询并赋值其他
            // 分组id和名字
            if ("base".equalsIgnoreCase(attrType)) {
                // 通过分组和属性关联表查询分组
                LambdaQueryWrapper<AttrAttrgroupRelationEntity> relationWrapper = new LambdaQueryWrapper<>();
                relationWrapper.eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId());
                AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(relationWrapper);

                // 判断
                if (Objects.nonNull(relationEntity) && Objects.nonNull(relationEntity.getAttrGroupId())) {
                    // 继续查询名字
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());

                    // 设置
                    attrRespVo.setAttrGroupId(attrGroupEntity.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            // 分类id和名字
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (Objects.nonNull(categoryEntity)) {
                attrRespVo.setCatelogName(categoryEntity.getName());
                attrRespVo.setCatelogId(categoryEntity.getCatId());
            }

            return attrRespVo;
        }).collect(Collectors.toList());

        pageUtils.setList(collect);

        return pageUtils;
    }

}