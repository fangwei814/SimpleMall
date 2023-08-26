package com.fangw.simplemall.product.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.product.entity.AttrEntity;
import com.fangw.simplemall.product.vo.AttrVo;

/**
 * 商品属性
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:47
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存属性，包括对应的attrgroupid
     * 
     * @param attr
     */
    void saveAttr(AttrVo attr);

    /**
     * 获取属性的分页信息
     * 
     * @param params
     * @param attrType
     * @param catelogId
     * @return PageUtils<AttrRespVo>
     */
    PageUtils queryBaseAttrPage(Map<String, Object> params, String attrType, Long catelogId);
}
