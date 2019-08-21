package com.bdaim.customgroup.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2019/2/20
 * @description
 */
@Entity
@Table(name = "customer_group_property")
@IdClass(CustomerGroupPropertyPK.class)
public class CustomerGroupPropertyDO {
    private int customerGroupId;
    private String propertyName;
    private String propertyValue;
    private Timestamp createTime;

    public CustomerGroupPropertyDO() {
    }

    public CustomerGroupPropertyDO(int customerGroupId, String propertyName, String propertyValue, Timestamp createTime) {
        this.customerGroupId = customerGroupId;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.createTime = createTime;
    }

    @Id
    @Column(name = "customer_group_id")
    public int getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(int customerGroupId) {
        this.customerGroupId = customerGroupId;
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
        CustomerGroupPropertyDO that = (CustomerGroupPropertyDO) o;
        return customerGroupId == that.customerGroupId &&
                Objects.equals(propertyName, that.propertyName) &&
                Objects.equals(propertyValue, that.propertyValue) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(customerGroupId, propertyName, propertyValue, createTime);
    }
}
