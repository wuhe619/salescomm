package com.bdaim.customersea.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 公海基础表
 *
 * @author chengning@salescomm.net
 * @date 2019/6/22
 * @description
 */
public class CustomerSeaParam {

    @NotNull(message = "pageNum参数必填")
    @Min(value = 0, message = "pageNum最小值为0")
    private Integer pageNum;
    @NotNull(message = "pageSize参数必填")
    @Min(value = 1, message = "pageSize最小值为1")
    @Max(value = 100, message = "pageSize最大值为100")
    private Integer pageSize;
    private Long id;
    private String custId;
    private Integer marketProjectId;
    private String marketProjectName;
    private String name;
    private String type;

    /**
     * 1 ：公海 2 私海
     * @return
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * 任务类型:1-自动 2-手动 3-机器人外呼 4-短信
     */
    private Integer taskType;
    /**
     * 呼叫模式 1-呼叫中心 2-双向呼叫 3-机器人呼叫
     */
    private Integer callType;
    /**
     * 呼叫渠道ID
     */
    private String callChannel;
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 是否需要人工跟进 1-需要 2-不需要
     */
    private Integer isPersonFollow;

    /**
     * 意向等级 A-F
     */
    private String intentLevel;

    /**
     * 线索领取方式 1-手动 2-系统自动分配
     */
    private Integer clueGetMode;

    /**
     * 线索领取限制 1-无限制 2-限制数量
     */
    private Integer clueGetRestrict;

    /**
     * 线索领取限制值
     */
    private String clueGetRestrictValue;
    private String historyTaskId;
    /**
     * 外显号
     */
    private String apparentNumber;
    private Integer callSpeed;
    private Integer callCount;
    private Long taskCreateTime;
    private Long taskEndTime;
    private Long createUid;
    private Long createTime;
    private Long updateUid;
    private Long updateTime;
    private Integer status;
    private String description;
    private String remark;
    private Integer taskSmsIndex;
    private String smsTemplateId;

    /**
     * 线索回收规则 1-不启用 2-启用
     */
    private Integer recoveryRule;
    /**
     * 线索领取后超时回收时间(天)
     */
    private String recoveryGetTimeout;
    /**
     * 线索初次跟进后超时回收时间(天)
     */
    private String recoveryFirstTimeout;
    /**
     * 回收提醒 1-发送提醒（24小时前） 2-不发送提醒
     */
    private Integer recoveryRemind;

    /**
     * 1-修改公海状态 2-修改公海配置信息 3-线索回收规则设置
     */
    private Integer operation;

    private String userType;
    private Long userId;

    /**
     * 员工可见私海数据 1-全部 2-指定时长
     */
    private Integer visibleDataType;
    /**
     * 员工可见私海数据时长
     */
    private String visibleDataTimeout;

    private String userGroupRole;

    private String userGroupId;

    public CustomerSeaParam() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
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

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getIsPersonFollow() {
        return isPersonFollow;
    }

    public void setIsPersonFollow(Integer isPersonFollow) {
        this.isPersonFollow = isPersonFollow;
    }

    public String getIntentLevel() {
        return intentLevel;
    }

    public void setIntentLevel(String intentLevel) {
        this.intentLevel = intentLevel;
    }

    public Integer getClueGetMode() {
        return clueGetMode;
    }

    public void setClueGetMode(Integer clueGetMode) {
        this.clueGetMode = clueGetMode;
    }

    public Integer getClueGetRestrict() {
        return clueGetRestrict;
    }

    public void setClueGetRestrict(Integer clueGetRestrict) {
        this.clueGetRestrict = clueGetRestrict;
    }

    public String getClueGetRestrictValue() {
        return clueGetRestrictValue;
    }

    public void setClueGetRestrictValue(String clueGetRestrictValue) {
        this.clueGetRestrictValue = clueGetRestrictValue;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getTaskSmsIndex() {
        return taskSmsIndex;
    }

    public void setTaskSmsIndex(Integer taskSmsIndex) {
        this.taskSmsIndex = taskSmsIndex;
    }

    public String getSmsTemplateId() {
        return smsTemplateId;
    }

    public void setSmsTemplateId(String smsTemplateId) {
        this.smsTemplateId = smsTemplateId;
    }

    public Integer getRecoveryRule() {
        return recoveryRule;
    }

    public void setRecoveryRule(Integer recoveryRule) {
        this.recoveryRule = recoveryRule;
    }

    public String getRecoveryGetTimeout() {
        return recoveryGetTimeout;
    }

    public void setRecoveryGetTimeout(String recoveryGetTimeout) {
        this.recoveryGetTimeout = recoveryGetTimeout;
    }

    public String getRecoveryFirstTimeout() {
        return recoveryFirstTimeout;
    }

    public void setRecoveryFirstTimeout(String recoveryFirstTimeout) {
        this.recoveryFirstTimeout = recoveryFirstTimeout;
    }

    public Integer getRecoveryRemind() {
        return recoveryRemind;
    }

    public void setRecoveryRemind(Integer recoveryRemind) {
        this.recoveryRemind = recoveryRemind;
    }

    public Integer getOperation() {
        return operation;
    }

    public void setOperation(Integer operation) {
        this.operation = operation;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getVisibleDataType() {
        return visibleDataType;
    }

    public void setVisibleDataType(Integer visibleDataType) {
        this.visibleDataType = visibleDataType;
    }

    public String getVisibleDataTimeout() {
        return visibleDataTimeout;
    }

    public void setVisibleDataTimeout(String visibleDataTimeout) {
        this.visibleDataTimeout = visibleDataTimeout;
    }

    public String getHistoryTaskId() {
        return historyTaskId;
    }

    public void setHistoryTaskId(String historyTaskId) {
        this.historyTaskId = historyTaskId;
    }

    public String getUserGroupRole() {
        return userGroupRole;
    }

    public void setUserGroupRole(String userGroupRole) {
        this.userGroupRole = userGroupRole;
    }

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }
}
