package com.bdaim.rbac.entity;

import javax.persistence.*;

import com.bdaim.resource.entity.UserRoleRelDOPK;

import java.sql.Date;

/**
 * Created by Mr.YinXin on 2017/3/28.
 */
@Entity
@Table(name = "t_user_role_rel", schema = "", catalog = "")
@IdClass(UserRoleRelDOPK.class)
public class UserRoleRelDO {
    private String id;
    private long role;
    private Integer level;
    private String optuser;
    private Date createTime;

    @Id
    @Column(name = "ID")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Id
    @Column(name = "ROLE")
    public long getRole() {
        return role;
    }

    public void setRole(long role) {
        this.role = role;
    }

    @Basic
    @Column(name = "LEVEL")
    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    @Basic
    @Column(name = "OPTUSER")
    public String getOptuser() {
        return optuser;
    }

    public void setOptuser(String optuser) {
        this.optuser = optuser;
    }

    @Basic
    @Column(name = "CREATE_TIME")
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserRoleRelDO that = (UserRoleRelDO) o;

        if (role != that.role) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (level != null ? !level.equals(that.level) : that.level != null) return false;
        if (optuser != null ? !optuser.equals(that.optuser) : that.optuser != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (role ^ (role >>> 32));
        result = 31 * result + (level != null ? level.hashCode() : 0);
        result = 31 * result + (optuser != null ? optuser.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        return result;
    }
}
