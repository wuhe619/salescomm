package com.bdaim.common.util.OperLog;



import com.bdaim.common.util.PropertiesUtil;
import com.bdaim.log.entity.OperLog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class OperlogUtils
{
	private static OperlogUtils olutils = null;
	
	public synchronized static OperlogUtils getInstance()
	{
		if (null == olutils)
		{
			//read config from file
			int queueSize = PropertiesUtil.getIntegerValue("oper_log_queue_size", 1000);
			int threadNum = PropertiesUtil.getIntegerValue("oper_log_thread_num", 2);
			int insertTimeoutMs = PropertiesUtil.getIntegerValue("oper_log_insert_timeout", 1000);
			olutils = new OperlogUtils(queueSize, threadNum, insertTimeoutMs);
		}
		return olutils;
	}
	
	BlockingQueue<Runnable> logqueue = new LinkedBlockingQueue<Runnable>();  
	private ThreadPoolExecutor executor = null;
	
	public static boolean isEnable() {
		return PropertiesUtil.getBooleanValue("is_enable_oper_log", false);
	}

	private OperlogUtils(int queueSize, int threadNum, int insertTimeoutMs)
	{
		logqueue = new LinkedBlockingQueue(queueSize);
		executor = new ThreadPoolExecutor(threadNum, threadNum * 2, 60, TimeUnit.SECONDS, logqueue);
	}
	
	public void insertLog(OperLog value)
	{
		//future no use
		try
		{
			executor.submit(new OperlogRunnable(value));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public int getQueueSize()
	{
		return logqueue.size();
	}
	
	public synchronized void terminate()
	{
		try
		{
			executor.shutdownNow();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		try
		{
			logqueue.clear();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public synchronized static void terminateUtils()
	{
		olutils.terminate();
		olutils = null;
	}
	
	public static void main(String[] args) throws InterruptedException 
	{
		for (int i = 0; i < 999; i++)
		{
			OperLog e = new OperLog();
			
			OperlogUtils.getInstance().insertLog(e);
		}
		Thread.sleep(2000);
		OperlogUtils.terminateUtils();
	}
	
}
