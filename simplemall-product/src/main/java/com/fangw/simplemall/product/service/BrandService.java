package com.fangw.simplemall.product.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.product.entity.BrandEntity;

/**
 * 品牌
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 级联更新，别的表涉及了brand名字也要同步更新
     *
     * @param brand
     */
    void updateDetail(BrandEntity brand);
}
