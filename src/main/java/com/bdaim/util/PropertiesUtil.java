package com.bdaim.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Properties;

public class PropertiesUtil {

    private final static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static Properties properties;

    private static String PROPERTY_NAME = "application.yml";

    static {
        Resource resource = new ClassPathResource(PROPERTY_NAME);
        try {
            YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
            yamlFactory.setResources(resource);
            properties = yamlFactory.getObject();
        } catch (Exception e) {
            logger.error("加载配置文件失败:{}",e);
        }
    }

    public static Properties getProperties() {
        return properties;
    }

    public static Integer getIntegerValue(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public static Integer getIntegerValue(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static String getStringValue(String key) {
        return (String) properties.getProperty(key);
    }

    public static String getStringValue(String key, String defaultValue) {
        String property = properties.getProperty(key);
        return property == null ? defaultValue : property;
    }

    public static boolean getBooleanValue(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public static boolean getBooleanValue(String key, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(properties.getProperty(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static Object getValue(String key) {
        return properties.getProperty(key);
    }

    public static void main(String[] args) {
        System.out.println(PropertiesUtil.getStringValue("server.port.1", "1"));

    }
}
