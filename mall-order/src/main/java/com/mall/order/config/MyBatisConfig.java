package com.mall.order.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @ClassName: MyBatisConfig
 * @Description: 分页插件
 * @Author: L
 * @Create: 2023-07-24 15:46
 * @Version: 1.0
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.mall.order.dao")
public class MyBatisConfig {

    /**
     * 新的分页插件,一缓和二缓遵循mybatis的规则,需要设置 MybatisConfiguration#useDeprecatedExecutor = false 避免缓存出现问题(该属性会在旧插件移除后一同移除)
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        //数据库类型 对于单一数据库类型来说,都建议配置该值,避免每次分页都去抓取数据库类型
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        //溢出总页数后是否进行处理(默认不处理,参见 插件#continuePage 方法)
        paginationInnerInterceptor.setOverflow(true);
        //单页分页条数限制(默认无限制,参见 插件#handlerLimit 方法)
        paginationInnerInterceptor.setMaxLimit(1000L);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }

}
