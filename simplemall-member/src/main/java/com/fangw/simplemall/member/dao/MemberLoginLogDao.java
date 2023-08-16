package com.fangw.simplemall.member.dao;

import com.fangw.simplemall.member.entity.MemberLoginLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员登录记录
 * 
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:45:08
 */
@Mapper
public interface MemberLoginLogDao extends BaseMapper<MemberLoginLogEntity> {
	
}
