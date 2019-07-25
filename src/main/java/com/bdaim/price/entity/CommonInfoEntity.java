package com.bdaim.price.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * 定价记录表
 *
 * @author duanliying
 * @date 2019/3/18
 * @description
 */
@Entity
@Table(name = "t_common_info", schema = "", catalog = "")
public class CommonInfoEntity {
    private Long id;
    private String serviceCode;
    private String serviceDesc;
    private Integer status;
    private Timestamp createTime;

    @Id
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "service_code")
    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    @Basic
    @Column(name = "STATUS")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "service_desc")
    public String getServiceDesc() {
        return serviceDesc;
    }

    public void setServiceDesc(String serviceDesc) {
        this.serviceDesc = serviceDesc;
    }

    @Basic
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommonInfoEntity that = (CommonInfoEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(serviceCode, that.serviceCode) &&
                Objects.equals(serviceDesc, that.serviceDesc) &&
                Objects.equals(status, that.status) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, serviceCode, serviceDesc, status, createTime);
    }
}

