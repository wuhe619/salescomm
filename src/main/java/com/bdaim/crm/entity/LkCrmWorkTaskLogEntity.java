package com.bdaim.crm.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "lkcrm_work_task_log", schema = "", catalog = "")
public class LkCrmWorkTaskLogEntity {
    private Integer logId;
    private Long userId;
    private String content;
    private Timestamp createTime;
    private Integer status;
    private Integer taskId;
    private Integer workId;

    @Id
    @Column(name = "log_id")
    @GeneratedValue
    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    @Basic
    @Column(name = "user_id")
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
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
    @Column(name = "task_id")
    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    @Basic
    @Column(name = "work_id")
    public Integer getWorkId() {
        return workId;
    }

    public void setWorkId(Integer workId) {
        this.workId = workId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmWorkTaskLogEntity that = (LkCrmWorkTaskLogEntity) o;
        return logId == that.logId &&
                userId == that.userId &&
                Objects.equals(content, that.content) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(status, that.status) &&
                Objects.equals(taskId, that.taskId) &&
                Objects.equals(workId, that.workId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId, userId, content, createTime, status, taskId, workId);
    }
}
