package com.bdaim.common.util;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;


public final class CalendarUtil {
	private static final Logger log = LoggerFactory.getLogger(CalendarUtil.class);
	public static final String CREDIT_CARD_DATE_FORMAT = "MM/yyyy";
	public static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";
	public static final String SHORT_DATE_FORMAT_YYYY_MM = "yyyy-MM";
	public static final String SHORT_DATE_DOT_FORMAT = "yyyy.MM.dd";
	public static final String SHORT_DATE_FORMAT_NO_DASH = "yyyyMMdd";
	public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String SIMPLE_DATE_FORMAT_NO_DASH = "yyyyMMddHHmmss";
	public static final String LOG_DATE_FORMAT = "yyyyMMdd_HH00";
	public static final String ZONE_DATE_FORMAT = "EEE yyyy-MM-dd HH:mm:ss zzz";
	public static final String DATE_FORMAT = "yyyy/MM/dd EEE";
	public static final String TIME_FORMAT = "HH:mm";

	public static int daysBetween(Calendar startTime, Calendar endTime) {
		if (startTime == null) {
			throw new IllegalArgumentException("startTime is null");
		}
		if (endTime == null) {
			throw new IllegalArgumentException("endTime is null");
		}
		if (startTime.compareTo(endTime) > 0) {
			throw new IllegalArgumentException("endTime is before the startTime");
		}
		return (int) ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / (1000 * 60 * 60 * 24));
	}
	public static Calendar startOfDayTomorrow() {
		Calendar calendar = Calendar.getInstance();
		truncateDay(calendar);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		return calendar;
	}


	public static Calendar startOfDayYesterday() {
		Calendar yesterday = Calendar.getInstance();
		truncateDay(yesterday);
		yesterday.add(Calendar.DAY_OF_MONTH, -1);
		return yesterday;
	}


	public static Calendar truncateDay(Calendar calendar) {
		if (calendar == null) {
			throw new IllegalArgumentException("input is null");
		}
		calendar.set(Calendar.AM_PM, Calendar.AM);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}


	public static String format(Calendar calendar) {
		String formatted = "";
		if (calendar != null) {
			formatted = new SimpleDateFormat().format(calendar.getTime());
		}
		return formatted;
	}


	public static String format(Time time) {
		String formatted = "";
		if (time != null) {
			formatted = new SimpleDateFormat(TIME_FORMAT).format(time.getTime());
		}
		return formatted;
	}


	public static String getDateString(Calendar calendar, String format) {
		if (calendar == null) {
			return null;
		}
		return getDateString(calendar.getTime(), format);
	}


	public static String getDefaultDateString(Date date) {
		if (null == date) {
			return "";
		}
		return getDateString(date, SIMPLE_DATE_FORMAT);
	}

	/**
	 * 获得默认格式化时间 'yyyy-MM-dd'
	 * 
	 * @param date
	 * @return
	 */
	public static String getShortDateString(Date date) {
		if (null == date) {
			return "";
		}
		return getDateString(date, SHORT_DATE_FORMAT);
	}


	public static String getDateString(Date date, String format) {
		if (date == null) {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}


	public static String getDateString(Date date, String format, Locale locale) {
		if (date == null) {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
		return sdf.format(date);
	}




	public static Date parseDate(String dateString, String pattern) {
		Date date = null;
		try {
			DateFormat format = new SimpleDateFormat(pattern);
			date = format.parse(dateString);
		} catch (ParseException ex) {
			log.error("Invalid date string: " + dateString, ex);
			throw new IllegalArgumentException("Invalid date string: " + dateString, ex);
		}

		return date;
	}

	public static Date parseDefaultDate(String dateString) {
		if (!StringUtils.hasText(dateString)) {
			return null;
		}
		Date date = null;
		try {
			DateFormat format = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
			date = format.parse(dateString);
		} catch (ParseException ex) {
			log.error("Invalid date string: " + dateString, ex);
			throw new IllegalArgumentException("Invalid date string: " + dateString, ex);
		}

		return date;
	}

	public static Calendar parseCalendarShort(String dateString) {
		if (!StringUtils.hasText(dateString)) {
			return null;
		}
		return parseCalendar(dateString, SHORT_DATE_FORMAT);
	}

	public static Calendar parseCalendar(String dateString) {
		if (!StringUtils.hasText(dateString)) {
			return null;
		}
		return parseCalendar(dateString, SIMPLE_DATE_FORMAT);
	}

	/**
	 * int 转时间
	 * 
	 * @param time
	 * @return
	 */
	public static String parseIntegerToDate(Integer time) {
		if(time ==null){
			return "00:00";
		}
		StringBuilder sb = new StringBuilder("");
		int xs = time / 3600;
		if(xs>0){
		if (xs < 10) {
			sb.append("0");
		}
		
		sb.append(xs);
		sb.append(":");
		}
		int fen = (time - 3600 * xs) / 60;
		if (fen < 10) {
			sb.append("0");
		}
		sb.append(fen);
		sb.append(":");
		int second = time - xs * 3600 - fen * 60;
		if (second < 10) {
			sb.append("0");
		}
		sb.append(second);
		return sb.toString();
	}


	public static Calendar parseCalendar(String dateString, String pattern) {
		Date date = parseDate(dateString, pattern);

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;
	}

	public static Date parseShortDate(String dateString) {
		if (!StringUtils.hasText(dateString)) {
			return null;
		}
		Date date = null;
		try {
			DateFormat format = new SimpleDateFormat(SHORT_DATE_FORMAT);
			date = format.parse(dateString);
		} catch (ParseException ex) {
			log.error("Invalid date string: " + dateString, ex);
			throw new IllegalArgumentException("Invalid date string: " + dateString, ex);
		}

		return date;
	}


	public static Calendar backOneDay(Calendar date) {
		Calendar cal = (Calendar) date.clone();
		cal.add(Calendar.DATE, -1);
		return cal;
	}

	/**
	 * Get how many days in current month.
	 */
	public static int daysForCurrentMonth() {
		Calendar c = Calendar.getInstance();
		int days = c.getActualMaximum(Calendar.DAY_OF_MONTH);

		return days;
	}

	/**
	 * Return the Calendar for the give date.
	 * 
	 * @param date
	 * @return
	 */
	public static Calendar fromDate(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		return c;
	}

	/**
	 * return the EPOCH = "1970-01-01 00:00:00"
	 * 
	 * @param dateString
	 * @param pattern
	 * @return
	 */
	public static Calendar epoch() {

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0);
		return c;
	}

	public static long getSimpleDateTimeMillis(long timeMillis) {
		Date date = new Date(timeMillis);
		String dateStr = getDateString(date, SHORT_DATE_FORMAT);
		Date transformDate = parseDate(dateStr, SHORT_DATE_FORMAT);
		return transformDate.getTime();
	}

	/**
	 * get the date from a day with days
	 * 
	 * @author <a href="mailto:wang-shuai@letv.com">Ousui</a>
	 * @param from which day
	 * @param days interval days, 0: today; positive: future; negative: history.
	 * @return
	 */
	public static Calendar getDateFromDate(Date from, long days) {
		long froml = from.getTime();
		// 时间间隔。
		long interval = days * 24l * 60l * 60l * 1000l;
		long millis = froml + interval;
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(millis);
		return now;
	}

	/**
	 * get the date from a day with days
	 * 
	 * @author <a href="mailto:wang-shuai@letv.com">Ousui</a>
	 * @param from which day
	 * @param days interval days, 0: today; positive: future; negative: history.
	 * @return String
	 */
	public static String getDateFromDate(Date from, long days, String format) {
		Calendar c = CalendarUtil.getDateFromDate(from, days);
		return CalendarUtil.getDateString(c, format);
	}

	public static String getDateFromDate(String from, long days, String format) {
		Date d = CalendarUtil.parseDate(from, format);
		Calendar c = CalendarUtil.getDateFromDate(d, days);
		return CalendarUtil.getDateString(c, format);
	}

	public static Calendar getDayStart() {
		Calendar cal = Calendar.getInstance();
		cal = truncateDay(cal);
		return cal;
	}

	public static Calendar getWeekDayStart() {
		Calendar cal = Calendar.getInstance();
		int day_of_week = cal.get(Calendar.DAY_OF_WEEK) - 2;
		cal.add(Calendar.DATE, -day_of_week);
		cal = truncateDay(cal);
		return cal;
	}

	public static Calendar getMonthDayStart() {
		Calendar cal = Calendar.getInstance();
		int day_of_month = cal.get(Calendar.DAY_OF_MONTH) - 1;
		cal.add(Calendar.DATE, -day_of_month);
		cal = truncateDay(cal);
		return cal;
	}
	/**
	 * 获取指定日期以后指定天数的时间
	 * @param cale  指定的时间
	 * @param days  时间间隔
	 * @return
	 */
	public static Date getDateByAfterDays(Calendar cale ,int days){
		cale.set(Calendar.DAY_OF_MONTH, cale.get(Calendar.DAY_OF_MONTH)+days);
		return cale.getTime();
	}
	/**
	 * 获取指定日期之前指定天数的时间
	 * @param cale  指定的时间
	 * @param days  时间间隔
	 * @return
	 */
	public static Date getDateByBeforeDays(Calendar cale ,int days){
		cale.set(Calendar.DAY_OF_MONTH, cale.get(Calendar.DAY_OF_MONTH)-days);
		return cale.getTime();
	}
	
	public static void main(String args[]){
	}
}
