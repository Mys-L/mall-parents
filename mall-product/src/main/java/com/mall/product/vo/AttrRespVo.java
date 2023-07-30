package com.mall.product.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: AttrRespVo
 * @Description:
 * @Author: L
 * @Create: 2023-07-24 20:25
 * @Version: 1.0
 */

@Data
public class AttrRespVo extends AttrVo implements Serializable {
    private String catelogName;
    private String groupName;
    private Long[] catelogPath;
}

