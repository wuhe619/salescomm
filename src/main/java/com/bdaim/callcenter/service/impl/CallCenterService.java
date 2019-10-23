package com.bdaim.callcenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.callcenter.dto.SeatCallCenterConfig;
import com.bdaim.callcenter.dto.SeatsInfo;
import com.bdaim.callcenter.dto.UnicomSendSmsParam;
import com.bdaim.common.util.*;
import com.bdaim.common.util.http.HttpUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dto.CustomerPropertyEnum;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.price.dto.ResourcesPriceDto;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.dto.CallBackParam;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.resource.service.MarketResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.transaction.Transactional;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/10
 * @description
 */
@Service("callCenterService")
@Transactional
public class CallCenterService {
    private static Logger LOG = LoggerFactory.getLogger(CallCenterService.class);
    @Resource
    private SeatsService seatsService;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private MarketResourceService marketResourceServiceImpl;
    private final static String UNICOM_BASE_URL = "http://120.52.23.243:10080/jzyxpt/";
    private final static String UNICOM_BASE_URL_V1 = "http://120.52.23.243:10080/sdyxinterface/20190426/";


    public static void main(String[] args) {
        //System.out.println(DigestUtils.md5Hex("13042719930903173X").toUpperCase());
        System.out.println(new CallCenterService().unicomExtensionDelete("LTBD2018090701", "11111111111"));
        //System.out.println(new CallCenterService().unicomDeleteMainNum("5b5badd5ee20615d:LTBD2018090701:BJHKK_zx1", "1", "", "LTBD2018090701", "CC48qcueeWmuGzLP9G9o8", "111111"));
    }


    public Map<String, Object> getCallCenterConfigData(String customerId, String userId) {
        Map<String, String> enterpriseMessage = seatsService.getEnterpriseMessage(customerId, "cuc");
        if (enterpriseMessage != null) {
            // 根据userId查询坐席信息
            Map<String, Object> result = new HashMap<>();
            SeatsInfo seatsInfo = seatsService.getCallProperty(userId, "cuc_seat");
            if (seatsInfo != null) {
                // 把呼叫中心企业ID和坐席ID 密码放在一起返回
                result.put("userId", userId);
                result.put("account", seatsInfo.getSeatId());
                result.put("seatsPassword", seatsInfo.getSeatPassword());
                result.put("callCenterId", enterpriseMessage.get("cuc_call_id"));
                result.put("apparentNumber", enterpriseMessage.get("cuc_apparent_number"));
                return result;
            }
        }
        return null;
    }


    public Map<String, Object> getCallCenterConfigDataV1(String customerId, String userId, String resourceId) {
        ResourcesPriceDto resourcesPriceDto = null;
        try {
            resourcesPriceDto = customerDao.getCustResourceMessageById(resourceId, customerId);
        } catch (Exception e) {
            LOG.error("获取企业配置参数异常:", e);
            throw new RuntimeException("获取企业配置参数异常", e);
        }
        // 根据userId查询坐席信息
        Map<String, Object> result = new HashMap<>();

        SeatsInfo seatsInfo = seatsService.getCallProperty(userId, "cuc_seat");
        if (seatsInfo != null) {
            // 把呼叫中心企业ID和坐席ID 密码放在一起返回
            result.put("userId", userId);
            result.put("account", seatsInfo.getSeatId());
            result.put("mainNumber", seatsInfo.getMainNumber());
            result.put("seatsPassword", seatsInfo.getSeatPassword());
        }
        if (resourcesPriceDto != null) {
            result.put("callCenterId", resourcesPriceDto.getCallCenterId());
            result.put("apparentNumber", resourcesPriceDto.getApparentNumber());
        }
        return result;
    }


    public Map<String, Object> getXzConfigData(String custId, String userId, String resourceId) {
        ResourcesPriceDto resourcesPriceDto = null;
        try {
            resourcesPriceDto = customerDao.getCustResourceMessageById(resourceId, custId);
        } catch (Exception e) {
            LOG.error("获取企业配置参数异常:", e);
            throw new RuntimeException("获取企业配置参数异常", e);
        }
        // 根据userId查询坐席信息
        Map<String, Object> result = new HashMap<>();

        SeatsInfo seatsInfo = seatsService.getCallProperty(userId, "xz_seat");
        if (seatsInfo != null) {
            // 把呼叫中心企业ID和坐席ID 密码放在一起返回
            result.put("userId", userId);
            result.put("account", seatsInfo.getSeatId());
            result.put("mainNumber", seatsInfo.getMainNumber());
            result.put("seatsPassword", seatsInfo.getSeatPassword());
        }
        if (resourcesPriceDto != null) {
            result.put("apparentNumber", resourcesPriceDto.getApparentNumber());
        }
        return result;
    }


