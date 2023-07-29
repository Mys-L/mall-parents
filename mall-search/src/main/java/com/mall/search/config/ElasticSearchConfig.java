package com.mall.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: ElasticSearchConfig
 * @Description:
 * @Author: L
 * @Create: 2023-07-29 08:14
 * @Version: 1.0
 */
@RefreshScope
@Configuration
public class ElasticSearchConfig {
    /**
     * @author: L
     * @description:  参照官网配置elasticsearch 提取 hostname，port 配置
     * @date: 08:07:31
     * @return: co.elastic.clients.elasticsearch.ElasticsearchClient
     */

    @Value("${elasticsearch.host-name}")
    private String hostname;
    @Value("${elasticsearch.port}")
    private int port;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(new HttpHost(hostname, port)).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
