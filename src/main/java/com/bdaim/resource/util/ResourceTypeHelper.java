package com.bdaim.resource.util;


import com.bdaim.common.util.spring.ConfigPropertiesHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class ResourceTypeHelper {

    public static List<Map<String,String>> getResourceList(){
        String str=ConfigPropertiesHolder.getConf("app.resourcetype").toString();
        List<Map<String,String>> list=new ArrayList<Map<String, String>>();
        Map<String,String> item=null;
        for (String type:str.split(";")){
            String[] name=type.split(":");
            item=new HashMap<String, String>();
            item.put("name",name[0]);
            item.put("value",name[1]);
            list.add(item);
        }
        return list;
    }
}
