package com.bdaim.crm.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "lkcrm_oa_examine_log", schema = "", catalog = "")
public class LkCrmOaExamineLogEntity {
    private long logId;
    private Integer recordId;
    private Long examineStepId;
    private Integer examineStatus;
    private Long createUser;
    private Timestamp createTime;
    private Long examineUser;
    private Timestamp examineTime;
    private String remarks;
    private Integer isRecheck;
    private Integer orderId;

    @Id
    @Column(name = "log_id")
    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    @Basic
    @Column(name = "record_id")
    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    @Basic
    @Column(name = "examine_step_id")
    public Long getExamineStepId() {
        return examineStepId;
    }

    public void setExamineStepId(Long examineStepId) {
        this.examineStepId = examineStepId;
    }

    @Basic
    @Column(name = "examine_status")
    public Integer getExamineStatus() {
        return examineStatus;
    }

    public void setExamineStatus(Integer examineStatus) {
        this.examineStatus = examineStatus;
    }

    @Basic
    @Column(name = "create_user")
    public Long getCreateUser() {
        return createUser;
    }

    public void setCreateUser(Long createUser) {
        this.createUser = createUser;
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
    @Column(name = "examine_user")
    public Long getExamineUser() {
        return examineUser;
    }

    public void setExamineUser(Long examineUser) {
        this.examineUser = examineUser;
    }

    @Basic
    @Column(name = "examine_time")
    public Timestamp getExamineTime() {
        return examineTime;
    }

    public void setExamineTime(Timestamp examineTime) {
        this.examineTime = examineTime;
    }

    @Basic
    @Column(name = "remarks")
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Basic
    @Column(name = "is_recheck")
    public Integer getIsRecheck() {
        return isRecheck;
    }

    public void setIsRecheck(Integer isRecheck) {
        this.isRecheck = isRecheck;
    }

    @Basic
    @Column(name = "order_id")
    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmOaExamineLogEntity that = (LkCrmOaExamineLogEntity) o;
        return logId == that.logId &&
                Objects.equals(recordId, that.recordId) &&
                Objects.equals(examineStepId, that.examineStepId) &&
                Objects.equals(examineStatus, that.examineStatus) &&
                Objects.equals(createUser, that.createUser) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(examineUser, that.examineUser) &&
                Objects.equals(examineTime, that.examineTime) &&
                Objects.equals(remarks, that.remarks) &&
                Objects.equals(isRecheck, that.isRecheck) &&
                Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId, recordId, examineStepId, examineStatus, createUser, createTime, examineUser, examineTime, remarks, isRecheck, orderId);
    }
}
