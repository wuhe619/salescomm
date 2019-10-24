package com.bdaim.common.util.hazelcast;


import com.bdaim.util.PropertiesUtil;

/**
 * 获取Hazelcast服务器
 *
 */
public class PhoneTobe {
	private static  ToBeRegisterDB tobe = null;
    public static ToBeRegisterDB getInstance() {  
        if (tobe == null) {    
            synchronized (PhoneTobe.class) {    
               if (tobe == null) {    
            	   tobe = new ToBeRegisterDB(PropertiesUtil.getStringValue("hazelcast_HOST"));
               }    
            }    
        }    
        return tobe;   
    }  
}
