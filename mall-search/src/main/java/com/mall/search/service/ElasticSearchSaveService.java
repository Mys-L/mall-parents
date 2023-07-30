package com.mall.search.service;

import com.mall.common.to.elasticsearch.SkuElasticModel;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName: ElasticSearchService
 * @Description:
 * @Author: L
 * @Create: 2023-07-30 12:56
 * @Version: 1.0
 */
public interface ElasticSearchSaveService {
    boolean productStatusUp(List<SkuElasticModel> skuElasticModels) throws IOException;
}
