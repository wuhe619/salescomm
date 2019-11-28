package com.bdaim.api.entity;

import com.bdaim.customer.entity.CustomerPropertyPK;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "t_customer_property")
@IdClass(ApiPropertyPK.class)
public class ApiProperty {
    @Id
    @Column(name = "api_id")
    private String api_id;
    //属性名
    @Id
    @Column(name = "property_name")
    private String propertyName;
    //属性值
    @Column(name = "property_value")
    private String propertyValue;

    @Column(name = "create_time")
    private Timestamp createTime;

    public String getApi_id() {
        return api_id;
    }

    public void setApi_id(String api_id) {
        this.api_id = api_id;
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

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
