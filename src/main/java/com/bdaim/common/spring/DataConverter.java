package com.bdaim.common.spring;

import com.bdaim.common.dao.SimpleHibernateDao;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @description:
 * @auther: Chacker
 * @date: 2019/8/2 12:19
 */
@Component
public class DataConverter extends SimpleHibernateDao {

    /**
     * 将Object对象里面的属性和值转化成Map对象
     *
     * @param obj
     * @return
     * @throws IllegalAccessException
     */
    public static Map<String, Object> objectToMap(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<String, Object>();
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = String.valueOf(field.get(obj));
            map.put(fieldName, value);
        }
        return map;
    }

    public static List<Map<String, Object>> objectListToMap(List<Object> objList) throws IllegalAccessException {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Object object : objList) {
            Map<String, Object> map = new HashMap<String, Object>();
            Class<?> clazz = object.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = String.valueOf(field.get(object));
                map.put(fieldName, value);
            }
            resultList.add(map);
        }
        return resultList;
    }
}
