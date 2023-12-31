package com.mall.product.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
/**
 * <p>Title: AttrRespVo</p>
 * Description：会员价格
 * date：2020/6/2 19:56
 */
@Data
public class MemberPrice implements Serializable {

    private Long id;
    private String name;
    private BigDecimal price;

}