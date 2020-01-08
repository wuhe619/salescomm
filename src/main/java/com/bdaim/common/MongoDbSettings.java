package com.bdaim.common;

import com.mongodb.MongoClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDbSettings {
    @Bean
    public MongoClientOptions mongoOptions() {
        return MongoClientOptions
                .builder()
                .maxWaitTime(10000)
                .serverSelectionTimeout(3000)
                .maxConnectionIdleTime(5000)
                .build();
    }
}
