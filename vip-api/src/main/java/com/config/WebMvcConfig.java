package com.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

//@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.dir}")
    private String upload;

    /**
     全局配置一次性解决每一个方法上都去加注解未免太麻烦了
     全局配置只需要在配置类中重写addCorsMappings方法
     表示本应用的所有方法都会去处理跨域请求，
     allowedMethods表示允许通过的请求数,
     allowedHeaders则表示允许的请求头。
     ，就不必在每个方法上单独配置跨域了
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("*");
    }

    /**
     * 静态资源跨域
     * @param registry
     */
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(upload)
                .setCachePeriod(1);
    }

    /**
     * 增加图片转换器
     * @param converters
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new BufferedImageHttpMessageConverter());
    }

//   @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        // 定义jwt拦截器的规则
//        registry.addInterceptor(new JwtFilter()).addPathPatterns("/**")
//                .excludePathPatterns("/api/guke/v1/loginByPhone")  // 使用手机号码登录
//                .excludePathPatterns("/api/guke/v1/regist")        // 注册接口
//                .excludePathPatterns("/api/guke/v1/checkPhone")    // 检验手机号码是否注册
//                .excludePathPatterns("/api/v1/code")               // 发送验证码
//                .excludePathPatterns("/api/guke/v1/loginByOpenId") // 使用openId登录
//                .excludePathPatterns("/api/v1/upload")             // 文件上传接口
//                .excludePathPatterns("/api/shiji/v1/shijiCodeCheck")     // 检验验证码是否正确
//                .excludePathPatterns("/tmp/**")                     // 放行静态资源
//                .excludePathPatterns("/images/**")                  //
//                .excludePathPatterns("/api/shiji/v1/regist")        // 注册
//                .excludePathPatterns("/api/shiji/v1/login")         // 司机登录
//                .excludePathPatterns("/api/shiji/v1/test")
//                .excludePathPatterns("/wx/check")
//                ;
//    }
}