    public Map<String, Object> unicomSeatLogin(String entId, String userId, String userPwd, String tel, int type) {
        Map<String, String> params = new HashMap<>();
        if (StringUtil.isEmpty(entId)) {
            //params.put("entId", UNICOM_ENT_ID);
        } else {
            params.put("entId", entId);
        }
        params.put("userId", userId);
        params.put("tel", tel);
        params.put("type", String.valueOf(type));
        params.put("agentPassword", userPwd);
        String result;
        try {
            LOG.info("联通登录接口参数:" + params.toString());
            result = HttpUtil.httpPost(UNICOM_BASE_URL + "callout/seatLogin", params, null);
            LOG.info("联通坐席登录返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通坐席登录失败:", e);
            throw new RuntimeException("联通坐席登录失败", e);
        }
        return null;
    }


    public Map<String, Object> unicomSeatLogout(String entId, String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("entId", entId);
        params.put("userId", userId);
        String result;
        try {
            result = HttpUtil.httpPost(UNICOM_BASE_URL + "callout/seatLogout", params, null);
            LOG.info("联通坐席登出返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通坐席登出失败:", e);
            throw new RuntimeException("联通坐席登出失败", e);
        }
        return null;
    }


    public Map<String, Object> unicomAgentReset(String entId, String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("entId", entId);
        params.put("userId", userId);
        String result;
        try {
            result = HttpUtil.httpPost(UNICOM_BASE_URL + "callout/agentReset", params, null);
            LOG.info("联通坐席重置返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通坐席重置失败:", e);
            throw new RuntimeException("联通坐席重置失败", e);
        }
        return null;
    }


    public Map<String, Object> unicomSeatMakeCallEx(String entId, String userId, String activityId, String provideId, String customerId, String showNumber) {
        Map<String, String> params = new HashMap<>();
        if (StringUtil.isEmpty(entId)) {
            //params.put("entId", UNICOM_ENT_ID);
        } else {
            params.put("entId", entId);
        }
        params.put("userId", userId);
        params.put("activityId", activityId);
        params.put("provideId", provideId);
        params.put("custormerId", customerId);

        params.put("showNumber", showNumber);

        String result;
        try {
            LOG.info("联通坐席外呼参数:" + params.toString());
            result = HttpUtil.httpPost(UNICOM_BASE_URL + "callout/seatMakeCallEx", params, null);
            LOG.info("联通坐席外呼返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通坐席外呼失败:", e);
            throw new RuntimeException("联通坐席外呼失败", e);
        }
        return null;
    }


    public Map<String, Object> unicomGetSeatStatus(String entId, String userId) {
        Map<String, String> params = new HashMap<>();
        if (StringUtil.isEmpty(entId)) {
            //params.put("entId", UNICOM_ENT_ID);
        } else {
            params.put("entId", entId);
        }
        params.put("userId", userId);
        String result;
        try {
            result = HttpUtil.httpPost(UNICOM_BASE_URL + "callout/getSeatStatus", params, null);
            LOG.info("联通获取坐席状态返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通坐席获取状态失败:", e);
            throw new RuntimeException("联通坐席获取状态失败", e);
        }
        return null;
    }


    public Map<String, Object> unicomSeatHangUp(String entId, String userId, String uuid) {
        Map<String, String> params = new HashMap<>();
        params.put("entId", entId);
        params.put("userId", userId);
        params.put("uuid", uuid);
        String result;
        try {
            result = HttpUtil.httpPost(UNICOM_BASE_URL + "callout/hangUp", params, null);
            LOG.info("联通坐席挂断返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通坐席挂断失败:", e);
            throw new RuntimeException("联通坐席挂断失败", e);
        }
        return null;
    }


