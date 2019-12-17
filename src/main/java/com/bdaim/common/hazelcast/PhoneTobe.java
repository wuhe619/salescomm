package com.bdaim.common.hazelcast;


import com.bdaim.AppConfig;

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
            	   tobe = new ToBeRegisterDB(AppConfig.getHazelcast_HOST());
               }    
            }    
        }    
        return tobe;   
    }  
}
