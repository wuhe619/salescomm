package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_crm_customer", schema = "crm", catalog = "")
public class LkCrmCustomerEntity {
    private int customerId;
    private String customerName;
    private Integer followup;
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

    @Id
    @Column(name = "customer_id")
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
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
}
