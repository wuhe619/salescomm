package com.bdaim.common.cache;


import com.bdaim.common.helper.BeanHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 读取一些配置信息
 */
public class ConfigReader {
    public static String DATA_SOURCE = "mrp.DataSourceManager";
    public static String USER_MANAGER = "mrp.UserManager";
    public static String CACHE_INSTANCE="mrp.CacheInstance";
    protected static Map<String, Object> conf = new HashMap<String, Object>();

    static {
        //读取配置文件
        String defaultConfName = "default-mrp.properties";
        String userConfName = "mrp-config.properties";
        InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream(defaultConfName);
        Properties defaultP = new Properties();
        try {
            defaultP.load(is);

            pop(defaultP, conf);
            defaultP.clear();
            defaultP.load(ConfigReader.class.getClassLoader().getResourceAsStream(userConfName));
            pop(defaultP, conf);
            BeanCache.add(DATA_SOURCE, BeanHelper.getInstance(conf.get(DATA_SOURCE).toString()));
            BeanCache.add(USER_MANAGER, BeanHelper.getInstance(conf.get(USER_MANAGER).toString()));
//            if (conf.get(CACHE_INSTANCE)!=null&&conf.get(CACHE_INSTANCE).toString()==null||conf.get(CACHE_INSTANCE).toString().equals(""))
//                BeanCache.add(CACHE_INSTANCE,BeanHelper.getInstance(conf.get(CACHE_INSTANCE).toString()));
//            else
//                BeanCache.add(CACHE_INSTANCE,BeanHelper.getInstance(DefaultDataCache.class.getName()));
            //这里会初始化BeanCache中的一些Bean
            BeanCache.init();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }

    private static void pop(Properties from, Map to) {
        Enumeration<Object> es = from.keys();
        while (es.hasMoreElements()) {
            String key = es.nextElement().toString();
            if ("".equals(key)) continue;
            to.put(key, from.get(key));
        }
    }

    public static Object getConf(String key) {
        return conf.get(key);
    }

}
