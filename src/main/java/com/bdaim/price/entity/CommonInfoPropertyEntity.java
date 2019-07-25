package com.bdaim.price.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author duanliying
 * @date 2019/3/18
 * @description
 */
@Entity
@Table(name = "t_common_info_property", schema = "", catalog = "")
@IdClass(CommonInfoPropertyDOPK.class)
public class CommonInfoPropertyEntity {
    @Id
    @Column(name = "zid")
    private Long zid;

    @Id
    @Column(name = "service_code")
    private String serviceCode;
    @Id
    @Column(name = "property_name")
    private String propertyName;

    @Basic
    @Column(name = "property_value")
    private String propertyValue;
    @Basic
    @Column(name = "create_time")
    private Timestamp createTime;

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public Long getZid() {
        return zid;
    }

    public void setZid(Long zid) {
        this.zid = zid;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

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
        CommonInfoPropertyEntity that = (CommonInfoPropertyEntity) o;
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
