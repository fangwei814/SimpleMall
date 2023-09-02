package com.fangw.simplemall.product.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fangw.simplemall.product.entity.AttrGroupEntity;
import com.fangw.simplemall.product.vo.SpuItemAttrGroupVo;

/**
 * 属性分组
 * 
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId,
        @Param("catalogId") Long catalogId);
}
