package com.bdaim.api.service;

import com.bdaim.api.Dto.ApiData;
import com.bdaim.api.dao.ApiDao;
import com.bdaim.api.entity.ApiEntity;
import com.bdaim.auth.LoginUser;
import com.bdaim.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApiService {
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ApiDao apiDao;


    public int saveApiProperty(ApiData apiData, String apiId,LoginUser lu ) {
        Map<String ,Object> map=new HashMap<>();
        if (StringUtil.isEmpty(apiId) || "0".equals(apiId)) {
            ApiEntity entity = new ApiEntity();
            entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
            entity.setContext(apiData.getContext());
            entity.setContextTexplate(apiData.getContextTemplate());
            entity.setCreateBy(lu.getName());
            entity.setName(apiData.getApiName());
            entity.setVersion(apiData.getApiVersion());
            entity.setProvider(lu.getName());
            int id = (int) apiDao.saveReturnPk(entity);
            map.put("apiId",id);
        } else {


        }

//        String sql ="INSERT INTO am_api_property (api_id,property_name,property_value,create_time) VALUE (?,?,?,?)";
//        jdbcTemplate.update(sql,id,key,value,new Timestamp(System.currentTimeMillis()));
        return 1;
    }

}
