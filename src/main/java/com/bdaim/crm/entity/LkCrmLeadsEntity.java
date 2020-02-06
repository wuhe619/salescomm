package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_crm_leads", schema = "", catalog = "")
public class LkCrmLeadsEntity {
    private Integer leadsId;
    private Integer isTransform;
    private Integer followup;
    private String leadsName;
    private Integer customerId;
    private Timestamp nextTime;
    private String telephone;
    private String mobile;
    private String address;
    private String remark;
    private int createUserId;
    private Integer ownerUserId;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String batchId;

    @Id
    @Column(name = "leads_id")
    @GeneratedValue
    public Integer getLeadsId() {
        return leadsId;
    }

    public void setLeadsId(Integer leadsId) {
        this.leadsId = leadsId;
    }

    @Basic
    @Column(name = "is_transform")
    public Integer getIsTransform() {
        return isTransform;
    }

    public void setIsTransform(Integer isTransform) {
        this.isTransform = isTransform;
    }

    @Basic
    @Column(name = "followup")
    public Integer getFollowup() {
        return followup;
    }

    public void setFollowup(Integer followup) {
        this.followup = followup;
    }

    @Basic
    @Column(name = "leads_name")
    public String getLeadsName() {
        return leadsName;
    }

    public void setLeadsName(String leadsName) {
        this.leadsName = leadsName;
    }

    @Basic
    @Column(name = "customer_id")
    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    @Basic
    @Column(name = "next_time")
    public Timestamp getNextTime() {
        return nextTime;
    }

    public void setNextTime(Timestamp nextTime) {
        this.nextTime = nextTime;
    }

    @Basic
    @Column(name = "telephone")
    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Basic
    @Column(name = "mobile")
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Basic
    @Column(name = "address")
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Basic
    @Column(name = "remark")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Basic
    @Column(name = "create_user_id")
    public int getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(int createUserId) {
        this.createUserId = createUserId;
    }

    @Basic
    @Column(name = "owner_user_id")
    public Integer getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Integer ownerUserId) {
        this.ownerUserId = ownerUserId;
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
    @Column(name = "update_time")
    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Basic
    @Column(name = "batch_id")
    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmLeadsEntity that = (LkCrmLeadsEntity) o;
        return leadsId == that.leadsId &&
                createUserId == that.createUserId &&
                Objects.equals(isTransform, that.isTransform) &&
                Objects.equals(followup, that.followup) &&
                Objects.equals(leadsName, that.leadsName) &&
                Objects.equals(customerId, that.customerId) &&
                Objects.equals(nextTime, that.nextTime) &&
                Objects.equals(telephone, that.telephone) &&
                Objects.equals(mobile, that.mobile) &&
                Objects.equals(address, that.address) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(ownerUserId, that.ownerUserId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(batchId, that.batchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leadsId, isTransform, followup, leadsName, customerId, nextTime, telephone, mobile, address, remark, createUserId, ownerUserId, createTime, updateTime, batchId);
    }
}
