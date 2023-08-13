package com.mall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName: ItemSaleAttrVo
 * @Description:
 * @Author: L
 * @Create: 2023-08-12 16:46
 * @Version: 1.0
 */
@ToString
@Data
public class ItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    /** AttrValueWithSkuIdVo两个属性 attrValue、skuIds */
    private List<AttrValueWithSkuIdVo> attrValues;
}
