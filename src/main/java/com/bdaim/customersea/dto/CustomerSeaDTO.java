package com.bdaim.customersea.dto;

import com.bdaim.customersea.entity.CustomerSea;

import java.sql.Timestamp;
import java.util.Map;

/**
 * 公海基础表
 * @author chengning@salescomm.net
 * @date 2019/6/22
 * @description
 */
public class CustomerSeaDTO {
    private String id;
    private Integer marketProjectId;
    private String marketProjectName;
    private String custId;
    private String custName;
    private String name;
    private String description;
    private Integer status;
    private Long createUid;
    private Timestamp createTime;
    private Long updateUid;
    private Timestamp updateTime;
    private String remark;
    private String quantity;
    private String taskId;
    private Integer taskType;
    private Timestamp taskEndTime;
    private Timestamp taskCreateTime;
    private String smsTemplateId;
    private String projectUserId;
    private String projectUserName;

    /**
     * 线索剩余量
     */
    private Long clueSurplusSum;
    /**
     * 累计未通量
     */
    private Long failCallSum;

    /**
     * 线索总量
     */
    private Long totalSum;
    /**
     * 未跟进数量
     */
    private Long noFollowSum;

    /**
     * 公海属性
     */
    private Map<String,Object> property;

    private String userGroupId;

    private String userGroupName;

    /**
     * 呼叫渠道ID
     */
    private String callChannel;

    private String callChannelName;

    private Integer callSpeed;
    private Integer callCount;
    /**
     * 呼叫模式 1-呼叫中心 2-双向呼叫 3-机器人呼叫
     */
    private Integer callType;

    /**
     * 1-单机 2-SaaS
     */
    private Integer callCenterType;
    /**
     *  短信
     */
    private Integer smsType;
    private String apparentNumber;

    public CustomerSeaDTO() {
    }

    public CustomerSeaDTO(CustomerSea customerSea) {
        this.setId(String.valueOf(customerSea.getId()));
        this.setMarketProjectId(customerSea.getMarketProjectId());
        this.setCustId(customerSea.getCustId());
        this.setName(customerSea.getName());
        this.setDescription(customerSea.getDescription());
        this.setStatus(customerSea.getStatus());
        this.setCreateUid(customerSea.getCreateUid());
        this.setCreateTime(customerSea.getCreateTime());
        this.setUpdateUid(customerSea.getUpdateUid());
        this.setUpdateTime(customerSea.getUpdateTime());
        this.setRemark(customerSea.getRemark());
        this.setQuantity(customerSea.getQuantity());
        this.setTaskId(customerSea.getTaskId());
        this.setTaskType(customerSea.getTaskType());
        this.setTaskEndTime(customerSea.getTaskEndTime());
        this.setTaskCreateTime(customerSea.getTaskCreateTime());
        this.setSmsTemplateId(customerSea.getSmsTemplateId());
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getMarketProjectId() {
        return marketProjectId;
    }

    public void setMarketProjectId(Integer marketProjectId) {
        this.marketProjectId = marketProjectId;
    }

    public String getMarketProjectName() {
        return marketProjectName;
    }

    public void setMarketProjectName(String marketProjectName) {
        this.marketProjectName = marketProjectName;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreateUid() {
        return createUid;
    }

    public void setCreateUid(Long createUid) {
        this.createUid = createUid;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateUid() {
        return updateUid;
    }

    public void setUpdateUid(Long updateUid) {
        this.updateUid = updateUid;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public Timestamp getTaskEndTime() {
        return taskEndTime;
    }

    public void setTaskEndTime(Timestamp taskEndTime) {
        this.taskEndTime = taskEndTime;
    }

    public Timestamp getTaskCreateTime() {
        return taskCreateTime;
    }

    public void setTaskCreateTime(Timestamp taskCreateTime) {
        this.taskCreateTime = taskCreateTime;
    }

    public String getSmsTemplateId() {
        return smsTemplateId;
    }

    public void setSmsTemplateId(String smsTemplateId) {
        this.smsTemplateId = smsTemplateId;
    }

    public String getProjectUserId() {
        return projectUserId;
    }

    public void setProjectUserId(String projectUserId) {
        this.projectUserId = projectUserId;
    }

    public String getProjectUserName() {
        return projectUserName;
    }

    public void setProjectUserName(String projectUserName) {
        this.projectUserName = projectUserName;
    }

    public Long getClueSurplusSum() {
        return clueSurplusSum;
    }

    public void setClueSurplusSum(Long clueSurplusSum) {
        this.clueSurplusSum = clueSurplusSum;
    }

    public Long getFailCallSum() {
        return failCallSum;
    }

    public void setFailCallSum(Long failCallSum) {
        this.failCallSum = failCallSum;
    }

    public Map<String, Object> getProperty() {
        return property;
    }

    public void setProperty(Map<String, Object> property) {
        this.property = property;
    }

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }

    public String getUserGroupName() {
        return userGroupName;
    }

    public void setUserGroupName(String userGroupName) {
        this.userGroupName = userGroupName;
    }

    public String getCallChannel() {
        return callChannel;
    }

    public void setCallChannel(String callChannel) {
        this.callChannel = callChannel;
    }

    public String getCallChannelName() {
        return callChannelName;
    }

    public void setCallChannelName(String callChannelName) {
        this.callChannelName = callChannelName;
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

    public Integer getCallType() {
        return callType;
    }

    public void setCallType(Integer callType) {
        this.callType = callType;
    }

    public Integer getCallCenterType() {
        return callCenterType;
    }

    public void setCallCenterType(Integer callCenterType) {
        this.callCenterType = callCenterType;
    }

    public Integer getSmsType() {
        return smsType;
    }

    public void setSmsType(Integer smsType) {
        this.smsType = smsType;
    }

    public String getApparentNumber() {
        return apparentNumber;
    }

    public void setApparentNumber(String apparentNumber) {
        this.apparentNumber = apparentNumber;
    }

    public Long getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(Long totalSum) {
        this.totalSum = totalSum;
    }

    public Long getNoFollowSum() {
        return noFollowSum;
    }

    public void setNoFollowSum(Long noFollowSum) {
        this.noFollowSum = noFollowSum;
    }
}
