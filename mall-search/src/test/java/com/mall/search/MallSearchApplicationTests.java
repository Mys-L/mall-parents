package com.mall.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.CreateResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@Slf4j
@SpringBootTest
class MallSearchApplicationTests {
    @Autowired
    ElasticsearchClient client;

    @Test
    void existsTest() throws IOException {
        //判断index是否存在
        BooleanResponse exists = client.indices().exists(e -> e.index("new_index"));
        System.out.println("判断索引是否存在："+exists.value());

    }

    @Test
    void createTest() throws IOException {
        //增加index
        CreateIndexResponse indexResponse = client.indices().create(i ->i.index("new_index"));
        System.out.println("创建新的索引："+indexResponse.acknowledged());
    }
    @Test
    public void queryTest() throws IOException {
        //查询Index
        GetIndexResponse getIndexResponse = client.indices().get(i -> i.index("new_index"));

        System.out.println("查询索引的结果：getIndexResponse.result() = "+getIndexResponse.result());
        System.out.println("查询索引的结果：getIndexResponse.result().keySet() = "+getIndexResponse.result().keySet());
    }

    @Test
    public void deleteTest() throws IOException {
        //删除index
        DeleteIndexResponse deleteIndexResponse = client.indices().delete(d -> d.index("new_index"));
        System.out.println("删除索引："+deleteIndexResponse.acknowledged());
    }

    @Test
   public void addIndexData() throws IOException {
        /*
         * Elasticsearch8.x版本中RestHighLevelClient被弃用，新版本中全新的Java客户端Elasticsearch Java API Client中常用API练习
         * https://www.jianshu.com/p/bf746aa59681
         * https://blog.csdn.net/b___w/article/details/123924063
         * 新版本可以直接传java对象
         * 新建document
         */
        User user = new User();
        user.setUserName("张三");
        user.setAge(22);
        user.setGender("男");
//        String jsonString = JSON.toJSONString(user);
        // 构建一个创建Doc的请求
        CreateResponse createResponse = client.create(e->e.index("new_index").id("2").document(user));
        System.out.println("createResponse.result() = " + createResponse.result());
    }

    @Test
    public void searchTest() throws IOException {
        SearchResponse<User> searchResult = client.search(e -> e.index("new_index"), User.class);
        HitsMetadata<User> hits = searchResult.hits();
        for (Hit<User> hit : hits.hits()) {
            System.out.println("user = " + hit.source().toString());
        }
        System.out.println("searchResult.hits().total().value() = " + searchResult.hits().total().value());

    }

}
@Data
class User{
    String userName;
    Integer age;
    String gender;
}