//package com.mall.search;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
//import co.elastic.clients.elasticsearch._types.query_dsl.Query;
//import co.elastic.clients.elasticsearch.core.*;
//import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
//import co.elastic.clients.elasticsearch.core.bulk.CreateOperation;
//import co.elastic.clients.elasticsearch.indices.ExistsRequest;
//import co.elastic.clients.elasticsearch.indices.*;
//import com.mall.search.constant.ElasticSearchConstant;
//import lombok.SneakyThrows;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @ClassName: ElasticAPI
// * @Description: elasticsearch 8 java api 的代码实现
// * @Author: L
// * @Create: 2023-08-02 22:35
// * @Version: 1.0
// */
//
//@SpringBootTest
//public class ElasticAPI {
//    @Autowired
//    ElasticsearchClient client;
//
//    private final String ELASTIC_INDEX = "elastic_index";
//
//    /**
//     * 索引
//     */
//    @SneakyThrows
//    @Test
//    void index(){
//        /**
//         * 索引的API
//         */
//        //获取索引客户端对象
//        ElasticsearchIndicesClient indices = client.indices();
//        //判断索引是否存在
//        ExistsRequest existsRequest = new ExistsRequest.Builder().index(ELASTIC_INDEX).build();
//        boolean flg = indices.exists(existsRequest).value();
//        if (!flg){
//            //构建器方式 构建对象 es基本全采用这种方式 构建器模式需要传递参数
//            CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder().index(ELASTIC_INDEX).build();
//            CreateIndexResponse createIndexResponse = indices.create(createIndexRequest);
//            System.out.println("创建索引的响应对象"+createIndexResponse);
//            //简写
////            CreateIndexResponse createIndexResponse1 = indices.create(req -> req.index(ELASTIC_INDEX));
//        }else {
//            System.out.println("索引 "+ELASTIC_INDEX + " 已经存在");
//        }
//        /**
//         * 查询索引
//         */
//        GetIndexRequest getIndexRequest = new GetIndexRequest.Builder().index(ELASTIC_INDEX).build();
//        GetIndexResponse getIndexResponse = indices.get(getIndexRequest);
//        IndexState indexState = getIndexResponse.get(ELASTIC_INDEX);
//        System.out.println("查询的响应结果"+getIndexResponse);
//        //简写
////        GetIndexResponse getIndexResponse1 = indices.get(g -> g.index(ELASTIC_INDEX));
//        /**
//         * 删除索引
//         */
//        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest.Builder().index(ELASTIC_INDEX).build();
//        DeleteIndexResponse deleteIndexResponse = indices.delete(deleteIndexRequest);
//        System.out.println("删除的响应结果"+deleteIndexResponse);
//        //简写
////        DeleteIndexResponse deleteIndexResponse1 = indices.delete(d -> d.index(ELASTIC_INDEX));
//    }
//
//    /**
//     * 文档的API
//     */
//    @SneakyThrows
//    @Test
//    void document (){
//        /**
//         * 增加文档
//         */
//        User user = new User(10001,"张三",30,"男");
//        CreateRequest<User> createRequest = new CreateRequest.Builder<User>()
//                .index(ELASTIC_INDEX)
//                .id("10001")
//                .document(user)
//                .build();
//        CreateResponse createResponse = client.create(createRequest);
//        System.out.println("创建文档响应结果"+createResponse);
//        //简写
////        Result result = client.create(req -> req.index(ELASTIC_INDEX).id("10001").document(user)).result();
//        /**
//         * 批量操作
//         */
//        List<BulkOperation> operationList = new ArrayList<BulkOperation>();
//        for (int i = 0; i <= 3; i++) {
//            CreateOperation<User> userCreateOperation = new CreateOperation.Builder<User>()
//                    .index(ELASTIC_INDEX)
//                    .id("200"+i)
//                    .document(new User(200+i,"李四"+i,30+i,"男"))
//                    .build();
//            BulkOperation opt = new BulkOperation.Builder().create(userCreateOperation).build();
//            operationList.add(opt);
//        }
//        BulkRequest bulkRequest = new BulkRequest.Builder()
//                .operations(operationList)
//                .build();
//        BulkResponse bulkResponse = client.bulk(bulkRequest);
//        System.out.println("批量文档响应结果"+bulkResponse);
//        //简写 比较特殊
//        List<User> users = new ArrayList<User>();
//        for (int i = 0; i < 6; i++) {
//            users.add(new User(10001+i,"张三"+i,30+i,"男"));
//        }
////        client.bulk(
////                req->{
////                    users.forEach(
////                            user1 -> {
////                                req.operations(
////                                        b->b.create(
////                                                d->d.index(ELASTIC_INDEX)
////                                                        .id(String.valueOf(user1.getId()))
////                                                        .document(user1)
////                                        )
////                                );
////                            }
////                    );
////                    return req;
////                }
////            );
//        /**
//         * 文档的删除
//         */
//        DeleteRequest deleteRequest = new DeleteRequest.Builder()
//                .index(ELASTIC_INDEX)
//                .id("10001")
//                .build();
//        DeleteResponse deleteResponse = client.delete(deleteRequest);
//        System.out.println("删除文档响应结果"+deleteResponse);
//        //简写
////        DeleteResponse deleteResponse1 = client.delete(d -> d.index(ELASTIC_INDEX).id("10001"));
//    }
//
//    /**
//     * 文档的search
//     */
//    @SneakyThrows
//    @Test
//    void search(){
//
//        MatchQuery.Builder matchQuery = new MatchQuery.Builder();
//        matchQuery.field("age").query("30");
//
//        Query query = new Query(matchQuery.build());
//
//        SearchRequest searchRequest = new SearchRequest.Builder()
//                .index(ELASTIC_INDEX)
//                .query(query)
//                .build();
//        SearchResponse<Object> searchResponse = client.search(searchRequest, Object.class);
//        System.out.println("查询响应结果"+searchResponse);
//
//        //简写
////        HitsMetadata<Object> hits = client.search(search -> search
////                        .index(ELASTIC_INDEX)
////                        .query(q -> q
////                                .match(m -> m
////                                        .field("age")
////                                        .query("30")
////                                )
////                        )
////                , Object.class).hits();
////
////        System.out.println("查询响应结果"+hits);
//
//
//    }
//
//    @SneakyThrows
//    @Test
//    void test(){
//        SearchRequest.Builder searchBuilder = new SearchRequest.Builder();
//        searchBuilder.index(ELASTIC_INDEX);
//
//        List<Integer> list = new ArrayList<>();
//        list.add(30);
//        list.add(31);
////        Query.Builder builder = new Query.Builder();
////        builder.match(m->m.field("age").query("30"));
////
////        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
////        Query.Builder queryBuilder = new Query.Builder();
////        queryBuilder.bool(boolQuery.build());
//
////        searchBuilder.query(queryBuilder.build());
//        searchBuilder.query(MatchQuery.of(t->t.field("gender").query("男"))._toQuery());
//        searchBuilder
//                .size(0)
//                .aggregations("name_agg", a->a.terms(v->v.field("name.keyword"))
//                    .aggregations("age_agg", a1->a1.terms(v->v.field("age").size(10))))
//                .aggregations("gender_agg", a2->a2.terms(v->v.field("gender.keyword").size(10)));
//
//        SearchRequest searchRequest = searchBuilder.build();
//
//        System.out.println("生成的DSL"+searchRequest.query());
//        System.out.println("生成的DSL1"+searchRequest.aggregations());
//        System.out.println("生成的DSL2"+searchRequest.aggregations().toString());
//        System.out.println("生成的DSL3"+searchRequest.aggregations().values());
//
//        SearchResponse<Object> search = client.search(searchRequest, Object.class);
//        System.out.println("结果"+search);
//        System.out.println("聚合结果"+search.aggregations());
//    }
//
//    @SneakyThrows
//    @Test
//    void testallprodute(){
//        SearchRequest.Builder searchBuilder = new SearchRequest.Builder();
//        searchBuilder.index(ElasticSearchConstant.PRODUCT_ES_INDEX);
//        SearchResponse<Object> searchResponse = client.search(searchBuilder.build(), Object.class);
//        System.out.println(searchResponse);
//    }
//}
