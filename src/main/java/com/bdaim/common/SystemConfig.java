package com.bdaim.common;

import com.bdaim.AppConfig;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemConfig.class);

    @Autowired
    private ConfigurableEnvironment environment;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initDatabasePropertySourceUsage() {
        /**
         * Springboot整合Elasticsearch 在项目启动前设置一下的属性，防止报错
         * 解决netty冲突后初始化client时还会抛出异常
         * java.lang.IllegalStateException: availableProcessors is already set to [4], rejecting [4]
         */
        System.setProperty("es.set.netty.runtime.available.processors", "false");
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


    //@Bean(name = "transportClient")
    public TransportClient transportClient() {
        // http://localhost:9201
        String cluster = "elasticsearch";
        String host = AppConfig.getEs_rest();
        host = host.substring(host.lastIndexOf("/") + 1, host.lastIndexOf(":"));
        LOGGER.info("开始初始化transportClient,host:{},port:{},cluster:{}", host, 9300, cluster);
        TransportClient transportClient = null;
        try {
            // 配置信息
            Settings esSetting = Settings.builder()
                    .put("cluster.name", cluster) //集群名字
                    .put("client.transport.sniff", true)//增加嗅探机制，找到ES集群
                    .put("thread_pool.search.size", 5)//增加线程池个数，暂时设为5
                    .build();
            //配置信息Settings自定义
            transportClient = new PreBuiltTransportClient(esSetting);
            TransportAddress transportAddress = new TransportAddress(InetAddress.getByName(host), 9300);
            transportClient.addTransportAddresses(transportAddress);
        } catch (Exception e) {
            LOGGER.error("初始化transportClient异常", e);
        }
        return transportClient;
    }

}
