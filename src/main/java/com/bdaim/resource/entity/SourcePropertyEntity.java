package com.bdaim.resource.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author duanliying
 * @date 2018/10/26
 * @description 成本价格
 */
@Entity
@Table(name = "t_source_property", schema = "", catalog = "")
public class SourcePropertyEntity implements Serializable {
    private String sourceId;
    private String propertyKey;
    private String propertyValue;

    @Id
    @Column(name = "source_id")
    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }



    @Id
    @Column(name = "property_key")
    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    @Basic
    @Column(name = "property_value")
    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    @Override
    public String toString() {
        return "SourcePropertyEntity{" +
                "sourceId='" + sourceId + '\'' +
                ", propertyKey='" + propertyKey + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                '}';
    }
}
