package com.bdaim.common.util;

import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DatetimeUtils {

	/**
	 * yyyy-MM-dd HH:mm:ss.S
	 */
	public static final DateTimeFormatter DATE_TIME_FORMATTER_SSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static final DateTimeFormatter YYYY_MM = DateTimeFormatter.ofPattern("yyyyMM");

	public static Date getMonthStart() {
		Calendar calendar = Calendar.getInstance();
	      //得到月初
	      calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
	      calendar.set(Calendar.HOUR_OF_DAY, 0);
	      calendar.set(Calendar.MINUTE, 0);
	      calendar.set(Calendar.SECOND, 0);
	      Date strDateFrom = calendar.getTime();
	      return strDateFrom;
	}
	
	public static Date getMonthEnd() {
		Calendar calendar = Calendar.getInstance();
	      //得到月初
			calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
	      calendar.set(Calendar.HOUR_OF_DAY, 23);
	      calendar.set(Calendar.MINUTE, 59);
	      calendar.set(Calendar.SECOND, 59);
	      Date strDateFrom = calendar.getTime();
	      return strDateFrom;
	}
	
	public static Date dateToDayStart(Date date)
	{
		if (null == date)return null;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 1);
		return c.getTime();
	}
	public static Date dateToDayEnd(Date date)
	{
		if (null == date)return null;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		return c.getTime();
	}
	
	public static void main(String[] args) {
		System.out.println(DatetimeUtils.dateToDayStart(Calendar.getInstance().getTime()));
		System.out.println(DatetimeUtils.dateToDayEnd(Calendar.getInstance().getTime()));
	}
}
