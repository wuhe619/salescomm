package com.bdaim.customgroup.dto;

import com.bdaim.customgroup.entity.CustomGroup;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;


public class CustomGroupDTO {

    private Integer id;
    private String name;
    private String desc;
    private Integer type;
    private Long userCount;
    private Long total;
    private Integer status;
    private String statusName;
    private Date createTime;
    private Date updateTime;

    private String createUser;
    private String updateUser;

    private Integer availably;
    private String purpose;
    private Integer updateCycle;
    private String groupCondition;
    private String api;
    private Date startTime;
    private Date endTime;
    private Integer downloadCount;
    // 时间周期0、全部 1、7天 2、15天 3、30天
    private Integer cycle;
    private String custId;
    private String enterpriseName;
    private Integer industryPoolId;
    private String industryPoolName;
    private String orderId;
    private String amount;
    private Integer quantity;
    private String remark;
    private String groupSource;

    private String taskId;
    private Integer taskPhoneIndex;
    private Integer taskType;
    private Timestamp taskEndTime;
    private Timestamp taskCreateTime;
    /**
     * 营销项目ID
     */
    private Integer marketProjectId;
    private String marketProjectName;
    private Map properties;

    public CustomGroupDTO() {

    }

    public CustomGroupDTO(CustomGroup cg) {
        this.id = cg.getId();
        this.name = cg.getName();
        this.desc = cg.getDesc();
        this.type = cg.getType();
        this.userCount = cg.getUserCount();
        this.total = cg.getTotal();
        this.status = cg.getStatus();
        this.createTime = cg.getCreateTime();
        this.updateTime = cg.getUpdateTime();
        this.createUser = cg.getCreateUserId();
        this.updateUser = cg.getUpdateUserId();
        this.enterpriseName = cg.getEnterpriseName();
        this.industryPoolId = cg.getIndustryPoolId();
        this.industryPoolName = cg.getIndustryPoolName();
        this.amount = String.valueOf(cg.getAmount());
        this.quantity = cg.getQuantity();
        this.taskId = cg.getTaskId();
        this.taskPhoneIndex = cg.getTaskPhoneIndex();
        this.taskType = cg.getTaskType();
        this.taskEndTime = cg.getTaskEndTime();
        this.taskCreateTime = cg.getTaskCreateTime();
        this.marketProjectId = cg.getMarketProjectId();
        this.custId = cg.getCustId();
        this.remark = cg.getRemark();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public Integer getAvailably() {
        return availably;
    }

    public void setAvailably(Integer availably) {
        this.availably = availably;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Integer getUpdateCycle() {
        return updateCycle;
    }

    public void setUpdateCycle(Integer updateCycle) {
        this.updateCycle = updateCycle;
    }

    public String getGroupCondition() {
        return groupCondition;
    }

    public void setGroupCondition(String groupCondition) {
        this.groupCondition = groupCondition;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Integer getCycle() {
        return cycle;
    }

    public void setCycle(Integer cycle) {
        this.cycle = cycle;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public Integer getIndustryPoolId() {
        return industryPoolId;
    }

    public void setIndustryPoolId(Integer industryPoolId) {
        this.industryPoolId = industryPoolId;
    }

    public String getIndustryPoolName() {
        return industryPoolName;
    }

    public void setIndustryPoolName(String industryPoolName) {
        this.industryPoolName = industryPoolName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getGroupSource() {
        return groupSource;
    }

    public void setGroupSource(String groupSource) {
        this.groupSource = groupSource;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getTaskPhoneIndex() {
        return taskPhoneIndex;
    }

    public void setTaskPhoneIndex(Integer taskPhoneIndex) {
        this.taskPhoneIndex = taskPhoneIndex;
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

    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}

