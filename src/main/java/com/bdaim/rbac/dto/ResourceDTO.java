package com.bdaim.rbac.dto;

import java.sql.Date;

/**
 * @author chengning@salescomm.net
 * @date 2019/3/21
 * @description
 */
public class ResourceDTO {
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Integer getSn() {
        return sn;
    }

    public void setSn(Integer sn) {
        this.sn = sn;
    }

    public String getOptuser() {
        return optuser;
    }

    public void setOptuser(String optuser) {
        this.optuser = optuser;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "ResourceDTO{" +
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
