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
        }
        ApiDefine apiDefine = apiData.getApiDefine();
        if (apiData.getUrlMappingId() == 0) {
            ApiUrlMappingEntity entity = new ApiUrlMappingEntity();

            entity.setApiId(apiId);
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
        //最大限制数
        if (apiData.getProductionTps() != 0) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "production_tps", String.valueOf(apiData.getProductionTps()));
        }
        //是否启用缓存
        if (StringUtil.isNotEmpty(apiData.getResponseCache())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "response_cache", apiData.getResponseCache());
        }
        //url
        if (StringUtil.isNotEmpty(apiDefine.getResource_url_pattern())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "resource_url_pattern", apiDefine.getResource_url_pattern());
        }
        //请求方式
        if (StringUtil.isNotEmpty(apiDefine.getRequest_method())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "request_method", apiDefine.getRequest_method());
        }
        //描述，说明
        if (StringUtil.isNotEmpty(apiDefine.getDescription())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "description", apiDefine.getDescription());
        }
        if (StringUtil.isNotEmpty(apiDefine.getParams())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "params", apiDefine.getParams());
        }
        if (StringUtil.isNotEmpty(apiData.getRsIds())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "rsIds", apiData.getRsIds());
        }
        return apiId;
    }

    public int updateStatusApiById(String apiId, LoginUser lu, int status) throws Exception {

        ApiEntity entity = apiDao.getApi(Integer.valueOf(apiId));
        if (entity == null) {
            throw new Exception("api不存在");
        }
        entity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        entity.setUpdateBy(lu.getUserName());
        switch (status) {
            case 0:
                entity.setStatus(ApiEntity.API_FOUND);
                break;
            case 1:
                entity.setStatus(ApiEntity.API_OFFLINE);
                break;
            case 2:
                entity.setStatus(ApiEntity.API_RELEASE);
                break;
        }
        return (int) apiDao.saveReturnPk(entity);
    }

    public String apis() {


        return null;
    }

}
