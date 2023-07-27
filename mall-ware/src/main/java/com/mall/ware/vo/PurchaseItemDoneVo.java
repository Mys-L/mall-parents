package com.mall.ware.vo;

import lombok.Data;

/**
 * @ClassName: PurchaseItemDoneVo
 * @Description: 采购项
 * @Author: L
 * @Create: 2023-07-27 08:15
 * @Version: 1.0
 */
@Data
public class PurchaseItemDoneVo {
    /**
     * "itemId":1,"status":3,"reason":"",
     * "itemId":3,"status":4,"reason":"无货"
     */
    private Long itemId;

    private Integer status;

    private String reason;
}
