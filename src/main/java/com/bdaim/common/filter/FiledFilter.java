package com.bdaim.common.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyPreFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.label.entity.LabelInfo;
import com.bdaim.rbac.entity.User;

import java.util.HashMap;
import java.util.Map;

public class FiledFilter implements PropertyPreFilter {
          
    private Map<Class<?>, String[]> includes = new HashMap<Class<?>, String[]>();
    private Map<Class<?>, String[]> excludes = new HashMap<Class<?>, String[]>();
    static {
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
    }
    private Map<Class<?>,String[]> defaultFilterMap = new HashMap<Class<?>, String[]>();
    public FiledFilter() {
    	defaultFilterMap.put(LabelInfo.class, new String[]{"parent","parentClass","parentCategory","labelSignature","childrenCategory"});
    	defaultFilterMap.put(User.class, new String[]{"createCategory","modifyCategory","applys","audits","devs","createCustomGroups","updateCustomGroups","createLabels","updateLabels","offlineLabels","department","userLabelCategoryLs"});
    	defaultFilterMap.put(CustomGroup.class, new String[]{"labels"});

    	this.excludes = defaultFilterMap;
    }
          
    public FiledFilter(Map<Class<?>, String[]> includes) {
        super();
        this.includes = includes;
    }
          
    public boolean apply(JSONSerializer serializer, Object source, String name) {
              
        //对象为空。直接放行
        if (source == null) {
            return true;
        }
              
        // 获取当前需要序列化的对象的类对象
        Class<?> clazz = source.getClass();
              
        // 无需序列的对象、寻找需要过滤的对象，可以提高查找层级
        // 找到不需要的序列化的类型
        for (Map.Entry<Class<?>, String[]> item : this.excludes.entrySet()) {
            // isAssignableFrom()，用来判断类型间是否有继承关系
            if (item.getKey().isAssignableFrom(clazz)) {
                String[] strs = item.getValue();
                      
                // 该类型下 此 name 值无需序列化
                if (isHave(strs, name)) {
                    return false;
                }
            }
        }
              
        // 需要序列的对象集合为空 表示 全部需要序列化
        if (this.includes.isEmpty()) {
            return true;
        }
              
        // 需要序列的对象
        // 找到不需要的序列化的类型
        for (Map.Entry<Class<?>, String[]> item : this.includes.entrySet()) {
            // isAssignableFrom()，用来判断类型间是否有继承关系
            if (item.getKey().isAssignableFrom(clazz)) {
                String[] strs = item.getValue();
                // 该类型下 此 name 值无需序列化
                if (isHave(strs, name)) {
                    return true;
                }
            }
        }
              
        return false;
    }
          
    /*
     * 此方法有两个参数，第一个是要查找的字符串数组，第二个是要查找的字符或字符串
     */
    public static boolean isHave(String[] strs, String s) {
              
        for (int i = 0; i < strs.length; i++) {
            // 循环查找字符串数组中的每个字符串中是否包含所有查找的内容
            if (strs[i].equals(s)) {
                // 查找到了就返回真，不在继续查询
                return true;
            }
        }
              
        // 没找到返回false
        return false;
    }
          
    public Map<Class<?>, String[]> getIncludes() {
        return includes;
    }
          
    public void setIncludes(Map<Class<?>, String[]> includes) {
        this.includes = includes;
    }
          
    public Map<Class<?>, String[]> getExcludes() {
        return excludes;
    }
          
    public void setExcludes(Map<Class<?>, String[]> excludes) {
        this.excludes = excludes;
    }
          
}