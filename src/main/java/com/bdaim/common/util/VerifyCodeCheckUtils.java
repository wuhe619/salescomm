package com.bdaim.common.util;

import com.bdaim.slxf.util.hazelcast.PhoneTobe;
import com.bdaim.slxf.util.hazelcast.ToBeRegisterDB;
import com.bdaim.slxf.util.hazelcast.UserIDNotFoundException;

/**
 * Created by Mr.YinXin on 2017/2/27.
 */
public class VerifyCodeCheckUtils {
    /**
     *
     * @param identifyValue
     * @param code
     * @return
     * @throws UserIDNotFoundException
     */
    public static boolean isSuccess(String identifyValue,String code) throws UserIDNotFoundException {

        ToBeRegisterDB tobe = PhoneTobe.getInstance();
        if(code.equals(tobe.getVerCodeFromMap(identifyValue))){
            return true;
        }else {
            return false;
        }
    }
}
