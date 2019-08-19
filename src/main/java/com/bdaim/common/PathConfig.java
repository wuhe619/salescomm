package com.bdaim.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String pathF = PROPERTIES.getProperty("file.separator");
        registry.addResourceHandler("/pdf/**").addResourceLocations("file:"+pathF+"data"+pathF+"file"+pathF+"pdf"+pathF);
    }
}