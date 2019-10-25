package com.bdaim.open.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.callcenter.service.impl.CallCenterService;
import com.bdaim.callcenter.service.impl.SeatsService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.service.PhoneService;
import com.bdaim.customer.dto.ApparentNumberDTO;
import com.bdaim.customer.dto.ApparentNumberQueryParam;
import com.bdaim.customer.dto.CustomerPropertyDTO;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.resource.dto.CallBackParam;
import com.bdaim.resource.dto.MarketResourceLogDTO;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.smscenter.service.SendSmsService;
import com.bdaim.template.dto.MarketTemplateDTO;
import com.bdaim.template.entity.MarketTemplate;
import com.bdaim.util.*;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author chengning@salescomm.net
 * @date 2019/4/2
 * @description
 */
@RestController
public class OpenApiAction extends BasicAction {

    private static Logger LOG = Logger.getLogger(SendSmsService.class);

    @Resource
    private SendSmsService smsService;
    @Resource
    private MarketResourceService marketResourceService;
    @Resource
    private CustomerService customerService;
    @Resource
    private CallCenterService callCenterService;
    @Resource
    private CustomGroupService customGroupService;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private PhoneService phoneService;
    @Resource
    private SeatsService seatsService;

    /**
     * 发送短信
     *
     * @param templateId
     * @param userIds
     * @param groupId
     * @return
     */
    @RequestMapping(value = "/sms/realtime", method = RequestMethod.POST)
    public Object sendSms(String templateId, String userIds, String groupId) {
        Map<String, Object> result = new HashMap<>();
        // 检查权限
        LoginUser u = opUser();
        if (u == null || StringUtil.isEmpty(u.getCustId())) {
            result.put("errorDesc", "04");
            return result;
        }

        LOG.info("sendSms.templateid="+templateId);
        LOG.info("sendSms.userIds="+userIds);
        LOG.info("sendSms.groupId="+groupId);
        // 检查参数
        if (StringUtil.isEmpty(templateId) || StringUtil.isEmpty(userIds) ||
                StringUtil.isEmpty(groupId)) {
            result.put("errorDesc", "02");
            return result;
        }
        // 查询短信模板是否存在
        MarketTemplateDTO template = smsService.getTemplate(templateId, 1, u.getCustId());
        if (template == null) {
            LOG.warn("custId:" + u.getCustId() + ",templateId:" + templateId + "不存在");
            result.put("errorDesc", "06");
            return result;
        }
        // 查询短信模板审核状态
        if (template.getStatus() != null && template.getStatus() != 2) {
            LOG.warn("custId:" + u.getCustId() + ",templateId:" + templateId + "未审核通过");
            result.put("errorDesc", "07");
            return result;
        }

        //检查数据权限
        boolean checkCgStatus = customGroupService.checkCGroupDataPermission(NumberConvertUtil.parseInt(groupId), u.getCustId());
        Set<String> superIds = new HashSet<>(Arrays.asList(userIds.split(",")));
        boolean checkCgListStatus = customGroupService.checkCGroupListDataPermission(NumberConvertUtil.parseInt(groupId), superIds);
        if (!checkCgStatus || !checkCgListStatus) {
            result.put("errorDesc", "04");
            return result;
        }

        String batchNumber = smsService.sendSmsToQueue(u.getCustId(), groupId, Arrays.asList(userIds.split(",")), templateId, String.valueOf(u.getId()), null);
        result.put("errorDesc", "00");
        result.put("batchId", batchNumber);
        return result;
    }


