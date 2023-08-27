package com.fangw.simplemall.ware.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.ware.dao.WareSkuDao;
import com.fangw.simplemall.ware.entity.WareSkuEntity;
import com.fangw.simplemall.ware.service.WareSkuService;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /*
        wareId: 123,//仓库id
        skuId: 123//商品id
         */
        LambdaQueryWrapper<WareSkuEntity> wrapper = new LambdaQueryWrapper<>();

        String wareId = (String)params.get("wareId");
        if (StringUtils.isNotBlank(wareId)) {
            wrapper.eq(WareSkuEntity::getWareId, wareId);
        }

        String skuId = (String)params.get("skuId");
        if (StringUtils.isNotBlank(skuId)) {
            wrapper.eq(WareSkuEntity::getSkuId, skuId);
        }

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

}