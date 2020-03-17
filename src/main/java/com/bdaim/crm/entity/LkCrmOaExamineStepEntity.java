package com.bdaim.crm.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "lkcrm_oa_examine_step", schema = "", catalog = "")
public class LkCrmOaExamineStepEntity {
    private long stepId;
    private Integer stepType;
    private int categoryId;
    private String checkUserId;
    private Integer stepNum;
    private Timestamp createTime;

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
    @Column(name = "category_id")
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmOaExamineStepEntity that = (LkCrmOaExamineStepEntity) o;
        return stepId == that.stepId &&
                categoryId == that.categoryId &&
                Objects.equals(stepType, that.stepType) &&
                Objects.equals(checkUserId, that.checkUserId) &&
                Objects.equals(stepNum, that.stepNum) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepId, stepType, categoryId, checkUserId, stepNum, createTime);
    }
}