    public Map<String, Object> unicomGetCallData(String uuid, String callStatus, String callReply, String entId, String activityId, String entPassword) {
        Map<String, String> params = new HashMap<>();
        params.put("uuid", uuid);
        params.put("callStatus", callStatus);
        params.put("callReply", callReply);
        params.put("entId", entId);
        params.put("activityId", activityId);
        params.put("entPassword", entPassword);
        String result;
        try {
            result = HttpUtil.httpPost(UNICOM_BASE_URL + "callout/getCallData", params, null);
            LOG.info("联通坐席获取通话记录返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                JSONArray jsonArray = JSON.parseArray(result);
                if (jsonArray.size() == 2) {
                    Map<String, Object> data;
                    data = jsonArray.getJSONObject(1);
                    data.put("code", jsonArray.getJSONObject(0).getString("code"));
                    data.put("msg", jsonArray.getJSONObject(0).getString("msg"));
                    return data;
                }
            }
        } catch (Exception e) {
            LOG.error("联通坐席获取通话记录失败:", e);
            throw new RuntimeException("联通坐席获取通话记录失败", e);
        }
        return null;
    }


    public Map<String, Object> unicomRecordByRequestId(String uuid, String entId) {
        Map<String, String> params = new HashMap<>();
        params.put("uuid", uuid);
        params.put("entId", entId);
        String result;
        try {
            result = HttpUtil.httpPost(UNICOM_BASE_URL + "callout/getRecordByRequestId", params, null);
            LOG.info("联通坐席获取录音文件返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通坐席获取录音文件失败:", e);
            throw new RuntimeException("联通坐席获取录音文件失败", e);
        }
        return null;
    }


    public Map<String, Object> unicomAddSeatAccount(String entId, String userId, String userName, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("entId", entId);
        params.put("userId", userId);
        params.put("userName", userName);
        params.put("password", password);
        String result;
        try {
            result = HttpUtil.httpPost(UNICOM_BASE_URL + "callout/extensionAccount", params, null);
            LOG.info("联通坐席创建返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通坐席创建失败:", e);
            throw new RuntimeException("联通坐席创建失败", e);
        }
        return null;
    }


    public Map<String, Object> unicomUpdateSeatPasswd(String entId, String userId, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("entId", entId);
        params.put("userId", userId);
        params.put("passwd", password);
        String result;
        try {
            result = HttpUtil.httpPost(UNICOM_BASE_URL + "callout/updateSeatPasswd", params, null);
            LOG.info("联通坐席修改密码返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通坐席修改密码失败:", e);
            throw new RuntimeException("联通坐席修改密码失败", e);
        }
        return null;
    }


    public Map<String, Object> unicomExtensionRegister(String entId, String tel, int type) {
        Map<String, String> params = new HashMap<>();
        params.put("entId", entId);
        params.put("tel", tel);
        params.put("type", String.valueOf(type));
        String result;
        try {
            result = HttpUtil.httpPost(UNICOM_BASE_URL + "callout/extensionRegister", params, null);
            LOG.info("联通坐席注册分机返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通坐席注册分机失败:", e);
            throw new RuntimeException("联通坐席注册分机失败", e);
        }
        return null;
    }


    public Map<String, Object> unicomSendMessageData(UnicomSendSmsParam unicomSendSmsParam) {
        Map<String, String> params = new HashMap<>();
        params.put("entId", unicomSendSmsParam.getEntId());
        params.put("activityId", unicomSendSmsParam.getActivityId());
        params.put("custormerId", unicomSendSmsParam.getCustomerId());
        params.put("provideId", unicomSendSmsParam.getProvideId());
        params.put("messageCode", unicomSendSmsParam.getMessageCode());
        params.put("variableOne", unicomSendSmsParam.getVariableOne());
        params.put("variableTwo", unicomSendSmsParam.getVariableThree());
        params.put("variableThree", unicomSendSmsParam.getVariableThree());
        params.put("variableFour", unicomSendSmsParam.getVariableFour());
        params.put("variableFive", unicomSendSmsParam.getVariableFive());
        String result;
        try {
            result = HttpUtil.httpPost(UNICOM_BASE_URL + "callout/sendMessageData", params, null);
            LOG.info("联通发送短信返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通发送短信失败:", e);
            throw new RuntimeException("联通发送短信失败", e);
        }
        return null;
    }


    public Map<String, Object> unicomExtensionDelete(String entId, String extension) {
        Map<String, String> params = new HashMap<>();
        params.put("entId", entId);
        params.put("extension", extension);
        String result;
        try {
            result = HttpUtil.httpPost(UNICOM_BASE_URL + "callout/extensiondelete", params, null);
            LOG.info("通坐席删除分机返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通坐席删除分机失败:", e);
            throw new RuntimeException("联通坐席删除分机失败", e);
        }
        return null;
    }

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private MarketResourceDao marketResourceDao;

