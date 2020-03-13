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
    private String custId;
    private long userId;
    private int sort;
    private String data;
    private int isHide;
    private int isSystem;
    private String bydata;
    private Timestamp createTime;
    private Timestamp updateTime;

    private Integer isDefault;
    private String noHideIds;
    private String hideIds;

    @Transient
    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    @Transient
    public String getNoHideIds() {
        return noHideIds;
    }

    public void setNoHideIds(String noHideIds) {
        this.noHideIds = noHideIds;
    }

    @Transient
    public String getHideIds() {
        return hideIds;
    }

    public void setHideIds(String hideIds) {
        this.hideIds = hideIds;
    }

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

    public LkCrmAdminSceneEntity setType(Integer type) {
        this.type = type;
        return this;
    }

    @Basic
    @Column(name = "cust_id")
    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
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
