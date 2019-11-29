package com.bdaim.api.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.api.Dto.ApiData;
import com.bdaim.api.Dto.ApiDefine;
import com.bdaim.api.dao.ApiDao;
import com.bdaim.api.dao.ApiUrlMappingDao;
import com.bdaim.api.entity.ApiEntity;
import com.bdaim.api.entity.ApiUrlMappingEntity;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.page.PageList;
import com.bdaim.common.page.Pagination;
import com.bdaim.customer.service.CustomerAppService;
import com.bdaim.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApiService {
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ApiDao apiDao;
    @Autowired
    private ApiUrlMappingDao apiUrlMappingDao;
    @Autowired
    private CustomerAppService customerAppService;


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

    public Map<String, Object> apis(PageParam page, JSONObject params) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select API_ID as apiId,API_NAME as apiName,CONTEXT as context,CREATED_BY as createdBy from am_api where 1=1 ");
        if (StringUtil.isNotEmpty(params.getString("apiName"))) {
            sql.append(" and API_NAME like '%" + params.getString("apiName") + "%'");
        }
        if (params.getInteger("status") != null) {
            sql.append(" and API_NAME status =" + params.getInteger("status"));
        }
        sql.append(" order by CREATED_TIME desc");
        PageList list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
        Map<String, Object> map = new HashMap<>();
        Object collect = list.getList().stream().map(m -> {
            Map dataMap = (Map) m;
            dataMap.put("subscribeNum", 0);
            return dataMap;
        }).collect(Collectors.toList());
        map.put("list", collect);
        map.put("total", list.getTotal());
        return map;
    }

    public ApiData getApiById(int apiId) throws Exception {
        ApiEntity apiEntity = apiDao.getApi(apiId);
        if (apiEntity == null) {
            throw new Exception("api:" + apiId + "不存在");
        }
        String sql = " select  property_name,property_value from am_api_property where api_id = ?";
        List<Map<String, Object>> propertyList = jdbcTemplate.queryForList(sql, apiEntity.getApiId());
        ApiData vo = new ApiData();
        ApiDefine apiDefine = new ApiDefine();
        propertyList.stream().forEach(map -> {
            String property_value = customerAppService.ObjectFormStr(map.get("property_value"));
            switch (map.get("property_name").toString()) {
                case "visibility":
                    vo.setVisibility(property_value);
                    break;
                case "api_thumb":
                    vo.setApiThumb(property_value);
                    break;
                case "tages":
                    vo.setTags(property_value);
                    break;
                case "endpoint_type":
                    vo.setEndpointType(property_value);
                    break;
                case "productionendpoints":
                    vo.setProductionendpoints(property_value);
                    break;
                case "default_version":
                    vo.setDefaultVersion(property_value);
                    break;
                case "tier":
                    vo.setTier(property_value);
                    break;
                case "transport_http":
                    vo.setTransportHttp(property_value);
                    break;
                case "toggle_throttle":
                    vo.setToggleThrottle(property_value);
                    break;
                case "production_tps":
                    vo.setProductionTps(Integer.valueOf(property_value));
                    break;
                case "response_cache":
                    vo.setResponseCache(property_value);
                    break;
                case "resource_url_pattern":
                    apiDefine.setResource_url_pattern(property_value);
                    break;
                case "request_method":
                    apiDefine.setRequest_method(property_value);
                    break;
                case "description":
                    apiDefine.setDescription(property_value);
                    break;
                case "params":
                    apiDefine.setParams(property_value);
                    break;
                case "rsIds":
                    vo.setRsIds(property_value);
                    break;
            }
        });
        vo.setApiDefine(apiDefine);

        return vo;
    }

}
