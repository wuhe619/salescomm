package com.bdaim.common.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 字典
 */
@Entity
@Table(name = "t_dic", schema = "", catalog = "")
public class Dic implements Serializable {

    private Long id;
    //名称
    private String name;

    //类别id
    private String dicTypeId;

    private Date createTime;
    private Date lastUpdateTime;
    private String createUser;
    private String lastUpdateUser;
    private Integer status;

    @Id
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "create_time")
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "create_user")
    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    @Basic
    @Column(name = "last_update_user")
    public String getLastUpdateUser() {
        return lastUpdateUser;
    }

    public void setLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }


    @Basic
    @Column(name = "last_update_time")
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
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
    @Column(name = "dic_type_id")
    public String getDicTypeId() {
        return dicTypeId;
    }

    public void setDicTypeId(String dicTypeId) {
        this.dicTypeId = dicTypeId;
    }

    @Basic
    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dic dic = (Dic) o;
        return Objects.equals(id, dic.id) &&
                Objects.equals(name, dic.name) &&
                Objects.equals(dicTypeId, dic.dicTypeId) &&
                Objects.equals(createTime, dic.createTime) &&
                Objects.equals(lastUpdateTime, dic.lastUpdateTime) &&
                Objects.equals(createUser, dic.createUser) &&
                Objects.equals(lastUpdateUser, dic.lastUpdateUser) &&
                Objects.equals(status, dic.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dicTypeId, createTime, lastUpdateTime, createUser, lastUpdateUser, status);
    }

    @Override
    public String toString() {
        return "Dic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dicTypeId='" + dicTypeId + '\'' +
                ", createTime=" + createTime +
                ", lastUpdateTime=" + lastUpdateTime +
                ", createUser='" + createUser + '\'' +
                ", lastUpdateUser='" + lastUpdateUser + '\'' +
                ", status=" + status +
                '}';
    }
}
