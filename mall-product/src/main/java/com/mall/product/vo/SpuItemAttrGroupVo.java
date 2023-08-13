package com.mall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName: SpuItemAttrGroup
 * @Description:
 * @Author: L
 * @Create: 2023-08-12 16:48
 * @Version: 1.0
 */
@ToString
@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    /** 两个属性attrName、attrValue */
    private List<SpuBaseAttrVo> attrs;
}
