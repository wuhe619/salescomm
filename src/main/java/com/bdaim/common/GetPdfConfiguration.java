package com.bdaim.common;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 把pdf的访问路径忽略到权限验证
 *
 * @description:
 * @auther: Chacker
 * @date: 2019/8/9 01:24
 */
@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER - 10)
public class GetPdfConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/pdf/**");
    }
}
