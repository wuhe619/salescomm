package com.bdaim.common.cache;

import java.util.HashMap;
import java.util.Map;

/** 这个主要是完成Bean的实例化缓存
 */
public class BeanCache {
    private static Map<String,Object> beanMap=null;
    static {
        beanMap=new HashMap<String, Object>();
        try {
            Class.forName(ConfigReader.class.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void add(String key,Object o){
        beanMap.put(key,o);
    }

    public static void remove(String key){
        beanMap.remove(key);
    }

    public static  <T> T getBean(String key,Class<T> tClass){
        return (T)beanMap.get(key);
    }

    public static Object getBean(String key){
        return beanMap.get(key);
    }

    public static void addClassInstance(String className) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        Class cl=Class.forName(className);
        beanMap.put(className,cl.newInstance());
    }

    public static <T> T getClassInstance(Class<T> tClass){
        return (T) beanMap.get(tClass.getName());
    }

    public static void init(){

    }
}
