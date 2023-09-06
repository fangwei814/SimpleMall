package com.fangw.simplemall.product.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fangw.simplemall.product.entity.SpuInfoEntity;

/**
 * spu信息
 * 
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    /**
     * 更新spu状态
     * 
     * @param spuId
     * @param code
     */
    void updateSpuStatus(@Param("spuId") Long spuId, @Param("code") int code);
}
