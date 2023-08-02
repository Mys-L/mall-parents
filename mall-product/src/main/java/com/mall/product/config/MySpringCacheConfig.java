package com.mall.product.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @ClassName: MySpringCacheConfig
 * @Description: 开始springcache
 *              为了将数据保存为json格式 自定义CacheConfiguration
 *              应为用了redis 自定义RedisCacheConfiguration即可
 * @Author: L
 * @Create: 2023-08-02 13:21
 * @Version: 1.0
 */
@EnableConfigurationProperties(CacheProperties.class)
@EnableCaching
@Configuration
public class MySpringCacheConfig {
    /**
     * 配置文件中的东西没有用上；
     * 1、原来和配置文件绑定的配置类是这样子的
     *      @ConfigurationProperties(prefix = "spring.cache")
     *      public class CacheProperties
     * 2、要让他生效
     *      @EnableConfigurationProperties(CacheProperties.class)
     */
    @Bean
    RedisCacheConfiguration redisCacheConfiguration (CacheProperties cacheProperties){
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        //修改默认序列化器
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        //将配置文件配置生效
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        //将配置文件中的所有配置都生效
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }
}
