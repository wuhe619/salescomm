package com.bdaim.common.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/** 这个是默认的 一种实现方式，将一些数据缓存的Cache中，同时支持正则删除，这样也是为了更好的实现当前业务
 */
public class DefaultDataCache implements DataCache {
    private static Map<String,Object> map=new HashMap<String, Object>();
    @Override
    public void insert(String key, Object value) {
        map.put(key,value);
    }

    @Override
    public void delete(String key) {
        map.remove(key);
    }

    @Override
    public void regexDel(String regex) {
        Iterator<String> it=map.keySet().iterator();
        Pattern pattern=Pattern.compile(regex);
        String key="";
        while (it.hasNext()){
            key=it.next();
            if (pattern.matcher(key).matches())map.remove(key);
        }

    }
}
