package com.bdaim.common.helper;

/** 这里主要是完成Bean的一些处理
 */
public class BeanHelper {

    public static Object getInstance(String claaName){
        try {
            Class rsClass=Class.forName(claaName);
            return rsClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
