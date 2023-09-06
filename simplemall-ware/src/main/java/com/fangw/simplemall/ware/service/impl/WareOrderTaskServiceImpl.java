package com.fangw.simplemall.ware.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.ware.dao.WareOrderTaskDao;
import com.fangw.simplemall.ware.entity.WareOrderTaskEntity;
import com.fangw.simplemall.ware.service.WareOrderTaskService;

@Service("wareOrderTaskService")
public class WareOrderTaskServiceImpl extends ServiceImpl<WareOrderTaskDao, WareOrderTaskEntity>
    implements WareOrderTaskService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareOrderTaskEntity> page =
            this.page(new Query<WareOrderTaskEntity>().getPage(params), new QueryWrapper<WareOrderTaskEntity>());

        return new PageUtils(page);
    }

    @Override
    public WareOrderTaskEntity getOrderTeskByOrderSn(String orderSn) {
        return getOne(new LambdaQueryWrapper<WareOrderTaskEntity>().eq(WareOrderTaskEntity::getOrderSn, orderSn));
    }

}