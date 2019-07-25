package com.bdaim.rbac.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author duanliying
 * @date 2019/3/13
 * 部分表实例类
 */
@Entity
@Table(name = "t_dept", schema = "", catalog = "")
public class DeptEntity implements Serializable {
    //部门id
    private Long id;
    //部门名字
    private String name;
    //操作用户
    private String optuser;
    //创建时间
    private Timestamp createTime;
    //修改时间
    private Timestamp modifyTime;
    //类型
    private Integer type;

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
        return optuser;
    }

    public void setOptuser(String optuser) {
        this.optuser = optuser;
    }

    @Basic
    @Column(name = "CREATE_TIME")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "MODIFY_TIME")
    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Basic
    @Column(name = "TYPE")

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }


    public DeptEntity() {
    }

    @Override
    public String toString() {
        return "DeptEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", optuser='" + optuser + '\'' +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", type=" + type +
                '}';
    }
}
