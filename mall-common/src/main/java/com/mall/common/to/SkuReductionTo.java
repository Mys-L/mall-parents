package com.mall.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName: SkuReductionTo
 * @Description: 打折信息
 * @Author: L
 * @Create: 2023-07-26 14:13
 * @Version: 1.0
 */
@Data
public class SkuReductionTo {
    private Long skuId;
    /***
     * fullCount、discount、countStatus  打折信息
     * 买几件、打几折、是否参数其他优惠
     */
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
