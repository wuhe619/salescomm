package com.bdaim.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Maps;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.servlet.DispatcherType;
import java.util.Map;

/**
 * Filter配置
 */
@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean xssFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");
        registration.setName("xssAndSqlFilter");
        registration.setOrder(Integer.MAX_VALUE);
        Map<String, String> initParameters = Maps.newHashMap();
        //-excludes用于配置不需要参数过滤的请求url;
        initParameters.put("excludes", "/favicon.ico,/img/*,/js/*,/css/*");
        //-isIncludeRichText默认为true，主要用于设置富文本内容是否需要过滤。
        initParameters.put("isIncludeRichText", "false");
        return registration;
    }

    /**
     * 描述 : xssObjectMapper
     *
     * @param builder builder
     * @return xssObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper xssObjectMapper(Jackson2ObjectMapperBuilder builder) {
        //解析器
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        //注册xss解析器
        SimpleModule xssModule = new SimpleModule("XssStringJsonSerializer");
        //xssModule.addSerializer(String.class, new XssJacksonSerializer());
        //xssModule.addDeserializer(String.class, new XssJacksonDeserializer());
        objectMapper.registerModule(xssModule);
        //返回
        return objectMapper;
    }


}
