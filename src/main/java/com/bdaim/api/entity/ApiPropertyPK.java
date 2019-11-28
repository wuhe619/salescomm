package com.bdaim.api.entity;

import java.io.Serializable;

public class ApiPropertyPK implements Serializable {
    private String apiId;
    private String propertyName;

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getPropertyName() {
        return propertyName;
    }
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
