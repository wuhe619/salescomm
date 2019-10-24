package com.bdaim.common.OperLog;


import com.bdaim.common.spring.SpringContextHelper;
import com.bdaim.log.entity.OperLog;
import com.bdaim.log.service.OperLogService;


public class OperlogRunnable implements Runnable
{
	OperLog value = null;
	OperLogService logserv = null;
	
	
	public OperlogRunnable(OperLog value)
	{
		this.value = value;
		logserv = (OperLogService) SpringContextHelper.getBean("operLogService");
	}

	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		try {
			//TO INSERT THE VO TO TABLE
			logserv.addOneOperLogInfo(value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("content:" + value.getContent());
	}
	
}
