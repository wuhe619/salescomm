package com.bdaim.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.api.Dto.ApiData;
import com.bdaim.api.Dto.ApiDefine;
import com.bdaim.api.dao.ApiDao;
import com.bdaim.api.dao.ApiUrlMappingDao;
import com.bdaim.api.dao.SubscriptionDao;
import com.bdaim.api.entity.ApiEntity;
import com.bdaim.api.entity.ApiProperty;
import com.bdaim.api.entity.ApiUrlMappingEntity;
import com.bdaim.api.entity.SubscriptionEntity;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.page.PageList;
import com.bdaim.common.page.Pagination;
import com.bdaim.customer.dao.AmApplicationDao;
import com.bdaim.customer.entity.AmApplicationEntity;
import com.bdaim.customer.service.CustomerAppService;
import com.bdaim.supplier.dao.SupplierDao;
import com.bdaim.util.BigDecimalUtil;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;
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
    @Resource
    private AmApplicationDao amApplicationDao;
    @Resource
    private SubscriptionDao subscriptionDao;
    @Resource
    private SupplierDao supplierDao;

    @Resource
    private CreateXML createXML;

    private static Logger logger = LoggerFactory.getLogger(ApiService.class);

    public int saveApiProperty(ApiData apiData, String id, LoginUser lu) throws Exception {
        int apiId;
        // 去除实体类String空格
        apiData = (ApiData) JavaBeanUtil.replaceBlankSpace(apiData);
        ApiDefine apiDefine = apiData.getApi_define();
        apiDefine = (ApiDefine) JavaBeanUtil.replaceBlankSpace(apiDefine);
        if (StringUtil.isEmpty(id) || "0".equals(id)) {
            String context = apiData.getContext();
            String apiVersion = context.substring(context.lastIndexOf("/") + 1);
            String contextTexplate = context.substring(0, context.lastIndexOf("/"));
            apiData.setApiVersion(apiVersion);
            apiData.setContextTemplate(contextTexplate);
            ApiEntity entity = new ApiEntity();
            entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
            entity.setContext(apiData.getContext());
            entity.setContextTexplate(apiData.getContextTemplate());
            entity.setCreateBy(lu.getUserName());
            entity.setName(apiData.getApiName());
            entity.setVersion(apiData.getApiVersion());
            entity.setProvider(lu.getUserName());
            entity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            entity.setUpdateBy(lu.getUserName());
            entity.setStatus(0);
            entity.setHttpMethod(apiDefine.getRequest_method());
            entity.setEndpointUrl(apiData.getProductionendpoints());
            apiId = (int) apiDao.saveReturnPk(entity);
            apiDao.dealCustomerInfo(String.valueOf(apiId), "status", "0");
        } else {
            ApiEntity entity = apiDao.getApi(NumberConvertUtil.parseInt(id));
            if (entity == null) {
                throw new Exception("API不存在");
            }
            entity.setContext(apiData.getContext());
            entity.setContextTexplate(apiData.getContextTemplate());
            entity.setName(apiData.getApiName());
            entity.setVersion(apiData.getApiVersion());
            entity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            entity.setUpdateBy(lu.getUserName());
            entity.setHttpMethod(apiDefine.getRequest_method());
            entity.setEndpointUrl(apiData.getProductionendpoints());
            apiId = (int) apiDao.saveReturnPk(entity);
        }

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
        if (StringUtil.isNotEmpty(apiData.getDescription())) {
            apiDao.dealCustomerInfo(String.valueOf(apiId), "descriptionw", apiDefine.getDescription());
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
//        apiDao.dealCustomerInfo(String.valueOf(apiId), "status", String.valueOf(status));
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
                int xml = createXML.createXML(apiId);
                if (xml == -1) {
                    throw new Exception("发布失败");
                }
                entity.setStatus(ApiEntity.API_RELEASE);
                break;
        }
        apiDao.update(entity);
        return 1;
    }

    public Map<String, Object> apis(PageParam page, JSONObject params) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select API_ID as apiId,API_NAME as apiName,CONTEXT as context,CREATED_BY as createdBy,status   from am_api where 1=1 ");
        if (params.containsKey("apiName")) {
            sql.append(" and API_NAME like '%" + params.getString("apiName") + "%'");
        }
        if (params.containsKey("status")) {
            if (StringUtil.isNotEmpty(params.getString("status"))) {
                sql.append(" and status =" + params.getInteger("status"));
            }
        }
        sql.append(" order by CREATED_TIME desc");
        PageList list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
        Map<String, Object> map = new HashMap<>();
        Object collect = list.getList().stream().map(m -> {
            Map dataMap = (Map) m;
            String apiId = dataMap.get("apiId").toString();
            String countSql = "select count(*) from am_subscription where API_ID=" + Integer.valueOf(apiId) + " and SUBS_CREATE_STATE='SUBSCRIBE'";
            Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
            dataMap.put("subscribeNum", count);
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
        ApiData vo = new ApiData(apiEntity);
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
                case "descriptionw":
                    vo.setDescription(property_value);
                    break;
            }
        });
        vo.setApi_define(apiDefine);

        return vo;
    }


    public int subApi(JSONObject params, String apiId, LoginUser lu) throws Exception {
        AmApplicationEntity amApplicationEntity = amApplicationDao.getByCustId(params.getString("custId"));
        if (amApplicationEntity == null) {
            logger.info("企业不存在");
            throw new Exception("企业不存在");
        }
        ApiEntity apiEntity = apiDao.getApi(Integer.valueOf(apiId));
        if (apiEntity == null) {
            logger.info("API不存在");
            throw new Exception("API不存在");
        }
        if (apiEntity.getStatus() != 2) {
            logger.info("非发布状态不可订阅");
            throw new Exception("非发布状态不可订阅");
        }
//            SubscriptionEntity subEntity = subscriptionDao.getById(apiEntity.getApiId(), amApplicationEntity.getId());
        String subSql1 = "select SUBSCRIPTION_ID as id  from am_subscription where APPLICATION_ID=" + amApplicationEntity.getId() + " and API_ID = " + apiEntity.getApiId();
//            SubscriptionEntity subEntity = jdbcTemplate.queryForObject(subSql1, SubscriptionEntity.class);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(subSql1);
        int subscriptionId;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 365 * 100);
        if (list.size() == 0) {

            String subSql = " insert into am_subscription (CREATED_BY,CREATED_TIME,API_ID,LAST_ACCESSED,SUB_STATUS,SUBS_CREATE_STATE,APPLICATION_ID,UPDATED_TIME) " +
                    "values('" + lu.getUserName() + "','" + new Timestamp(System.currentTimeMillis()) + "'," + apiEntity.getApiId() + ",'" + new Timestamp(System.currentTimeMillis()) +
                    "','UNBLOCKED','SUBSCRIBE'," + amApplicationEntity.getId() + ",'" + new Timestamp(System.currentTimeMillis()) + "')";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            PreparedStatementCreator preparedStatementCreator = con -> {
                PreparedStatement ps = con.prepareStatement(subSql, Statement.RETURN_GENERATED_KEYS);
                return ps;
            };

            jdbcTemplate.update(preparedStatementCreator, keyHolder);
            subscriptionId = keyHolder.getKey().intValue();
            logger.info("订阅API成功,客户Id:{},subscriptionId:{}", params.getString("custId"), subscriptionId);

            String sql = "REPLACE INTO am_subscription_charge(SUBSCRIPTION_ID,CHARGE_ID,EFFECTIVE_DATE,EXPIRE_DATE,START_VOLUME,TIER_VOLUME,CREATE_TIME,CREATE_BY,UPDATE_TIME,UPDATE_BY) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?)";
            jdbcTemplate.update(sql, new Object[]{subscriptionId, 1, new Timestamp(System.currentTimeMillis()), calendar.getTime(), 0, 10000, new Timestamp(System.currentTimeMillis()), lu.getUserName(), new Timestamp(System.currentTimeMillis()), lu.getUserName()});
            logger.info("初始化API定价信息成功,客户Id:{},subscriptionId:{}", params.getString("custId"), subscriptionId);
        } else {
            subscriptionId = Integer.valueOf(list.get(0).get("id").toString());
            logger.info("重新订阅API只更改订阅状态,客户Id:{},subscriptionId:{}", params.getString("custId"), subscriptionId);
            String sql = "update am_subscription  set SUBS_CREATE_STATE=? ,SUB_STATUS=?,UPDATED_BY=? ,UPDATED_TIME=? where SUBSCRIPTION_ID=? ";
            jdbcTemplate.update(sql, new Object[]{"SUBSCRIBE","UNBLOCKED", lu.getUserName(), new Timestamp(System.currentTimeMillis()), subscriptionId});
            logger.info("更改API订阅状态成功,客户Id:{},subscriptionId:{}", params.getString("custId"), subscriptionId);
            String chargeSql = "SELECT SUBSCRIPTION_ID FROM am_subscription_charge WHERE SUBSCRIPTION_ID = ? ";
            List<Map<String, Object>> chargeList = jdbcTemplate.queryForList(chargeSql, subscriptionId);
            if (chargeList == null || chargeList.size() == 0) {
                logger.info("重新订阅API补全定价信息,客户Id:{},subscriptionId:{}", params.getString("custId"), subscriptionId);
                sql = "REPLACE INTO am_subscription_charge(SUBSCRIPTION_ID,CHARGE_ID,EFFECTIVE_DATE,EXPIRE_DATE,START_VOLUME,TIER_VOLUME,CREATE_TIME,CREATE_BY,UPDATE_TIME,UPDATE_BY) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)";
                jdbcTemplate.update(sql, new Object[]{subscriptionId, 1, new Timestamp(System.currentTimeMillis()), calendar.getTime(), 0, 100000, new Timestamp(System.currentTimeMillis()), lu.getUserName(), new Timestamp(System.currentTimeMillis()), lu.getUserName()});
            }
        }
        return subscriptionId;
    }

    public int subApiUpdate(JSONObject params, String apiId, LoginUser lu) throws Exception {
        AmApplicationEntity amApplicationEntity = amApplicationDao.getByCustId(params.getString("custId"));
        if (amApplicationEntity == null) {
            throw new Exception("企业不存在");
        }
        SubscriptionEntity entity = subscriptionDao.getById(Integer.valueOf(apiId), amApplicationEntity.getId());
        entity.setSubsCreateState("UNSUBSCRIBE");
        entity.setUpdatedBy(lu.getUserName());
        entity.setUpdatedTime(new Timestamp(System.currentTimeMillis()));
        //entity.setSubStatus("BLOCKED");
        subscriptionDao.update(entity);
        return 1;
    }

    public int priceApi(JSONObject params, String apiId, LoginUser lu) throws Exception {
        AmApplicationEntity amApplicationEntity = amApplicationDao.getByCustId(params.getString("custId"));
        if (amApplicationEntity == null) {
            throw new Exception("企业不存在");
        }
        SubscriptionEntity entity = subscriptionDao.getById(Integer.valueOf(apiId), amApplicationEntity.getId());
        String sql = "update am_subscription_charge set unit_price=?,UPDATE_BY=?,UPDATE_TIME=? where SUBSCRIPTION_ID=?";
        jdbcTemplate.update(sql, new Object[]{params.getDoubleValue("price") * 10000, lu.getUserName(), new Timestamp(System.currentTimeMillis()), entity.getId()});
        return 1;
    }

    public Map<String, Object> subApiNoSubscribeList(PageParam page, String custId, String apiName) throws Exception {
        logger.info("开始获取未订阅列表");
        AmApplicationEntity amApplicationEntity = amApplicationDao.getByCustId(custId);
        if (amApplicationEntity == null) {
            throw new Exception("企业不存在");
        }
        StringBuffer sql = new StringBuffer();
        sql.append(" select sub.APPLICATION_ID ,api.API_ID as apiId,api.API_NAME as apiName,sub.SUBS_CREATE_STATE as subCreateState,sub.CREATED_TIME as createTime");
        sql.append(" from am_api api left join am_subscription sub  on  api.API_ID=sub.API_ID");
        sql.append(" where api.API_ID not in");
        sql.append(" (select API_ID from am_subscription where APPLICATION_ID = " + amApplicationEntity.getId() + " and SUBS_CREATE_STATE = 'SUBSCRIBE')");
        sql.append(" and api.status=2");
        if (StringUtil.isNotEmpty(apiName)) {
            sql.append(" and api.API_NAME  = '" + apiName + "'");
        }
        page.setSort("api.CREATED_TIME");
        page.setDir("desc");
        Page list = apiDao.sqlPageQuery(sql.toString(), page.getPageNum(), page.getPageSize());
        //PageList list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
        Object collect = list.getData().stream().map(m -> {
            Map map = (Map) m;
            map.put("realName", "");
            map.put("resourceId", "");
            map.put("subCreateState", "UNSUBSCRIBE");
            ApiProperty property = apiDao.getProperty(map.get("apiId").toString(), "rsIds");
            if (property == null) return map;
            String propertyValue = property.getPropertyValue();
            JSONArray jsonArray = JSONArray.parseArray(propertyValue);
            StringBuffer rsIds = new StringBuffer();
            StringBuffer sulist = new StringBuffer();
            jsonArray.stream().forEach(p -> {
                Map pmap = (Map) p;
                rsIds.append(pmap.get("rsId")).append(",");
                Object supplierIds = pmap.get("supplier");
                sulist.append(supplierIds).append(",");
            });
            if (rsIds.length() > 0) rsIds.deleteCharAt(rsIds.length() - 1);

            StringBuffer suppliers = new StringBuffer();
            if (sulist.length() > 0) {
                sulist.deleteCharAt(sulist.length() - 1);
                supplierDao.getSuppliers(sulist.toString()).stream().forEach(name -> {
                    suppliers.append(name).append(",");
                });
                suppliers.deleteCharAt(suppliers.length() - 1);
            }
//            if (suppliers.length() > 0) rsIds.deleteCharAt(suppliers.length() - 1);
            map.put("realName", suppliers);
            map.put("resourceId", rsIds);
            return map;
        }).collect(Collectors.toList());
        Map map = new HashMap();
        map.put("data", collect);
        map.put("total", list.getTotal());
        return map;
    }

    public Map<String, Object> subApiSubscribeList(PageParam page, String custId, String apiName) throws Exception {
//        AmApplicationEntity amApplicationEntity = amApplicationDao.getByCustId(custId);
//        if (amApplicationEntity == null) {
//            throw new Exception("企业不存在");
//        }
        StringBuffer sql = new StringBuffer();
        sql.append(" select sub.APPLICATION_ID ,api.context,sub.sub_status,api.API_ID as apiId,api.API_NAME as apiName,sub.SUBS_CREATE_STATE as subCreateState," +
                "sub.CREATED_TIME as createTime,cus.real_name as realName,cus.cust_id as custId,ch.unit_price as price");
        sql.append(" from am_api api left join am_subscription sub  on  api.API_ID=sub.API_ID");
        sql.append(" left join am_application app on  app.APPLICATION_ID = sub.APPLICATION_ID");
        sql.append(" left join t_customer cus on cus.cust_id=app.SUBSCRIBER_ID");
        sql.append(" left join am_subscription_charge ch on ch.SUBSCRIPTION_ID= sub.SUBSCRIPTION_ID");
        sql.append(" where sub.SUBS_CREATE_STATE = 'SUBSCRIBE'");
        if (StringUtil.isNotEmpty(apiName)) {
            sql.append(" and api.API_NAME = '" + apiName + "'");
        }
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" and cus.cust_id = '" + custId + "'");
        }
        page.setSort("sub.CREATED_TIME");
        page.setDir("desc");
        try {
            PageList list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
            Object collect = list.getList().stream().map(m -> {
                Map map = (Map) m;
                map.put("realName", "");
                map.put("resourceId", "");
                ApiProperty property = apiDao.getProperty(map.get("apiId").toString(), "rsIds");
                if (property == null) return map;
                String propertyValue = property.getPropertyValue();
                JSONArray jsonArray = JSONArray.parseArray(propertyValue);
                StringBuffer sulist = new StringBuffer();
                StringBuffer rsIds = new StringBuffer();
                jsonArray.stream().forEach(p -> {
                    Map pmap = (Map) p;
                    rsIds.append(pmap.get("rsId")).append(",");
                    Object supplierIds = pmap.get("supplier");
                    sulist.append(supplierIds).append(",");
                });
                if (rsIds.length() > 0) rsIds.deleteCharAt(rsIds.length() - 1);
                StringBuffer suppliers = new StringBuffer();
                if (sulist.length() > 0) {
                    sulist.deleteCharAt(sulist.length() - 1);
                    supplierDao.getSuppliers(sulist.toString()).stream().forEach(name -> {
                        suppliers.append(name).append(",");
                    });
                    suppliers.deleteCharAt(suppliers.length() - 1);
                }
                Double price = 0.0;
                if (map.get("price") != null) price = Double.valueOf(map.get("price").toString()) / 10000;
                map.put("price", price);
                map.put("realName", suppliers);
                map.put("resourceId", rsIds);
                map.put("priceType", "单一定价");
                return map;
            }).collect(Collectors.toList());
            Map map = new HashMap();
            map.put("data", collect);
            map.put("total", list.getTotal());
            return map;
        } catch (Exception e) {
            logger.info("错误信息：" + e.getMessage());
        }
        return null;
    }

    //客户调用记录
    public PageList subApiLogs(JSONObject params, PageParam page) {
        StringBuffer sql = new StringBuffer();
        //, count(api.API_ID) as countNum
        sql.append(" select api.API_ID as apiId, api.API_NAME as apiName, que.RESPONSE_MSG as body,round(log.CHARGE/10000) as charge,que.SERVICE_TIME as serviceTime");
        sql.append(" from rs_log_" + params.getString("callMonth") + " log left join am_api api  on  log.API_ID =api.API_ID");
        sql.append(" left join am_charge_" + params.getString("callMonth") + " que on que.ID=log.API_LOG_ID");
        sql.append(" where 1=1");
        if (params.containsKey("apiName")) {
            sql.append(" and api.API_NAME like '%" + params.getString("apiName") + "%'");
        }
        sql.append(" group by log.API_ID");
        PageList list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
        List list1 = new ArrayList();
        list.getList().stream().forEach(m -> {
            Map map = (Map) m;
            Object apiId = map.get("apiId");
            map.put("countNum", 0);
            if (apiId != null) {
                String sql1 = "select count(*) from  rs_log_" + params.getString("callMonth") + " where API_ID=" + Integer.valueOf(apiId.toString());
                Integer unt = jdbcTemplate.queryForObject(sql1, Integer.class);
                map.put("countNum", unt);
            }
            list1.add(map);
        });
        list.setList(list1);
        return list;
    }

    //资源调用记录
    public Map resApiLogs(JSONObject params, PageParam page) {
        Map result = new HashMap();
        result.put("list", new ArrayList<>());
        result.put("total", 0);
        StringBuffer sql = new StringBuffer();
//        sql.append(" select log.RS_ID as rsId,res.resname as resname, api.API_NAME as apiName,que.USER_NAME as userName," +
//                " que.SERVICE_TIME as serviceTime,que.RESPONSE_TIME as responseTime,round(log.CHARGE/10000) as charge,que.RESPONSE_MSG as body");
//        sql.append(" from rs_log_" + params.getString("callMonth") + " log left join t_market_resource res on log.RS_ID=res.resource_id");
//        sql.append(" left join am_api api on api.API_ID = log.API_ID ");
//        sql.append(" left join am_charge_" + params.getString("callMonth") + " que on que.ID=log.API_LOG_ID");
//        sql.append(" where log.supplier_id="+params.getLong("supplierId")+" and log.RS_ID = " + params.getLong("resourceId"));
        sql.append("select resource_id as rsId,resname from t_market_resource res where supplier_id="+params.getString("supplierId"));

        PageList list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
        if(list!=null && list.getTotal()>0){
//            List<Map<String,Object>> dataList = list.getList();
              Object collect = list.getList().stream().map(m -> {
                Map map = (Map) m;
                map.put("amount","0");
                map.put("num",0);
                String countSql = "select count(0)num from rs_log_" + params.getString("callMonth")+" where rs_id="+map.get("rsId");
                Map<String,Object> countNum = jdbcTemplate.queryForMap(countSql);
                map.put("num",countNum.get("num"));
                String sumsql= " select sum(charge) as amount from rs_log_" + params.getString("callMonth")+" where rs_id="+map.get("rsId");
                Map<String,Object> amountMap = jdbcTemplate.queryForMap(sumsql);
                if(amountMap!=null){
                    Object amount = amountMap.get("amount");
                    if(amount!=null){
                        String amountStr = BigDecimalUtil.strDiv(amount.toString(),"10000",2);
                        map.put("amount",amountStr);
                    }
                }
                return map;
            }).collect(Collectors.toList());
            result.put("list",collect);
            result.put("total",list.getTotal());
        }
        return result;
    }


}
