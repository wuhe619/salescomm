package com.bdaim.crm.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_admin_scene_default", schema = "", catalog = "")
public class LkCrmAdminSceneDefaultEntity {
    private int defaultId;
    private int type;
    private long userId;
    private int sceneId;

    @Id
    @Column(name = "default_id")
    public int getDefaultId() {
        return defaultId;
    }

    public void setDefaultId(int defaultId) {
        this.defaultId = defaultId;
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
    @Column(name = "user_id")
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "scene_id")
    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmAdminSceneDefaultEntity that = (LkCrmAdminSceneDefaultEntity) o;
        return defaultId == that.defaultId &&
                type == that.type &&
                userId == that.userId &&
                sceneId == that.sceneId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultId, type, userId, sceneId);
    }
}