package com.bdaim.customer.dto;

import com.bdaim.customer.entity.CustomerProperty;

import java.sql.Timestamp;

/**
 *  企业属性信息
 *
 * @author chengning@salescomm.net
 * @date 2019/4/23 10:53
 */
public class CustomerPropertyDTO {
    private String custId;
    private String propertyName;
    private String propertyValue;
    private Timestamp createTime;

    public CustomerPropertyDTO(CustomerProperty cp) {
        this.custId = cp.getCustId();
        this.propertyName = cp.getPropertyName();
        this.propertyValue = cp.getPropertyValue();
        this.createTime = cp.getCreateTime();
    }

    public CustomerPropertyDTO() {
    }
    public CustomerPropertyDTO(String custId, String propertyName, String propertyValue) {
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

    @Override
    public String toString() {
        return "CustomerPropertyDTO{" +
                "custId='" + custId + '\'' +
                ", propertyName='" + propertyName + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
