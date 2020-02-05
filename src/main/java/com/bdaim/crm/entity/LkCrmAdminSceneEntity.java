package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_admin_scene", schema = "", catalog = "")
public class LkCrmAdminSceneEntity {
    private Integer sceneId;
    private Integer type;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Integer getSceneId() {
        return sceneId;
    }

    public LkCrmAdminSceneEntity setSceneId(Integer sceneId) {
        this.sceneId = sceneId;
        return this;
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
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public LkCrmAdminSceneEntity setName(String name) {
        this.name = name;
        return this;
    }

    @Basic
    @Column(name = "user_id")
    public long getUserId() {
        return userId;
    }

    public LkCrmAdminSceneEntity setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    @Basic
    @Column(name = "sort")
    public int getSort() {
        return sort;
    }

    public LkCrmAdminSceneEntity setSort(int sort) {
        this.sort = sort;
        return this;
    }

    @Basic
    @Column(name = "data")
    public String getData() {
        return data;
    }

    public LkCrmAdminSceneEntity setData(String data) {
        this.data = data;
        return this;
    }

    @Basic
    @Column(name = "is_hide")
    public int getIsHide() {
        return isHide;
    }

    public LkCrmAdminSceneEntity setIsHide(int isHide) {
        this.isHide = isHide;
        return this;
    }

    @Basic
    @Column(name = "is_system")
    public int getIsSystem() {
        return isSystem;
    }

    public LkCrmAdminSceneEntity setIsSystem(int isSystem) {
        this.isSystem = isSystem;
        return this;
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

    public LkCrmAdminSceneEntity setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
        return this;
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
