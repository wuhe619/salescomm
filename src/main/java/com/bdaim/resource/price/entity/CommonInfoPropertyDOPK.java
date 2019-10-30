package com.bdaim.resource.price.entity;

import java.io.Serializable;


public class CommonInfoPropertyDOPK implements Serializable {
    private Long zid;
    private String serviceCode;
    private String propertyName;

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
}
