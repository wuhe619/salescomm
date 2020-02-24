package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_crm_customer", schema = "", catalog = "")
public class  LkCrmCustomerEntity {
    private Integer customerId;
    private String customerName;
    private Integer followup;
    private String custId;
    private int isLock;
    private Timestamp nextTime;
    private String dealStatus;
    private String mobile;
    private String telephone;
    private String website;
    private String remark;
    private Integer createUserId;
    private Integer ownerUserId;
    private String roUserId;
    private String rwUserId;
    private String address;
    private String location;
    private String detailAddress;
    private String lng;
    private String lat;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String batchId;

    //移出方式（1.移除2.转为团队成员）
    private Integer transferType;
    //同时变更（1.联系人2.商机3.合同）
    private String changeType;
    //权限（1.只读2.只写）
    private Integer power;
    //变更负责人
    private Integer newOwnerUserId;
    private String ids;
    private String memberIds;
    private String customerIds;
    private Integer checkstatus;
    private String businessName;

    private String company;
    @Id
    @Column(name = "customer_id")
    @GeneratedValue
    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
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
    @Column(name = "customer_name")
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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
    @Column(name = "is_lock")
    public int getIsLock() {
        return isLock;
    }

    public void setIsLock(int isLock) {
        this.isLock = isLock;
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
    @Column(name = "deal_status")
    public String getDealStatus() {
        return dealStatus;
    }

    public void setDealStatus(String dealStatus) {
        this.dealStatus = dealStatus;
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
    @Column(name = "telephone")
    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Basic
    @Column(name = "website")
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
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
    public Integer getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Integer createUserId) {
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
    @Column(name = "ro_user_id")
    public String getRoUserId() {
        return roUserId;
    }

    public void setRoUserId(String roUserId) {
        this.roUserId = roUserId;
    }

    @Basic
    @Column(name = "rw_user_id")
    public String getRwUserId() {
        return rwUserId;
    }

    public void setRwUserId(String rwUserId) {
        this.rwUserId = rwUserId;
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
    @Column(name = "location")
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Basic
    @Column(name = "detail_address")
    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    @Basic
    @Column(name = "lng")
    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    @Basic
    @Column(name = "lat")
    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
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

    @Basic
    @Column(name = "company")
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmCustomerEntity that = (LkCrmCustomerEntity) o;
        return customerId == that.customerId &&
                isLock == that.isLock &&
                Objects.equals(customerName, that.customerName) &&
                Objects.equals(followup, that.followup) &&
                Objects.equals(nextTime, that.nextTime) &&
                Objects.equals(dealStatus, that.dealStatus) &&
                Objects.equals(mobile, that.mobile) &&
                Objects.equals(telephone, that.telephone) &&
                Objects.equals(website, that.website) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(createUserId, that.createUserId) &&
                Objects.equals(ownerUserId, that.ownerUserId) &&
                Objects.equals(roUserId, that.roUserId) &&
                Objects.equals(rwUserId, that.rwUserId) &&
                Objects.equals(address, that.address) &&
                Objects.equals(location, that.location) &&
                Objects.equals(detailAddress, that.detailAddress) &&
                Objects.equals(lng, that.lng) &&
                Objects.equals(lat, that.lat) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(batchId, that.batchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, customerName, followup, isLock, nextTime, dealStatus, mobile, telephone, website, remark, createUserId, ownerUserId, roUserId, rwUserId, address, location, detailAddress, lng, lat, createTime, updateTime, batchId);
    }

    @Transient
    public Integer getTransferType() {
        return transferType;
    }

    public void setTransferType(Integer transferType) {
        this.transferType = transferType;
    }
    @Transient
    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }
    @Transient
    public Integer getPower() {
        return power;
    }

    public void setPower(Integer power) {
        this.power = power;
    }
    @Transient
    public Integer getNewOwnerUserId() {
        return newOwnerUserId;
    }

    public void setNewOwnerUserId(Integer newOwnerUserId) {
        this.newOwnerUserId = newOwnerUserId;
    }
    @Transient
    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }
    @Transient
    public String getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(String memberIds) {
        this.memberIds = memberIds;
    }
    @Transient
    public String getCustomerIds() {
        return customerIds;
    }

    public void setCustomerIds(String customerIds) {
        this.customerIds = customerIds;
    }
    @Transient
    public Integer getCheckstatus() {
        return checkstatus;
    }

    public void setCheckstatus(Integer checkstatus) {
        this.checkstatus = checkstatus;
    }
    @Transient
    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }
}
