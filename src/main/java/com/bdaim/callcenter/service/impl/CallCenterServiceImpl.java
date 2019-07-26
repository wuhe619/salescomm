package com.bdaim.callcenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.bdaim.callcenter.dto.SeatsInfo;
import com.bdaim.callcenter.dto.UnicomSendSmsParam;
import com.bdaim.callcenter.service.CallCenterService;
import com.bdaim.common.util.HttpUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.price.dto.ResourcesPriceDto;
import com.bdaim.resource.service.impl.MarketResourceServiceImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/10
 * @description
 */
@Service("callCenterService")
@Transactional
public class CallCenterServiceImpl implements CallCenterService {
    private static Log LOG = LogFactory.getLog(CallCenterServiceImpl.class);
    @Resource
    private SeatsServiceImpl seatsService;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private MarketResourceServiceImpl marketResourceServiceImpl;
    private final static String UNICOM_BASE_URL = "http://120.52.23.243:10080/jzyxpt/";


    public static void main(String[] args) {
        //System.out.println(DigestUtils.md5Hex("13042719930903173X").toUpperCase());
        System.out.println(new CallCenterServiceImpl().unicomExtensionDelete("LTBD2018090701", "11111111111"));
        //System.out.println(new CallCenterServiceImpl().unicomDeleteMainNum("5b5badd5ee20615d:LTBD2018090701:BJHKK_zx1", "1", "", "LTBD2018090701", "CC48qcueeWmuGzLP9G9o8", "111111"));
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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


    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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


}
