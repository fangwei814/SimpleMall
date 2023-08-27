package com.fangw.simplemall.ware.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.ware.dao.PurchaseDetailDao;
import com.fangw.simplemall.ware.entity.PurchaseDetailEntity;
import com.fangw.simplemall.ware.service.PurchaseDetailService;

@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity>
    implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /*
        key: '华为',//检索关键字
        status: 0,//状态
        wareId: 1,//仓库id
         */
        LambdaQueryWrapper<PurchaseDetailEntity> wrapper = new LambdaQueryWrapper<>();
        // 1.key
        String key = (String)params.get("key");
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(obj -> {
                obj.eq(PurchaseDetailEntity::getPurchaseId, key).or().eq(PurchaseDetailEntity::getSkuId, key);
            });
        }

        // 2.status
        String status = (String)params.get("status");
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(PurchaseDetailEntity::getStatus, status);
        }

        // 3.wareId
        String wareId = (String)params.get("wareId");
        if (StringUtils.isNotBlank(wareId)) {
            wrapper.eq(PurchaseDetailEntity::getWareId, wareId);
        }

        IPage<PurchaseDetailEntity> page = this.page(new Query<PurchaseDetailEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

}