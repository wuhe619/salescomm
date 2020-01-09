package com.bdaim;

import com.bdaim.api.service.ApiService;
import com.bdaim.common.auth.AuthController;
import com.bdaim.common.auth.service.TokenCacheService;
import com.bdaim.common.db.HibernateConfig;
import com.bdaim.common.error.ErrorController;
import com.bdaim.common.security.AuthExceptionHandler;
import com.bdaim.common.security.SecurityConfig;
import com.bdaim.common.security.TokenAuthenticationFilter;
import com.bdaim.common.security.TokenAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

//import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 启动类
 */
@SpringBootApplication()
@Import({SecurityConfig.class, TokenAuthenticationFilter.class, TokenAuthenticationProvider.class, AuthExceptionHandler.class, AuthController.class, TokenCacheService.class, ErrorController.class, HibernateConfig.class})
@ServletComponentScan(basePackages = "com.bdaim.common.controller")
//@EnableScheduling
public class BPApp {

    private static Logger logger = LoggerFactory.getLogger(BPApp.class);

    public static void main(String[] args) {
        SpringApplication.run(BPApp.class, args);
        System.out.println(AppConfig.getYtx_spuid());
        logger.info("dbUrl:" + AppConfig.getDbUrl());
    }


}
