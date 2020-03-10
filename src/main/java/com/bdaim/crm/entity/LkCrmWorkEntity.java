package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_work", schema = "", catalog = "")
public class LkCrmWorkEntity {
    private Integer workId;
    private String name;
    private String custId;
    private Integer status;
    private Timestamp createTime;
    private Long createUserId;
    private String description;
    private String color;
    private Integer isOpen;
    private String ownerUserId;
    private Timestamp archiveTime;
    private Timestamp deleteTime;
    private String batchId;

    @Id
    @Column(name = "work_id")
    @GeneratedValue
    public Integer getWorkId() {
        return workId;
    }

    public void setWorkId(Integer workId) {
        this.workId = workId;
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
    @Column(name = "create_user_id")
    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    @Basic
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
    @Column(name = "is_open")
    public Integer getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(Integer isOpen) {
        this.isOpen = isOpen;
    }

    @Basic
    @Column(name = "owner_user_id")
    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    @Basic
    @Column(name = "archive_time")
    public Timestamp getArchiveTime() {
        return archiveTime;
    }

    public void setArchiveTime(Timestamp archiveTime) {
        this.archiveTime = archiveTime;
    }

    @Basic
    @Column(name = "delete_time")
    public Timestamp getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Timestamp deleteTime) {
        this.deleteTime = deleteTime;
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
        LkCrmWorkEntity that = (LkCrmWorkEntity) o;
        return workId == that.workId &&
                Objects.equals(name, that.name) &&
                Objects.equals(status, that.status) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(createUserId, that.createUserId) &&
                Objects.equals(description, that.description) &&
                Objects.equals(color, that.color) &&
                Objects.equals(isOpen, that.isOpen) &&
                Objects.equals(ownerUserId, that.ownerUserId) &&
                Objects.equals(archiveTime, that.archiveTime) &&
                Objects.equals(deleteTime, that.deleteTime) &&
                Objects.equals(batchId, that.batchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId, name, status, createTime, createUserId, description, color, isOpen, ownerUserId, archiveTime, deleteTime, batchId);
    }
}
