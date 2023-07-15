package com.mall.order.dao;

import com.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author L
 * @email L@163.com
 * @date 2023-07-15 15:54:43
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