    public String handleCallBack(Map<String, Object> params) {
        MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<>();
        // 请求api的双向回呼接口
        urlVariables.add("interfaceID", "SaleBackCallService");
        for (Map.Entry<String, Object> param : params.entrySet()) {
            urlVariables.add(param.getKey(), param.getValue());
        }
        LogUtil.info("调用ds-api双向回呼接口前构造数据:" + params.toString());
        String result = restTemplate.postForObject(Constant.LABEL_API
                + "/sales/rest.do", urlVariables, String.class);

        return result;
    }

    public String handleCallBack(CallBackParam param, String custId) {
        param.setAction(SaleApiUtil.DAIL_BACK_CALL_ACTION);
        CustomerProperty cp = customerDao.getProperty(custId, CustomerPropertyEnum.CALL_BACK_APP_ID.getKey());
        if (cp != null && StringUtil.isNotEmpty(cp.getPropertyValue())) {
            param.setAppid(cp.getPropertyValue());
        } else {
            param.setAppid(SaleApiUtil.ONLINE_CALL_BACK_APP_ID);
        }
        LogUtil.info("调用双向回呼接口请求数据:" + JSON.toJSONString(param));
        String result = SaleApiUtil.sendCallBack(JSON.toJSONString(param), SaleApiUtil.ENV);
        LogUtil.info("调用双向回呼接口返回数据:" + result);
        return result;
    }

    public String handleCallBack0(CallBackParam param, String custId, String userId) {
        param.setAction(SaleApiUtil.DAIL_BACK_CALL_ACTION);
        // 处理应用ID
        String appId = null;
        String resourceId = null;
        try {
            resourceId = seatsService.checkSeatConfigStatus(userId, custId);
            if (StringUtil.isNotEmpty(resourceId)) {
                MarketResourceEntity mr = marketResourceDao.getMarketResource(NumberConvertUtil.parseInt(resourceId));
                if (mr == null) {
                    LOG.warn("userId:" + userId + ",custId:" + custId + ",呼叫线路资源为空,resourceId:" + resourceId);
                }
                // 资源无效
                if (2 == mr.getStatus()) {
                    LOG.warn("userId:" + userId + ",custId:" + custId + ",呼叫线路资源状态无效,resourceId:" + resourceId);
                }
                ResourcePropertyEntity callConfig = marketResourceDao.getProperty(resourceId, "price_config");
                if (callConfig == null) {
                    LOG.warn("userId:" + userId + ",custId:" + custId + ",未查询到资源,resourceId:" + resourceId);
                }
                if (StringUtil.isEmpty(callConfig.getPropertyValue())) {
                    LOG.warn("userId:" + userId + ",custId:" + custId + ",呼叫线路资源配置为空,resourceId:" + resourceId);
                }
                JSONObject property = JSON.parseObject(callConfig.getPropertyValue());
                if (property != null) {
                    LOG.info("userId:" + userId + ",custId:" + custId + ",呼叫线路资源配置信息:" + property);
                    SeatCallCenterConfig seatConfig = JSON.parseObject(property.getString("call_center_config"), SeatCallCenterConfig.class);
                    if (seatConfig != null) {
                        appId = seatConfig.getCallCenterAppIp();
                    }
                } else {
                    LOG.warn("userId:" + userId + ",custId:" + custId + ",呼叫线路资源转换为空,resourceId:" + resourceId);
                }
            }
        } catch (Exception e) {
            LOG.error("坐席:" + userId + ",获取客户选择资源的双呼应用ID失败,resourceId:" + resourceId, e);
        }
        CustomerProperty cp = customerDao.getProperty(custId, CustomerPropertyEnum.CALL_BACK_APP_ID.getKey());
        if (StringUtil.isNotEmpty(appId)) {
            param.setAppid(appId);
        } /*else if (cp != null && StringUtil.isNotEmpty(cp.getPropertyValue())) {
            param.setAppid(cp.getPropertyValue());
        } */ else {
            param.setAppid(SaleApiUtil.ONLINE_CALL_BACK_APP_ID);
        }
        LOG.info("调用双向回呼接口请求数据:" + JSON.toJSONString(param));
        String result = SaleApiUtil.sendCallBack(JSON.toJSONString(param), SaleApiUtil.ENV);
        LOG.info("调用双向回呼接口返回数据:" + result);
        return result;
    }

