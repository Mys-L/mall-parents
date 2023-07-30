/**
  * Copyright 2019 bejson.com 
  */
package com.mall.product.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>Title: Attr</p>
 * Description：
 * date：2020/6/5 14:58
 */
@Data
public class Attr implements Serializable {

    private Long attrId;
    private String attrName;
    private String attrValue;

}