package com.fangw.simplemall.ware.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.ware.entity.WareOrderTaskEntity;

/**
 * 库存工作单
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:50:20
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询最新库存状态
     * 
     * @param orderSn
     * @return
     */
    WareOrderTaskEntity getOrderTeskByOrderSn(String orderSn);
}