    /**
     * 查询短信发送记录
     *
     * @param batchId
     * @param userId
     * @return
     */
    @RequestMapping(value = "/sms/response", method = RequestMethod.GET)
    public Object getSendSmsLog(String batchId, String userId) {
        Map<String, Object> result = new HashMap<>();
        // 检查权限
        LoginUser u = opUser();
        if (u == null || StringUtil.isEmpty(u.getCustId())) {
            result.put("errorDesc", "04");
            return result;
        }
        // 检查参数
        if (StringUtil.isEmpty(batchId) || StringUtil.isEmpty(userId)) {
            result.put("errorDesc", "02");
            return result;
        }
        Map<String, Object> data = smsService.getSendSmsLog(u.getCustId(), batchId, userId);
        if (data == null) {
            result.put("errorDesc", "01");
            return result;
        }
        result.put("errorDesc", "00");
        // 处理发送结果
        if ("1001".equals(String.valueOf(data.get("status")))) {
            result.put("code", "0");
        } else {
            result.put("code", "1");
        }
        result.put("createTime", data.get("createTime"));
        result.put("activeTime", data.get("activeTime"));
        return result;
    }


    /**
     * 添加短信模板
     *
     * @param data
     * @param title
     * @return
     */
    @RequestMapping(value = "/sms/template", method = RequestMethod.POST)
    public Object getTemplate(String data, String title, String signatures,String resourceId) {
        Map<String, Object> result = new HashMap<>();
        // 检查权限
        LoginUser u = opUser();
        if (u == null || StringUtil.isEmpty(u.getCustId())) {
            result.put("errorDesc", "04");
            return result;
        }
//        LOG.info("getTemplate data="+data==null?""+data+";title="+title==null?"":title+";signatures="+signatures==null?"":signatures+";resourceId="+resourceId==null?"":resourceId);
        // 检查参数
        if (StringUtil.isEmpty(data) || StringUtil.isEmpty(title) || StringUtil.isEmpty(signatures) || StringUtil.isEmpty(resourceId)) {
            result.put("errorDesc", "02");
            return result;
        }
        CustomerProperty property = new CustomerProperty();
        property.setCustId(u.getCustId());
        property.setPropertyName("sms_config");
        CustomerPropertyDTO sms_config = customerService.getCustomerProperty(property);
        if(sms_config==null || StringUtil.isEmpty(sms_config.getPropertyValue())){
            result.put("errorDesc", "06");
            return result;
        }
        String sms_config_v = sms_config.getPropertyValue();
        JSONArray arr = JSON.parseArray(sms_config_v);
        boolean resoureIdIsValid = false;
        if(arr.size()>0){
            for(int i=0;i<arr.size();i++){
                JSONObject json = arr.getJSONObject(i);
                String resource_Id = json.getString("resourceId");
                if(StringUtil.isNotEmpty(resource_Id) && resourceId.equals(resource_Id)){
                    resoureIdIsValid = true;
                }
            }
        }
        if(!resoureIdIsValid){
            LOG.error("resourceId "+resourceId+" 未配置");
            result.put("errorDesc", "06");
            return result;
        }

        MarketTemplate m = new MarketTemplate();
        m.setCustId(u.getCustId());
        m.setOperator(String.valueOf(u.getId()));
        m.setTypeCode(1);
        m.setMouldContent(data);
        m.setResourceId(resourceId);
        m.setTitle(title);
        m.setSmsSignatures(signatures);
        int templateId = marketResourceService.saveMarketTemplate(m);
        if (templateId == 0) {
            result.put("errorDesc", "05");
            return result;
        }
        result.put("errorDesc", "00");
        result.put("templateId", templateId);
        return result;
    }

    /**
     * 查询短信模板
     *
     * @param templateId
     * @return
     */
    @RequestMapping(value = "/sms/template", method = RequestMethod.GET)
    public Object createTemplate(String templateId) {
        Map<String, Object> result = new HashMap<>();
        // 检查权限
        LoginUser u = opUser();
        if (u == null || StringUtil.isEmpty(u.getCustId())) {
            result.put("errorDesc", "04");
            return result;
        }
        // 检查参数
        if (StringUtil.isEmpty(templateId)) {
            result.put("errorDesc", "02");
            return result;
        }
        MarketTemplateDTO data = smsService.getTemplate(templateId, 1, u.getCustId());
        if (data == null) {
            result.put("errorDesc", "01");
            return result;
        }
        result.put("errorDesc", "00");
        result.put("data", data.getMouldContent());
        result.put("status", data.getStatus());
        return result;
    }

