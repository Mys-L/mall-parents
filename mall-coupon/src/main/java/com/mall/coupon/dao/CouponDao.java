package com.mall.coupon.dao;

import com.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author L
 * @email L@163.com
 * @date 2023-07-15 15:57:09
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
