package com.bdaim.common.util.spring;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.bdaim.common.util.DESUtils;

public class EncryptPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
private String[] encryptPropNames = { "jdbc.user", "jdbc.pass" };
 
    @Override
    protected String convertProperty(String propertyName, String propertyValue) {
        if (isEncryptProp(propertyName)) {
            String decryptValue = null;
			try {
				decryptValue = DESUtils.decrypt(propertyValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
            return decryptValue;
        } else {
            return propertyValue;
        }
    }
 
    /**
     * 判断是否是加密的属性
     * 
     * @param propertyName
     * @return
     */
    private boolean isEncryptProp(String propertyName) {
        for (String encryptpropertyName : encryptPropNames) {
            if (encryptpropertyName.equals(propertyName))
                return true;
        }
        return false;
    }
}