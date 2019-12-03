package com.bdaim.online.unicom.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.util.StringUtil;
import com.bdaim.util.UnicomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author chengning@salescomm.net
 * @date 2019-11-25 10:03
 */
@Service
@Transactional
public class UnicomService {

    private final static Logger LOG = LoggerFactory.getLogger(UnicomService.class);

    @Autowired
    private MarketResourceDao marketResourceDao;
    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private CustomerUserDao customerUserDao;

    /**
     * 根据客户ID查询配置的联通外呼资源
     *
     * @param custId
     * @return
     */
    private JSONObject getConfig(String custId) {
        CustomerProperty customerProperty = customerDao.getProperty(custId, "call_config");
        if (customerProperty == null || StringUtil.isEmpty(customerProperty.getPropertyValue())) {
            LOG.warn("客户:{}联通外呼渠道配置为空", custId);
            return null;
        }
        JSONArray configs = JSON.parseArray(customerProperty.getPropertyValue());
        if (configs == null || configs.size() == 0) {
            LOG.warn("客户:{}联通外呼渠道配置为空", custId);
            return null;
        }
        // 查找客户关联的联通外呼资源配置
        JSONObject jsonObject = (JSONObject) configs.stream().filter(s -> ((JSONObject) s).getString("type").equals("4")).findFirst().get();
        int resourceId = jsonObject.getIntValue("resourceId");
        MarketResourceEntity mr = marketResourceDao.getMarketResource(resourceId);
        if (mr == null) {
            LOG.warn("custId:" + custId + ",呼叫线路资源为空,resourceId:" + resourceId);
            return null;
        }
        // 资源无效
        if (2 == mr.getStatus()) {
            LOG.warn("custId:" + custId + ",呼叫线路资源状态无效,resourceId:" + resourceId);
            return null;
        }
        ResourcePropertyEntity callConfig = marketResourceDao.getProperty(String.valueOf(resourceId), "price_config");
        if (callConfig == null) {
            LOG.warn("custId:" + custId + ",未查询到资源,resourceId:" + resourceId);
            return null;
        }
        if (StringUtil.isEmpty(callConfig.getPropertyValue())) {
            LOG.warn("custId:" + custId + ",呼叫线路资源配置为空,resourceId:" + resourceId);
            return null;
        }
        JSONObject property = JSON.parseObject(callConfig.getPropertyValue());
        return property;
    }

    /**
     * 通过联通接口添加主叫号码
     *
     * @param custId
     * @param extensionNumber
     * @return
     * @throws Exception
     */
    public JSONObject addUserExtension(String custId, String extensionNumber) throws Exception {
        JSONObject result = new JSONObject();
        result.put("code", -1);
        //获取token,加密获取sign
        JSONObject config = getConfig(custId);
        if (config == null || config.size() == 0) {
            LOG.warn("custId:" + custId + ",联通呼叫线路资源配置为空");
            result.put("msg", "custId:" + custId + ",联通呼叫线路资源配置为空");
            return result;
        }
        String pwd = config.getString("entPassword");
        String entId = config.getString("entId");
        String key = config.getString("entKey");
        JSONObject jsonObject = UnicomUtil.registerUserExtension(pwd, entId, key, extensionNumber);
        // 成功
        if (jsonObject != null && "08000".equals(jsonObject.getString("code"))) {
            result.put("code", 1);
            result.put("msg", jsonObject.getString("msg"));
            return result;
        }
        result.put("msg", jsonObject.getString("msg"));
        return result;
    }

    /**
     * 通过联通接口删除主叫号码
     *
     * @param custId
     * @param extensionNumber
     * @return
     * @throws Exception
     */
    public JSONObject deleteUserExtension(String custId, String extensionNumber) throws Exception {
        JSONObject result = new JSONObject();
        result.put("code", -1);
        //获取token,加密获取sign
        JSONObject config = getConfig(custId);
        if (config == null || config.size() == 0) {
            LOG.warn("custId:" + custId + ",联通呼叫线路资源配置为空");
            result.put("msg", "custId:" + custId + ",联通呼叫线路资源配置为空");
            return result;
        }
        String pwd = config.getString("entPassword");
        String entId = config.getString("entId");
        String key = config.getString("entKey");
        JSONObject jsonObject = UnicomUtil.failureUserExtension(pwd, entId, key, extensionNumber);
        // 成功
        if (jsonObject != null && "08000".equals(jsonObject.getString("code"))) {
            result.put("code", 1);
            result.put("msg", jsonObject.getString("msg"));
            return result;
        }
        result.put("msg", jsonObject.getString("msg"));
        return result;
    }

    /**
     * 联通坐席外呼接口
     * @param custId
     * @param userId
     * @param dataId
     * @param showNumber
     * @return
     */
    public JSONObject unicomSeatMakeCall(String custId, long userId, String dataId, String showNumber) {
        // 查询客户配置的联通呼叫参数
        JSONObject result = new JSONObject();
        result.put("code", -1);
        //获取token,加密获取sign
        JSONObject config = getConfig(custId);
        if (config == null || config.size() == 0) {
            LOG.warn("custId:" + custId + ",联通呼叫线路资源配置为空");
            result.put("msg", "custId:" + custId + ",联通呼叫线路资源配置为空");
            return result;
        }
        // 查询用户主叫号码 work_num work_num_status
        CustomerUserPropertyDO work_num_status = customerUserDao.getProperty(String.valueOf(userId), "work_num_status");
        if (!"1".equals(work_num_status)) {
            LOG.warn("custId:{},userId:{}主叫号码审核状态异常", custId, userId);
            result.put("msg", "用户主叫号码审核状态异常");
            return result;
        }
        CustomerUserPropertyDO work_num = customerUserDao.getProperty(String.valueOf(userId), "work_num");
        if (work_num == null || StringUtil.isEmpty(work_num.getPropertyValue())) {
            LOG.warn("custId:{},userId:{}主叫号码未配置", custId, userId);
            result.put("msg", "用户主叫号码未配置");
            return result;
        }

        String pwd = config.getString("entPassword");
        String entId = config.getString("entId");
        String key = config.getString("entKey");
        JSONObject jsonObject = UnicomUtil.unicomSeatMakeCall(entId, dataId, pwd, work_num.getPropertyValue(), showNumber, key);
        return jsonObject;
    }
}
