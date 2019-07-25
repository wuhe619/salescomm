package com.bdaim.resource.entity;

import javax.persistence.*;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author duanliying
 * @date 2018/12/28
 * @description
 */
@Entity
@Table(name = "t_market_resource_property", schema = "", catalog = "")
@IdClass(ResourcePropertyDOPK.class)
public class ResourcePropertyEntity {
    private Integer resourceId;
    private String propertyName;
    private String propertyValue;
    private Timestamp createTime;

    public ResourcePropertyEntity(Integer resourceId, String propertyName, String propertyValue, Timestamp createTime) {
        this.resourceId = resourceId;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.createTime = createTime;
    }

    public ResourcePropertyEntity() {
    }

    @Id
    @Column(name = "resource_id")
    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
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
    public String toString() {
        return "ResourcePropertyEntity{" +
                "resourceId=" + resourceId +
                ", propertyName='" + propertyName + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
