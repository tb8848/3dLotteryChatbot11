package com.config;


import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 我们自己实例化对象，放到spring容器
 */
@Configuration
public class MyBatisPage {

    @Bean
    public PaginationInnerInterceptor paginationInterceptor()
    {
        return new PaginationInnerInterceptor();
    }
}
