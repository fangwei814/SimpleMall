package com.fangw.simplemall.product.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.product.entity.SkuSaleAttrValueEntity;
import com.fangw.simplemall.product.vo.SkuItemSaleAttrsVo;

/**
 * sku销售属性&值
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取指定spu的所有属性
     * 
     * @param spuId
     * @return
     */
    List<SkuItemSaleAttrsVo> getSaleAttrsBySpuId(Long spuId);
}
