package com.bdaim.common.util;

import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {
	//配置文件的路径，项目所有需要手动读取的配置参数均保存在config.properties中(还有一些其他配置文件，但其他配置文件都是不需要手动读取的)
	private static String configFilePath="config.properties";
	
	private static Properties prop = new Properties();
	private static final ConfigUtil instance=new ConfigUtil();
	
	private  ConfigUtil() {
		init(configFilePath);
	}
	
	public static ConfigUtil getInstance(){
		return instance;
	}
	
	private void init(String fileName){
		try {
			InputStream in=this.getClass().getClassLoader().getResourceAsStream(fileName);
			prop.load(in);
		} catch (Exception e) {
			LogUtil.error("解读properties文件异常:",e);
		}
	}
	
	public String get(String key){
		return prop.getProperty(key).toString().trim();
	}
	
	public String get(String key, String defaultValue){
		try {
			return prop.getProperty(key).toString().trim();
		}
		catch (Exception ex) {
			return defaultValue;
		}
	}
	
	public int getInteger(String key){
		return Integer.valueOf(prop.getProperty(key).toString().trim());
	}
	
	public int getInteger(String key, int defaultValue){
		try {
			return Integer.valueOf(prop.getProperty(key).toString().trim());
		}
		catch (Exception ex) {
			return defaultValue;
		}
	}
	
	public boolean getBoolean(String key){
		return "true".equals(prop.getProperty(key).toString().trim().toLowerCase()) ? true : false;
	}
	
	public boolean getBoolean(String key, boolean defaultValue){
		try {
			return "true".equals(prop.getProperty(key).toString().trim().toLowerCase()) ? true : false;
		}
		catch (Exception ex) {
			return defaultValue;
		}
	}
	
	//测试方法
	public static void main(String[] args) {
		String value =ConfigUtil.getInstance().get("asd", "sd");
		System.out.println(value);
	}
}
