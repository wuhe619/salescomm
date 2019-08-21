package com.bdaim.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;

/**
 * @author ningmeng
 * @date 2018/10/25
 * @description
 */
public class NumberConvertUtil {

    private final static Logger LOG = LoggerFactory.getLogger(NumberConvertUtil.class);

    private static NumberFormat numberFormat = NumberFormat.getInstance();

    /**
     * 计算百分比(保留2位小数)
     *
     * @param num   除数
     * @param total 被除数
     * @return
     */
    public static String getPercent(long num, long total) {
        String result;
        try {
            if (0==num || 0==total){
                return "0";
            }
            // 设置精确到小数点后2位
            numberFormat.setMaximumFractionDigits(2);
            result = numberFormat.format((float) num / (float) total * 100);
        } catch (Exception e) {
            result = "0.00";
            LOG.error("计算百分比失败,num:" + num + ",total:" + total + ",result:" + result, e);
        }
        return result;
    }

    /**
     * @param num   除数
     * @param total 被除数
     * @return
     */
    public static String getAverage(long num, long total) {
        String result;
        try {
            if (0==num || 0==total){
                return "0";
            }
            // 设置精确到小数点后2位
            numberFormat.setMaximumFractionDigits(2);
            result = numberFormat.format((float) num / (float) total);
        } catch (Exception e) {
            result = "0.00";
            LOG.error("计算平均值失败,num:" + num + ",total:" + total + ",result:" + result, e);
        }
        return result;
    }

    /**
     * String转换为Long
     *
     * @param value
     * @return
     */
    public static long parseLong(String value) {
        long result;
        try {
            result = Long.parseLong(value);
        } catch (Exception e) {
            result = 0;
            LOG.error("String转换为Long失败,value:" + value + ",result:" + result, e);
        }
        return result;
    }

    /**
     * Object转换为Long
     *
     * @param value
     * @return
     */
    public static long parseLong(Object value) {
        long result;
        try {
            result = Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            result = 0;
            LOG.error("String转换为Long失败,value:" + value + ",result:" + result, e);
        }
        return result;
    }

    /**
     * String转换为Double
     *
     * @param value
     * @return
     */
    public static double parseDouble(String value) {
        double result;
        try {
            result = Double.parseDouble(value);
        } catch (Exception e) {
            result = 0;
            LOG.error("String转换为Double失败,value:" + value + ",result:" + result, e);
        }
        return result;
    }

    /**
     * 元转换为分(只取2位小数)
     *
     * @param value
     * @return
     */
    public static int transformtionCent(double value) {
        BigDecimal bigDecimal = new BigDecimal(Double.toString(value)).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_DOWN);
        return bigDecimal.intValue();
    }
    /**
     * 分转换为元(只取2位小数)
     *
     * @param value
     * @return
     */
    public static double transformtionElement(Object value) {
        BigDecimal bigDecimal = new BigDecimal(String.valueOf(value)).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_DOWN);
        return bigDecimal.doubleValue();
    }

    /**
     * String转int
     *
     * @param value
     * @return
     */
    public static int transformtionInt(String value) {
        BigDecimal bigDecimal = new BigDecimal(value).setScale(0, BigDecimal.ROUND_DOWN);
        return bigDecimal.intValue();
    }

    /**
     * String转换为int
     *
     * @param value
     * @return
     */
    public static int parseInt(String value) {
        int result;
        try {
            result = Integer.parseInt(value);
        } catch (Exception e) {
            result = 0;
            LOG.error("String转换为Long失败,value:" + value + ",result:" + result, e);
        }
        return result;
    }

    public static int parseInt(Object value) {
        int result;
        try {
            result = Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            result = 0;
            LOG.error("String转换为Long失败,value:" + value + ",result:" + result, e);
        }
        return result;
    }

    /**
     * 对象转long类型
     *
     * @param value
     * @return
     */
    public static Long everythingToLong(Object value) {
        if (null == value) return null;

        try {
            return (Long) value;
        } catch (Exception ex) {
            try {
                return ((BigInteger) value).longValue();
            } catch (Exception ex2) {
                return ((BigDecimal) value).longValue();
            }
        }
    }

    /**
     * 对象转int
     */
    public static Integer everythingToInt(Object value) {
        if (null == value) return null;
        try {
            return (Integer) value;
        } catch (Exception ex) {
            try {
                return ((BigInteger) value).intValue();
            } catch (Exception ex2) {
                return ((BigDecimal) value).intValue();
            }
        }
    }

    /**
     * 2数相除
     *
     * @param num
     * @param total
     * @return
     */
    public static String divNumber(long num, long total) {
        String result;
        try {
            // 设置精确到小数点后2位
            numberFormat.setMaximumFractionDigits(2);
            result = numberFormat.format((float) num / (float) total);
        } catch (Exception e) {
            result = "0.00";
            LOG.error("计算百分比失败,num:" + num + ",total:" + total + ",result:" + result, e);
        }
        return result;
    }

}
