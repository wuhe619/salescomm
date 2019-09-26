package com.bdaim.customs.utils;

import com.bdaim.common.util.spring.SpringContextHelper;
import com.bdaim.customs.entity.BusiTypeEnum;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class HMetaDataDefUtil {

    @PostConstruct
    private void init(){
        JdbcTemplate jdbcTemplate = (JdbcTemplate) SpringContextHelper.getBean("jdbcTemplate");
        List<String> types = BusiTypeEnum.getTypeList();
        for(String type:types){
            String sql=" create table  if not exists  h_data_manager_"+type+" like h_data_manager";
            jdbcTemplate.execute(sql);
        }

    }
}
