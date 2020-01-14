package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_admin_scene", schema = "crm", catalog = "")
public class LkCrmAdminSceneEntity {
    private int sceneId;
    private int type;
    private String name;
    private long userId;
    private int sort;
    private String data;
    private int isHide;
    private int isSystem;
    private String bydata;
    private Timestamp createTime;
    private Timestamp updateTime;

    @Id
    @Column(name = "scene_id")
    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
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
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "user_id")
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "sort")
    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    @Basic
    @Column(name = "data")
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Basic
    @Column(name = "is_hide")
    public int getIsHide() {
        return isHide;
    }

    public void setIsHide(int isHide) {
        this.isHide = isHide;
    }

    @Basic
    @Column(name = "is_system")
    public int getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(int isSystem) {
        this.isSystem = isSystem;
    }

    @Basic
    @Column(name = "bydata")
    public String getBydata() {
        return bydata;
    }

    public void setBydata(String bydata) {
        this.bydata = bydata;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmAdminSceneEntity that = (LkCrmAdminSceneEntity) o;
        return sceneId == that.sceneId &&
                type == that.type &&
                userId == that.userId &&
                sort == that.sort &&
                isHide == that.isHide &&
                isSystem == that.isSystem &&
                Objects.equals(name, that.name) &&
                Objects.equals(data, that.data) &&
                Objects.equals(bydata, that.bydata) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sceneId, type, name, userId, sort, data, isHide, isSystem, bydata, createTime, updateTime);
    }
}
