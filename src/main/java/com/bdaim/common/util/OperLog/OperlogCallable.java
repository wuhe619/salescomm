package com.bdaim.common.util.OperLog;



import java.util.concurrent.Callable;

import com.bdaim.log.entity.OperLog;


public class OperlogCallable implements Callable<Boolean>
{
	OperLog value = null;
	
	
	public OperlogCallable(OperLog value)
	{
		this.value = value;
	}
	
	@Override
	public Boolean call() throws Exception 
	{
		// TODO Auto-generated method stub
		try
		{
			
			return true;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}
	
}
