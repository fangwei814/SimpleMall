package com.fangw.simplemall.product.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.product.entity.AttrGroupEntity;
import com.fangw.simplemall.product.vo.AttrGroupWithAttrsVo;
import com.fangw.simplemall.product.vo.SpuItemAttrGroupVo;

/**
 * 属性分组
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    /**
     * 先查询所有的属性分组，然后查所有的属性
     * 
     * @param catelogId
     * @return
     */
    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);

    /**
     * 查处当前spuId对应的所有属性分组信息 以及 当前分组下的所有属性对应的值
     * 
     * @param spuId
     * @param catalogId
     * @return
     */
    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}
