package com.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.to.SkuReductionTo;
import com.mall.common.utils.PageUtils;
import com.mall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author L
 * @email L@163.com
 * @date 2023-07-15 15:57:09
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

