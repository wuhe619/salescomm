package com.bdaim.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author chengning@salescomm.net
 * @date 2020-01-02 10:57
 */
@Configuration
public class SystemConfig {

    @Autowired
    private ConfigurableEnvironment environment;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initDatabasePropertySourceUsage() {
        // 获取系统属性集合
        MutablePropertySources propertySources = environment.getPropertySources();
        Map<String, Object> config = new HashMap<>();
        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM t_market_resource_property WHERE property_name = 'xz_call_api' ");
        for (Map<String, Object> k : list) {
            config.put(String.valueOf(k.get("property_name")), k.get("property_value"));
        }
        // 将转换后的列表加入属性中
        Properties properties = new Properties();
        properties.putAll(config);
        // 将属性转换为属性集合,并指定名称
        PropertiesPropertySource constants = new PropertiesPropertySource("system-config", properties);
        propertySources.addLast(constants);
    }
}
