package com.fangw.simplemall.coupon.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.to.SkuReductionTo;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.coupon.entity.SkuFullReductionEntity;

/**
 * 商品满减信息
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:19:07
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo reductionTo);
}
