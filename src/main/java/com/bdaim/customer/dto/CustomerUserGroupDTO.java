package com.bdaim.customer.dto;


import com.bdaim.customer.entity.CustomerUserGroup;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2018/10/18
 * @description
 */
public class CustomerUserGroupDTO implements Serializable {
    private String id;
    private String name;
    private Integer userCount;
    private Integer loginCount;
    private Integer freeCount;
    private Integer busyCount;
    private Long callCount;
    private String createUser;
    private Integer status;
    private Timestamp createTime;
    private String groupLeaderId;
    private String groupLeaderName;
    private Integer leavel;
    private String province;
    private String city;
    private String pid;
    private String remark;
    private String jobName;

    public CustomerUserGroupDTO(CustomerUserGroup customerUserGroup) {
        this.id = customerUserGroup.getId();
        this.name = customerUserGroup.getName();
        this.userCount = customerUserGroup.getUserCount();
        this.loginCount = customerUserGroup.getLoginCount();
        this.freeCount = customerUserGroup.getFreeCount();
        this.busyCount = customerUserGroup.getBusyCount();
        this.callCount = customerUserGroup.getCallCount();
        this.createUser = customerUserGroup.getCreateUser();
        this.status = customerUserGroup.getStatus();
        this.createTime = customerUserGroup.getCreateTime();
        this.groupLeaderId = customerUserGroup.getGroupLeaderId();
        this.pid = customerUserGroup.getPid();
        this.leavel = customerUserGroup.getLeavel();
        this.city = customerUserGroup.getCity();
        this.province = customerUserGroup.getProvince();
        this.remark = customerUserGroup.getRemark();
    }

    public CustomerUserGroupDTO() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    public Integer getFreeCount() {
        return freeCount;
    }

    public void setFreeCount(Integer freeCount) {
        this.freeCount = freeCount;
    }

    public Integer getBusyCount() {
        return busyCount;
    }

    public void setBusyCount(Integer busyCount) {
        this.busyCount = busyCount;
    }

    public Long getCallCount() {
        return callCount;
    }

    public void setCallCount(Long callCount) {
        this.callCount = callCount;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
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

    public String getGroupLeaderId() {
        return groupLeaderId;
    }

    public void setGroupLeaderId(String groupLeaderId) {
        this.groupLeaderId = groupLeaderId;
    }

    public String getGroupLeaderName() {
        return groupLeaderName;
    }

    public void setGroupLeaderName(String groupLeaderName) {
        this.groupLeaderName = groupLeaderName;
    }

    public Integer getLeavel() {
        return leavel;
    }

    public void setLeavel(Integer leavel) {
        this.leavel = leavel;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public String toString() {
        return "CustomerUserGroupDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", userCount=" + userCount +
                ", loginCount=" + loginCount +
                ", freeCount=" + freeCount +
                ", busyCount=" + busyCount +
                ", callCount=" + callCount +
                ", createUser='" + createUser + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", groupLeaderId='" + groupLeaderId + '\'' +
                ", groupLeaderName='" + groupLeaderName + '\'' +
                ", leavel=" + leavel +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", pid='" + pid + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
