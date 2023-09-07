package com.fangw.simplemall.coupon.service.impl;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.coupon.dao.SeckillSkuRelationDao;
import com.fangw.simplemall.coupon.entity.SeckillSkuRelationEntity;
import com.fangw.simplemall.coupon.service.SeckillSkuRelationService;

@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity>
    implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<SeckillSkuRelationEntity> wrapper = new LambdaQueryWrapper<>();
        String promotionSessionId = (String)params.get("promotionSessionId");
        if (StringUtils.isNotBlank(promotionSessionId)) {
            wrapper.eq(SeckillSkuRelationEntity::getPromotionSessionId, promotionSessionId);
        }

        IPage<SeckillSkuRelationEntity> page =
            this.page(new Query<SeckillSkuRelationEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

}