    /**
     * 查询单条话单
     *
     * @param callId
     * @return
     */
    @RequestMapping(value = "/call/response", method = RequestMethod.GET)
    public Object getCallLog(String callId) {
        Map<String, Object> result = new HashMap<>();
        // 检查权限
        LoginUser u = opUser();
        if (u == null || StringUtil.isEmpty(u.getCustId())) {
            result.put("errorDesc", "04");
            return result;
        }
        // 检查参数
        if (StringUtil.isEmpty(callId)) {
            result.put("errorDesc", "02");
            return result;
        }
        LocalDateTime nowTime = LocalDateTime.now();
        String startTime = nowTime.minusMonths(1).format(DatetimeUtils.DATE_TIME_FORMATTER);
        String endTime = nowTime.format(DatetimeUtils.DATE_TIME_FORMATTER);
        // 查询本月通话记录
        Map<String, Object> data = marketResourceService.getRecordVoiceLogByTouchId(callId, u.getCustId(), nowTime, startTime, endTime);
        if (data == null) {
            // 查询上月通话记录
            data = marketResourceService.getRecordVoiceLogByTouchId(callId, u.getCustId(), nowTime.minusMonths(1), startTime, endTime);
        }
        if (data == null) {
            result.put("errorDesc", "01");
            return result;
        }
        result.put("errorDesc", "00");
        // 处理接听结果
        if ("1001".equals(String.valueOf(data.get("status")))) {
            result.put("code", "0");
        } else {
            result.put("code", "1");
        }
        result.put("time", data.get("called_duration"));
        String call_data = (String) data.getOrDefault("call_data", "");
        if (StringUtil.isNotEmpty(call_data)) {
            JSONObject json = JSON.parseObject(call_data);
            String callerDuration = json.getString("callerDuration");
            String calledDuration = json.getString("calledDuration");
            String calledStartTime = json.getString("calledStartTime");
            String calledEndTime = json.getString("calledEndTime");
            String callerStartTime = json.getString("callerStartTime");
            String callerEndTime = json.getString("callerEndTime");
            String caller = json.getString("caller");
            result.put("callerDuration", callerDuration);
            result.put("calledDuration", calledDuration);
            result.put("calledStartTime", calledStartTime);
            result.put("calledEndTime", calledEndTime);
            result.put("callerStartTime", callerStartTime);
            result.put("callerEndTime", callerEndTime);
            result.put("caller", caller);
        }
        return result;
    }

    /**
     * 查询录音文件
     *
     * @param callId
     * @return
     */
    @RequestMapping(value = "/call/audio", method = RequestMethod.GET)
    public Object getCallAudio(String callId) {
        Map<String, Object> result = new HashMap<>();
        // 检查权限
        LoginUser u = opUser();
        if (u == null || StringUtil.isEmpty(u.getCustId())) {
            result.put("errorDesc", "04");
            return result;
        }
        // 检查参数
        if (StringUtil.isEmpty(callId)) {
            result.put("errorDesc", "02");
            return result;
        }
        LocalDateTime nowTime = LocalDateTime.now();
        String startTime = nowTime.minusMonths(1).format(DatetimeUtils.DATE_TIME_FORMATTER);
        String endTime = nowTime.format(DatetimeUtils.DATE_TIME_FORMATTER);
        // 查询本月通话记录
        Map<String, Object> data = marketResourceService.getRecordVoiceLogByTouchId(callId, u.getCustId(), nowTime, startTime, endTime);
        if (data == null) {
            LOG.warn("callId:" + callId + ",查询上个月的通话记录:" + nowTime.minusMonths(1));
            // 查询上月通话记录
            data = marketResourceService.getRecordVoiceLogByTouchId(callId, u.getCustId(), nowTime.minusMonths(1), startTime, endTime);
        }
        if (data == null) {
            LOG.warn("callId:" + callId + ",未查询到通话记录");
            result.put("errorDesc", "01");
            return result;
        }
        LOG.info("callId:" + callId + ",查询到通话记录:" + data);
        String recordUrl = String.valueOf(data.get("recordurl"));
        if (StringUtil.isEmpty(recordUrl)) {
            LOG.warn("callId:" + callId + ",未查询到通话录音地址");
            result.put("errorDesc", "01");
            return result;
        }
        String base64 = marketResourceService.getVoiceBase64ByTouchId(String.valueOf(data.get("user_id")), recordUrl);
        if (StringUtil.isEmpty(base64)) {
            LOG.warn("callId:" + callId + ",录音地址转换后的base64为空:" + base64);
            result.put("errorDesc", "01");
            return result;
        }

        result.put("errorDesc", "00");
        result.put("data", base64);
        return result;
    }


