package com.fangw.simplemall.product.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.product.entity.ProductAttrValueEntity;

/**
 * spu属性值
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存商品属性
     * 
     * @param collect
     */
    void saveProductAttr(List<ProductAttrValueEntity> collect);

    /**
     * 查询spu的规格参数
     * 
     * @param spuId
     * @return
     */
    List<ProductAttrValueEntity> baseAttrlistforspu(Long spuId);

    /**
     * 更新spu属性
     * 
     * @param spuId
     * @param entities
     */
    void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities);
}
