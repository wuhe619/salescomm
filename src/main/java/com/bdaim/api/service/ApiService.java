package com.bdaim.api.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;

@Service
public class ApiService {
    @Resource
    private JdbcTemplate jdbcTemplate;


    public int saveApiProperty(int id,String key,String value){
        String sql ="INSERT INTO am_api_property (api_id,property_name,property_value,create_time) VALUE (?,?,?,?)";
        jdbcTemplate.update(sql,id,key,value,new Timestamp(System.currentTimeMillis()));
        return 1;
    }

}
