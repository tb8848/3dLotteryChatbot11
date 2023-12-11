package com.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ContentVersionStrategy;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.util.Arrays;

@Configuration
public class ImageMapperConfig implements WebMvcConfigurer {

    @Value("${upload.dir}")
    private String upload;

    @Value("${upload.tmp.dir}")
    private String tmp ;

    @Autowired
    private LoginInterceptor apiInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        VersionResourceResolver versionResourceResolver = new VersionResourceResolver()
                .addVersionStrategy(new ContentVersionStrategy(), "/**");

        String os = System.getProperty("os.name");
        registry.addResourceHandler("/upload/**").addResourceLocations("file:" + upload).setCachePeriod(31556926).resourceChain(true).addResolver(versionResourceResolver); ;
        registry.addResourceHandler("/image/**").addResourceLocations("file:" + upload).setCachePeriod(31556926).resourceChain(true).addResolver(versionResourceResolver);
        registry.addResourceHandler("/screen/**").addResourceLocations("file:" + upload + "screen/").setCachePeriod(31556926).resourceChain(true).addResolver(versionResourceResolver);;
        registry.addResourceHandler("/video/**").addResourceLocations("file:" + upload + "video/").setCachePeriod(31556926).resourceChain(true).addResolver(versionResourceResolver);;
        registry.addResourceHandler("/images/**").addResourceLocations("file:" + upload + "images/").setCachePeriod(31556926).resourceChain(true).addResolver(versionResourceResolver);;
        registry.addResourceHandler("/apks/**").addResourceLocations("file:" + upload + "apks/").setCachePeriod(31556926).resourceChain(true).addResolver(versionResourceResolver);;

        registry.addResourceHandler("/banners/**").addResourceLocations("file:" + upload + "images/banners").setCachePeriod(31556926).resourceChain(true).addResolver(versionResourceResolver);;
        // 临时目录添加访问映射
        registry.addResourceHandler("/tmp/**").addResourceLocations("file:" + tmp ).setCachePeriod(31556926).resourceChain(true).addResolver(versionResourceResolver);;

        registry.addResourceHandler("/upload/**").addResourceLocations("/upload/").setCachePeriod(31556926).resourceChain(true).addResolver(versionResourceResolver); ;
        registry.addResourceHandler("/admin/**.html").addResourceLocations("/admin/") ;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiInterceptor).addPathPatterns("/**").excludePathPatterns(Arrays.asList("/admin/adminLogin","/admin/**/*.css","/admin/**/*.js","/admin/**/*.gif","/admin/**/*.png","/admin/**/*.jpg","/admin/**/*.html"));
    }
}
