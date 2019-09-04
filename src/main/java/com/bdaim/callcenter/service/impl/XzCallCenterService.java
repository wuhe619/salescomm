package com.bdaim.callcenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.callcenter.dto.*;
import com.bdaim.callcenter.util.XzCallCenterUtil;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customersea.dao.CustomerSeaDao;
import com.bdaim.customersea.entity.CustomerSea;
import com.bdaim.customersea.entity.CustomerSeaProperty;
import com.bdaim.markettask.dao.MarketTaskDao;
import com.bdaim.markettask.entity.MarketTask;
import com.bdaim.markettask.entity.MarketTaskProperty;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

/**
 * @author chengning@salescomm.net
 * @date 2019/4/29
 * @description
 */
@Service("xzCallCenterService")
@Transactional
public class XzCallCenterService {

    public static final Logger LOG = LoggerFactory.getLogger(XzCallCenterService.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String NOTICE_URL = "";

    @Resource
    private MarketTaskDao marketTaskDao;

    @Resource
    private CustomerSeaDao customerSeaDao;

    @Resource
    private MarketResourceDao marketResourceDao;


    /**
     * 创建讯众自动外呼任务
     *
     * @param callCenterId
     * @param name
     * @param apparentNumber
     * @param callSpeed
     * @param callCount
     * @param startTime
     * @param endTime
     * @param pullPhoneUrl   取号接口
     * @return
     */
    public JSONObject addXzAutoTask(String callCenterId, String name, String apparentNumber, int callSpeed, int callCount, long startTime, long endTime, String pullPhoneUrl) {
        JSONObject xzTaskConfig = null;
        // 添加讯众自动外呼任务
        LocalDateTime taskStartTime = LocalDateTime.ofEpochSecond(startTime, 0, ZoneOffset.ofHours(8));
        LocalDateTime taskEndTime = LocalDateTime.ofEpochSecond(endTime, 0, ZoneOffset.ofHours(8));
        int maxConcurrentNumber = queryMaxCallOutNumber(callCenterId);
        LOG.info("讯众企业ID:" + callCenterId + ",最大并发数:" + maxConcurrentNumber);
        Map<String, Object> result = addAutoTask(name, taskStartTime, taskEndTime, apparentNumber,
                callSpeed, callCount, callCenterId, maxConcurrentNumber);
        if (result != null && "0".equals(String.valueOf(result.get("code")))) {
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(result));
            xzTaskConfig = JSON.parseObject(jsonObject.getString("data"));
            // 设置讯众自动外呼第三方取号地址
            addPhoneUrl(callCenterId, "admin", xzTaskConfig.getString("taskidentity"), pullPhoneUrl);
        } else {
            xzTaskConfig = new JSONObject();
        }
        return xzTaskConfig;
    }


    /**
     * @param taskName            任务名称
     * @param expireStartTime     有效期开始时间
     * @param expireEndTime       有效期结束时间
     * @param showNum             外显号
     * @param callSpeed           呼叫速度,范围1-9
     * @param countType           外呼次数 1-3
     * @param callCenterId        企业ID
     * @param maxConcurrentNumber 外呼并发数 最大并发数。最大并发数，值范围为1-企业最大并发数
     * @return
     */
    public Map<String, Object> addAutoTask(String taskName, LocalDateTime expireStartTime, LocalDateTime expireEndTime, String showNum,
                                           int callSpeed, int countType, String callCenterId, int maxConcurrentNumber) {
        XzAddAutoTask map = new XzAddAutoTask();
        map.setTaskname(taskName);
        map.setExpirdatebegin(DATE_TIME_FORMATTER.format(expireStartTime));
        map.setExpirdateend(DATE_TIME_FORMATTER.format(expireEndTime));
        map.setShownum(showNum);
        map.setCalloutspeed(String.valueOf(callSpeed));
        map.setCounttype(String.valueOf(countType));
        map.setCompid(callCenterId);
        map.setNoticeurl(NOTICE_URL);

        map.setDailmodel("3");
        map.setCallinterval("30");
        map.setRingingduration("60");
        map.setTimeruleid("");
        map.setWaitvoiceid("");
        map.setSeatallocationmodel("1");
        map.setMaxconcurrentnumber(String.valueOf(maxConcurrentNumber));
        map.setIntelligence_num("");
        map.setCreator("admin");
        LOG.info("添加讯众自动外呼任务请求参数:" + map);
        Map<String, Object> result = null;
        try {
            result = XzCallCenterUtil.addAutoTask(map);
        } catch (Exception e) {
            LOG.error("添加讯众自动外呼任务异常", e);
            try {
                result = XzCallCenterUtil.addAutoTask(map);
            } catch (Exception e1) {
                LOG.error("重试添加讯众自动外呼任务异常", e);
            }
        }
        LOG.info("添加讯众自动外呼任务结果:" + result);
        return result;
    }

