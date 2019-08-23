package com.bdaim.markettask.dto;

import com.bdaim.markettask.entity.MarketTask;

import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2019/4/26
 * @description
 */
public class MarketTaskDTO {
    private String id;
    private String custId;
    private String name;
    private Integer customerGroupId;
    private String smsTemplateId;
    private String taskId;
    private String touchMode;
    private Integer taskType;
    /**
     * 1-呼叫中心 2-双呼 3-机器人
     */
    private Integer callType;

    /**
     * 呼叫中心配置
     */
    private String callCenterConfig;

    /**
     * 1-单机 2-SaaS
     */
    private Integer callCenterType;
    /**
     *  短信
     */
    private Integer smsType;
    private String callChannel;
    private String callChannelName;
    private String apparentNumber;
    private Integer callSpeed;
    private Integer callCount;
    private Timestamp taskCreateTime;
    private Timestamp taskEndTime;
    private Integer status;

    public MarketTaskDTO() {
    }

    public MarketTaskDTO(MarketTask m, String apparentNumber, Integer callSpeed, Integer callCount) {
        this.id = m.getId();
        this.custId = m.getCustId();
        this.name = m.getName();
        this.customerGroupId = m.getCustomerGroupId();
        this.smsTemplateId = m.getSmsTemplateId();
        this.taskId = m.getTaskId();
        this.taskType = m.getTaskType();
        this.apparentNumber = apparentNumber;
        this.callSpeed = callSpeed;
        this.callCount = callCount;
        this.taskCreateTime = m.getTaskCreateTime();
        this.taskEndTime = m.getTaskEndTime();
        this.status = m.getStatus();
    }

    public String getId() {
        return id;
    }

    public String getCallCenterConfig() {
        return callCenterConfig;
    }

    public void setCallCenterConfig(String callCenterConfig) {
        this.callCenterConfig = callCenterConfig;
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

    public Integer getCallSpeed() {
        return callSpeed;
    }

    public void setCallSpeed(Integer callSpeed) {
        this.callSpeed = callSpeed;
    }

    public Integer getCallCount() {
        return callCount;
    }

    public void setCallCount(Integer callCount) {
        this.callCount = callCount;
    }

    public Timestamp getTaskCreateTime() {
        return taskCreateTime;
    }

    public void setTaskCreateTime(Timestamp taskCreateTime) {
        this.taskCreateTime = taskCreateTime;
    }

    public Timestamp getTaskEndTime() {
        return taskEndTime;
    }

    public void setTaskEndTime(Timestamp taskEndTime) {
        this.taskEndTime = taskEndTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCallChannelName() {
        return callChannelName;
    }

    public void setCallChannelName(String callChannelName) {
        this.callChannelName = callChannelName;
    }

    public Integer getSmsType() {
        return smsType;
    }

    public void setSmsType(Integer smsType) {
        this.smsType = smsType;
    }

    public Integer getCallCenterType() {
        return callCenterType;
    }

    public void setCallCenterType(Integer callCenterType) {
        this.callCenterType = callCenterType;
    }

    @Override
    public String toString() {
        return "MarketTaskDTO{" +
                "id='" + id + '\'' +
                ", custId='" + custId + '\'' +
                ", name='" + name + '\'' +
                ", customerGroupId=" + customerGroupId +
                ", smsTemplateId='" + smsTemplateId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", touchMode='" + touchMode + '\'' +
                ", taskType=" + taskType +
                ", callType=" + callType +
                ", callCenterType=" + callCenterType +
                ", smsType=" + smsType +
                ", callChannel='" + callChannel + '\'' +
                ", callChannelName='" + callChannelName + '\'' +
                ", apparentNumber='" + apparentNumber + '\'' +
                ", callSpeed=" + callSpeed +
                ", callCount=" + callCount +
                ", taskCreateTime=" + taskCreateTime +
                ", taskEndTime=" + taskEndTime +
                ", status=" + status +
                '}';
    }
}
