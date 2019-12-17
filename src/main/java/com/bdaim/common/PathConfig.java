package com.bdaim.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bdaim.AppConfig;

import java.util.Properties;

/**
 * 把以 /pdf 开头的访问请求，映射到静态资源文件的 /data/file/pdf目录下
 *
 * @description:
 * @auther: Chacker
 * @date: 2019/8/9 00:40
 */
@Configuration
public class PathConfig implements WebMvcConfigurer {
    protected static final Properties PROPERTIES = new Properties(System.getProperties());

    private static final String APP_NAME = AppConfig.getApp();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String pathF = PROPERTIES.getProperty("file.separator");
        registry.addResourceHandler("/pdf/**").addResourceLocations("file:"+pathF+"data"+pathF+"file"+pathF+"pdf"+pathF);
        registry.addResourceHandler("/pic/**").addResourceLocations("file:"+pathF+"data"+pathF+"upload"+pathF);
    }
    @Bean
    public AuthInterceptor securityInterceptor() {
        return new AuthInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加拦截器
        registry.addInterceptor(securityInterceptor()).addPathPatterns("/open/**");
    }
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // 标识不为精准营销项目则不进行返回值操作
        if ("online".equalsIgnoreCase(APP_NAME)) {
            configurer.defaultContentType(MediaType.APPLICATION_JSON);
        }
    }
}