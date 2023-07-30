package com.mall.common.to.elasticsearch;

import lombok.Data;

import java.io.Serializable;

/**
 * 存储这个sku是否有库存
 * @author L
 */
@Data
public class SkuHasStockVo implements Serializable {
	private Long skuId;
	/**是否有库存*/
	private Boolean hasStock;
}
