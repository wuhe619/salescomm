package com.bdaim.common.cache;

/** 缓存的实现接口，用户可以自己实现，如果没有实现，将采用默认实现
 * 默认的实现是将数据缓存到Java对象中，如果数据量大，请选用其实的缓存方式
 */
public interface DataCache {

    public void insert(String key, Object value);

    public void delete(String key);

    public void regexDel(String regex);

}
