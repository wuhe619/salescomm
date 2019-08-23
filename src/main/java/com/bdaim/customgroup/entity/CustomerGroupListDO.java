package com.bdaim.customgroup.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Mr.YinXin on 2017/3/6.
 */
@Entity
@Table(name = "t_customer_group_list", schema = "", catalog = "")
@IdClass(CustomerGroupListDOPK.class)
public class CustomerGroupListDO {
    private String id;
    private String custId;
    private Long userId;
    private Timestamp updateTime;
    private Timestamp createTime;
    private Integer status;
    private Integer customerGroupId;
    private Integer sourceId;
    private String groupConditionMd5;

    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
    @Column(name = "user_id")
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "update_time")
    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
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
    @Column(name = "STATUS")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "customer_group_id")
    public Integer getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(Integer customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    @Basic
    @Column(name = "source_id")
    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    @Id
    @Column(name = "group_condition_md5")
    public String getGroupConditionMd5() {
        return groupConditionMd5;
    }

    public void setGroupConditionMd5(String groupConditionMd5) {
        this.groupConditionMd5 = groupConditionMd5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomerGroupListDO that = (CustomerGroupListDO) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (custId != null ? !custId.equals(that.custId) : that.custId != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (updateTime != null ? !updateTime.equals(that.updateTime) : that.updateTime != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (customerGroupId != null ? !customerGroupId.equals(that.customerGroupId) : that.customerGroupId != null)
            return false;
        if (sourceId != null ? !sourceId.equals(that.sourceId) : that.sourceId != null) return false;
        if (groupConditionMd5 != null ? !groupConditionMd5.equals(that.groupConditionMd5) : that.groupConditionMd5 != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (custId != null ? custId.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (customerGroupId != null ? customerGroupId.hashCode() : 0);
        result = 31 * result + (sourceId != null ? sourceId.hashCode() : 0);
        result = 31 * result + (groupConditionMd5 != null ? groupConditionMd5.hashCode() : 0);
        return result;
    }
}
