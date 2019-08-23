package com.bdaim.customgroup.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 */
public class CustomerGrpOrdParam implements Serializable{
    private String groupName;
    private int orderType;
    private String groupId;
    private String startTime;
    private String endTime;
    @NotNull(message = "pageNum参数必填")
    @Min(value = 0, message = "pageNum最小值为0")
    private int pageNum;
    @NotNull(message = "pageSize参数必填")
    @Min(value = 1, message = "pageSize最小值为1")
    @Max(value = 100, message = "pageSize最大值为100")
    private int pageSize;

    private String custUserName;
    private String enterpriseName;
    private String dataSource;
    /**
     * 1-成功 3-处理中
     */
    private String status;

    /**
     * 用户群组ID
     */
    private String userGroupId;
    /**
     * 1-自动 2-手动
     */
    private String taskType;

    /**
     * 1-查询营销任务(包含未分配) 2-查询通话记录的营销任务(包含通话数量)
     */
    private String action;

    private String supplierName;

    private Integer chargingType;

    /**
     * 项目ID
     */
    private String marketProjectId;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCustUserName() {
        return custUserName;
    }

    public void setCustUserName(String custUserName) {
        this.custUserName = custUserName;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Integer getChargingType() {
        return chargingType;
    }

    public void setChargingType(Integer chargingType) {
        this.chargingType = chargingType;
    }

    public String getMarketProjectId() {
        return marketProjectId;
    }

    public void setMarketProjectId(String marketProjectId) {
        this.marketProjectId = marketProjectId;
    }

    @Override
    public String toString() {
        return "CustomerGrpOrdParam{" +
                "groupName='" + groupName + '\'' +
                ", orderType=" + orderType +
                ", groupId='" + groupId + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", custUserName='" + custUserName + '\'' +
                ", enterpriseName='" + enterpriseName + '\'' +
                ", dataSource='" + dataSource + '\'' +
                ", status='" + status + '\'' +
                ", userGroupId='" + userGroupId + '\'' +
                ", taskType='" + taskType + '\'' +
                ", action='" + action + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", chargingType=" + chargingType +
                ", marketProjectId='" + marketProjectId + '\'' +
                '}';
    }
}
