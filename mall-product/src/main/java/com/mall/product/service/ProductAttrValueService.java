package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author L
 * @email L@163.com
 * @date 2023-07-15 15:53:36
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     *  SpuInfoServiceImpl 方法：saveSpuInfo-> 保存spu的规格参数 同时需要操作另一张表:pms_product_attr_value
     */
    void saveProductAttrValue(List<ProductAttrValueEntity> list);
}

