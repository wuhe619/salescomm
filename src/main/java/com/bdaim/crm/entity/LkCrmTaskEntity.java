package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_task", schema = "", catalog = "")
public class LkCrmTaskEntity {
    private Integer taskId;
    private String custId;
    private String name;
    private Long createUserId;
    private Long mainUserId;
    private String ownerUserId;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Integer status;
    private Integer classId;
    private String labelId;
    private String description;
    private Integer pid;
    private Timestamp startTime;
    private Timestamp stopTime;
    private Integer priority;
    private Integer workId;
    private Integer isTop;
    private Integer isOpen;
    private Integer orderNum;
    private Integer topOrderNum;
    private Timestamp archiveTime;
    private Integer ishidden;
    private Timestamp hiddenTime;
    private String batchId;
    private Integer isArchive;

    @Id
    @Column(name = "task_id")
    @GeneratedValue
    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
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
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    @Column(name = "main_user_id")
    public Long getMainUserId() {
        return mainUserId;
    }

    public void setMainUserId(Long mainUserId) {
        this.mainUserId = mainUserId;
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
    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "class_id")
    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    @Basic
    @Column(name = "label_id")
    public String getLabelId() {
        return labelId;
    }

    public void setLabelId(String labelId) {
        this.labelId = labelId;
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
    @Column(name = "pid")
    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    @Basic
    @Column(name = "start_time")
    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "stop_time")
    public Timestamp getStopTime() {
        return stopTime;
    }

    public void setStopTime(Timestamp stopTime) {
        this.stopTime = stopTime;
    }

    @Basic
    @Column(name = "priority")
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Basic
    @Column(name = "work_id")
    public Integer getWorkId() {
        return workId;
    }

    public void setWorkId(Integer workId) {
        this.workId = workId;
    }

    @Basic
    @Column(name = "is_top")
    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
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
    @Column(name = "order_num")
    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    @Basic
    @Column(name = "top_order_num")
    public Integer getTopOrderNum() {
        return topOrderNum;
    }

    public void setTopOrderNum(Integer topOrderNum) {
        this.topOrderNum = topOrderNum;
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
    @Column(name = "ishidden")
    public Integer getIshidden() {
        return ishidden;
    }

    public void setIshidden(Integer ishidden) {
        this.ishidden = ishidden;
    }

    @Basic
    @Column(name = "hidden_time")
    public Timestamp getHiddenTime() {
        return hiddenTime;
    }

    public void setHiddenTime(Timestamp hiddenTime) {
        this.hiddenTime = hiddenTime;
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
    @Column(name = "is_archive")
    public Integer getIsArchive() {
        return isArchive;
    }

    public void setIsArchive(Integer isArchive) {
        this.isArchive = isArchive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmTaskEntity that = (LkCrmTaskEntity) o;
        return taskId == that.taskId &&
                Objects.equals(name, that.name) &&
                Objects.equals(createUserId, that.createUserId) &&
                Objects.equals(mainUserId, that.mainUserId) &&
                Objects.equals(ownerUserId, that.ownerUserId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(status, that.status) &&
                Objects.equals(classId, that.classId) &&
                Objects.equals(labelId, that.labelId) &&
                Objects.equals(description, that.description) &&
                Objects.equals(pid, that.pid) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(stopTime, that.stopTime) &&
                Objects.equals(priority, that.priority) &&
                Objects.equals(workId, that.workId) &&
                Objects.equals(isTop, that.isTop) &&
                Objects.equals(isOpen, that.isOpen) &&
                Objects.equals(orderNum, that.orderNum) &&
                Objects.equals(topOrderNum, that.topOrderNum) &&
                Objects.equals(archiveTime, that.archiveTime) &&
                Objects.equals(ishidden, that.ishidden) &&
                Objects.equals(hiddenTime, that.hiddenTime) &&
                Objects.equals(batchId, that.batchId) &&
                Objects.equals(isArchive, that.isArchive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, name, createUserId, mainUserId, ownerUserId, createTime, updateTime, status, classId, labelId, description, pid, startTime, stopTime, priority, workId, isTop, isOpen, orderNum, topOrderNum, archiveTime, ishidden, hiddenTime, batchId, isArchive);
    }
}
