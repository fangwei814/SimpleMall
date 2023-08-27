package com.fangw.simplemall.product.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.product.entity.AttrAttrgroupRelationEntity;
import com.fangw.simplemall.product.vo.AttrGroupRelationVo;

/**
 * 属性&属性分组关联
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:47
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 批量保存
     * 
     * @param vos
     */
    void saveBatch(List<AttrGroupRelationVo> vos);
}
