package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_oa_action_record", schema = "", catalog = "")
public class LkCrmOaActionRecordEntity {
    private int logId;
    private int userId;
    private int type;
    private Integer actionId;
    private Timestamp createTime;
    private String joinUserIds;
    private String deptIds;
    private String content;

    @Id
    @Column(name = "log_id")
    @GeneratedValue
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    @Basic
    @Column(name = "user_id")
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "type")
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Basic
    @Column(name = "action_id")
    public Integer getActionId() {
        return actionId;
    }

    public void setActionId(Integer actionId) {
        this.actionId = actionId;
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
    @Column(name = "join_user_ids")
    public String getJoinUserIds() {
        return joinUserIds;
    }

    public void setJoinUserIds(String joinUserIds) {
        this.joinUserIds = joinUserIds;
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
    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmOaActionRecordEntity that = (LkCrmOaActionRecordEntity) o;
        return logId == that.logId &&
                userId == that.userId &&
                type == that.type &&
                Objects.equals(actionId, that.actionId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(joinUserIds, that.joinUserIds) &&
                Objects.equals(deptIds, that.deptIds) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId, userId, type, actionId, createTime, joinUserIds, deptIds, content);
    }
}
