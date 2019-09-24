package com.bdaim.rbac.entity;

import java.io.Serializable;

/**
 * 
 */
public class UserPropertyPK implements Serializable {
    private Long userId;
    private String propertyName;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

    
    
}