    /**
     * 双呼
     *
     * @param phone           主叫号
     * @param userId          被叫id
     * @param customerGroupId 客群id
     * @param apparentNumber 外显号
     * @return
     */
    @RequestMapping(value = "/call/2way", method = RequestMethod.POST)
    public JSONObject call2way(String phone, String userId, String customerGroupId, String apparentNumber) {
        LOG.info("phone:" + (phone == null ? "" : phone) + ";userId:" + (userId == null ? "" : userId) +
                ";customerGroupId:" + (customerGroupId == null ? "" : customerGroupId) +";apparentNumber:"+apparentNumber);
        JSONObject result = new JSONObject();
        if (StringUtil.isEmpty(phone) || StringUtil.isEmpty(userId) || StringUtil.isEmpty(customerGroupId) || StringUtil.isEmpty(apparentNumber)) {
            result.put("errorDesc", "02");
            return result;
        }

        String custId = opUser().getCustId();
        CustomGroup group = customGroupService.getCustomGroupById(Integer.valueOf(customerGroupId));
        if (group == null || !custId.equals(group.getCustId())) {
            result.put("errorDesc", "04");
            return result;
        }
        String sql = " select * from t_customer_group_list_" + customerGroupId + " where id='" + userId + "'";
        List<Map<String, Object>> d = jdbcTemplate.queryForList(sql);
        if (d == null || d.isEmpty()) {
            LOG.error("客群 " + customerGroupId + " 无" + userId);
            result.put("errorDesc", "04");
            return result;
        }

        /*try {
            JSONObject jsonObject = supplierService.getCustomerCallPriceConfig(custId);
            LOG.info("客户配置的渠道:" + jsonObject);
            List<MarketResourceDTO> call2way = (List<MarketResourceDTO>) jsonObject.get("call2way");
            for (MarketResourceDTO dto : call2way) {
                apparentNumber = customerService.getCustomerApparentNumber(custId, "", String.valueOf(dto.getResourceId()));
                if (StringUtil.isNotEmpty(apparentNumber)) {
                    break;
                }
            }
        } catch (Exception e) {
            LOG.error("获取用户配置的呼叫渠道ID异常,", e);
        }*/

        // 查询企业是否设置双向呼叫外显号码
        /*if (StringUtil.isEmpty(apparentNumber)) {
            apparentNumber = customerService.getCustomerApparentNumber(custId, "");
            if (StringUtil.isEmpty(apparentNumber)) {
                // 穿透查询一次之前配置的外显号
                apparentNumber = marketResourceService.selectCustCallBackApparentNumber(custId);
            }
        }*/
        ApparentNumberQueryParam m = new ApparentNumberQueryParam();
        m.setCustId(custId);
        m.setStatus(1);
        List<ApparentNumberDTO> numberList = customerService.listApparentNumber(m);
        if (numberList.size() == 0) {
            result.put("errorDesc", "07");
            LOG.error("未申请外显号码");
            return result;
        }
        boolean apparentNumberIsValid = false;
        for(ApparentNumberDTO dto:numberList){
            if(dto.getApparentNumber().equals(apparentNumber)){
                apparentNumberIsValid = true;
                break;
            }
        }
        if(!apparentNumberIsValid){
            result.put("errorDesc", "07");
            LOG.error("未申请外显号码或外显号无效");
            return result;
        }
        // 判断是余额是否充足
        boolean juddeg = marketResourceService.judRemainAmount(custId);
        if (juddeg == false) {
            result.put("errorDesc", "06");
            LOG.error("余额不足");
            return result;
        }

        String tranOrderId = opUser().getId() + Long.toString(IDHelper.getTouchId());
        boolean success = false;
        JSONObject jsonObject = null;
        try {
            CallBackParam callBackParams = new CallBackParam();
            // 主叫号码
            callBackParams.setSrc(phone);
            String bphone;
            if (StringUtil.isNotEmpty(userId)) {
                bphone = phoneService.getPhoneBySuperId(userId);
            } else {
                LOG.warn("被叫为空,superId:" + userId);
                result.put("errorDesc", "08");
                return result;
            }

            callBackParams.setDst(bphone);
            callBackParams.setSrcclid(apparentNumber);
            callBackParams.setDstclid(apparentNumber);
            callBackParams.setCustomParm(tranOrderId + "_" + customerGroupId);

            String callBackResult = callCenterService.handleCallBack0(callBackParams, opUser().getCustId(), String.valueOf(opUser().getId()));
            // 更新通话次数
            marketResourceService.updateCallCountV2(customerGroupId, userId);
            LogUtil.info("调用api双向回呼接口返回:" + callBackResult);
            success = false;
            jsonObject = null;
            if (StringUtil.isNotEmpty(callBackResult)) {
                jsonObject = JSON.parseObject(callBackResult);
                // 发送双向呼叫请求成功
                if (StringUtil.isNotEmpty(jsonObject.getString("statusCode")) && "0".equals(jsonObject.getString("statusCode"))) {
                    success = true;
                    // 如果失败则把错误信息返回
                } else {
                    LOG.error(jsonObject.getString("statusMsg"));
                }
            }
        } catch (RestClientException e) {
            LOG.error("调用api双向回呼接口失败", e);
        }
        MarketResourceLogDTO marketResourceLogDTO = new MarketResourceLogDTO();
        // 执行成功
        if (success) {
            // 唯一请求ID
            String requestId = jsonObject.getString("requestId");
            // 主叫成功
            marketResourceLogDTO.setCallSid(requestId);
            marketResourceLogDTO.setStatus(1001);
//            marketResourceService.insertLogV3(marketResourceLogDTO);
            result.put("errorDesc", "00");
            result.put("callId", tranOrderId);
        } else {
            // 异常返回输出错误码和错误信息
            LOG.error("请求发送双向呼叫失败,返回数据:" + jsonObject);
            // 主叫失败
            marketResourceLogDTO.setStatus(1002);
            result.put("errorDesc", "09");
        }
        // 插入外呼日志表
        marketResourceLogDTO.setTouch_id(tranOrderId);
        marketResourceLogDTO.setType_code("1");
        marketResourceLogDTO.setResname("voice");
        marketResourceLogDTO.setUser_id(opUser().getId());
        marketResourceLogDTO.setCust_id(custId);
        marketResourceLogDTO.setSuperId(userId);
        marketResourceLogDTO.setRemark("");
        marketResourceLogDTO.setMarketTaskId(customerGroupId);
        if (StringUtil.isNotEmpty(customerGroupId)) {
            marketResourceLogDTO.setCustomerGroupId(Integer.parseInt(customerGroupId));
        }
        // 判断是否管理员进行的外呼
        if ("1".equals(opUser().getUserType())) {
            marketResourceLogDTO.setCallOwner(2);
        } else {
            marketResourceLogDTO.setCallOwner(1);
        }
        marketResourceService.insertLogV3(marketResourceLogDTO);
        return result;
    }

}
