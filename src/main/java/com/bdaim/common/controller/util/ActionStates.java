package com.bdaim.common.controller.util;

import net.sf.json.JSONObject;

/**
 * 
 */
public class ActionStates {
    public static JSONObject SUCCESS_JSON=null;
    public static JSONObject FAIL_JSON=null;

    static {
        SUCCESS_JSON=new JSONObject();
        FAIL_JSON=new JSONObject();
        SUCCESS_JSON.put("result","success");
        FAIL_JSON.put("result","fail");
    }
}