package com.bdaim.common.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

	private static Properties properties;
	
	static{
		properties = new Properties();
		try {
			InputStream in = PropertiesUtil.class.getClassLoader().getResourceAsStream("config.properties");
			properties.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Properties getProperties() {
		return properties;
	}
	
	public static Integer getIntegerValue(String key){
		return Integer.parseInt(properties.getProperty(key));
	}
	
	public static Integer getIntegerValue(String key, int defaultValue){
		try {
			return Integer.parseInt(properties.getProperty(key));
		} catch (Exception ex) {
			return defaultValue;
		}
	}
	
	public static String getStringValue(String key){
		return (String)properties.getProperty(key);
	}
	
	public static boolean getBooleanValue(String key){
		return Boolean.parseBoolean(properties.getProperty(key));
	}
	
	public static boolean getBooleanValue(String key, boolean defaultValue){
		try {
			return Boolean.parseBoolean(properties.getProperty(key));
		} catch (Exception ex) {
			return defaultValue;
		}
	}
	
	public static Object getValue(String key){
		return properties.getProperty(key);
	}
	
	public static void main(String[] args)
	{
		System.out.println(PropertiesUtil.getStringValue("KeyGeneratorRule"));
		
	}
	
}
