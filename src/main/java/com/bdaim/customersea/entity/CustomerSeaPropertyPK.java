package com.bdaim.customersea.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2019/6/19
 * @description
 */
public class CustomerSeaPropertyPK implements Serializable {
    private String customerSeaId;
    private String propertyName;

    @Column(name = "customer_sea_id")
    @Id
    public String getCustomerSeaId() {
        return customerSeaId;
    }

    public void setCustomerSeaId(String customerSeaId) {
        this.customerSeaId = customerSeaId;
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
        CustomerSeaPropertyPK that = (CustomerSeaPropertyPK) o;
        return Objects.equals(customerSeaId, that.customerSeaId) &&
                Objects.equals(propertyName, that.propertyName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(customerSeaId, propertyName);
    }
}
