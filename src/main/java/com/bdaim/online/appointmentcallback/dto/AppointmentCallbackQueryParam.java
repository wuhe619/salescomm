package com.bdaim.online.appointmentcallback.dto;

/**
 *  预约回拨
 *
 * @author chengning@salescomm.net
 * @date 2019/2/12 16:41
 */
public class AppointmentCallbackQueryParam {
    private String superid;
    private Integer customerGroupId;
    private String custId;
    private String backupPhone;
    private String appointmentStartTime;
    private String appointmentEndTime;
    private String operator;
    private String remark;
    private int pageNum;
    private int pageSize;
    private String status;
    private String userGroupId;
    private String userGroupRole;
    private String userType;
    private String userId;
    private String marketTaskId;
    private String marketTaskName;


    public String getSuperid() {
        return superid;
    }

    public void setSuperid(String superid) {
        this.superid = superid;
    }

    public Integer getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(Integer customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getBackupPhone() {
        return backupPhone;
    }

    public void setBackupPhone(String backupPhone) {
        this.backupPhone = backupPhone;
    }

    public String getAppointmentStartTime() {
        return appointmentStartTime;
    }

    public void setAppointmentStartTime(String appointmentStartTime) {
        this.appointmentStartTime = appointmentStartTime;
    }

    public String getAppointmentEndTime() {
        return appointmentEndTime;
    }

    public void setAppointmentEndTime(String appointmentEndTime) {
        this.appointmentEndTime = appointmentEndTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }

    public String getUserGroupRole() {
        return userGroupRole;
    }

    public void setUserGroupRole(String userGroupRole) {
        this.userGroupRole = userGroupRole;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMarketTaskId() {
        return marketTaskId;
    }

    public void setMarketTaskId(String marketTaskId) {
        this.marketTaskId = marketTaskId;
    }

    public String getMarketTaskName() {
        return marketTaskName;
    }

    public void setMarketTaskName(String marketTaskName) {
        this.marketTaskName = marketTaskName;
    }

    @Override
    public String toString() {
        return "AppointmentCallbackQueryParam{" +
                "superid='" + superid + '\'' +
                ", customerGroupId=" + customerGroupId +
                ", custId='" + custId + '\'' +
                ", backupPhone='" + backupPhone + '\'' +
                ", appointmentStartTime='" + appointmentStartTime + '\'' +
                ", appointmentEndTime='" + appointmentEndTime + '\'' +
                ", operator='" + operator + '\'' +
                ", remark='" + remark + '\'' +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", status='" + status + '\'' +
                ", userGroupId='" + userGroupId + '\'' +
                ", userGroupRole='" + userGroupRole + '\'' +
                ", userType='" + userType + '\'' +
                ", userId='" + userId + '\'' +
                ", marketTaskId='" + marketTaskId + '\'' +
                ", marketTaskName='" + marketTaskName + '\'' +
                '}';
    }
}
