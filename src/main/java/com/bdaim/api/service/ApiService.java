package com.bdaim.api.service;

import com.bdaim.api.Dto.ApiData;
import com.bdaim.api.dao.ApiDao;
import com.bdaim.api.dao.ApiUrlMappingDao;
import com.bdaim.api.entity.ApiEntity;
import com.bdaim.api.entity.ApiUrlMappingEntity;
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
    @Autowired
    private ApiUrlMappingDao apiUrlMappingDao;


    public int saveApiProperty(ApiData apiData, String id, LoginUser lu) throws Exception {
        Map<String, Object> map = new HashMap<>();
        int apiId;
        if (StringUtil.isEmpty(id) || "0".equals(id)) {
            ApiEntity entity = new ApiEntity();
            entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
            entity.setContext(apiData.getContext());
            entity.setContextTexplate(apiData.getContextTemplate());
            entity.setCreateBy(lu.getUserName());
            entity.setName(apiData.getApiName());
            entity.setVersion(apiData.getApiVersion());
            entity.setProvider(lu.getUserName());
            entity.setStatus(0);
            apiId = (int) apiDao.saveReturnPk(entity);
            map.put("apiId", id);
        } else {
            ApiEntity entity = apiDao.getApi(Integer.valueOf(id));
            if (entity == null) {
                throw new Exception("API不存在");
            }
            entity.setContext(apiData.getContext());
            entity.setContextTexplate(apiData.getContextTemplate());
            entity.setName(apiData.getApiName());
            entity.setVersion(apiData.getApiVersion());
            entity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            entity.setUpdateBy(lu.getUserName());
            apiId = (int) apiDao.saveReturnPk(entity);
            map.put("apiId", id);
        }

        if (apiData.getUrlMappingId() == 0) {
            ApiUrlMappingEntity entity = new ApiUrlMappingEntity();
            entity.setApiId(Integer.valueOf(map.get("apiId").toString()));
            entity.setHttpMethod(apiData.getTransportHttp());
            entity.setAuthScheme("Any");
            entity.setThrottlingTier("Unlimited");
            entity.setUrlPattern(apiData.getProductionendpoints());
            apiUrlMappingDao.saveReturnPk(entity);
        } else {
            ApiUrlMappingEntity entity = apiUrlMappingDao.getApiUrlMapping(apiData.getUrlMappingId());
            if (entity == null) {
                throw new Exception("ApiUrl不存在");
            }
            entity.setHttpMethod(apiData.getTransportHttp());
            entity.setUrlPattern(apiData.getProductionendpoints());
            apiUrlMappingDao.saveReturnPk(entity);
        }
        if (StringUtil.isNotEmpty(apiData.getVisibility())) {
//apiDao.dealCustomerInfo(apiId);
        }


//        String sql ="INSERT INTO am_api_property (api_id,property_name,property_value,create_time) VALUE (?,?,?,?)";
//        jdbcTemplate.update(sql,id,key,value,new Timestamp(System.currentTimeMillis()));
        return 1;
    }

}
