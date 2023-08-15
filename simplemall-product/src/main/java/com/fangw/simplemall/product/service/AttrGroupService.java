package com.fangw.simplemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.product.entity.AttrGroupEntity;

import java.util.Map;

/**
 * 属性分组
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 00:07:40
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

