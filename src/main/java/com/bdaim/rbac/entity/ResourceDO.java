package com.bdaim.rbac.entity;

import javax.persistence.*;
import java.sql.Date;

/**
 * @author chengning@salescomm.net
 * @date 2019/3/21
 * @description
 */
@Entity
@Table(name = "t_resource", schema = "", catalog = "")
public class ResourceDO {
    private long id;
    private String uri;
    private String name;
    private int type;
    private String remark;
    private Long pid;
    private Integer sn;
    private String optuser;
    private Date createTime;
    private Date modifyTime;
    private Integer sort;
    private Integer platform;

    @Id
    @Column(name = "ID")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "URI")
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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
    @Column(name = "TYPE")
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Basic
    @Column(name = "REMARK")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Basic
    @Column(name = "PID")
    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    @Basic
    @Column(name = "SN")
    public Integer getSn() {
        return sn;
    }

    public void setSn(Integer sn) {
        this.sn = sn;
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

    @Basic
    @Column(name = "MODIFY_TIME")
    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Basic
    @Column(name = "SORT")
    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @Basic
    @Column(name = "platform")
    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "ResourceDO{" +
                "id=" + id +
                ", uri='" + uri + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", remark='" + remark + '\'' +
                ", pid=" + pid +
                ", sn=" + sn +
                ", optuser='" + optuser + '\'' +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", sort=" + sort +
                ", platform=" + platform +
                '}';
    }
}
