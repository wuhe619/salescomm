package com.bdaim.crm.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_admin_user_role", schema = "", catalog = "")
public class LkCrmAdminUserRoleEntity {
    private int id;
    private long userId;
    private int roleId;

    @Id
    @Column(name = "id")
    @GeneratedValue
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
    @Column(name = "role_id")
    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    private String userIds;

    @Transient
    public String getUserIds() {
        return userIds;
    }

    public void setUserIds(String userIds) {
        this.userIds = userIds;
    }

    private String roleIds;

    @Transient
    public String getRoleIds(){
        return roleIds;
    }

    public void setRoleIds(String roleIds){
        this.roleIds = roleIds;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmAdminUserRoleEntity that = (LkCrmAdminUserRoleEntity) o;
        return id == that.id &&
                userId == that.userId &&
                roleId == that.roleId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, roleId);
    }
}
