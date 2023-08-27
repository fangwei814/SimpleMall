package com.fangw.simplemall.product.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fangw.simplemall.product.entity.AttrAttrgroupRelationEntity;

/**
 * 属性&属性分组关联
 * 
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:47
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    /**
     * 删除关联关系
     * 
     * @param entities
     */
    void deleteBatchRelations(@Param("entities") List<AttrAttrgroupRelationEntity> entities);
}
