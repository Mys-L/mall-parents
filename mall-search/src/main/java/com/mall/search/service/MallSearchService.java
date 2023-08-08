package com.mall.search.service;


import com.mall.search.vo.SearchParam;
import com.mall.search.vo.SearchResult;

/**
 * @description: 页面检索Service
 **/
public interface MallSearchService {

    /**
     * @description:
     * @param param  检索的所有参数
     * @return 返回检索的结果,里面包含页面需要的所有信息
     */
    SearchResult search(SearchParam param);
}

