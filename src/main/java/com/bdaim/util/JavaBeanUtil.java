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
}
