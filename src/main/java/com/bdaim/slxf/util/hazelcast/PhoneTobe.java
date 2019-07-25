package com.bdaim.slxf.util.hazelcast;

import com.bdaim.common.util.PropertiesUtil;

/**
 * 获取Hazelcast服务器
 * 日期：2017/2/21
 * @author lich@bdcsdk.com
 *
 */
public class PhoneTobe {
	private static ToBeRegisterDB tobe = null;
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
