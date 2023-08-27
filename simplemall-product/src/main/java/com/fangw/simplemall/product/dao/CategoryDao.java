package com.fangw.simplemall.product.dao;

import com.fangw.simplemall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 19:26:46
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
