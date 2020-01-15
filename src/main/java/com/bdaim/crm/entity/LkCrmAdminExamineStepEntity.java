package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_admin_examine_step", schema = "", catalog = "")
public class LkCrmAdminExamineStepEntity {
    private long stepId;
    private Integer stepType;
    private int examineId;
    private String checkUserId;
    private Integer stepNum;
    private Timestamp createTime;
    private String remarks;

    @Id
    @Column(name = "step_id")
    public long getStepId() {
        return stepId;
    }

    public void setStepId(long stepId) {
        this.stepId = stepId;
    }

    @Basic
    @Column(name = "step_type")
    public Integer getStepType() {
        return stepType;
    }

    public void setStepType(Integer stepType) {
        this.stepType = stepType;
    }

    @Basic
    @Column(name = "examine_id")
    public int getExamineId() {
        return examineId;
    }

    public void setExamineId(int examineId) {
        this.examineId = examineId;
    }

    @Basic
    @Column(name = "check_user_id")
    public String getCheckUserId() {
        return checkUserId;
    }

    public void setCheckUserId(String checkUserId) {
        this.checkUserId = checkUserId;
    }

    @Basic
    @Column(name = "step_num")
    public Integer getStepNum() {
        return stepNum;
    }

    public void setStepNum(Integer stepNum) {
        this.stepNum = stepNum;
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
        LkCrmAdminExamineStepEntity that = (LkCrmAdminExamineStepEntity) o;
        return stepId == that.stepId &&
                examineId == that.examineId &&
                Objects.equals(stepType, that.stepType) &&
                Objects.equals(checkUserId, that.checkUserId) &&
                Objects.equals(stepNum, that.stepNum) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(remarks, that.remarks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepId, stepType, examineId, checkUserId, stepNum, createTime, remarks);
    }
}
