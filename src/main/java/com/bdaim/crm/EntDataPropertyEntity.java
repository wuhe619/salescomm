package com.bdaim.crm;

/**
 * 企业基本信息属性
 */
public class EntDataPropertyEntity {

    private Long id;
    private String propertyName;
    private String propertyValue;

    public EntDataPropertyEntity() {
    }

    public EntDataPropertyEntity(Long id, String propertyName, String propertyValue) {
        this.id = id;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }
}
