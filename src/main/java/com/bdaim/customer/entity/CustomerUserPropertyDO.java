package com.bdaim.customer.entity;

import javax.persistence.*;

/**
 * @author duanliying
 * @date 2018/9/19
 * @description 坐席信息
 */
/*@Entity
@Table(name = "t_customer_user_property")
@IdClass(CustomerUserPropertyPK.class)*/
public class CustomerUserPropertyDO {


    //用户id
    @Id
    @Column(name = "user_id")
    private String userId;
    //属性Key
    @Id
    @Column(name = "property_name")
    private String propertyName;
    //属性值（坐席账号+密码+分机号）
    @Column(name = "property_value")
    private String propertyValue;
    @Column(name = "create_time")
    private String createTime;

    public CustomerUserPropertyDO(String userId, String propertyName, String propertyValue, String createTime) {
        this.userId = userId;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.createTime = createTime;
    }

    public CustomerUserPropertyDO() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "CustomerUserPropertyDO{" +
                ", userId='" + userId + '\'' +
                ", propertyName='" + propertyName + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
