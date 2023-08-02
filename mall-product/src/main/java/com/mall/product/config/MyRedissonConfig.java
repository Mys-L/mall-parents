package com.mall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @ClassName: MyRedissonConfig
 * @Description: redisson 使用方法配置方式，且是单节点
 * @Author: L
 * @Create: 2023-08-01 23:08
 * @Version: 1.0
 */
@Configuration
public class MyRedissonConfig {
    /**
     * 初次使用redisson 代码方式配置
     * 之后用 redisson-spring-boot-starter 直接写配置文件
     */
    @Bean(destroyMethod="shutdown")
    RedissonClient redisson() throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.10.100:6379");
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

}
