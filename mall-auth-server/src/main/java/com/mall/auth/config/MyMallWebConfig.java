package com.mall.auth.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @ClassName: MallWebConfig
 * @Description:
 * @Author: L
 * @Create: 2023-08-13 15:57
 * @Version: 1.0
 */

@Configuration
public class MyMallWebConfig implements WebMvcConfigurer {
    /**
     * 视图映射
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        /**
         * 如果controller只是跳转视图功能，可以直接注入controller
         *     @GetMapping("/login.html")
         *     public String loginPage(){
         *         return "login";
         *     }
         * registry.addViewController("/login.html").setViewName("login");
         */
        //只是get请求能映射
//        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");
    }
}
