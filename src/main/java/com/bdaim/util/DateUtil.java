package com.bdaim.util;

import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateUtil {

    public static final String YYYY_MM_DD_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM = "yyyy-MM";

    /**
     * 将字符串转化为DATE
     *
     * @param dtFormat 格式yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd或 yyyy-M-dd或 yyyy-M-d或
     *                 yyyy-MM-d或 yyyy-M-dd
     * @param def      如果格式化失败返回null
     * @return
     */
    public static Date fmtStrToDate(String dtFormat) {
        if (dtFormat == null) {
            return null;
        }
        try {
            if (dtFormat.length() == 9 || dtFormat.length() == 8) {
                String[] dateStr = dtFormat.split("-");
                dtFormat = dateStr[0] + (dateStr[1].length() == 1 ? "-0" : "-") + dateStr[1] + (dateStr[2].length() == 1 ? "-0" : "-") + dateStr[2];
            }
            if (dtFormat.length() != 10 & dtFormat.length() != 19)
                return null;
            if (dtFormat.length() == 10)
                dtFormat = dtFormat + " 00:00:00";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.parse(dtFormat);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Description:格式化日期,如果格式化失败返回def
     *
     * @param dtFormat
     * @param def
     * @return
     * @author ganliang
     * @since：2008-2-15 下午05:01:37
     */
    public static Date fmtStrToDate(String dtFormat, Date def) {
        Date d = fmtStrToDate(dtFormat);
        if (d == null)
            return def;
        return d;
    }

    /**
     * 返回当日短日期型
     *
     * @return
     * @author ganliang
     * @since：2008-2-15 下午05:03:13
     */
    public static Date getToDay() {
        return toShortDate(new Date());
    }

    /**
     * Description:格式化日期,String字符串转化为Date
     *
     * @param date
     * @param dtFormat 例如:yyyy-MM-dd HH:mm:ss yyyyMMdd
     * @return
     * @author ganliang
     * @since：2007-7-10 上午11:24:00
     */
    public static String fmtDateToStr(Date date, String dtFormat) {
        if (date == null)
            return "";
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(dtFormat);
            return dateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Description:按指定格式 格式化日期
     *
     * @param date
     * @param dtFormat
     * @return
     * @author ganliang
     * @since：2007-12-10 上午11:25:07
     */
    public static Date fmtStrToDate(String date, String dtFormat) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(dtFormat);
            return dateFormat.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String fmtDateToYMDHM(Date date) {
        return fmtDateToStr(date, "yyyy-MM-dd HH:mm");
    }

    public static String fmtDateToYMDHMSS(Date date) {
        return fmtDateToStr(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String fmtDateToYMD(Date date) {
        return fmtDateToStr(date, "yyyy-MM-dd");
    }

    public static String fmtDateToYM(Date date) {
        return fmtDateToStr(date, "yyyy-MM");
    }

    public static String fmtDateToM(Date date) {
        return fmtDateToStr(date, "MM");
    }

    /**
     * Description:只保留日期中的年月日
     *
     * @param date
     * @return
     * @author ganliang
     * @since：2007-12-10 上午11:25:50
     */
    public static Date toShortDate(Date date) {
        String strD = fmtDateToStr(date, "yyyy-MM-dd");
        return fmtStrToDate(strD);
    }

    /**
     * Description:只保留日期中的年月日
     *
     * @param date格式要求yyyy -MM-dd……………………
     * @return
     * @author ganliang
     * @since：2007-12-10 上午11:26:12
     */
    public static Date toShortDate(String date) {
        if (date != null && date.length() >= 10) {
            return fmtStrToDate(date.substring(0, 10));
        } else
            return fmtStrToDate(date);
    }

    /**
     * 求对日
     *
     * @param countMonth :月份的个数(几个月)
     * @param flag       :true 求前countMonth个月的对日:false 求下countMonth个月的对日
     * @return
     */
    public static Date getCounterglow(int countMonth, boolean before) {
        Calendar ca = Calendar.getInstance();
        return getCounterglow(ca.getTime(), before ? -countMonth : countMonth);
    }

    /**
     * Description: 求对日 加月用+ 减月用-
     *
     * @param date
     * @param countMonth
     * @return
     * @since：2007-12-13 下午03:16:47
     */
    public static Date getCounterglow(Date date, int num) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.add(Calendar.MONTH, num);
        return ca.getTime();
    }

    /**
     * Description:加一天
     *
     * @param date
     * @return
     * @author ganliang
     * @since：2007-12-13 下午02:57:38
     */
    public static Date addDay(Date date) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        cd.add(Calendar.DAY_OF_YEAR, 1);
        return cd.getTime();
    }

    /**
     * Description:判断一个日期是否为工作日(非周六周日)
     *
     * @param date
     * @return
     * @author ganliang
     * @since：2007-12-13 下午03:01:35
     */
    public static boolean isWorkDay(Date date) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek != Calendar.SUNDAY || dayOfWeek != Calendar.SATURDAY)
            return false;
        return true;
    }

    /**
     * Description:取一个月的最后一天
     *
     * @param date1
     * @return
     * @author ganliang
     * @since：2007-12-13 下午03:28:21
     */
    public static Date getLastDayOfMonth(Date date1) {
        Calendar date = Calendar.getInstance();
        date.setTime(date1);
        date.set(Calendar.DAY_OF_MONTH, 1);
        date.add(Calendar.MONTH, 1);
        date.add(Calendar.DAY_OF_YEAR, -1);
        return toShortDate(date.getTime());
    }

    /**
     * 求开始截至日期之间的天数差.
     *
     * @param d1 开始日期
     * @param d2 截至日期
     * @return 返回相差天数
     */
    public static int getDaysInterval(Date d1, Date d2) {
        if (d1 == null || d2 == null)
            return 0;
        Date[] d = new Date[2];
        d[0] = d1;
        d[1] = d2;
        Calendar[] cal = new Calendar[2];
        for (int i = 0; i < cal.length; i++) {
            cal[i] = Calendar.getInstance();
            cal[i].setTime(d[i]);
            cal[i].set(Calendar.HOUR_OF_DAY, 0);
            cal[i].set(Calendar.MINUTE, 0);
            cal[i].set(Calendar.SECOND, 0);
        }
        long m = cal[0].getTime().getTime();
        long n = cal[1].getTime().getTime();
        int ret = (int) Math.abs((m - n) / 1000 / 3600 / 24);
        return ret;
    }

    public static String getDayOfWeek(Date date) {
        Calendar cl = Calendar.getInstance();
        cl.setTime(date);
        return "周" + toChNumber(cl.get(Calendar.DAY_OF_WEEK) - 1);
    }

    /**
     * 将数字转为中文。 "0123456789"->"〇一二三四五六七八九"
     *
     * @param num 长度为1,'0'-'9'的字符串
     * @return
     */
    private static String toChNumber(int num) {
        final String str = "〇一二三四五六七八九";
        return str.substring(num, num + 1);
    }

    /**
     * Description:指定日期加或减days天
     *
     * @param date1日期
     * @param days天数
     * @return
     * @author ganliang
     * @since：2007-12-17 下午03:47:12
     */
    public static Date addDay(Date date1, int days) {
        Calendar date = Calendar.getInstance();
        date.setTime(date1);
        date.add(Calendar.DAY_OF_YEAR, days);
        return date.getTime();
    }

    /**
     * Description:指定日期加或减months月
     *
     * @param date1
     * @param months
     * @return
     * @author ganliang
     * @since：2008-3-5 下午05:17:26
     */
    public static Date addMonth(Date date1, int months) {
        Calendar date = Calendar.getInstance();
        date.setTime(date1);
        date.add(Calendar.MONTH, months);
        return date.getTime();
    }

    /**
     * Description:指定日期加或减years年
     *
     * @param date1
     * @param years
     * @return
     */
    public static Date addYear(Date date1, int years) {
        Calendar date = Calendar.getInstance();
        date.setTime(date1);
        date.add(Calendar.YEAR, years);
        return date.getTime();
    }

    /**
     * 指定期间的开始日期
     *
     * @param date 指定日期
     * @param type 期间类型
     * @param diff 与指定日期的范围
     * @return
     */
    public static Date getPeriodStart(Calendar date, int type, int diff) {
        date.add(type, diff * (-1));
        return date.getTime();
    }

    /**
     * 指定期间的开始日期
     *
     * @param date 指定日期
     * @param type 期间类型
     * @param diff 与指定日期的范围
     * @return
     */
    public static Date getPeriodStart(Date date, int type, int diff) {
        return getPeriodStart(dateToCalendar(date), type, diff);
    }

    /**
     * 指定期间的结束日期
     *
     * @param date 指定日期
     * @param type 期间类型
     * @param diff 与指定日期的范围
     * @return
     */
    public static Date getPeriodEnd(Calendar date, int type, int diff) {
        date.add(type, diff);
        return date.getTime();
    }

    /**
     * 指定期间的结束日期
     *
     * @param date 指定日期
     * @param type 期间类型
     * @param diff 与指定日期的范围
     * @return
     */
    public static Date getPeriodEnd(Date date, int type, int diff) {
        return getPeriodEnd(dateToCalendar(date), type, diff);
    }

    /**
     * 指定日期所在星期的第一天
     *
     * @param date
     * @return
     */
    public static Date getWeekStart(Date date) {
        Calendar cdate = dateToCalendar(date);
        cdate.set(Calendar.DAY_OF_WEEK, 2);
        return cdate.getTime();
    }

    /**
     * 将java.util.Date类型转换成java.util.Calendar类型
     *
     * @param date
     * @return
     */
    public static Calendar dateToCalendar(Date date) {
        Calendar cdate = Calendar.getInstance();
        cdate.setTime(date);
        return cdate;
    }

    /**
     * 指定日期所在月的第一天
     *
     * @param date
     * @return
     */
    public static Date getMonthStart(Date date) {
        Calendar cdate = dateToCalendar(date);
        cdate.set(Calendar.DAY_OF_MONTH, 1);
        return toShortDate(cdate.getTime());
    }

    /**
     * 指定日期所在上月的第一天
     *
     * @param date
     * @return
     */
    public static Date getLastMonthStart(Date date) {
        Calendar cdate = dateToCalendar(date);
        cdate.set(Calendar.DAY_OF_MONTH, 1);
        cdate.add(Calendar.MONTH, -1);
        return toShortDate(cdate.getTime());
    }

    /**
     * 指定日期所在旬的第一天
     *
     * @param date
     * @return
     */
    public static Date getTenDaysStart(Date date) {
        Calendar cdate = dateToCalendar(date);
        int day = cdate.get(Calendar.DAY_OF_MONTH) / 10 * 10 + 1;
        if (cdate.get(Calendar.DAY_OF_MONTH) % 10 == 0 || day == 31)
            day = day - 10;
        cdate.set(Calendar.DAY_OF_MONTH, day);
        return cdate.getTime();
    }

    /**
     * 指定日期所在旬的最后一天
     *
     * @param date
     * @return
     */
    public static Date getTenDaysEnd(Date date) {
        Calendar cdate = dateToCalendar(date);
        if (cdate.get(Calendar.DAY_OF_MONTH) / 10 == 2
                && cdate.get(Calendar.DAY_OF_MONTH) != 20)
            return getLastDayOfMonth(date);
        else
            return addDay(getTenDaysStart(addDay(date, 10)), -1);
    }

    /**
     * 指定日期所在年的第一天
     *
     * @param date
     * @return
     */
    public static Date getYearStart(Date date) {
        Calendar cdate = dateToCalendar(date);
        cdate.set(Calendar.DAY_OF_YEAR, 1);
        return cdate.getTime();
    }

    /**
     * 指定日期所在季度的第一天
     *
     * @param date
     * @return
     */
    public static Date getQuarterStart(Date date) {
        Calendar cdate = dateToCalendar(date);
        int month = (cdate.get(Calendar.MONTH) / 3) * 3;
        cdate.set(Calendar.MONTH, month);
        return getMonthStart(cdate.getTime());
    }

    /**
     * 指定日期返回带中文的字符串（目前为年月日类型，之后补充）
     *
     * @param date
     * @param format
     * @return
     */
    public static String dateToStringByChinese(String format, Date date) {
        String dateString = fmtDateToStr(date, format);
        String[] dateStringArray = dateString.split("-");
        if ("yyyy-MM-dd".equals(format)) {
            dateString = dateStringArray[0] + "年" + dateStringArray[1] + "月"
                    + dateStringArray[2] + "日";
        } else if ("yyyy-MM".equals(format)) {
            dateString = dateStringArray[0] + "年" + dateStringArray[1] + "月";
        }
        return dateString;
    }

    public static Date getLastDayOfYear(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
        String years = dateFormat.format(date);
        years += "-12-31";
        Date returnDate = fmtStrToDate(years);
        return toShortDate(returnDate);
    }

    /**
     * 计算两个日期之间相差的月数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int getMonths(Date date1, Date date2) {
        int iMonth = 0;
        int flag = 0;
        try {
            Calendar objCalendarDate1 = Calendar.getInstance();
            objCalendarDate1.setTime(date1);

            Calendar objCalendarDate2 = Calendar.getInstance();
            objCalendarDate2.setTime(date2);

            if (objCalendarDate2.equals(objCalendarDate1))
                return 0;
            if (objCalendarDate1.after(objCalendarDate2)) {
                Calendar temp = objCalendarDate1;
                objCalendarDate1 = objCalendarDate2;
                objCalendarDate2 = temp;
            }
            if (objCalendarDate2.get(Calendar.DAY_OF_MONTH) < objCalendarDate1.get(Calendar.DAY_OF_MONTH))
                flag = 1;

            if (objCalendarDate2.get(Calendar.YEAR) > objCalendarDate1.get(Calendar.YEAR))
                iMonth = ((objCalendarDate2.get(Calendar.YEAR) - objCalendarDate1.get(Calendar.YEAR)) * 12 + objCalendarDate2.get(Calendar.MONTH) - flag) - objCalendarDate1.get(Calendar.MONTH);
            else
                iMonth = objCalendarDate2.get(Calendar.MONTH) - objCalendarDate1.get(Calendar.MONTH) - flag;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return iMonth;
    }

    /**
     * 指定日期上一个旬的第一天
     */
    public static Date getLastTenStartDate(Date date) {
        Date returnDate = DateUtil.toShortDate(date);
        returnDate = DateUtil.getTenDaysStart(date);
        returnDate = DateUtil.addDay(returnDate, -1);
        returnDate = DateUtil.getTenDaysStart(returnDate);
        return DateUtil.toShortDate(returnDate);
    }

    /**
     * 指定日期上一个旬的最后一天
     */
    public static Date getLastTenEndDate(Date date) {
        Date returnDate = DateUtil.toShortDate(date);
        returnDate = DateUtil.getTenDaysStart(date);
        returnDate = DateUtil.addDay(returnDate, -1);
        return DateUtil.toShortDate(returnDate);
    }

    /**
     * 指定日期上个月第一天
     */
    public static Date getLastMonthStartDate(Date date) {
        Date returnDate = DateUtil.toShortDate(date);
        returnDate = DateUtil.getLastMonthStart(date);
        return DateUtil.toShortDate(returnDate);
    }

    /**
     * 指定日期上个月最后一天
     */
    public static Date getLastMonthEndDate(Date date) {
        Date returnDate = DateUtil.toShortDate(date);
        returnDate = DateUtil.getMonthStart(date);
        returnDate = DateUtil.addDay(returnDate, -1);
        return DateUtil.toShortDate(returnDate);
    }

    public static long getLong(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String time = date;
        Date d = format.parse(time);
        long l = d.getTime();
        return l;
    }

    public static long getLongData(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = date;
        Date d = format.parse(time);
        long l = d.getTime();
        return l;
    }

    public static long getLongValue(String date) throws ParseException {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = date;
        Date d = format.parse(time);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(c.DATE, -1);
        Date temp_date = c.getTime();
        String dateDecmail = format.format(temp_date);
        Date dt = format.parse(dateDecmail);
        return dt.getTime();
    }

    public static Timestamp getTimestamp(Date date, String dtFormat) {
        if (date == null)
            return null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(dtFormat);
            return Timestamp.valueOf(dateFormat.format(date));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String timstampToString(Timestamp timestamp, String format) {
        DateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(timestamp);
    }

    /**
     * @param format
     * @return
     * @Comment 包含当天
     */
    public static String getPastWeekDate(Date date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        Calendar c = Calendar.getInstance();
        //过去七天
        c.setTime(date);
        c.add(Calendar.DATE, -10);
        Date d = c.getTime();
        return df.format(d);
    }

    public static String getPastMonthDate(Date date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, -1);
        Date m = c.getTime();
        return df.format(m);
    }

    public static String getPastThreeMonthDate(Date date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, -3);
        Date m = c.getTime();
        return df.format(m);
    }


    public static LocalDateTime getDateTimeOfTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    public static String getDateOfYearAndMonth() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String result = simpleDateFormat.format(date);
        return result;
    }

    /**
     * @description 年月日处理（返回年和月）
     */
    public static Map<String, Integer> getYearMessage(String billDate) {
        //判断参数是否为空
        int year = 0;
        int month = 0;
        if (StringUtil.isNotEmpty(billDate)) {
            //对日期进行拆分
            year = Integer.parseInt(billDate.substring(0, 4));
            month = Integer.parseInt(billDate.substring(4, 6));
        } else {
            year = LocalDate.now().getYear();
            month = LocalDate.now().getMonthValue();
        }
        Map<String, Integer> map = new HashMap<>();
        map.put("year", year);
        map.put("month", month);
        return map;
    }

    /**
     * 获取当前月份格式化后的日期字符串
     *
     * @return 201811
     */
    public static String getNowMonthToYYYYMM() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    public static String getPastNMonthDate(Date date, String format, int N) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, N);
        Date m = c.getTime();
        return df.format(m);
    }


    /**
     * 将秒数转换为日时分秒，
     *
     * @param second
     * @return
     */
    public static String secondToTime(long second) {
        second = second % 86400;
        long hours = second / 3600;
        second = second % 3600;
        long minutes = second / 60;
        second = second % 60;
        StringBuilder result = new StringBuilder();
        if (hours >= 10) {
            result.append(hours).append(":");
        } else {
            result.append("0").append(hours).append(":");
        }
        if (minutes >= 10) {
            result.append(minutes).append(":");
        } else {
            result.append("0").append(minutes).append(":");
        }
        if (second >= 10) {
            result.append(second);
        } else {
            result.append("0").append(second);
        }
        return result.toString();

    }

    /**
     * 校验时间是否在相同年月
     *
     * @return
     */
    public static boolean isSameMonth(LocalDateTime date1, LocalDateTime date2) {
        if ((date1.getYear() == date2.getYear()) && (date1.getMonthValue() == date2.getMonthValue())) {
            return true;
        }
        return false;
    }


    /**
     * @param date
     * @param pattern
     * @return
     * @see
     */
    public static String format(Date date, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        String rs = null;
        if (date != null) {
            Instant instant = date.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
            rs = localDateTime.format(formatter);
        }
        return rs;
    }

    public static String formatDate(String pattern, Date date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        String rs = null;
        if (date != null) {
            Instant instant = date.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
            rs = localDateTime.format(formatter);
        }
        return rs;
    }

    public static void main(String[] args) {
//        System.out.println(secondToTime(121));
//        System.out.println(getPastNMonthDate(new Date(), "yyyyMM", -5));
//        System.out.println(fmtStrToDate("20190224000000", "yyyyMMddHHmmss"));
//
//        Date d = DateUtil.fmtStrToDate("2019-02-12 12:23:49");
//        Timestamp s = new Timestamp(d.getTime());
//        System.out.println(s);

        String s2=DateUtil.fmtDateToStr(new Date(),"yyyyMMddHHmmss");
        System.out.println(s2);

    }
}
