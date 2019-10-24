package com.bdaim.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.lang.reflect.InvocationTargetException;


/**
 * log4j日志Util
 */
public class LogUtil {
	private static Logger logger=null;
	private static final String clazzName=LogUtil.class.getName();
	
	static{
		//获取该类的调用者(调用者类名)
		StackTraceElement[] stackTraceElements=new Throwable().getStackTrace();
		String callerClassName=stackTraceElements[1].getClassName();
		//用调用者类名初始化logger,保证日志输出的类是调用者类名
		logger=LoggerFactory.getLogger(callerClassName);
	}
	
	private LogUtil(){}
	
	//---------------------error---------------------------------
	public static void error(String msg){
		//必须用这种方法调用才能获取调用者的正确代码行号
		logger.error(clazzName, Level.ERROR, msg, null);
	}
	
	public static void error(Throwable e){
		if(e instanceof InvocationTargetException){
			e=((InvocationTargetException) e).getTargetException();
		}
		error("Exception: "+e.toString());
		for(int i=0;i<e.getStackTrace().length;i++){
			error(e.getStackTrace()[i].toString());
		}
	}
	
	public static void error(String msg,Throwable e){
		error(msg);
		if(e instanceof InvocationTargetException){
			e=((InvocationTargetException) e).getTargetException();
		}
		error("Exception: "+e.toString());
		for(int i=0;i<e.getStackTrace().length;i++){
			error(e.getStackTrace()[i].toString());
		}
	}
	
	//---------------------warn----------------------------------
	public static void warn(String msg){
		//必须用这种方法调用才能获取调用者的正确代码行号
		logger.warn(clazzName, Level.WARN, msg, null);
	}
	
	//---------------------info----------------------------------
	public static void info(String msg){
		//必须用这种方法调用才能获取调用者的正确代码行号
		logger.info(clazzName, Level.INFO, msg, null);
	}
	
	
}
