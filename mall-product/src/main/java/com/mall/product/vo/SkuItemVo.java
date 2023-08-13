package com.mall.product.vo;

import com.mall.product.entity.SkuImagesEntity;
import com.mall.product.entity.SkuInfoEntity;
import com.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @ClassName: SkuItemVo
 * @Description:
 * @Author: L
 * @Create: 2023-08-12 16:45
 * @Version: 1.0
 */

@Data
public class SkuItemVo {
    /** 1 sku基本信息的获取:如标题*/
    SkuInfoEntity info;

    boolean hasStock = true;

    /** 2 sku的图片信息*/
    List<SkuImagesEntity> images;

    /** 3 获取spu的销售属性组合。每个attrName对应一个value-list*/
    List<ItemSaleAttrVo> saleAttr;

    /** 4 获取spu的介绍*/
    SpuInfoDescEntity desc;

    /*** 5 获取spu的规格参数信息，每个分组的包含list*/
    List<SpuItemAttrGroupVo> groupAttrs;

    /*** 6 秒杀信息*/
    SeckillInfoVo seckillInfoVo;
}
