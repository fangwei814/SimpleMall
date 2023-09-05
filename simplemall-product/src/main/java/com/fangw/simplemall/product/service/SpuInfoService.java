package com.fangw.simplemall.product.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.product.entity.SpuInfoEntity;
import com.fangw.simplemall.product.vo.SpuSaveVo;

/**
 * spu信息
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存SPU基本信息
     * 
     * @param spuInfo
     */
    void saveSpuInfo(SpuSaveVo spuInfo);

    /**
     * 保存
     * 
     * @param infoEntity
     */
    void saveBaseSpuInfo(SpuInfoEntity infoEntity);

    /**
     * 利用params里的属性
     * 
     * @param params
     * @return
     */
    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 商品上架
     * 
     * @param spuId
     */
    void up(Long spuId);

    /**
     * 用skuId查spu信息
     * 
     * @param skuId
     * @return
     */
    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}
