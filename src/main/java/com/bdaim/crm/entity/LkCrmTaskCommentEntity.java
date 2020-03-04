package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_task_comment", schema = "", catalog = "")
public class LkCrmTaskCommentEntity {
    private Integer commentId;
    private int userId;
    private String content;
    private Timestamp createTime;
    private Integer mainId;
    private Integer pid;
    private Integer status;
    private Integer typeId;
    private Integer favour;
    private Integer type;

    @Id
    @Column(name = "comment_id")
    @GeneratedValue
    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
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
    @Column(name = "main_id")
    public Integer getMainId() {
        return mainId;
    }

    public void setMainId(Integer mainId) {
        this.mainId = mainId;
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
    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "type_id")
    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    @Basic
    @Column(name = "favour")
    public Integer getFavour() {
        return favour;
    }

    public void setFavour(Integer favour) {
        this.favour = favour;
    }

    @Basic
    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmTaskCommentEntity that = (LkCrmTaskCommentEntity) o;
        return commentId == that.commentId &&
                userId == that.userId &&
                Objects.equals(content, that.content) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(mainId, that.mainId) &&
                Objects.equals(pid, that.pid) &&
                Objects.equals(status, that.status) &&
                Objects.equals(typeId, that.typeId) &&
                Objects.equals(favour, that.favour) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentId, userId, content, createTime, mainId, pid, status, typeId, favour, type);
    }
}
