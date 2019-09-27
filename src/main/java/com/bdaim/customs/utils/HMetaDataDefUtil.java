package com.bdaim.customs.utils;

import com.bdaim.common.util.spring.SpringContextHelper;
import com.bdaim.customs.entity.BusiTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class HMetaDataDefUtil {
    private static Logger log = LoggerFactory.getLogger(HMetaDataDefUtil.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;


    @PostConstruct
    private void init(){
       // JdbcTemplate jdbcTemplate = (JdbcTemplate) SpringContextHelper.getBean("jdbcTemplate");
        List<String> types = BusiTypeEnum.getTypeList();
        for(String type:types){
            String sql=" create table  if not exists  h_data_manager_"+type+" like h_data_manager";
            jdbcTemplate.execute(sql);
        }
        log.info("create table finish");

    }
}
