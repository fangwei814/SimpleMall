package com.fangw.simplemall.order.dao;

import com.fangw.simplemall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:48:03
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
