package com.bdaim.customer.entity;

import java.io.Serializable;

/**
 * 
 */
public class CustomerPropertyPK implements Serializable {
    private String custId;
    private String propertyName;
	public String getCustId() {
		return custId;
	}
	public void setCustId(String custId) {
		this.custId = custId;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

    
    
}
