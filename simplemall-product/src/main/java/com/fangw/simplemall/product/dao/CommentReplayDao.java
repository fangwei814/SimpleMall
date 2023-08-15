package com.fangw.simplemall.product.dao;

import com.fangw.simplemall.product.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 * 
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 00:07:40
 */
@Mapper
public interface CommentReplayDao extends BaseMapper<CommentReplayEntity> {
	
}
