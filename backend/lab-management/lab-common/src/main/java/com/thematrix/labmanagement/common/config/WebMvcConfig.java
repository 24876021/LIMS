package com.thematrix.labmanagement.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置和资源映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedMethods("*")
                .allowedOriginPatterns("http://*:8081","http://*:8083")
            .allowedHeaders("*")
            .allowCredentials(true)
            .exposedHeaders("")
            .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/avatar/**")
                .addResourceLocations("file:" + uploadPath + "avatar/");
        registry.addResourceHandler("/uploads/attachment/**")
                .addResourceLocations("file:" + uploadPath + "attachment/");
    }
}
