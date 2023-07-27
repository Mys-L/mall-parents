package com.mall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @ClassName: PurchaseDoneVo
 * @Description: 完成采购vo
 * @Author: L
 * @Create: 2023-07-27 08:14
 * @Version: 1.0
 */
@Data
public class PurchaseDoneVo {
    /** 采购单id*/
    @NotNull
    private Long id;

    /** 采购项(需求) */
    private List<PurchaseItemDoneVo> items;
}