    /**
     * 联通坐席外呼接口v1
     * data_id 联通返回唯一id    callerNumber 主叫号码   showNumber   外显号（不传默认从企业外显号码池随机一个外显号码 key加密私钥
     */
    public Map<String, Object> unicomSeatMakeCallExV1(String entId, String customerId, String entPassWord, String callerNumber, String showNumber, String key) {
        Map<String, String> params = new HashMap<>();
        params.put("dataId", customerId);
        params.put("callerNumber", callerNumber);
        params.put("showNumber", showNumber);
        String result;
        try {
            Map<String, Object> headers = new HashMap<>(16);
            //获取请求token
            if (StringUtil.isEmpty(entPassWord)) {
                entPassWord = "111111";
            }
            String token = unicomGetToken(entPassWord, entId);
            //签名处理
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String sysTime = df.format(new Date());
            String parmToken = token + sysTime;
            LOG.info("加密数据是:" + parmToken);
            String sgin = encryptThreeDESECB(URLEncoder.encode(parmToken, "UTF-8"), key);
            LOG.info("加密结果是:" + sgin);
            headers.put("Sig", sgin);
            headers.put("Content-Type", "application/json;charset=utf-8");
            LOG.info("联通外呼接口请求地址是：" + UNICOM_BASE_URL_V1 + "call/makeCall/" + entId + " 联通坐席外呼参数:" + params.toString());
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "call/makeCall/" + entId, params, null);
            LOG.info("联通坐席外呼返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通坐席外呼失败:", e);
            throw new RuntimeException("联通坐席外呼失败", e);
        }
        return null;
    }

    /**
     * 联通短信接口v1
     *
     * @desc:联通短信触达 wordId 话术码  variableOne  变量标识1  variableTwo 变量标识2 variableThree 变量标识3 variableFour 变量标识4 dataList 发送短信加密手机号集合 key加密私钥
     */
    public Map<String, Object> unicomSeatMakeSmsV1(UnicomSendSmsParam unicomSendSmsParam) {
        String entId = unicomSendSmsParam.getEntId();
        String key = unicomSendSmsParam.getKey();
        LOG.info("联通短信触达企业id是:" + entId + "密钥是：" + key);
        Map<String, String> params = new HashMap<>();
        params.put("wordId", unicomSendSmsParam.getWordId());
        params.put("dataId", unicomSendSmsParam.getCustomerId());
        params.put("variableOne", unicomSendSmsParam.getVariableOne());
        params.put("variableTwo", unicomSendSmsParam.getVariableThree());
        params.put("variableThree", unicomSendSmsParam.getVariableThree());
        params.put("variableFour", unicomSendSmsParam.getVariableFour());
        params.put("variableFive", unicomSendSmsParam.getVariableFive());
        String result;
        try {
            Map<String, Object> headers = new HashMap<>(16);
            //获取请求token
            String entPassWord = "";
            if (StringUtil.isEmpty(unicomSendSmsParam.getEntPassWord())) {
                entPassWord = "111111";
            }
            String token = unicomGetToken(entPassWord, entId);
            //签名处理
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String sysTime = df.format(new Date());
            String parmToken = token + sysTime;
            LOG.info("加密数据是:" + parmToken);
            String sgin = encryptThreeDESECB(URLEncoder.encode(parmToken, "UTF-8"), key);
            LOG.info("加密结果是:" + sgin);
            headers.put("Sig", sgin);
            headers.put("Content-Type", "application/json;charset=utf-8");
            LOG.info("联通短信触达接口请求地址是：" + UNICOM_BASE_URL_V1 + "msg/sendMsg/" + entId + " 联通短信触达参数:" + params.toString());
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "msg/sendMsg/" + entId, params, null);
            LOG.info("联通短信触达结果返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通短信触达失败:", e);
            throw new RuntimeException("联通短信触达异常", e);
        }
        return null;
    }


    /** V1
     * 设置中间号接口
     * dataId 数据id  callerNumber 主叫号码   showNumber   外显号（不传默认从企业外显号码池随机一个外显号码 key加密私钥
     */
    public Map<String, Object> unicomSetMiddleNumV1(String entId, String customerId, String entPassWord, String callerNumber, String showNumber, String key) {
        Map<String, String> params = new HashMap<>();
        params.put("dataId", customerId);
        params.put("callerNumber", callerNumber);
        params.put("showNumber", showNumber);
        String result;
        try {
            Map<String, Object> headers = new HashMap<>(16);
            //获取请求token
            if (StringUtil.isEmpty(entPassWord)) {
                entPassWord = "111111";
            }
            String token = unicomGetToken(entPassWord, entId);
            //签名处理
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String sysTime = df.format(new Date());
            String parmToken = token + sysTime;
            LOG.info("加密数据是:" + parmToken);
            String sgin = encryptThreeDESECB(URLEncoder.encode(parmToken, "UTF-8"), key);
            LOG.info("加密结果是:" + sgin);
            headers.put("Sig", sgin);
            headers.put("Content-Type", "application/json;charset=utf-8");
            LOG.info("联通设置中间号接口请求地址是：" + UNICOM_BASE_URL_V1 + "singleCall/ambSetNumber/" + entId + " 设置中间号参数:" + params.toString());
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "singleCall/ambSetNumber/" + entId, params, null);
            LOG.info("联通设置中间号返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通设置中间号失败:", e);
            throw new RuntimeException("联通设置中间号异常", e);
        }
        return null;
    }

    /**
     * 获取企业登录token
     *
     * @param entPassword
     * @param entId
     * @return
     */
    public String unicomGetToken(String entPassword, String entId) {
        Map<String, String> params = new HashMap<>();
        params.put("entPassword", entPassword);
        String result;
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("Content-Type", "application/json;charset=utf-8");
            LOG.info("联通获取token请求地址是：" + UNICOM_BASE_URL_V1 + "login/getToken/" + entId + "联通获取token参数:" + params.toString());
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "login/getToken/" + entId, params, headers);
            //result = "{ \"code\": \"000\", \"msg\": \"success\", \"data\": { \"token\": \"eyJ0eXAiOiJKV1\" } }";
            LOG.info("联通获取token联通返回结果:" + result);
            if (StringUtil.isNotEmpty(result)) {
                JSONObject jsonObject = JSON.parseObject(result);
                String code = jsonObject.getString("code");
                if ("000".equals(code)) {
                    String token = jsonObject.getJSONObject("data").getString("token");
                    LOG.info("联通获取token:" + token);
                    return token;
                } else {
                    LOG.warn("联通获取token失败:");
                    return "";
                }
            }
        } catch (Exception e) {
            LOG.error("联通获取token参数失败:", e);
            throw new RuntimeException("联通获取token失败", e);
        }
        return null;
    }

    /**
     * token加密流程
     *
     * @param src
     * @param key
     * @return
     * @throws Exception
     */
    public static String encryptThreeDESECB(final String src, final String key) throws Exception {

        final DESedeKeySpec dks = new DESedeKeySpec(key.getBytes("UTF-8"));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        final SecretKey securekey = keyFactory.generateSecret(dks);

        final Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, securekey);
        final byte[] b = cipher.doFinal(src.getBytes());

        final BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(b).replaceAll("\r", "").replaceAll("\n", "");

    }

    /**
     * token解密流程
     *
     * @param
     * @param key
     * @return
     * @throws Exception
     */
    public static String decryptThreeDESECB(final String token, final String key) throws Exception {
        final BASE64Decoder decoder = new BASE64Decoder();
        final byte[] bytesrc = decoder.decodeBuffer(token);
        final DESedeKeySpec dks = new DESedeKeySpec(key.getBytes("UTF-8"));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        final SecretKey securekey = keyFactory.generateSecret(dks);
        final Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, securekey);
        final byte[] retByte = cipher.doFinal(bytesrc);
        return new String(retByte);
    }
    /*public static void main(String[] args) throws Exception {
        String key = "wuhwfoihfiuwbfqegiefuwfe";
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String sysTime =df.format(new Date());
        // 加密流程
        String token="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUz";
        String parmToken=token+sysTime;
        String sgin = encryptThreeDESECB(URLEncoder.encode(parmToken, "UTF-8"), key);
        System.out.println(sgin);

        // 解密流程
        String tele_decrypt = decryptThreeDESECB(sgin, key);
        System.out.println("模拟代码解密:" + tele_decrypt);

    }*/

}
