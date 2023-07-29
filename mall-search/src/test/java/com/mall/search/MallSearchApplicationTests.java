package com.mall.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class MallSearchApplicationTests {
    @Autowired
    ElasticsearchClient client;

    @Test
    void existsTest() throws IOException {
        //判断index是否存在
        BooleanResponse exists = client.indices().exists(e -> e.index("test-java"));
        System.out.println(exists.value());
    }

    @Test
    void createTest() throws IOException {
        //增加index
        CreateIndexResponse indexResponse = client.indices().create(i -> {
            return i.index("test-java");
        });
        System.out.println(indexResponse.acknowledged());
    }
    @Test
    public void queryTest() throws IOException {
        //查询Index
        GetIndexResponse getIndexResponse = client.indices().get(i -> i.index("test-java"));
        System.out.println(getIndexResponse.toString());
    }

    @Test
    public void deleteTest() throws IOException {
        //删除index
        DeleteIndexResponse deleteIndexResponse = client.indices().delete(d -> d.index("test-java"));
        System.out.println(deleteIndexResponse.acknowledged());
    }


}
@Data
class User{
    String userName;
    Integer age;
    String gender;
}