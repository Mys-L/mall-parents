package com.mall.product.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>Title: BaseAttrs</p>
 * Description：
 * date：2020/6/5 14:58
 * @author L
 */
@Data
public class BaseAttrs implements Serializable {

    private Long attrId;
    private String attrValues;
    private int showDesc;
}