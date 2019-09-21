package com.bdaim.common.util;

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
    public static Map<String,Object> convertBeanToMap(Object bean) throws IntrospectionException,IllegalAccessException, InvocationTargetException {
        Class type = bean.getClass();
        Map<String,Object> returnMap = new HashMap<>();
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

    public static List<Map<String, Object>> convertBeanToMapList(List obj) throws IllegalAccessException {
        List<Map<String, Object>> list = new ArrayList<>(16);
        for (Object o : obj) {
            Map<String, Object> map = new HashMap<>();
            Class<?> clazz = o.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                /*
                 * Returns the value of the field represented by this {@code Field}, on the
                 * specified object. The value is automatically wrapped in an object if it
                 * has a primitive type.
                 * 注:返回对象该该属性的属性值，如果该属性的基本类型，那么自动转换为包装类
                 */
                Object value = field.get(o);
                map.put(fieldName, value);
            }
            list.add(map);
        }

        return list;
    }
}
