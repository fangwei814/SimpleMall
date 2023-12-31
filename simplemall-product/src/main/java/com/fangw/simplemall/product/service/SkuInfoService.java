package com.fangw.simplemall.product.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.product.entity.SkuInfoEntity;
import com.fangw.simplemall.product.vo.SkuItemVo;

/**
 * sku信息
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    /**
     * 带条件
     * 
     * @param params
     * @return
     */
    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 查询spuId对应的所有sku
     * 
     * @param spuId
     * @return
     */
    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    /**
     * 展示当前sku的详细信息
     * 
     * @param skuId
     * @return
     */
    SkuItemVo item(Long skuId);
}
