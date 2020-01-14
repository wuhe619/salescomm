package com.bdaim.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaBeanUtil {
    public static Map<String, Object> convertBeanToMap(Object bean) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        Class type = bean.getClass();
        Map<String, Object> returnMap = new HashMap<>();
        BeanInfo beanInfo = Introspector.getBeanInfo(type);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();
            if (!propertyName.equals("class")) {
                Method readMethod = descriptor.getReadMethod();
                Object result = readMethod.invoke(bean, new Object[0]);
                if (result != null) {
                    returnMap.put(propertyName, result);
                } else {
                    returnMap.put(propertyName, "");
                }
            }
        }
        return returnMap;
    }

    public static List<Map<String, Object>> convertJsonObjectToMapList(List data) throws IllegalAccessException {
        List<Map<String, Object>> list = new ArrayList<>(16);
        List<Map<String, Object>> obj = new ArrayList<>();
        for (Object o : data) {
            if (o instanceof List) {
                obj.addAll((List) o);
            } else {
                obj.add((Map<String, Object>) o);
            }
        }
        for (Object o : obj) {
            Class<?> clazz = o.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                if (!"map".equalsIgnoreCase(fieldName)) {
                    continue;
                }
                Object value = field.get(o);
                list.add((Map<String, Object>) value);
            }

        }

        return list;
    }

    /**
     * 为object中的所有String属性去除空格字符
     *
     * @param object
     * @return
     */
    public static Object replaceBlankSpace(Object object) {
        if (object == null) {
            return null;
        }
        //获取该类中所有的域(属性)
        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {
            //对所有的属性判断是否为String类型
            if (field.getType().equals(String.class)) {
                //将私有属性设置为可访问状态
                field.setAccessible(true);
                try {
                    String string = (String) field.get(object);
                    if(StringUtil.isNotEmpty(string)){
                        //将所有的空格字符用""替换
                        string = string.replaceAll(" ", "");
                    }
                    //相当于调用了set方法设置属性
                    field.set(object, string);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return object;
    }
}
