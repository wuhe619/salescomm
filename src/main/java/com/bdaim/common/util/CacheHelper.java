package com.bdaim.common.util;

import java.io.Serializable;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.log4j.Logger;

public class CacheHelper {
	private static String CACHE_DEFAULT_NAME = "default_cache";
	private static CacheManager manager = null;
	private static Logger log = Logger.getLogger(CacheHelper.class);
	static {
		manager = CacheManager.newInstance();
		CacheConfiguration config = new CacheConfiguration();
		config.setName(CacheHelper.CACHE_DEFAULT_NAME);
		config.setTimeToIdleSeconds(15 * 60);
		config.setTimeToLiveSeconds(30 * 60);
		config.setMaxEntriesLocalHeap(10000);
		config.setMaxEntriesLocalDisk(1000000);
		Cache c = new Cache(config);
		manager.addCache(c);
	}

	/**
	 * 添加缓存
	 * 
	 * @param name
	 * @param config
	 */
	public static Cache setCache(String name) {
		CacheConfiguration config = new CacheConfiguration();
		config.setName(name);
		config.setTimeToIdleSeconds(60 * 60);
		config.setTimeToLiveSeconds(60 * 60 * 24);
		config.setMaxEntriesLocalHeap(10000);
		config.setMaxEntriesLocalDisk(1000000);
		return setCache(config);
	}

	/**
	 * 添加缓存
	 * 
	 * @param name
	 * @param config
	 */
	public static Cache setCache(CacheConfiguration config) {
		if (checkIsNull())
			return null;
		Cache cache = null;
		if (config == null)
			return null;
		String name = config.getName();
		if (name.equalsIgnoreCase(CacheHelper.CACHE_DEFAULT_NAME))
			return manager.getCache(CacheHelper.CACHE_DEFAULT_NAME);
		if (containsCache(name)) {
			manager.removeCache(name);
		}
		cache = new Cache(config);
		manager.addCache(cache);
		return cache;
	}

	public static boolean containsCache(String name) {
		if (checkIsNull())
			return false;
		if (name == null)
			return false;
		return manager.cacheExists(name);
	}

	/**
	 * 获取缓存
	 * 
	 * @param name
	 * @return
	 */
	public static Cache getCache(String name) {
		if (checkIsNull())
			return null;
		Cache cache = null;
		if (containsCache(name)) {
			cache = manager.getCache(name);
		} else {
			cache = setCache(name);
		}
		return cache;
	}

	/**
	 * 设置缓存
	 * 
	 * @param cacheName
	 * @param key
	 * @param value
	 */
	public static void setValue(String cacheName, String key, Serializable value) {
		Cache cache = getCache(cacheName);
		if (cache == null)
			return;
		Element element = new Element(key, value);
		cache.put(element);
	}

	/**
	 * 查询缓存结果
	 * 
	 * @param cacheName
	 * @param key
	 * @return
	 */
	public static Object getValue(String cacheName, String key) {
		Cache cache = getCache(cacheName);
		if (cache == null)
			return null;
		Element e = cache.get(key);
		return e == null ? null : e.getObjectValue();
	}
	
	public static Object getValue(String key) {
		return getValue(CacheHelper.CACHE_DEFAULT_NAME, key);
	}
	
	/**
	 * 设置到默认缓存
	 * 
	 * @param key
	 * @param value
	 */
	public static void setValue(String key, Serializable value) {
		setValue(CacheHelper.CACHE_DEFAULT_NAME, key, value);
	}

	/**
	 * 获取缓存结果
	 * 
	 * @param key
	 * @return
	 */
	public static String getStringValue(String key) {
		Object obj = getValue(CacheHelper.CACHE_DEFAULT_NAME, key);
		return obj == null ? null : obj.toString();
	}

	/**
	 * 获取缓存key
	 * 
	 * @param key
	 * @return
	 */
	public static List<String> getKeys(String cacheName) {
		if (cacheName == null)
			cacheName = CACHE_DEFAULT_NAME;
		Cache cache = getCache(cacheName);
		return cache.getKeys();
	}

	public static boolean containsKey(String name) {
		Cache cache = getCache(CACHE_DEFAULT_NAME);
		if (cache == null)
			return false;
		return cache.isKeyInCache(name);
	}

	/**
	 * 删除以字符串开头的缓存
	 * 
	 * @param pre
	 * @return
	 */
	public static synchronized boolean removePreKey(String pre) {
		Cache cache = getCache(CACHE_DEFAULT_NAME);
		if (cache == null)
			return false;
		for (String key : getKeys(CACHE_DEFAULT_NAME)) {
			if (key.startsWith(pre))
				cache.remove(key);
		}
		return true;
	}

	public static boolean checkIsNull() {
		if (manager == null) {
			log.error("cache manager is null!");
			return true;
		} else
			return false;
	}

	@Override
	protected void finalize() throws Throwable {
		if (manager != null) {
			manager.removeCache(CACHE_DEFAULT_NAME);
			manager.shutdown();
			log.info("cache manager shutdown.");
		}
		super.finalize();
	}

}
