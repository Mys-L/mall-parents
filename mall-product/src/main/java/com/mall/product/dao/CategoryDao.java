package com.mall.product.dao;

import com.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author L
 * @email L@163.com
 * @date 2023-07-15 15:53:37
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
