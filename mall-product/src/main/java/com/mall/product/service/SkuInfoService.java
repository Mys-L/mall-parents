package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.SkuInfoEntity;

import java.util.Map;

/**
 * sku信息
 *
 * @author L
 * @email L@163.com
 * @date 2023-07-15 15:53:37
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     *  SpuInfoServiceImpl 方法：saveSpuInfo-> 6->6.1) 保存sku 的基本信息 pms_sku_info
     */
    void saveSkuInfo(SkuInfoEntity skuInfoEntity);
}

