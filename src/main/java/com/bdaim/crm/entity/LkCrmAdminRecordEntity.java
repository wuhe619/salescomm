package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_admin_record", schema = "", catalog = "")
public class LkCrmAdminRecordEntity {
    private Integer recordId;
    private String custId;
    private String types;
    private String typesId;
    private String content;
    private String category;
    private Date nextTime;
    private String businessIds;
    private String contactsIds;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Long createUserId;
    private String batchId;
    private Integer taskId;
    private String taskName;
    private Integer isEvent;
    private Integer isTask;


    @Transient
    public Integer getIsEvent() {
        return isEvent;
    }

    public void setIsEvent(Integer isEvent) {
        this.isEvent = isEvent;
    }

    @Transient
    public Integer getIsTask() {
        return isTask;
    }

    public void setIsTask(Integer isTask) {
        this.isTask = isTask;
    }

    @Transient
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Id
    @Column(name = "record_id")
    @GeneratedValue
    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
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
    @Column(name = "types")
    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    @Basic
    @Column(name = "types_id")
    public String getTypesId() {
        return typesId;
    }

    public void setTypesId(String typesId) {
        this.typesId = typesId;
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
    @Column(name = "category")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Basic
    @Column(name = "next_time")
    public Date getNextTime() {
        return nextTime;
    }

    public void setNextTime(Date nextTime) {
        this.nextTime = nextTime;
    }

    @Basic
    @Column(name = "business_ids")
    public String getBusinessIds() {
        return businessIds;
    }

    public void setBusinessIds(String businessIds) {
        this.businessIds = businessIds;
    }

    @Basic
    @Column(name = "contacts_ids")
    public String getContactsIds() {
        return contactsIds;
    }

    public void setContactsIds(String contactsIds) {
        this.contactsIds = contactsIds;
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
    @Column(name = "create_user_id")
    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
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
    @Column(name = "task_id")
    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmAdminRecordEntity that = (LkCrmAdminRecordEntity) o;
        return recordId == that.recordId &&
                typesId == that.typesId &&
                createUserId == that.createUserId &&
                Objects.equals(types, that.types) &&
                Objects.equals(content, that.content) &&
                Objects.equals(category, that.category) &&
                Objects.equals(nextTime, that.nextTime) &&
                Objects.equals(businessIds, that.businessIds) &&
                Objects.equals(contactsIds, that.contactsIds) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(batchId, that.batchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId, types, typesId, content, category, nextTime, businessIds, contactsIds, createTime, updateTime, createUserId, batchId);
    }
}