    /**
     * @param taskId        任务ID
     * @param expireEndTime 有效期结束时间
     * @param showNum       外显号
     * @param callSpeed     呼叫速度,范围1-9
     * @param countType     外呼次数 1-3
     * @param callCenterId  企业ID
     * @return
     */
    public Map<String, Object> editAutoTask(String taskId, LocalDateTime expireEndTime, String showNum,
                                            int callSpeed, int countType, String callCenterId) {
        Map<String, Object> map = null;
        XzEditAutoTask param = new XzEditAutoTask();
        try {
            map = XzCallCenterUtil.queryAutoTask(callCenterId, taskId);
        } catch (Exception e) {
            LOG.error("查询讯众自动外呼异常,", e);
            try {
                map = XzCallCenterUtil.queryAutoTask(callCenterId, taskId);
            } catch (Exception e1) {
                LOG.error("重试查询讯众自动外呼异常,", e);
            }
        }
        if (map != null && "0".equals(String.valueOf(map.get("code")))) {
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(map));
            param = JSON.parseObject(jsonObject.getString("data"), XzEditAutoTask.class);
            param.setExpirdateend(DATE_TIME_FORMATTER.format(expireEndTime));
            param.setShownum(showNum);
            param.setCalloutspeed(String.valueOf(callSpeed));
            param.setCounttype(String.valueOf(countType));
        }
        param.setCompid(callCenterId);
        param.setModifier("admin");
        LOG.info("修改讯众自动外呼任务请求参数:" + param);
        Map<String, Object> result = null;
        try {
            result = XzCallCenterUtil.editAutoTask(param);
        } catch (Exception e) {
            LOG.error("修改讯众自动外呼任务异常", e);
            try {
                result = XzCallCenterUtil.editAutoTask(param);
            } catch (Exception e1) {
                LOG.error("重试修改讯众自动外呼任务异常", e);
            }
        }
        LOG.info("修改讯众自动外呼任务结果:" + result);
        return result;
    }

    /**
     * 讯众自动外呼任务添加成员
     *
     * @param taskIdentity
     * @param members
     * @param callCenterId
     * @return
     */
    public Map<String, Object> addTaskMembers(String taskIdentity, Set<String> members, String callCenterId) {
        XzAddTaskMember param = new XzAddTaskMember();
        param.setTaskidentity(taskIdentity);
        param.setMembers(StringUtils.join(members, ","));
        param.setCompid(callCenterId);
        param.setCreator("admin");
        LOG.info("讯众自动外呼任务添加成员请求参数:" + param);
        Map<String, Object> result = null;
        try {
            result = XzCallCenterUtil.addTaskMembers(param);
        } catch (Exception e) {
            LOG.error("讯众自动外呼任务添加成员异常,", e);
        }
        LOG.info("讯众自动外呼任务添加成员返回结果:" + result);
        return result;
    }

    /**
     * 讯众自动外呼任务删除成员
     *
     * @param taskIdentity
     * @param members
     * @param callCenterId
     * @return
     */
    public Map<String, Object> removeTaskMembers(String taskIdentity, Set<String> members, String callCenterId) {
        XzRemoveTaskMember param = new XzRemoveTaskMember();
        param.setTaskidentity(taskIdentity);
        param.setMembers(StringUtils.join(members, ","));
        param.setCompid(callCenterId);
        LOG.info("讯众自动外呼任务删除成员请求参数:" + param);
        Map<String, Object> result = null;
        try {
            result = XzCallCenterUtil.removeTaskMembers(param);
        } catch (Exception e) {
            LOG.error("讯众自动外呼任务删除成员异常,", e);
        }
        LOG.info("讯众自动外呼任务删除成员返回结果:" + result);
        return result;
    }

    /**
     * 获取讯众自动外呼成员
     *
     * @param taskIdentity
     * @param type
     * @param callCenterId
     * @return
     */
    public JSONObject getTaskMembers(String taskIdentity, int type, String callCenterId) {
        JSONObject result = null;
        try {
            result = XzCallCenterUtil.getTaskMembers(taskIdentity, type, callCenterId);
        } catch (Exception e) {
            LOG.error("讯众获取自动外呼任务成员异常,", e);
        }
        LOG.info("讯众获取自动外呼任务成员返回结果:" + result);
        return result;
    }

    /**
     * 讯众自动外呼添加拉取号码url
     *
     * @param callCenterId
     * @param userId
     * @param taskIdentity
     * @param url
     * @return
     */
    public JSONObject addPhoneUrl(String callCenterId, String userId, String taskIdentity, String url) {
        String result = null;
        try {
            result = XzCallCenterUtil.addPhoneUrl(callCenterId, userId, taskIdentity, url);
        } catch (Exception e) {
            LOG.error("讯众自动外呼添加拉取号码url异常,", e);
        }
        return JSON.parseObject(result);
    }

    /**
     * 查询企业最大呼叫并发数
     *
     * @param callCenterId
     * @return
     * @throws Exception
     */
    public int queryMaxCallOutNumber(String callCenterId) {
        int number = 0;
        String result = null;
        try {
            result = XzCallCenterUtil.queryMaxCallOutNumber(callCenterId);
        } catch (Exception e) {
            LOG.error("讯众查询企业最大呼叫并发数异常,", e);
            number = 0;
        }
        LOG.info("讯众查询企业最大呼叫并发数返回结果:" + result);
        if (result != null) {
            JSONObject jsonObject = JSON.parseObject(result);
            number = JSON.parseObject(jsonObject.getString("data")).getIntValue("number");
        }
        return number;
    }

    /**
     * 查询讯众自动外呼监控信息
     *
     * @param type 1-营销任务 2-公海
     * @param id
     * @return
     */
    public XzAutoTaskMonitor getXzAutoTaskMonitor(int type, String id) {
        XzAutoTaskMonitor data = null;
        String compId = null, taskId = null, callChannel = null;
        if (1 == type) {
            MarketTask marketTask = marketTaskDao.get(id);
            if (marketTask != null) {
                taskId = marketTask.getTaskId();
                MarketTaskProperty property = marketTaskDao.getProperty(id, "callChannel");
                if (property != null) {
                    callChannel = property.getPropertyValue();
                }
            }
        } else if (2 == type) {
            CustomerSea customerSea = customerSeaDao.get(NumberConvertUtil.parseLong(id));
            if (customerSea != null) {
                taskId = customerSea.getTaskId();
                CustomerSeaProperty property = customerSeaDao.getProperty(id, "callChannel");
                if (property != null) {
                    callChannel = property.getPropertyValue();
                }
            }
        } else {
            return new XzAutoTaskMonitor();
        }
        if (StringUtil.isEmpty(taskId) || StringUtil.isEmpty(callChannel)) {
            LOG.warn("查询讯众自动外呼监控信息未查询到任务ID和渠道,type:[" + type + "],id:[" + id + "]");
            return new XzAutoTaskMonitor();
        }

        ResourcePropertyEntity mrp = marketResourceDao.getProperty(callChannel, "price_config");
        if (mrp != null && StringUtil.isNotEmpty(mrp.getPropertyValue())) {
            JSONObject callCenterConfig = JSON.parseObject(mrp.getPropertyValue());
            // 呼叫中心类型SaaS模式则创建讯众自动外呼任务
            if ("1".equals(callCenterConfig.getString("type")) && "2".equals(callCenterConfig.getString("call_center_type"))) {
                compId = callCenterConfig.getJSONObject("call_center_config").getString("callCenterId");
            }
        }
        JSONObject result = null;
        try {
            result = XzCallCenterUtil.getAutoMonitorData(compId, taskId);
        } catch (Exception e) {
            LOG.error("查询讯众自动外呼监控信息异常,", e);
        }
        LOG.info("查询讯众自动外呼监控信息返回结果:" + result);
        if (result != null && result.getBooleanValue("issuccess")) {
            data = JSON.parseObject(result.getString("data"), XzAutoTaskMonitor.class);
        }
        if (data == null) {
            data = new XzAutoTaskMonitor();
        }
        return data;
    }
}
