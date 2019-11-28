package com.bdaim.api.service;

import com.bdaim.api.Dto.ApiData;
import com.bdaim.api.Dto.ApiDefine;
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
        ApiDefine apiDefine = apiData.getApiDefine();
        if (apiData.getUrlMappingId() == 0) {
            ApiUrlMappingEntity entity = new ApiUrlMappingEntity();

            entity.setApiId(Integer.valueOf(map.get("apiId").toString()));
            entity.setHttpMethod(apiDefine.getRequest_method());
            entity.setAuthScheme("Any");
            entity.setThrottlingTier("Unlimited");
            entity.setUrlPattern(apiDefine.getResource_url_pattern());
            apiUrlMappingDao.saveReturnPk(entity);
        } else {
            ApiUrlMappingEntity entity = apiUrlMappingDao.getApiUrlMapping(apiData.getUrlMappingId());
            if (entity == null) {
                throw new Exception("ApiUrl不存在");
            }
            entity.setHttpMethod(apiDefine.getRequest_method());
            entity.setUrlPattern(apiDefine.getResource_url_pattern());
            apiUrlMappingDao.saveReturnPk(entity);
        }
        if (StringUtil.isNotEmpty(apiData.getVisibility())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "visibility", apiData.getVisibility());
        }
        //缩略图连接
        if (StringUtil.isNotEmpty(apiData.getApiThumb())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "api_thumb", apiData.getVisibility());
        }
        //标签
        if (StringUtil.isNotEmpty(apiData.getTags())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "tages", apiData.getTags());
        }
        //终端类型
        if (StringUtil.isNotEmpty(apiData.getEndpointType())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "endpoint_type", apiData.getEndpointType());
        }
        //服务端地址
        if (StringUtil.isNotEmpty(apiData.getProductionendpoints())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "productionendpoints", apiData.getProductionendpoints());
        }
        //是否作为默认版本
        if (StringUtil.isNotEmpty(apiData.getDefaultVersion())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "default_version", apiData.getDefaultVersion());
        }
        //有效等级
        if (StringUtil.isNotEmpty(apiData.getTier())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "tier", apiData.getTier());
        }
        //传输协议
        if (StringUtil.isNotEmpty(apiData.getTransportHttp())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "transport_http", apiData.getTransportHttp());
        }
        //是否启用限流
        if (StringUtil.isNotEmpty(apiData.getToggleThrottle())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "toggle_throttle", apiData.getToggleThrottle());
        }
        if (apiData.getProductionTps() != 0) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "production_tps", String.valueOf(apiData.getProductionTps()));
        }

        return apiId;
    }

}
