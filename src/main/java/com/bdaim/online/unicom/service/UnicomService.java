package com.bdaim.online.unicom.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.bill.dto.CallBackInfoParam;
import com.bdaim.bill.dto.SeatCallDeductionResult;
import com.bdaim.bill.service.TransactionService;
import com.bdaim.callcenter.dto.CallTypeParamEnum;
import com.bdaim.callcenter.dto.VoiceLogCallDataDTO;
import com.bdaim.customer.account.dao.TransactionDao;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


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
    @Autowired
    private TransactionDao transactionDao;
    @Autowired
    private TransactionService transactionService;

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
     * @param userId
     * @param account
     * @param status  1-冻结 0-开启
     * @return
     */
    /*public JSONObject saveUpdateUserExtensionByUserId(String userId, String account, int status) {
        JSONObject result = new JSONObject();
        result.put("code", -1);
        CustomerUser user = null;
        if (StringUtil.isNotEmpty(userId)) {
            user = customerUserDao.get(NumberConvertUtil.parseLong(userId));
        } else if (StringUtil.isNotEmpty(account)) {
            user = customerUserDao.getCustomerUserByLoginName(account);
        }
        JSONObject jsonObject = null;
        if (user != null) {
            //当账号有效时处理
            CustomerUserPropertyDO callType = customerUserDao.getProperty(user.getId().toString(), "call_type");
            CustomerUserPropertyDO work_num = customerUserDao.getProperty(user.getId().toString(), "work_num");
            // 联通双呼添加主叫号码
            if (callType != null && work_num != null && CallTypeParamEnum.UNICOM_CALL2_WAY.getPropertyName().equals(callType.getPropertyValue())) {
                try {
                    if (1 == status) {
                        // 冻结
                        jsonObject = deleteUserExtension(user.getCust_id(), work_num.getPropertyValue());
                    } else if (0 == status) {
                        // 开启
                        jsonObject = addUserExtension(user.getCust_id(), work_num.getPropertyValue());
                    }
                } catch (Exception e) {
                    LOG.error("添加/删除联通主叫号码异常", e);
                }
            }
        }
        return jsonObject;
    }*/


    /**
     * 通过联通接口添加主叫号码
     *
     * @param custId
     * @param extensionNumber
     * @return
     */
   /* public JSONObject addUserExtension(String custId, String extensionNumber) {
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
        JSONObject jsonObject = null;
        try {
            jsonObject = UnicomUtil.registerUserExtension(pwd, entId, key, extensionNumber);
        } catch (Exception e) {
            LOG.error("联通接口添加主叫号码异常", e);
        }
        // 成功
        *//*if (jsonObject != null && "08000".equals(jsonObject.getString("code"))) {
            result.put("code", 1);
            result.put("msg", jsonObject.getString("msg"));
            return jsonObject;
        }*//*
        return jsonObject;
    }*/

    /**
     * 通过联通接口删除主叫号码
     *
     * @param custId
     * @param extensionNumber
     * @return
     * @throws Exception
     */
    /*public JSONObject deleteUserExtension(String custId, String extensionNumber) {
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
        JSONObject jsonObject = null;
        try {
            jsonObject = UnicomUtil.failureUserExtension(pwd, entId, key, extensionNumber);
        } catch (Exception e) {
            LOG.error("联通接口删除主叫号码异常", e);
        }
        // 成功
        *//*if (jsonObject != null && "08000".equals(jsonObject.getString("code"))) {
            result.put("code", 1);
            result.put("msg", jsonObject.getString("msg"));
            return result;
        }
        result.put("msg", jsonObject.getString("msg"));*//*
        return jsonObject;
    }*/

    /**
     * 联通坐席外呼接口
     *
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
        if (work_num_status == null || !"1".equals(work_num_status.getPropertyValue())) {
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

        String pwd = config.getJSONObject("call_center_config").getString("entPassword");
        String entId = config.getJSONObject("call_center_config").getString("entId");
        String key = config.getJSONObject("call_center_config").getString("entKey");
        JSONObject jsonObject = UnicomUtil.unicomSeatMakeCall(entId, dataId, pwd, work_num.getPropertyValue(), showNumber, key);
        return jsonObject;
    }

    /**
     * 更新联通通话记录,同时进行扣费
     *
     * @param param
     * @return
     */
    public int updateCallRecord(CallBackInfoParam param) {
        String yyyy_mm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String monthTableName = ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + yyyy_mm;
        String updateVoiceLogSql = "UPDATE " + monthTableName + " SET auto_id = ?, feetime = ?, pay_mode = ?, appId = ?, call_data = ?," +
                "recordurl = ? ,called_duration = ? ,amount=? ,summ_minute=?, prod_amount=?, resource_id=? WHERE callSid = ?";
        String selectVoiceLogSql = "SELECT touch_id, cust_id, user_id, create_time, superid, callSid, customer_group_id, call_data, market_task_id, customer_sea_id, recordurl, called_duration FROM " + monthTableName + " WHERE callSid = ?";
        String updateTouchSql = "UPDATE " + monthTableName + " set status =? WHERE callSid =?";
        int flag = 0;
        BigDecimal bigDecimal;
        String userId, custId, recordurl, called_duration;
        //0：通话未完成  1：通话完成
        String type = param.getType().trim();
        //通话时长
        String callDuration = param.getCallDuration().trim();
        //通话唯一标识  对应数据库的callSid
        String callSid = param.getUuid().trim();
        LOG.info("查询呼叫记录SQL:{}", selectVoiceLogSql);
        List<Map<String, Object>> touchMaps = marketResourceDao.sqlQuery(selectVoiceLogSql, callSid);
        if (touchMaps.size() > 0) {
            String callData = String.valueOf(touchMaps.get(0).get("call_data"));
            recordurl = String.valueOf(touchMaps.get(0).get("recordurl"));
            called_duration = String.valueOf(touchMaps.get(0).get("called_duration"));
            // 如果话单已经更新过则跳过本条话单
            if (StringUtil.isNotEmpty(callData) && StringUtil.isNotEmpty(recordurl) && StringUtil.isNotEmpty(called_duration)) {
                LOG.warn("callSid:" + callSid + ",已处理,跳过");
                return 0;
            }
            userId = String.valueOf(touchMaps.get(0).get("user_id"));
            custId = String.valueOf(touchMaps.get(0).get("cust_id"));
            JSONObject config = getConfig(custId);
            String resourceId = config.getString("resourceId");
            if (StringUtil.isEmpty(resourceId)) {
                resourceId = "0";
            }
            SeatCallDeductionResult seatCallDeductionResult = new SeatCallDeductionResult();
            if (StringUtil.isNotEmpty(type) && StringUtil.isNotEmpty(callDuration) && Integer.parseInt(type) == 1 && Integer.parseInt(callDuration) > 0) {
                // 更新通话日志表的通话状态为成功
                marketResourceDao.executeUpdateSQL(updateTouchSql, 1001, callSid);
                bigDecimal = new BigDecimal((double) NumberConvertUtil.parseInt(callDuration) / 60);
                int callDurationTime = bigDecimal.setScale(0, RoundingMode.CEILING).intValue();
                try {
                    seatCallDeductionResult = transactionService.seatCallDeduction0(userId, custId, resourceId, 0, callDurationTime);
                } catch (Exception e) {
                    LOG.error("客户:" + custId + ",资源:" + resourceId + "扣费失败:", e);
                }
            }
            //处理时间保存回调记录信息
            String startTime = param.getStartTime().trim();
            String endTime = param.getEndTime().trim();
            if (StringUtil.isNotEmpty(startTime)) {
                startTime = startTime.replaceAll("-", "/");
            }
            if (StringUtil.isNotEmpty(endTime)) {
                endTime = endTime.replaceAll("-", "/");
            }

            VoiceLogCallDataDTO voiceLogCallDataDTO = new VoiceLogCallDataDTO(param.getLocalUrl(), startTime, endTime, callDuration,
                    param.getRemoteUrl(), startTime, endTime, callDuration, callDuration, recordurl, "");
            //auto_id = ?, feetime = ?, pay_mode = ?, amount = ?, appId = ?, call_data = ?,recordurl = ? WHERE callSid = ?
            flag += marketResourceDao.executeUpdateSQL(updateVoiceLogSql, callSid, callDuration, 1, param.getEntId(),
                    JSON.toJSONString(voiceLogCallDataDTO), recordurl, callDuration, seatCallDeductionResult.getCustAmount(), seatCallDeductionResult.getSummMinute(),
                    seatCallDeductionResult.getProdAmount(), resourceId, callSid);
            LOG.info("联通callSid:{}通话记录保存状态:{}", callSid, flag);
        } else {
            LOG.info("未查询到该条记录回调数据是:" + param.toString());
        }
        return flag;
    }


    /**
     * 保存联通录音文件
     *
     * @param param
     * @return
     * @throws Exception
     */
    public int saveCallRecordFile(JSONObject param) {
        String callSid = param.getString("uuid");
        int code = 1;
        String yyyy_mm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String monthTableName = ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + yyyy_mm;
        String queryTouchSql = "SELECT touch_id touchId ,cust_id custId,user_id userId,superid superId,call_data FROM " + monthTableName + " WHERE callSid=?";
        //根据callSid 查询是否存在通过话记录
        List<Map<String, Object>> logList = marketResourceDao.sqlQuery(queryTouchSql, callSid);
        if (logList.size() > 0) {
            //获取录音文件地址
            String recordUrl = param.getString("recordUrl");
            //保存录音文件
            String voiceFilePath = FileUtil.savePhoneRecordFileReturnPath(recordUrl, String.valueOf(logList.get(0).get("userId")));
            if (StringUtil.isNotEmpty(voiceFilePath)) {
                LOG.info("开始进行录音文件转换:" + voiceFilePath);
                try {
                    // 文件转换
                    FileUtil.wavToMp3(voiceFilePath, voiceFilePath.replaceAll(".wav", ".mp3"));
                } catch (Exception e) {
                    LOG.error("录音文件转换失败:", e);
                }
            }
            String call_data = String.valueOf(logList.get(0).get("call_data"));
            if (StringUtil.isEmpty(call_data)) {
                call_data = "{}";
            }
            JSONObject callData = JSON.parseObject(call_data);
            callData.put("recordUrl", recordUrl);
            //更新通话记录录音地址
            String updateUrlSql = "UPDATE " + monthTableName + " SET recordurl = ? ,call_data = ? WHERE callSid = ?";
            recordUrl = recordUrl.replaceAll("wav", "mp3");
            int i = marketResourceDao.executeUpdateSQL(updateUrlSql, recordUrl, callData.toJSONString(), callSid);
            LOG.info("更新通话记录表callId:{},状态:{}", callSid, i);
            if (i > 0) {
                code = 0;
            }
        } else {
            LOG.info("未查询到通话记录,uuid:{}", callSid);
        }
        return code;
    }
}
