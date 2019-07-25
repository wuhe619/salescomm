package com.bdaim.customer.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @description 企业属性信息
 */
@Entity
@Table(name = "t_customer_property")
@IdClass(CustomerPropertyPK.class)
public class CustomerProperty {
	////pay_password=支付密码    remain_amount=余额  
    //企业id
	@Id
    @Column(name = "cust_id")
    private String custId;
    //属性名
	@Id
    @Column(name = "property_name")
    private String propertyName;
    //属性值
    @Column(name = "property_value")
    private String propertyValue;
    
    @Column(name = "create_time")
    private Timestamp createTime;

    public CustomerProperty(String custId, String propertyName, String propertyValue, Timestamp createTime) {
        this.custId = custId;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.createTime = createTime;
    }

    public CustomerProperty() {
    }
    public CustomerProperty(String custId, String propertyName, String propertyValue) {
    	this.custId = custId;
    	this.propertyName = propertyName;
    	this.propertyValue = propertyValue;
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

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}


}
