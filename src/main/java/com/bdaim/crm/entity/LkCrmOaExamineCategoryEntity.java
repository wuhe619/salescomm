package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_oa_examine_category", schema = "crm", catalog = "")
public class LkCrmOaExamineCategoryEntity {
    private int categoryId;
    private String title;
    private String remarks;
    private Integer type;
    private Integer createUserId;
    private Integer status;
    private Integer isSys;
    private Integer examineType;
    private String userIds;
    private String deptIds;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Integer isDeleted;
    private Timestamp deleteTime;
    private Integer deleteUserId;

    @Id
    @Column(name = "category_id")
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
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
    @Column(name = "remarks")
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
    @Column(name = "create_user_id")
    public Integer getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Integer createUserId) {
        this.createUserId = createUserId;
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
    @Column(name = "is_sys")
    public Integer getIsSys() {
        return isSys;
    }

    public void setIsSys(Integer isSys) {
        this.isSys = isSys;
    }

    @Basic
    @Column(name = "examine_type")
    public Integer getExamineType() {
        return examineType;
    }

    public void setExamineType(Integer examineType) {
        this.examineType = examineType;
    }

    @Basic
    @Column(name = "user_ids")
    public String getUserIds() {
        return userIds;
    }

    public void setUserIds(String userIds) {
        this.userIds = userIds;
    }

    @Basic
    @Column(name = "dept_ids")
    public String getDeptIds() {
        return deptIds;
    }

    public void setDeptIds(String deptIds) {
        this.deptIds = deptIds;
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
    @Column(name = "is_deleted")
    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
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
    @Column(name = "delete_user_id")
    public Integer getDeleteUserId() {
        return deleteUserId;
    }

    public void setDeleteUserId(Integer deleteUserId) {
        this.deleteUserId = deleteUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmOaExamineCategoryEntity that = (LkCrmOaExamineCategoryEntity) o;
        return categoryId == that.categoryId &&
                Objects.equals(title, that.title) &&
                Objects.equals(remarks, that.remarks) &&
                Objects.equals(type, that.type) &&
                Objects.equals(createUserId, that.createUserId) &&
                Objects.equals(status, that.status) &&
                Objects.equals(isSys, that.isSys) &&
                Objects.equals(examineType, that.examineType) &&
                Objects.equals(userIds, that.userIds) &&
                Objects.equals(deptIds, that.deptIds) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(isDeleted, that.isDeleted) &&
                Objects.equals(deleteTime, that.deleteTime) &&
                Objects.equals(deleteUserId, that.deleteUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId, title, remarks, type, createUserId, status, isSys, examineType, userIds, deptIds, createTime, updateTime, isDeleted, deleteTime, deleteUserId);
    }
}
