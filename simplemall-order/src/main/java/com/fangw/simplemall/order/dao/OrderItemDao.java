package com.fangw.simplemall.order.dao;

import com.fangw.simplemall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:48:03
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
