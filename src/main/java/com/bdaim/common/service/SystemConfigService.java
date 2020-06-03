package com.bdaim.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author chengning@salescomm.net
 * @description TODO
 * @date 2020/5/25 17:56
 */
@Service
public class SystemConfigService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int saveSystemConfig(String key, String value) {
        return jdbcTemplate.update("UPDATE t_system_config SET property_value = ?  WHERE property_name = ? ", value, key);
    }
}
