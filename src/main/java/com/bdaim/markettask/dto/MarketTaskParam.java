package com.bdaim.markettask.dto;

import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2019/4/24
 * @description
 */
public class MarketTaskParam {
    private String id;
    private String custId;
    private String name;
    private Integer customerGroupId;
    private String groupCondition;
    private String remark;
    private Integer quantity;
    private String smsTemplateId;
    private String taskId;
    private String touchMode;
    private Integer taskType;
    private Integer callType;
    private String callChannel;
    private String apparentNumber;
    private String callSpeed;
    private String callCount;
    private Long taskCreateTime;
    private Long taskEndTime;
    private List<String> userIds;
    private Long createUid;
    private Long createTime;
    private Long updateUid;
    private Long updateTime;
    private Integer status;
    private String ringingduration;
    private String timeruleid;
    /**
     * 1-历史任务Id
     */
    private String historyTaskId;

    /**
     * 发送批次数
     */
    private Integer smsBatchCount;

    /**
     * 短信发送规则 1-平均分配 2-指定发送
     */
    private Integer smsSendRule;

    private List<Map<String, Object>> smsSendConfig;

    /**
     * 短信发送时间
     */
    private String sendTime;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(Integer customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    public String getGroupCondition() {
        return groupCondition;
    }

    public void setGroupCondition(String groupCondition) {
        this.groupCondition = groupCondition;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSmsTemplateId() {
        return smsTemplateId;
    }

    public void setSmsTemplateId(String smsTemplateId) {
        this.smsTemplateId = smsTemplateId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTouchMode() {
        return touchMode;
    }

    public void setTouchMode(String touchMode) {
        this.touchMode = touchMode;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public Integer getCallType() {
        return callType;
    }

    public void setCallType(Integer callType) {
        this.callType = callType;
    }

    public String getCallChannel() {
        return callChannel;
    }

    public void setCallChannel(String callChannel) {
        this.callChannel = callChannel;
    }

    public String getApparentNumber() {
        return apparentNumber;
    }

    public void setApparentNumber(String apparentNumber) {
        this.apparentNumber = apparentNumber;
    }

    public String getCallSpeed() {
        return callSpeed;
    }

    public void setCallSpeed(String callSpeed) {
        this.callSpeed = callSpeed;
    }

    public Long getTaskCreateTime() {
        return taskCreateTime;
    }

    public void setTaskCreateTime(Long taskCreateTime) {
        this.taskCreateTime = taskCreateTime;
    }

    public Long getTaskEndTime() {
        return taskEndTime;
    }

    public void setTaskEndTime(Long taskEndTime) {
        this.taskEndTime = taskEndTime;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public Long getCreateUid() {
        return createUid;
    }

    public void setCreateUid(Long createUid) {
        this.createUid = createUid;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateUid() {
        return updateUid;
    }

    public void setUpdateUid(Long updateUid) {
        this.updateUid = updateUid;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getCallCount() {
        return callCount;
    }

    public void setCallCount(String callCount) {
        this.callCount = callCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getHistoryTaskId() {
        return historyTaskId;
    }

    public void setHistoryTaskId(String historyTaskId) {
        this.historyTaskId = historyTaskId;
    }

    public Integer getSmsBatchCount() {
        return smsBatchCount;
    }

    public void setSmsBatchCount(Integer smsBatchCount) {
        this.smsBatchCount = smsBatchCount;
    }

    public Integer getSmsSendRule() {
        return smsSendRule;
    }

    public void setSmsSendRule(Integer smsSendRule) {
        this.smsSendRule = smsSendRule;
    }

    public List<Map<String, Object>> getSmsSendConfig() {
        return smsSendConfig;
    }

    public void setSmsSendConfig(List<Map<String, Object>> smsSendConfig) {
        this.smsSendConfig = smsSendConfig;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getRingingduration() {
        return ringingduration;
    }

    public void setRingingduration(String ringingduration) {
        this.ringingduration = ringingduration;
    }

    public String getTimeruleid() {
        return timeruleid;
    }

    public void setTimeruleid(String timeruleid) {
        this.timeruleid = timeruleid;
    }
}
