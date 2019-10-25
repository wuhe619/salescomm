package com.bdaim.util;

import com.google.gson.GsonBuilder;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitElasticSearchConfig {

    private JestClient client;

    @Bean
    public JestClient jestClient() {
        return client;
    }

    public InitElasticSearchConfig(){
        client = getClientConfig(PropertiesUtil.getStringValue("es.rest")) ;
    }

    public JestClient getClientConfig(String esUrl){
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(esUrl)
                .gson(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create())
                .multiThreaded(true)
                .readTimeout(10000)
                .build());
        JestClient client = factory.getObject();
        return client ;
    }
}
