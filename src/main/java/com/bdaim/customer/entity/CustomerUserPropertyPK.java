package com.bdaim.customer.entity;

import java.io.Serializable;

/**
 * 
 */
public class CustomerUserPropertyPK implements Serializable {
    private String userId;
    private String propertyName;
	
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

    
    
}
