package com.mall.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName: SpuBoundsTo
 * @Description: 远程调用对象  成长积分、购物积分
 * @Author: L
 * @Create: 2023-07-26 13:55
 * @Version: 1.0
 */
@Data
public class SpuBoundTo implements Serializable {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
