package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_oa_announcement", schema = "", catalog = "")
public class LkCrmOaAnnouncementEntity {
    private int announcementId;
    private String title;
    private String content;
    private Integer createUserId;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Timestamp startTime;
    private Timestamp endTime;
    private String deptIds;
    private String ownerUserIds;
    private String readUserIds;

    @Id
    @Column(name = "announcement_id")
    public int getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(int announcementId) {
        this.announcementId = announcementId;
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
    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
    @Column(name = "start_time")
    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "end_time")
    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
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
    @Column(name = "owner_user_ids")
    public String getOwnerUserIds() {
        return ownerUserIds;
    }

    public void setOwnerUserIds(String ownerUserIds) {
        this.ownerUserIds = ownerUserIds;
    }

    @Basic
    @Column(name = "read_user_ids")
    public String getReadUserIds() {
        return readUserIds;
    }

    public void setReadUserIds(String readUserIds) {
        this.readUserIds = readUserIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmOaAnnouncementEntity that = (LkCrmOaAnnouncementEntity) o;
        return announcementId == that.announcementId &&
                Objects.equals(title, that.title) &&
                Objects.equals(content, that.content) &&
                Objects.equals(createUserId, that.createUserId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(deptIds, that.deptIds) &&
                Objects.equals(ownerUserIds, that.ownerUserIds) &&
                Objects.equals(readUserIds, that.readUserIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(announcementId, title, content, createUserId, createTime, updateTime, startTime, endTime, deptIds, ownerUserIds, readUserIds);
    }
}
