package com.bdaim.log.dto;

import com.bdaim.log.entity.SuperDataOperLog;

import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2019/7/9
 * @description
 */
public class SuperDataOperLogDTO {
    private Long userId;
    private String userName;
    private String dataId;
    private String customerSeaId;
    private String customerGroupId;
    private Integer eventType;
    private String objId;
    private String objName;
    private Timestamp createTime;
    private String reason;
    private String remark;

    public SuperDataOperLogDTO() {
    }

    public SuperDataOperLogDTO(SuperDataOperLog s) {
        this.userId = s.getUserId();
        this.dataId = s.getListId();
        this.customerSeaId = s.getCustomerSeaId();
        this.customerGroupId = s.getCustomerGroupId();
        this.eventType = s.getEventType();
        this.objId = s.getObjectCode();
        this.reason = s.getReason();
        this.remark = s.getRemark();
        this.createTime = s.getCreateTime();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getCustomerSeaId() {
        return customerSeaId;
    }

    public void setCustomerSeaId(String customerSeaId) {
        this.customerSeaId = customerSeaId;
    }

    public String getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(String customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    public Integer getEventType() {
        return eventType;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }

    public String getObjId() {
        return objId;
    }

    public void setObjId(String objId) {
        this.objId = objId;
    }

    public String getObjName() {
        return objName;
    }

    public void setObjName(String objName) {
        this.objName = objName;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "SuperDataOperLogDTO{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", dataId='" + dataId + '\'' +
                ", customerSeaId='" + customerSeaId + '\'' +
                ", customerGroupId='" + customerGroupId + '\'' +
                ", eventType=" + eventType +
                ", objId='" + objId + '\'' +
                ", objName='" + objName + '\'' +
                ", createTime=" + createTime +
                ", reason='" + reason + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
