package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_admin_examine_record", schema = "crm", catalog = "")
public class LkCrmAdminExamineRecordEntity {
    private int recordId;
    private Integer examineId;
    private Long examineStepId;
    private Integer examineStatus;
    private Long createUser;
    private Timestamp createTime;
    private String remarks;

    @Id
    @Column(name = "record_id")
    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    @Basic
    @Column(name = "examine_id")
    public Integer getExamineId() {
        return examineId;
    }

    public void setExamineId(Integer examineId) {
        this.examineId = examineId;
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
    @Column(name = "remarks")
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmAdminExamineRecordEntity that = (LkCrmAdminExamineRecordEntity) o;
        return recordId == that.recordId &&
                Objects.equals(examineId, that.examineId) &&
                Objects.equals(examineStepId, that.examineStepId) &&
                Objects.equals(examineStatus, that.examineStatus) &&
                Objects.equals(createUser, that.createUser) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(remarks, that.remarks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId, examineId, examineStepId, examineStatus, createUser, createTime, remarks);
    }
}
