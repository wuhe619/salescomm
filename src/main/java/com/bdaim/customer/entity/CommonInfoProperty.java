package com.bdaim.customer.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;


@Entity
@Table(name = "t_common_info_property", schema = "", catalog = "")
@IdClass(CommonInfoPropertyPK.class)
public class CommonInfoProperty {

    private Long zid;

    private String serviceCode;

    private String propertyName;

    private String propertyValue;

    private Date createTime;

    @Basic
    @Column(name = "create_time")
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "property_value")
    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    @Id
    @Column(name = "zid")
    public Long getZid() {
        return zid;
    }

    public void setZid(Long zid) {
        this.zid = zid;
    }

    @Id
    @Basic
    @Column(name = "service_code")
    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    @Id
    @Basic
    @Column(name = "property_name")
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommonInfoProperty that = (CommonInfoProperty) o;
        return Objects.equals(zid, that.zid) &&
                Objects.equals(serviceCode, that.serviceCode) &&
                Objects.equals(propertyName, that.propertyName) &&
                Objects.equals(propertyValue, that.propertyValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zid, serviceCode, propertyName, propertyValue);
    }
}
