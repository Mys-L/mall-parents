package com.mall.search.controller;

import com.mall.common.exception.BizCodeEnum;
import com.mall.common.to.elasticsearch.SkuElasticModel;
import com.mall.common.utils.R;
import com.mall.search.service.ElasticSearchSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName: ElasticSearchController
 * @Description: 搜索服务相关
 * @Author: L
 * @Create: 2023-07-30 12:50
 * @Version: 1.0
 */
@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSearchSaveController {
    @Autowired
    private ElasticSearchSaveService elasticSaveService;

    /**
     * 商品上架
     * 商品信息 保存到 elasticsearch
     */
    @PostMapping("/product") // ElasticSaveController
    public R productStatusUp(@RequestBody List<SkuElasticModel> skuElasticModels){
        boolean b = false;
        try {
            b = elasticSaveService.productStatusUp(skuElasticModels);
        } catch (IOException e) {
            log.error(e.getMessage());
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (!b){
            return R.ok();
        }else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }

}
