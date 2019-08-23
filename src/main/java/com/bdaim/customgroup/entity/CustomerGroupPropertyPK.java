package com.bdaim.customgroup.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2019/2/20
 * @description
 */
public class CustomerGroupPropertyPK implements Serializable {
    private int customerGroupId;
    private String propertyName;

    @Column(name = "customer_group_id")
    @Id
    public int getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(int customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    @Column(name = "property_name")
    @Id
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
        CustomerGroupPropertyPK that = (CustomerGroupPropertyPK) o;
        return customerGroupId == that.customerGroupId &&
                Objects.equals(propertyName, that.propertyName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(customerGroupId, propertyName);
    }
}
