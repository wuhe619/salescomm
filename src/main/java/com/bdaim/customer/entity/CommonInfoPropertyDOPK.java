package com.bdaim.customer.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;


public class CommonInfoPropertyDOPK implements Serializable {
    private Long zid;
    private String serviceCode;
    private String propertyName;

    @Column(name = "zid")
    @Id
    public Long getZid() {
        return zid;
    }

    public void setZid(Long zid) {
        this.zid = zid;
    }
    @Column(name = "service_code")
    @Id
    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    @Column(name = "property_name")
    @Id
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
