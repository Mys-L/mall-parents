package com.mall.search.service.impl;

import com.alibaba.fastjson2.JSON;
import com.mall.common.to.elasticsearch.SkuElasticModel;
import com.mall.search.config.ElasticSearchConfig;
import com.mall.search.constant.ElasticSearchConstant;
import com.mall.search.service.ElasticSearchSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName: ElasticSearchService
 * @Description:
 * @Author: L
 * @Create: 2023-07-30 12:57
 * @Version: 1.0
 */
@Slf4j
@Service("elasticSearchService")
public class ElasticSearchSaveServiceImpl implements ElasticSearchSaveService {

    //    @Autowired
//    ElasticsearchClient client;
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuElasticModel> skuElasticModels) throws IOException {
        //将商品保存到elasticsearch中
        //1 给es中创建索引 product (索引常用到抽取常量)
        //2 给es中保存数据 批量保存 https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/8.6/indexing-bulk.html
//        BulkRequest.Builder br = new BulkRequest.Builder();
//        for (SkuElasticModel model : skuElasticModels) {
//            br.operations(op->op
//                    .index(i->i
//                            .index(ElasticSearchConstant.PRODUCT_ES_INDEX)
//                            .id(String.valueOf(model.getSkuId()))
//                            .document(model)
//                    )
//            );
//        }
//        BulkResponse result = client.bulk(br.build());
//        if (result.errors()) {
//            log.error("商品上架 -> 批量保存出错！");
//            for (BulkResponseItem item: result.items()) {
//                if (item.error() != null) {
//                    log.error("{} 号商品,错误原因：{}",item.id(),item.error().reason());
//                }
//            }
//        }
//        log.info("elasticsearch 返回数据是：{}",result.toString());
//        return result.errors();

        // 保存到es
        // 1 给es中建立索引，product
        BulkRequest bulkRequest = new BulkRequest();
        // 1 构造保存请求
        for (SkuElasticModel model : skuElasticModels) {
            IndexRequest indexRequest = new IndexRequest(ElasticSearchConstant.PRODUCT_ES_INDEX);
            indexRequest.id(model.getSkuId().toString());
            String s = JSON.toJSONString(model);
            indexRequest.source(s, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);
        // TODO 如果批量错误
        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        log.info("商品上架完成,{},返回数据,{}",collect,bulk.toString());
        return  b;
    }
}
