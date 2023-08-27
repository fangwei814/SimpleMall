package com.fangw.simplemall.order.dao;

import com.fangw.simplemall.order.entity.PaymentInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付信息表
 * 
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:48:03
 */
@Mapper
public interface PaymentInfoDao extends BaseMapper<PaymentInfoEntity> {
	
}
