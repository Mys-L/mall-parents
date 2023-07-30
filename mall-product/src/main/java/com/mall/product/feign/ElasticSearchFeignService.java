package com.mall.product.feign;

import com.mall.common.to.elasticsearch.SkuElasticModel;
import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @ClassName: ElasticSearchFeignService
 * @Description: 搜索服务
 * @Author: L
 * @Create: 2023-07-30 14:06
 * @Version: 1.0
 */
@FeignClient("mall-search")
public interface ElasticSearchFeignService {

    /**
     * 远程保存数据到elasticsearch
     */
    @PostMapping("/search/save/product") // ElasticSaveController
    R productStatusUp(@RequestBody List<SkuElasticModel> skuElasticModels);

}
