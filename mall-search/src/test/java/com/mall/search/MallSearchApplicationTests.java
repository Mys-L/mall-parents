//package com.mall.search;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
//import co.elastic.clients.elasticsearch._types.query_dsl.Query;
//import co.elastic.clients.elasticsearch.core.CreateResponse;
//import co.elastic.clients.elasticsearch.core.IndexResponse;
//import co.elastic.clients.elasticsearch.core.SearchRequest;
//import co.elastic.clients.elasticsearch.core.SearchResponse;
//import co.elastic.clients.elasticsearch.core.search.Hit;
//import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
//import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
//import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
//import co.elastic.clients.transport.endpoints.BooleanResponse;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.io.IOException;
//
//@Slf4j
//@SpringBootTest
//class MallSearchApplicationTests {
//    @Autowired
//    ElasticsearchClient client;
//
//    @Test
//    void existsTest() throws IOException {
//        //判断index是否存在
//        BooleanResponse exists = client.indices().exists(e -> e.index("new_index"));
//        System.out.println("判断索引是否存在："+exists.value());
//
//    }
//
//    @Test
//    void createTest() throws IOException {
//        //增加index
////        CreateIndexResponse indexResponse = client.indices().create(i ->i.index("new_index"));
////        System.out.println("创建新的索引："+indexResponse.acknowledged());
//
//        //增加index 并同时加入数据
//        User user = new User(1000,"yanyan", 20,"女");
//        IndexResponse indexResponse1 = client.index(d -> d
//                .index("newapi")
//                .id("1")
//                .document(user)
//        );
//
//        System.out.println("创建新的索引："+indexResponse1.result().toString());
//    }
//    @Test
//    public void queryTest() throws IOException {
//        //查询Index
//        GetIndexResponse getIndexResponse = client.indices().get(i -> i.index("new_index"));
//
//        System.out.println("查询索引的结果：getIndexResponse.result() = "+getIndexResponse.result());
//        System.out.println("查询索引的结果：getIndexResponse.result().keySet() = "+getIndexResponse.result().keySet());
//        System.out.println("查询索引的结果：getIndexResponse = "+getIndexResponse.toString());
//    }
//
//    @Test
//    public void deleteTest() throws IOException {
//        //删除index
//        DeleteIndexResponse deleteIndexResponse = client.indices().delete(d -> d.index("new_index"));
//        System.out.println("删除索引："+deleteIndexResponse.acknowledged());
//    }
//
//    /**
//     * TODO 官网api
//     *https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/indexing-bulk.html
//     */
//    @Test
//   public void addIndexData() throws IOException {
//        /*
//         * Elasticsearch8.x版本中RestHighLevelClient被弃用，新版本中全新的Java客户端Elasticsearch Java API Client中常用API练习
//         * https://www.jianshu.com/p/bf746aa59681
//         * https://blog.csdn.net/b___w/article/details/123924063
//         * https://blog.csdn.net/u013979493/article/details/123172320
//         * 新版本可以直接传java对象
//         * 新建document
//         */
//        User user = new User();
//        user.setName("张三");
//        user.setAge(22);
//        user.setGender("男");
////        String jsonString = JSON.toJSONString(user);
//        // 构建一个创建Doc的请求
//        CreateResponse createResponse = client.create(e->e.index("new_index").id("2").document(user));
//        System.out.println("createResponse.result() = " + createResponse.result());
//    }
//
//    @Test
//    public void searchTest() throws IOException {
//        SearchResponse<User> searchResult = client.search(e -> e.index("new_index"), User.class);
//        HitsMetadata<User> hits = searchResult.hits();
//        for (Hit<User> hit : hits.hits()) {
//            System.out.println("user = " + hit.source().toString());
//        }
//
//        System.out.println("searchResult.hits().total().value() = " + searchResult.hits().total().value());
//
//    }
//
//
//    @SneakyThrows
//    @Test
//    public void searchData(){
//
//        //查询所有
////        SearchResponse<Object> searchResponse1 = client.search(s -> s.index("bank"), Object.class);
////        System.out.println("查询时间(毫秒) = searchResponse1 "+searchResponse1.took());
////        System.out.println("查询总记录数 = searchResponse1 "+searchResponse1.hits().total().value());
//        //按条件查询
//        String searchText = "mill";
////        SearchResponse<Object> searchResponse = client.search(s -> s
////                        .index("bank")
////                        .query(q -> q
////                                .match(m -> m
////                                        .field("address")
////                                        .query(searchText))),
////                Object.class);
////        System.out.println("查询时间(毫秒) = "+searchResponse.took());
////        System.out.println("查询总记录数 = "+searchResponse.hits().total().value());
////        searchResponse.hits().hits().forEach(o ->
////                System.out.println("查询结果："+o.source().toString())
////        );
//        //查询条件
//        Query query = MatchQuery.of(m -> m
//                .field("address")
//                .query(searchText)
//        )._toQuery();
//        //#按照年龄聚合，并且求这些年龄段的这些人的平均薪资
//        /**
//         * GET bank/_search
//         * {
//         *   "query": {
//         *     "match_all": {}
//         *   },
//         *   "aggs": {
//         *     "age_agg": {
//         *       "terms": {
//         *         "field": "age",
//         *         "size": 100
//         *       },
//         *       "aggs": {
//         *         "balance_avg": {
//         *           "avg": {
//         *             "field": "balance"
//         *           }
//         *         }
//         *       }
//         *     }
//         *   }
//         * }
//         */
//        SearchResponse<Object> searchR1= client.search(s -> s
//                        .index("bank")
//                        .query(query) //添加条件
//                        .size(0)
//                        .aggregations("age_agg", a -> a
//                                .terms(t -> t
//                                        .field("age")
//                                        .size(100)
//                                )
//                                .aggregations("balance_avg", balance -> balance
//                                        .avg(ba -> ba
//                                                .field("balance")
//                                        )
//                                )
//                        )
//                , Object.class);
//        //条件加组合查询
//        System.out.println(searchR1.took());
//        System.out.println(searchR1.hits().total().value());
//        System.out.println(searchR1);
////
////        LongTermsAggregate ageAgg = searchR1.aggregations().get("age_agg").lterms();
////        Buckets<LongTermsBucket> bucketsAgeAgg = ageAgg.buckets();
////        for (LongTermsBucket b : bucketsAgeAgg.array()) {
////            System.out.println("年龄 = "+b.key() + " 有 " + b.docCount()+" 个");
////            AvgAggregate balanceAvg = b.aggregations().get("balance_avg").avg();
////            System.out.println("该年龄段平均工资 = " + balanceAvg.value());
////        }
//
//    }
//
//    @SneakyThrows
//    @Test
//    void testDSL(){
//        String searchText = "mill";
////        Query query = MatchQuery.of(m -> m
////                .field("address")
////                .query(searchText)
////        )._toQuery();
//
//        MatchQuery.Builder builder1 = new MatchQuery.Builder();
//        builder1.field("address");
//        builder1.query(searchText);
//
//        Query.Builder builder2 = new Query.Builder();
//        builder2.match(v->v.field("address").query(searchText));
//
//        SearchRequest.Builder builder = new SearchRequest.Builder();
//        builder.index("bank");
//        builder.size(0);
//
//        //builder.query(builder1.build()._toQuery());
//        builder.query(builder2.build());
//
//        builder.aggregations("age_agg", a -> a
//                .terms(t -> t
//                        .field("age")
//                        .size(100)
//                )
//                .aggregations("balance_avg", balance -> balance
//                        .avg(ba -> ba
//                                .field("balance")
//                        )
//                )
//        );
//        SearchResponse<Object> search = client.search(builder.build(), Object.class);
//        System.out.println(search.took());
//        System.out.println(search.hits().total().value());
//        System.out.println(search);
//    }
//
//}
//
//
//
//
