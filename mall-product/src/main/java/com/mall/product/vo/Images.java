package com.mall.product.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Images implements Serializable {

    private String imgUrl;
    private int defaultImg;
}