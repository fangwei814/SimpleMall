package com.fangw.simplemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.product.entity.AttrEntity;

import java.util.Map;

/**
 * 商品属性
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:47
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

