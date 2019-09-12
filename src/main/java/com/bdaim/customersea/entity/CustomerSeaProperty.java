package com.bdaim.customersea.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2019/6/19
 * @description
 */
@Entity
@Table(name = "t_customer_sea_property", schema = "", catalog = "")
@IdClass(CustomerSeaPropertyPK.class)
public class CustomerSeaProperty {
    private String customerSeaId;
    private String propertyName;
    private String propertyValue;
    private Timestamp createTime;

    public CustomerSeaProperty() {
    }

    public CustomerSeaProperty(long customerSeaId, String propertyName, String propertyValue, Timestamp createTime) {
        this.customerSeaId = String.valueOf(customerSeaId);
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.createTime = createTime;
    }

    @Id
    @Column(name = "customer_sea_id")
    public String getCustomerSeaId() {
        return customerSeaId;
    }

    public void setCustomerSeaId(String customerSeaId) {
        this.customerSeaId = customerSeaId;
    }

    @Id
    @Column(name = "property_name")
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Basic
    @Column(name = "property_value")
    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
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
        CustomerSeaProperty that = (CustomerSeaProperty) o;
        return Objects.equals(customerSeaId, that.customerSeaId) &&
                Objects.equals(propertyName, that.propertyName) &&
                Objects.equals(propertyValue, that.propertyValue) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(customerSeaId, propertyName, propertyValue, createTime);
    }

    @Override
    public String toString() {
        return "CustomerSeaProperty{" +
                "customerSeaId='" + customerSeaId + '\'' +
                ", propertyName='" + propertyName + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
