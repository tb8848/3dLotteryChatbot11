package com.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrosConfig {
    @Bean
    public FilterRegistrationBean filterRegistrationBean(){
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new MyCrosFilter());
        bean.addUrlPatterns("/*");
        return bean;
    }
}
//
//@Configuration
//public class CrosConfig  implements WebMvcConfigurer {
//
////    @Override
////    public void addViewControllers(ViewControllerRegistry registry) {
////        registry.addViewController("/").setViewName("forward:/index.html");
////        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
////    }
////
////
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/vip/**")
//                .allowedOrigins("*")
//                .allowCredentials(true)
//                .allowedMethods("GET", "POST", "DELETE", "PUT")
//                .maxAge(3600 * 24);
//    }
//
//}
//
//
////package com.mobai.config;
////import org.springframework.context.annotation.Bean;
////import org.springframework.web.servlet.config.annotation.CorsRegistry;
////import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/////** * Software：IntelliJ IDEA 2021.1.1 x64 * Author: https://www.mobaijun.com * Date: 2021/7/7 10:55 * ClassName:CorsConfig * 类描述： cors配置类 *///
////@Configuration
////public class CorsConfig {
////    @Bean    public WebMvcConfigurer corsConfigurer() {
////        return new WebMvcConfigurer() {
////            // 重写父类提供的跨域请求处理的接口
////        @Override
////        public void addCorsMappings(CorsRegistry registry) {                // 添加映射路径                registry.addMapping("/**")                        // 放行哪些原始域                        .allowedOriginPatterns("*")                        // 是否发送Cookie信息                        .allowCredentials(true)                        // 放行哪些原始域(请求方式)                        .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")                        // 放行哪些原始域(头部信息)                        .allowedHeaders("*")                        // 暴露哪些头部信息（因为跨域访问默认不能获取全部头部信息）                        .exposedHeaders("Header1", "Header2")                        // 预请求的结果有效期，默认1800分钟,3600是一小时                        .maxAge(3600);            }        };    }}
