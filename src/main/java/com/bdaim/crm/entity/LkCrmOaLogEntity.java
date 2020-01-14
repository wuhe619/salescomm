package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_oa_log", schema = "crm", catalog = "")
public class LkCrmOaLogEntity {
    private int logId;
    private int categoryId;
    private String title;
    private String content;
    private String tomorrow;
    private String question;
    private int createUserId;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String sendUserIds;
    private String sendDeptIds;
    private String readUserIds;
    private String batchId;

    @Id
    @Column(name = "log_id")
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
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
    @Column(name = "tomorrow")
    public String getTomorrow() {
        return tomorrow;
    }

    public void setTomorrow(String tomorrow) {
        this.tomorrow = tomorrow;
    }

    @Basic
    @Column(name = "question")
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    @Basic
    @Column(name = "create_user_id")
    public int getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(int createUserId) {
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
    @Column(name = "send_user_ids")
    public String getSendUserIds() {
        return sendUserIds;
    }

    public void setSendUserIds(String sendUserIds) {
        this.sendUserIds = sendUserIds;
    }

    @Basic
    @Column(name = "send_dept_ids")
    public String getSendDeptIds() {
        return sendDeptIds;
    }

    public void setSendDeptIds(String sendDeptIds) {
        this.sendDeptIds = sendDeptIds;
    }

    @Basic
    @Column(name = "read_user_ids")
    public String getReadUserIds() {
        return readUserIds;
    }

    public void setReadUserIds(String readUserIds) {
        this.readUserIds = readUserIds;
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
        LkCrmOaLogEntity that = (LkCrmOaLogEntity) o;
        return logId == that.logId &&
                categoryId == that.categoryId &&
                createUserId == that.createUserId &&
                Objects.equals(title, that.title) &&
                Objects.equals(content, that.content) &&
                Objects.equals(tomorrow, that.tomorrow) &&
                Objects.equals(question, that.question) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(sendUserIds, that.sendUserIds) &&
                Objects.equals(sendDeptIds, that.sendDeptIds) &&
                Objects.equals(readUserIds, that.readUserIds) &&
                Objects.equals(batchId, that.batchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId, categoryId, title, content, tomorrow, question, createUserId, createTime, updateTime, sendUserIds, sendDeptIds, readUserIds, batchId);
    }
}
