package com.mall.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @ClassName: MallSpringSessionConfig
 * @Description: 扩大session作用域
 * @Author: L
 * @Create: 2023-10-25 13:10
 * @Version: 1.0
 */
@Configuration
public class MyMallSpringSessionConfig {

    @Bean //通过修改CookieSerializer扩大session的作用域至**.mall.com
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setCookieName("MALLSESSION");
        cookieSerializer.setDomainName("mall.com");
        return cookieSerializer;
    }

    @Bean //由于默认使用jdk进行序列化，通过导入RedisSerializer修改为json序列化
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

}
