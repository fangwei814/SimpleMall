package com.fangw.simplemall.coupon.dao;

import com.fangw.simplemall.coupon.entity.CouponHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券领取历史记录
 * 
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:19:07
 */
@Mapper
public interface CouponHistoryDao extends BaseMapper<CouponHistoryEntity> {
	
}
