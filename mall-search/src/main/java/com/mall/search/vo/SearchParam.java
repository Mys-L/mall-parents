package com.mall.search.vo;


import lombok.Data;

import java.util.List;

/**
 * @ClassName: SearchParam
 * @Description: 封装页面所有可能传递过来的查询条件
 * @Author: L
 * @Create: 2023-08-2,  15:38
 * @Version: 1.0
 * catalog3Id=225&keyword=小米&sort=saleCount_asc&hasStock=0/1&brandId=1&brandId=2&attrs=1_5寸:8寸&attrs=2_16G:8G
 */

@Data
public class SearchParam {
    /**
     * 页面传递过来的全文匹配关键字
     */
    private String keyword;
    /*
     *三级分类id
     */
    private Long catalog3Id;
    /**
     *   sort=saleCount_asc/desc
     *   sort=skuPrice_asc/desc
     *   sort=hotScore_asc/desc
     *   排序条件 v
     */
    private String sort;

    /**
     * 好多的过滤条件
     *  hasStock(是否有货)、skuPrice区间、brandId、catalog3Id、attrs
     *  hasStock=0/1
     *  skuPrice=1_500/_500/500_
     *  brandId=1
     *  attrs=2_5存:6寸
     */
    /**
     * 是否只显示有货  v 0（无库存）1（有库存）
     */
    public Integer hasStock;
    /**
     * 价格区间查询  v
     */

    private String skuPrice;
    /**
     * 按照品牌进行查询，可以多选  v
     */
    private List<Long> brandId;
    /**
     * 按照属性进行筛选  v
     */
    private List<String> attrs;
    /**
     *  页码
     */
    private Integer pageNum = 1;
    /**
     * 原生的所有查询条件
     */
    private String _queryString;

}
