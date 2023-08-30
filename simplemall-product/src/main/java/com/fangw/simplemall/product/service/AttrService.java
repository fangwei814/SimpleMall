package com.fangw.simplemall.product.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.product.entity.AttrEntity;
import com.fangw.simplemall.product.vo.AttrGroupRelationVo;
import com.fangw.simplemall.product.vo.AttrRespVo;
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

    /**
     * 获取详细返回信息，包括属性分组和多级分类信息
     * 
     * @param attrId
     * @return
     */
    AttrRespVo getDetail(Long attrId);

    /**
     * 更新，包括关联表
     * 
     * @param attr
     */
    void updateAttr(AttrVo attr);

    /**
     * 获取属性分组关联的所有属性
     * 
     * @param attrgroupId
     * @return
     */
    List<AttrEntity> getRelationAttr(Long attrgroupId);

    /**
     * 批量删除属性和属性分组的关联
     * 
     * @param vos
     */
    void deleteRelation(AttrGroupRelationVo[] vos);

    /**
     * 找到不属于该分组的属性
     * 
     * @param params
     * @param attrgroupId
     * @return
     */
    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    /**
     * 查询可以被检索的
     * 
     * @param attrIds
     * @return
     */
    List<Long> selectSearchAttrIds(List<Long> attrIds);
}
