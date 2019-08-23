package com.bdaim.customer.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/27
 * @description
 */
@Entity
@Table(name = "t_customer_user_group", schema = "", catalog = "")
public class CustomerUserGroup implements Serializable {
    private String id;
    private String name;
    private String custId;
    private Integer userCount;
    private Integer loginCount;
    private Integer freeCount;
    private Integer busyCount;
    private Long callCount;
    private String createUser;
    private Integer status;
    private Timestamp createTime;
    private String groupLeaderId;
    private Integer leavel;
    private String province;
    private String city;
    private String pid;
    private String remark;




    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "cust_id")
    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    @Basic
    @Column(name = "user_count")
    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    @Basic
    @Column(name = "login_count")
    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    @Basic
    @Column(name = "free_count")
    public Integer getFreeCount() {
        return freeCount;
    }

    public void setFreeCount(Integer freeCount) {
        this.freeCount = freeCount;
    }

    @Basic
    @Column(name = "busy_count")
    public Integer getBusyCount() {
        return busyCount;
    }

    public void setBusyCount(Integer busyCount) {
        this.busyCount = busyCount;
    }

    @Basic
    @Column(name = "call_count")
    public Long getCallCount() {
        return callCount;
    }

    public void setCallCount(Long callCount) {
        this.callCount = callCount;
    }

    @Basic
    @Column(name = "create_user")
    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    @Basic
    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "group_leader_id")
    public String getGroupLeaderId() {
        return groupLeaderId;
    }

    public void setGroupLeaderId(String groupLeaderId) {
        this.groupLeaderId = groupLeaderId;
    }

    @Basic
    @Column(name = "leavel")
    public Integer getLeavel() {
        return leavel;
    }

    public void setLeavel(Integer leavel) {
        this.leavel = leavel;
    }
    @Basic
    @Column(name = "province")
    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
    @Basic
    @Column(name = "city")
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
    @Basic
    @Column(name = "pid")
    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
    @Basic
    @Column(name = "remark")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "CustomerUserGroup{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", custId='" + custId + '\'' +
                ", userCount=" + userCount +
                ", loginCount=" + loginCount +
                ", freeCount=" + freeCount +
                ", busyCount=" + busyCount +
                ", callCount=" + callCount +
                ", createUser='" + createUser + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", groupLeaderId='" + groupLeaderId + '\'' +
                ", leavel=" + leavel +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", pid='" + pid + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
