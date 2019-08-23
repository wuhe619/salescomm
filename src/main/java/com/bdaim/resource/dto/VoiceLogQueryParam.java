package com.bdaim.resource.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019/1/18 10:20
 */
public class VoiceLogQueryParam {

    private Integer intentStatus;
    private String userName;
    private String startTime;
    private String endTime;
    private String callStatus;
    private String intentLevel;
    private String customerGroupId;
    private String superId;
    private String remark;
    private String userId;
    private String marketTaskId;

    public Integer getIntentStatus() {
        return intentStatus;
    }

    public void setIntentStatus(Integer intentStatus) {
        this.intentStatus = intentStatus;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public String getIntentLevel() {
        return intentLevel;
    }

    public void setIntentLevel(String intentLevel) {
        this.intentLevel = intentLevel;
    }

    public String getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(String customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    public String getSuperId() {
        return superId;
    }

    public void setSuperId(String superId) {
        this.superId = superId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    @Override
    public String toString() {
        return "VoiceLogQueryParam{" +
                "intentStatus=" + intentStatus +
                ", userName='" + userName + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", callStatus='" + callStatus + '\'' +
                ", intentLevel='" + intentLevel + '\'' +
                ", customerGroupId='" + customerGroupId + '\'' +
                ", superId='" + superId + '\'' +
                ", remark='" + remark + '\'' +
                ", userId='" + userId + '\'' +
                ", marketTaskId='" + marketTaskId + '\'' +
                '}';
    }
}
