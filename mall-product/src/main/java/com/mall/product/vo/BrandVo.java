package com.mall.product.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: BrandVo
 * @Description:
 * @Author: L
 * @Create: 2023-07-25 14:07
 * @Version: 1.0
 */
@Data
public class BrandVo implements Serializable {

    private Long brandId;
    private String brandName;

}
