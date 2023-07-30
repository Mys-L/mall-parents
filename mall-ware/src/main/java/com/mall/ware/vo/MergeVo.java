package com.mall.ware.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: MergeVo
 * @Description:
 * @Author: L
 * @Create: 2023-07-26 22:11
 * @Version: 1.0
 */
@Data
public class MergeVo implements Serializable {
    /**
     * 采购单的ID
     */
    private Long purchaseId;

    /**
     * [1,2,3,4]
     * 合并项集合
     */
    private List<Long> items;
}
