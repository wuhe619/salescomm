package com.bdaim.rbac.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author duanliying
 * @date 2019/3/13
 * @description
 */
@Entity
@Table(name = "t_role", schema = "", catalog = "")
public class RoleEntity implements Serializable {
    //部门id
    private Long id;
    //部门名字
    private String name;
    //操作用户
    private String optUser;
    //创建时间
    private Timestamp createTime;
    //修改时间
    private Timestamp modifyTime;
    //类型
    private Integer type;
    //部门id
    private Long deptId;

    @Id
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "NAME")

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "OPTUSER")

    public String getOptuser() {
        return optUser;
    }

    public void setOptuser(String optuser) {
        this.optUser = optuser;
    }

    @Basic
    @Column(name = "CREATE_TIME")
    public Timestamp getCreate_time() {
        return createTime;
    }

    public void setCreate_time(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "MODIFY_TIME")
    public Timestamp getModify_time() {
        return modifyTime;
    }

    public void setModify_time(Timestamp modify_time) {
        this.modifyTime = modify_time;
    }

    @Basic
    @Column(name = "TYPE")

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Basic
    @Column(name = "DEPTID")

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public RoleEntity() {
    }

    @Override
    public String toString() {
        return "RoleEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", optUser='" + optUser + '\'' +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", type=" + type +
                ", deptId=" + deptId +
                '}';
    }
}
