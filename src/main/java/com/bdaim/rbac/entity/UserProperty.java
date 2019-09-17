package com.bdaim.rbac.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @description 企业属性信息
 */
@Entity
@Table(name = "t_user_property")
@IdClass(UserPropertyPK.class)
public class UserProperty {
    @Id
    @Column(name = "user_id")
    private Long userId;
    //属性名
    @Id
    @Column(name = "property_name")
    private String propertyName;
    //属性值
    @Column(name = "property_value")
    private String propertyValue;

    @Column(name = "create_time")
    private Timestamp createTime;

    public UserProperty(Long userId, String propertyName, String propertyValue, Timestamp createTime) {
        this.userId = userId;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.createTime = createTime;
    }

    public UserProperty() {
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
