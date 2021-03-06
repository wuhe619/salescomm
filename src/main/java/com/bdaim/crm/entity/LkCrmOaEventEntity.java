package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_oa_event", schema = "", catalog = "")
public class LkCrmOaEventEntity {
    private Integer eventId;
    private String title;
    private String custId;
    private String content;
    private Date startTime;
    private Date endTime;
    private Long createUserId;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Integer type;
    private String ownerUserIds;
    private String address;
    private String remark;
    private String color;
    private Integer remindType;

    private String customerIds;
    private String contactsIds;
    private String businessIds;
    private String contractIds;

    @Transient
    public String getCustomerIds() {
        return customerIds;
    }

    public void setCustomerIds(String customerIds) {
        this.customerIds = customerIds;
    }

    @Transient
    public String getContactsIds() {
        return contactsIds;
    }

    public void setContactsIds(String contactsIds) {
        this.contactsIds = contactsIds;
    }

    @Transient
    public String getBusinessIds() {
        return businessIds;
    }

    public void setBusinessIds(String businessIds) {
        this.businessIds = businessIds;
    }

    @Transient
    public String getContractIds() {
        return contractIds;
    }

    public void setContractIds(String contractIds) {
        this.contractIds = contractIds;
    }

    @Id
    @Column(name = "event_id")
    @GeneratedValue
    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    @Basic
    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Basic
    @Column(name = "start_time")
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "end_time")
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Basic
    @Column(name = "create_user_id")
    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
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
    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Basic
    @Column(name = "owner_user_ids")
    public String getOwnerUserIds() {
        return ownerUserIds;
    }

    public void setOwnerUserIds(String ownerUserIds) {
        this.ownerUserIds = ownerUserIds;
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
    @Column(name = "color")
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Basic
    @Column(name = "remind_type")
    public Integer getRemindType() {
        return remindType;
    }

    public void setRemindType(Integer remindType) {
        this.remindType = remindType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmOaEventEntity that = (LkCrmOaEventEntity) o;
        return eventId == that.eventId &&
                createUserId == that.createUserId &&
                Objects.equals(title, that.title) &&
                Objects.equals(content, that.content) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(type, that.type) &&
                Objects.equals(ownerUserIds, that.ownerUserIds) &&
                Objects.equals(address, that.address) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(color, that.color) &&
                Objects.equals(remindType, that.remindType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, title, content, startTime, endTime, createUserId, createTime, updateTime, type, ownerUserIds, address, remark, color, remindType);
    }
}
