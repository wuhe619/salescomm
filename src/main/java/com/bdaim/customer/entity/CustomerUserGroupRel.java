package com.bdaim.customer.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/27
 * @description
 */
@Entity
@Table(name = "t_customer_user_group_rel", schema = "", catalog = "")
public class CustomerUserGroupRel implements Serializable {
    private String groupId;
    private String userId;
    private Integer status;
    private Timestamp createTime;
    private Integer type;

    @Id
    @Column(name = "group_id", updatable = false)
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Id
    @Column(name = "user_id", updatable = false)
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "status", updatable = false)
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "create_time", updatable = false)
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerUserGroupRel that = (CustomerUserGroupRel) o;
        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(status, that.status) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(groupId, userId, status, createTime);
    }

    @Override
    public String toString() {
        return "CustomerUserGroupRel{" +
                "groupId='" + groupId + '\'' +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", type=" + type +
                '}';
    }
}
