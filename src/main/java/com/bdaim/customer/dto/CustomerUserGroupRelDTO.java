package com.bdaim.customer.dto;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/27
 * @description
 */
public class CustomerUserGroupRelDTO implements Serializable {
    private String groupId;
    private String groupName;
    private String userId;
    private Integer status;
    private Timestamp createTime;
    private Integer type;
    /**
     * 职场ID
     */
    private String jobMarketId;

    public CustomerUserGroupRelDTO(String groupId, String groupName, Integer type, String jobMarketId) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.type = type;
        this.jobMarketId = jobMarketId;
    }

    public CustomerUserGroupRelDTO(String groupId, String userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getJobMarketId() {
        return jobMarketId;
    }

    public void setJobMarketId(String jobMarketId) {
        this.jobMarketId = jobMarketId;
    }

    @Override
    public String toString() {
        return "CustomerUserGroupRelDTO{" +
                "groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", type=" + type +
                ", jobMarketId='" + jobMarketId + '\'' +
                '}';
    }
}
