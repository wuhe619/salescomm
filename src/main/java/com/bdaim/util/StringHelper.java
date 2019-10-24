package com.bdaim.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * <pre>
 * 字符串的常见操作方法
 * </pre>
 * 
 * @author chenhewei
 * @version 创建时间：2011-5-1 18:03:06
 */
public class StringHelper {
	public static final String EMPTY = "";

	/**
	 * <p>
	 * Checks if a String is empty ("") or null.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.isEmpty(null)      = true
	 * StringUtils.isEmpty("")        = true
	 * StringUtils.isEmpty(" ")       = false
	 * StringUtils.isEmpty("bob")     = false
	 * StringUtils.isEmpty("  bob  ") = false
	 * </pre>
	 * 
	 * <p>
	 * NOTE: This method changed in Lang version 2.0. It no longer trims the
	 * String. That functionality is available in isBlank().
	 * </p>
	 * 
	 * @param sourceString
	 *            the String to check, may be null
	 * @return <code>true</code> if the String is empty or null
	 */
	public static boolean isEmpty(String sourceString) {
		return sourceString == null || sourceString.length() == 0;
	}

	/**
	 * <p>
	 * Checks if a String is not empty ("") and not null.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.isNotEmpty(null)      = false
	 * StringUtils.isNotEmpty("")        = false
	 * StringUtils.isNotEmpty(" ")       = true
	 * StringUtils.isNotEmpty("bob")     = true
	 * StringUtils.isNotEmpty("  bob  ") = true
	 * </pre>
	 * 
	 * @param sourceString
	 *            the String to check, may be null
	 * @return <code>true</code> if the String is not empty and not null
	 */
	public static boolean isNotEmpty(String sourceString) {
		return !StringHelper.isEmpty(sourceString);
	}

	/**
	 * <p>
	 * Checks if a String is whitespace, empty ("") or null.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.isBlank(null)      = true
	 * StringUtils.isBlank("")        = true
	 * StringUtils.isBlank(" ")       = true
	 * StringUtils.isBlank("bob")     = false
	 * StringUtils.isBlank("  bob  ") = false
	 * </pre>
	 * 
	 * @param sourceString
	 *            the String to check, may be null
	 * @return <code>true</code> if the String is null, empty or whitespace
	 * @since 2.0
	 */
	public static boolean isBlank(String sourceString) {
		int stringLength;
		if (sourceString == null || (stringLength = sourceString.length()) == 0) {
			return true;
		}

		for (int i = 0; i < stringLength; i++) {
			if ((Character.isWhitespace(sourceString.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>
	 * Checks if a String is not empty (""), not null and not whitespace only.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.isNotBlank(null)      = false
	 * StringUtils.isNotBlank("")        = false
	 * StringUtils.isNotBlank(" ")       = false
	 * StringUtils.isNotBlank("bob")     = true
	 * StringUtils.isNotBlank("  bob  ") = true
	 * </pre>
	 * 
	 * @param sourceString
	 *            the String to check, may be null
	 * @return <code>true</code> if the String is not empty and not null and not
	 *         whitespace
	 */
	public static boolean isNotBlank(String sourceString) {
		return !StringHelper.isBlank(sourceString);
	}

	/**
	 * <p>
	 * Converts a String to upper case as per {@link String#toUpperCase()}.
	 * </p>
	 * 
	 * <p>
	 * A <code>null</code> input String returns <code>null</code>.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.upperCase(null)  = null
	 * StringUtils.upperCase("")    = ""
	 * StringUtils.upperCase("aBc") = "ABC"
	 * </pre>
	 * 
	 * <p>
	 * <strong>Note:</strong> As described in the documentation for
	 * {@link String#toUpperCase()}, the result of this method is affected by
	 * the current locale. For platform-independent case transformations, the
	 * method {@link #lowerCase(String, Locale)} should be used with a specific
	 * locale (e.g. {@link Locale#ENGLISH}).
	 * </p>
	 * 
	 * @param string
	 *            the String to upper case, may be null
	 * @return the upper cased String, <code>null</code> if null String input
	 */
	public static String upperCase(String string) {
		if (string == null) {
			return null;
		}
		return string.toUpperCase();
	}

	/**
	 * <p>
	 * Converts a String to upper case as per {@link String#toUpperCase(Locale)}
	 * .
	 * </p>
	 * 
	 * <p>
	 * A <code>null</code> input String returns <code>null</code>.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.upperCase(null, Locale.ENGLISH)  = null
	 * StringUtils.upperCase("", Locale.ENGLISH)    = ""
	 * StringUtils.upperCase("aBc", Locale.ENGLISH) = "ABC"
	 * </pre>
	 * 
	 * @param string
	 *            the String to upper case, may be null
	 * @param locale
	 *            the locale that defines the case transformation rules, must
	 *            not be null
	 * @return the upper cased String, <code>null</code> if null String input
	 * @since 2.5
	 */
	public static String upperCase(String string, Locale locale) {
		if (string == null) {
			return null;
		}
		return string.toUpperCase(locale);
	}

	/**
	 * <p>
	 * Converts a String to lower case as per {@link String#toLowerCase()}.
	 * </p>
	 * 
	 * <p>
	 * A <code>null</code> input String returns <code>null</code>.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.lowerCase(null)  = null
	 * StringUtils.lowerCase("")    = ""
	 * StringUtils.lowerCase("aBc") = "abc"
	 * </pre>
	 * 
	 * <p>
	 * <strong>Note:</strong> As described in the documentation for
	 * {@link String#toLowerCase()}, the result of this method is affected by
	 * the current locale. For platform-independent case transformations, the
	 * method {@link #lowerCase(String, Locale)} should be used with a specific
	 * locale (e.g. {@link Locale#ENGLISH}).
	 * </p>
	 * 
	 * @param string
	 *            the String to lower case, may be null
	 * @return the lower cased String, <code>null</code> if null String input
	 */
	public static String lowerCase(String string) {
		if (string == null) {
			return null;
		}
		return string.toLowerCase();
	}

	/**
	 * <p>
	 * Converts a String to lower case as per {@link String#toLowerCase(Locale)}
	 * .
	 * </p>
	 * 
	 * <p>
	 * A <code>null</code> input String returns <code>null</code>.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.lowerCase(null, Locale.ENGLISH)  = null
	 * StringUtils.lowerCase("", Locale.ENGLISH)    = ""
	 * StringUtils.lowerCase("aBc", Locale.ENGLISH) = "abc"
	 * </pre>
	 * 
	 * @param string
	 *            the String to lower case, may be null
	 * @param locale
	 *            the locale that defines the case transformation rules, must
	 *            not be null
	 * @return the lower cased String, <code>null</code> if null String input
	 * @since 2.5
	 */
	public static String lowerCase(String string, Locale locale) {
		if (string == null) {
			return null;
		}
		return string.toLowerCase(locale);
	}

	/**
	 * <p>
	 * Removes control characters (char &lt;= 32) from both ends of this String,
	 * handling <code>null</code> by returning <code>null</code>.
	 * </p>
	 * 
	 * <p>
	 * The String is trimmed using {@link String#trim()}. Trim removes start and
	 * end characters &lt;= 32. To strip whitespace use {@link #strip(String)}.
	 * </p>
	 * 
	 * <p>
	 * To trim your choice of characters, use the {@link #strip(String, String)}
	 * methods.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.trim(null)          = null
	 * StringUtils.trim("")            = ""
	 * StringUtils.trim("     ")       = ""
	 * StringUtils.trim("abc")         = "abc"
	 * StringUtils.trim("    abc    ") = "abc"
	 * </pre>
	 * 
	 * @param sourceString
	 *            the String to be trimmed, may be null
	 * @return the trimmed string, <code>null</code> if null String input
	 */
	public static String trim(String sourceString) {
		return sourceString == null ? null : sourceString.trim();
	}

	/**
	 * <p>
	 * Removes control characters (char &lt;= 32) from both ends of this String
	 * returning <code>null</code> if the String is empty ("") after the trim or
	 * if it is <code>null</code>.
	 * 
	 * <p>
	 * The String is trimmed using {@link String#trim()}. Trim removes start and
	 * end characters &lt;= 32. To strip whitespace use
	 * {@link #stripToNull(String)}.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.trimToNull(null)          = null
	 * StringUtils.trimToNull("")            = null
	 * StringUtils.trimToNull("     ")       = null
	 * StringUtils.trimToNull("abc")         = "abc"
	 * StringUtils.trimToNull("    abc    ") = "abc"
	 * </pre>
	 * 
	 * @param sourceString
	 *            the String to be trimmed, may be null
	 * @return the trimmed String, <code>null</code> if only chars &lt;= 32,
	 *         empty or null String input
	 */
	public static String trimToNull(String sourceString) {
		String ts = trim(sourceString);
		return isEmpty(ts) ? null : ts;
	}

	/**
	 * <p>
	 * Removes control characters (char &lt;= 32) from both ends of this String
	 * returning an empty String ("") if the String is empty ("") after the trim
	 * or if it is <code>null</code>.
	 * 
	 * <p>
	 * The String is trimmed using {@link String#trim()}. Trim removes start and
	 * end characters &lt;= 32. To strip whitespace use
	 * {@link #stripToEmpty(String)}.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.trimToEmpty(null)          = ""
	 * StringUtils.trimToEmpty("")            = ""
	 * StringUtils.trimToEmpty("     ")       = ""
	 * StringUtils.trimToEmpty("abc")         = "abc"
	 * StringUtils.trimToEmpty("    abc    ") = "abc"
	 * </pre>
	 * 
	 * @param sourceString
	 *            the String to be trimmed, may be null
	 * @return the trimmed String, or an empty String if <code>null</code> input
	 */
	public static String trimToEmpty(String sourceString) {
		return sourceString == null ? EMPTY : sourceString.trim();
	}

	/**
	 * <p>
	 * Compares two Strings, returning <code>true</code> if they are equal.
	 * </p>
	 * 
	 * <p>
	 * <code>null</code>s are handled without exceptions. Two <code>null</code>
	 * references are considered to be equal. The comparison is case sensitive.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.equals(null, null)   = true
	 * StringUtils.equals(null, "abc")  = false
	 * StringUtils.equals("abc", null)  = false
	 * StringUtils.equals("abc", "abc") = true
	 * StringUtils.equals("abc", "ABC") = false
	 * </pre>
	 * 
	 * @see java.lang.String#equals(Object)
	 * @param string1
	 *            the first String, may be null
	 * @param string2
	 *            the second String, may be null
	 * @return <code>true</code> if the Strings are equal, case sensitive, or
	 *         both <code>null</code>
	 */
	public static boolean equals(String string1, String string2) {
		return string1 == null ? string2 == null : string1.equals(string2);
	}

	/**
	 * <p>
	 * Compares two Strings, returning <code>true</code> if they are equal
	 * ignoring the case.
	 * </p>
	 * 
	 * <p>
	 * <code>null</code>s are handled without exceptions. Two <code>null</code>
	 * references are considered equal. Comparison is case insensitive.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.equalsIgnoreCase(null, null)   = true
	 * StringUtils.equalsIgnoreCase(null, "abc")  = false
	 * StringUtils.equalsIgnoreCase("abc", null)  = false
	 * StringUtils.equalsIgnoreCase("abc", "abc") = true
	 * StringUtils.equalsIgnoreCase("abc", "ABC") = true
	 * </pre>
	 * 
	 * @see java.lang.String#equalsIgnoreCase(String)
	 * @param string1
	 *            the first String, may be null
	 * @param string2
	 *            the second String, may be null
	 * @return <code>true</code> if the Strings are equal, case insensitive, or
	 *         both <code>null</code>
	 */
	public static boolean equalsIgnoreCase(String string1, String string2) {
		return string1 == null ? string2 == null : string1
				.equalsIgnoreCase(string2);
	}

	/**
	 * <p>
	 * Checks if String contains a search character, handling <code>null</code>.
	 * This method uses {@link String#indexOf(int)}.
	 * </p>
	 * 
	 * <p>
	 * A <code>null</code> or empty ("") String will return <code>false</code>.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.contains(null, *)    = false
	 * StringUtils.contains("", *)      = false
	 * StringUtils.contains("abc", 'a') = true
	 * StringUtils.contains("abc", 'z') = false
	 * </pre>
	 * 
	 * @param string
	 *            the String to check, may be null
	 * @param searchChar
	 *            the character to find
	 * @return true if the String contains the search character, false if not or
	 *         <code>null</code> string input
	 */
	public static boolean contains(String string, char searchChar) {
		if (isEmpty(string)) {
			return false;
		}
		return string.indexOf(searchChar) >= 0;
	}

	/**
	 * <p>
	 * Checks if String contains a search String, handling <code>null</code>.
	 * This method uses {@link String#indexOf(String)}.
	 * </p>
	 * 
	 * <p>
	 * A <code>null</code> String will return <code>false</code>.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.contains(null, *)     = false
	 * StringUtils.contains(*, null)     = false
	 * StringUtils.contains("", "")      = true
	 * StringUtils.contains("abc", "")   = true
	 * StringUtils.contains("abc", "a")  = true
	 * StringUtils.contains("abc", "z")  = false
	 * </pre>
	 * 
	 * @param string
	 *            the String to check, may be null
	 * @param searchString
	 *            the String to find, may be null
	 * @return true if the String contains the search String, false if not or
	 *         <code>null</code> string input
	 */
	public static boolean contains(String string, String searchString) {
		if (string == null || searchString == null) {
			return false;
		}
		return string.indexOf(searchString) >= 0;
	}

	/**
	 * <p>
	 * Checks if String contains a search String irrespective of case, handling
	 * <code>null</code>. Case-insensitivity is defined as by
	 * {@link String#equalsIgnoreCase(String)}.
	 * 
	 * <p>
	 * A <code>null</code> String will return <code>false</code>.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.contains(null, *) = false
	 * StringUtils.contains(*, null) = false
	 * StringUtils.contains("", "") = true
	 * StringUtils.contains("abc", "") = true
	 * StringUtils.contains("abc", "a") = true
	 * StringUtils.contains("abc", "z") = false
	 * StringUtils.contains("abc", "A") = true
	 * StringUtils.contains("abc", "Z") = false
	 * </pre>
	 * 
	 * @param string
	 *            the String to check, may be null
	 * @param searchString
	 *            the String to find, may be null
	 * @return true if the String contains the search String irrespective of
	 *         case or false if not or <code>null</code> string input
	 */
	public static boolean containsIgnoreCase(String string, String searchString) {
		if (string == null || searchString == null) {
			return false;
		}

		int len = searchString.length();
		int max = string.length() - len;
		for (int i = 0; i <= max; i++) {
			if (string.regionMatches(true, i, searchString, 0, len)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <pre>
	 * 功能: 判断字符串是否为空字符或NULL
	 * </pre>
	 * 
	 * @param string
	 * @return boolean
	 */
	public static boolean IsNullOrEmpty(String string) {
		return string == null || string.equals("");
	}

	/**
	 * <pre>
	 * 功能: 判断一组字符串是否为空字符或NULL
	 * </pre>
	 * 
	 * @param sourceString
	 * @return boolean
	 */
	public static boolean ArrayStringIsNullOrEmpty(String... strings) {
		for (String string : strings) {
			if (IsNullOrEmpty(string)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * <pre>
	 * 功能: 字符串追加字符串,中间包含分隔符
	 * </pre>
	 * 
	 * @param tag
	 * @param sourceString
	 * @param appendString
	 */
	public static synchronized String AppendStringWithTag(String tag,
			String sourceString, String appendString) {
		if (sourceString == null || sourceString.equals("")) {
			sourceString = appendString;
		} else {
			sourceString += (tag + appendString);
		}
		return sourceString;
	}

	/**
	 * <pre>
	 * 功能: 字符串追加字符串,中间包含分隔符
	 * </pre>
	 * 
	 * @param tag
	 * @param sourceBuilder
	 * @param appendString
	 * @return StringBuilder
	 */
	public static synchronized StringBuilder AppendStringWithTag(String tag,
			StringBuilder sourceBuilder, String appendString) {
		if (sourceBuilder == null) {
			sourceBuilder = new StringBuilder();
		}

		if (sourceBuilder.length() == 0) {
			sourceBuilder.append(appendString);
		} else {
			sourceBuilder.append(tag);
			sourceBuilder.append(appendString);
		}

		return sourceBuilder;
	}

	/**
	 * <pre>
	 * 功能: 特殊字符串转换为HTML标签
	 * 1. 为了避免数据类容破换JS中的JSON格式.
	 * 2. 为了保存字符格式在HTML中显示一致.
	 * </pre>
	 * 
	 * @param target
	 * @return String 创建时间:2011-5-9 11:41:22
	 */
	public static synchronized String HtmEncode(String target) {
		StringBuffer stringbuffer = new StringBuffer();

		int j = target.length();
		for (int i = 0; i < j; i++) {
			char charCode = target.charAt(i);
			switch (charCode) {
			case 60:
				stringbuffer.append("&lt;");
				break;

			case 62:
				stringbuffer.append("&gt;");
				break;

			case 38:
				stringbuffer.append("&amp;");
				break;

			case 34:
				stringbuffer.append("&quot;");
				break;

			case 169:
				stringbuffer.append("&copy;");
				break;

			case 174:
				stringbuffer.append("&reg;");
				break;

			case 165:
				stringbuffer.append("&yen;");
				break;

			case 8364:
				stringbuffer.append("&euro;");
				break;

			case 8482:
				stringbuffer.append("&#153;");
				break;

			case 13:
				if (i < j - 1 && target.charAt(i + 1) == 10) {
					stringbuffer.append("<br>");
					i++;
				}
				break;

			case 32:
				if (i < j - 1 && target.charAt(i + 1) == ' ') {
					stringbuffer.append(" &nbsp;");
					i++;
					break;
				}

			default:
				stringbuffer.append(charCode);
				break;
			}
		}

		return stringbuffer.toString();
	}

	public static String escape(String sourceString) {
		StringBuffer tempStringBuffer = new StringBuffer();
		tempStringBuffer.ensureCapacity(sourceString.length() * 6);
		for (int i = 0; i < sourceString.length(); i++) {
			char tempCharCode = sourceString.charAt(i);
			if (Character.isDigit(tempCharCode)
					|| Character.isLowerCase(tempCharCode)
					|| Character.isUpperCase(tempCharCode)) {
				tempStringBuffer.append(tempCharCode);
			} else if (tempCharCode < 256) {
				tempStringBuffer.append("%");
				if (tempCharCode < 16) {
					tempStringBuffer.append("0");
				}
				tempStringBuffer.append(Integer.toString(tempCharCode, 16));
			} else {
				tempStringBuffer.append("%u");
				tempStringBuffer.append(Integer.toString(tempCharCode, 16));
			}
		}

		return tempStringBuffer.toString();
	}

	/**
	 * <pre>
	 * 功能: 数组转换为字符串
	 * </pre>
	 * 
	 * @param objects
	 * @param tag
	 * @return String
	 */
	public static synchronized String ArrayConverString(Object[] objects,
			String tag) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Object object : objects) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(tag);
			}

			stringBuilder.append(object);
		}
		return stringBuilder.toString();
	}

	/**
	 * IP转换成10位数字
	 * 
	 * @param ip
	 *            IP
	 * @return 10位数字
	 */
	public static long ip2num(String ip) {
		long ipNum = 0;
		try {
			if (ip != null) {
				String ips[] = ip.split("\\.");
				for (int i = 0; i < ips.length; i++) {
					int k = Integer.parseInt(ips[i]);
					ipNum = ipNum + k * (1L << ((3 - i) * 8));
				}
			}
		} catch (Exception e) {
		}
		return ipNum;
	}

	// 将十进制整数形式转换成127.0.0.1形式的ip地址
	public static String num2ip(long longIp) {
		StringBuffer sb = new StringBuffer("");
		// 直接右移24位
		sb.append(String.valueOf((longIp >>> 24)));
		sb.append(".");
		// 将高8位置0，然后右移16位
		sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
		sb.append(".");
		// 将高16位置0，然后右移8位
		sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
		sb.append(".");
		// 将高24位置0
		sb.append(String.valueOf((longIp & 0x000000FF)));
		return sb.toString();
	}

	/**
	 * 在字符串头部添加字符，使原字符串达到指定的长度
	 * 
	 * @param source
	 *            源字符串
	 * @param filling
	 *            填充字符
	 * @param lastLen
	 *            填充后的总长度
	 * @return 如果源字符串长度大于lastLen，返回原字符串，否则用filling填充源字符串后返回结果
	 */
	public static String fillString(String source, char filling, int lastLen) {
		StringBuffer temp = new StringBuffer();
		if (source.length() < lastLen) {
			int fillLen = lastLen - source.length();
			for (int i = 0; i < fillLen; i++) {
				temp.append(filling);
			}
		}
		temp.append(source);
		return temp.toString();
	}

	/**
	 * 在字符串头部添加字符，使原字符串达到指定的长度
	 * 
	 * @param source
	 *            源字符串
	 * @param filling
	 *            填充字符
	 * @param lastLen
	 *            填充后的总长度
	 * @return 如果源字符串长度大于lastLen，返回原字符串，否则用filling填充源字符串后返回结果
	 */
	public static String fillString(int source, char filling, int lastLen) {
		return fillString(String.valueOf(source), filling, lastLen);
	}

	/**
	 * 在字符串头部添加字符，使原字符串达到指定的长度
	 * 
	 * @param source
	 *            源字符串
	 * @param filling
	 *            填充字符
	 * @param lastLen
	 *            填充后的总长度
	 * @return 如果源字符串长度大于lastLen，返回原字符串，否则用filling填充源字符串后返回结果
	 */
	public static String fillStringRight(String source, char filling,
			int lastLen) {
		StringBuffer temp = new StringBuffer();
		temp.append(source);
		if (source.length() < lastLen) {
			int fillLen = lastLen - source.length();
			for (int i = 0; i < fillLen; i++) {
				temp.append(filling);
			}
		}
		return temp.toString();
	}

	/**
	 * 格式化一个数字字符串为9,999,999的格式,如果字符串无法格式化返回0
	 * 
	 * @param money
	 *            数字字符串
	 * @return 格式化好的数字字符串
	 */
	public static String formatString(Object object) {
		String formatMoney = "0";
		String srcString = objectToString(object);
		try {
			DecimalFormat myformat3 = new DecimalFormat();
			myformat3.applyPattern("###,##0");
			long n = Long.parseLong(srcString);
			formatMoney = myformat3.format(n);
		} catch (Exception exception) {
			//
		}

		return formatMoney;
	}

	/**
	 * 格式化一个数字字符串为9,999,999.99的格式,如果字符串无法格式化返回0.00
	 * 
	 * @param money
	 *            数字字符串
	 * @return 格式化好的数字字符串
	 */
	public static String formatMoney(String money) {
		String formatMoney = "0.00";
		try {
			DecimalFormat myformat3 = new DecimalFormat();
			myformat3.applyPattern("###,##0.00");
			double n = Double.parseDouble(money);
			formatMoney = myformat3.format(n);
		} catch (Exception exception) {
			//
		}

		return formatMoney;
	}

	/**
	 * 格式化一个数字字符串为9,999,999.99的格式,如果字符串无法格式化返回0.00
	 * 
	 * @param money
	 *            数字字符串
	 * @return 格式化好的数字字符串
	 */
	public static String formatMoney(BigDecimal money) {
		String formatMoney = "0.00";
		try {
			DecimalFormat myformat3 = new DecimalFormat();
			myformat3.applyPattern("###,##0.00");
			formatMoney = myformat3.format(money);
		} catch (Exception exception) {
			//
		}

		return formatMoney;
	}

	/**
	 * 格式化一个数字字符串为9,999,999的格式,如果字符串无法格式化返回0
	 * 
	 * @param money
	 *            数字字符串
	 * @return 格式化好的数字字符串
	 */
	public static String formatNumber(Object object) {
		if (object == null || object.toString().equals("")) {
			return "0.00";
		}
		DecimalFormat myformat3 = new DecimalFormat();
		myformat3.applyPattern("0.00");
		String result = myformat3.format(Double.parseDouble(object.toString()));
		return result;
	}

	public static String formatPercent(Object object) {
		if (object == null || object.toString().equals("")) {
			return "0.00";
		}
		DecimalFormat myformat3 = new DecimalFormat();
		myformat3.applyPattern("0.00");
		String result = myformat3.format(Double.parseDouble(object.toString()));
		return result + "%";
	}

	/**
	 * 格式化sql语句，使符合标准select x,sum(y) from table where 1=1 group by x [order by
	 * x]
	 * 
	 * @param sql
	 * @return
	 */
	public static String formatSql(String sql) {
		sql = sql.replaceAll("\\s+", " ").replaceAll(" from ", " FROM ")
				.replaceAll(" from\\(", " FROM \\(")
				.replaceAll(" where ", " WHERE ");
		return sql;
	}

	public static List<Map<String, Object>> jsonStrToList(String str) {
		List<Map<String, Object>> list0 = new ArrayList<Map<String, Object>>();
		JSONArray strJson = JSONArray.fromObject(str);
		for (Iterator i = strJson.iterator(); i.hasNext();) {
			JSONObject jsonObj = JSONObject.fromObject(i.next());
			Map<String, Object> map = new HashMap<String, Object>();
			for (Iterator ii = jsonObj.keys(); ii.hasNext();) {
				String s = ii.next().toString();
				Object obj = jsonObj.opt(s);
				map.put(s, obj);
			}
			list0.add(map);
		}
		return list0;
	}

	public static JSONArray ascSort(JSONArray arr, final String[] order) {
		Object[] objs = arr.toArray();
		Arrays.sort(objs, new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				for (String s : order) {
					Object obj1 = JSONObject.fromObject(o1).opt(s);
					Object obj2 = JSONObject.fromObject(o2).opt(s);
					int compare = 0;
					if (obj1 instanceof Integer && obj2 instanceof Integer) {
						int i1 = Integer.parseInt(obj1.toString());
						int i2 = Integer.parseInt(obj2.toString());
						if (i1 < i2)
							compare = -1;
						else
							compare = 1;
					} else if (obj1 instanceof Number && obj2 instanceof Number) {
						double i1 = Double.parseDouble(obj1.toString());
						double i2 = Double.parseDouble(obj2.toString());
						if (i1 < i2)
							compare = -1;
						else
							compare = 1;
					} else if (obj1 instanceof String) {
						if (obj1.toString().matches(
								"^20[1-9]{2}-[0-12]{2}-[0-31]{2}")) {
							try {
								Date d1 = sdf.parse(obj1.toString());
								Date d2 = sdf.parse(obj2.toString());
								compare = d1.compareTo(d2);
							} catch (ParseException e) {
								e.printStackTrace();
							}

						} else {
							compare = obj1.toString()
									.compareTo(obj2.toString());
						}
					}
					if (compare != 0)
						return compare;
				}
				return 0;
			}
		});
		return JSONArray.fromObject(objs);
	}

	public static JSONArray descSort(JSONArray arr, final String[] order) {
		Object[] objs = arr.toArray();
		Arrays.sort(objs, new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				for (String s : order) {
					Object obj1 = JSONObject.fromObject(o1).opt(s);
					Object obj2 = JSONObject.fromObject(o2).opt(s);
					int compare = 0;
					if (obj1 instanceof Integer && obj2 instanceof Integer) {
						int i1 = Integer.parseInt(obj1.toString());
						int i2 = Integer.parseInt(obj2.toString());
						if (i1 > i2)
							compare = -1;
						else
							compare = 1;
					} else if (obj1 instanceof Number && obj2 instanceof Number) {
						double i1 = Double.parseDouble(obj1.toString());
						double i2 = Double.parseDouble(obj2.toString());
						if (i1 > i2)
							compare = -1;
						else
							compare = 1;
					} else if (obj1 instanceof String) {
						if (obj1.toString().matches(
								"^20[1-9]{2}-[0-12]{2}-[0-31]{2}")) {
							try {
								Date d1 = sdf.parse(obj1.toString());
								Date d2 = sdf.parse(obj2.toString());
								compare = d2.compareTo(d1);
							} catch (ParseException e) {
								e.printStackTrace();
							}

						} else {
							compare = obj2.toString()
									.compareTo(obj1.toString());
						}
					}
					if (compare != 0)
						return compare;
				}
				return 0;
			}
		});
		return JSONArray.fromObject(objs);
	}

//	public static String byteToObj(byte[] bytes) {
//		Object obj = null;
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		try {
//			ObjectOutputStream oos = new ObjectOutputStream(bos);
//			bos.write(bytes);
//			oos.writeObject(obj);
//			return obj.toString();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

	public static byte[] objToByte(Object obj) {

		return null;
	}

	public static String formatDate(Object obj) {
		if (obj instanceof Date) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			return sdf.format(obj);
		}
		return null;
	}

	/**
	 * 字符串数组转字符串,以concatChar字符串做为分割
	 * 
	 * @param params
	 * @param concatChar
	 * @return
	 */
	public static String arrayToString(String[] params, String concatChar) {
		String result = "";
		if (params != null && params.length > 0) {
			for (String param : params) {
				result += param.concat(concatChar);
			}
			result = result.substring(0, result.length() - 1);
		} else {
			result = null;
		}

		return result;
	}

	/**
	 * 获取客户端的IP地址
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	public static boolean isChinaString(String value) {
		Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]+");
		Matcher matcher = pattern.matcher(value);
		return matcher.find();
	}

	public static boolean isPhoneNumber(String value) {
		Pattern pattern = Pattern.compile("\\d{11,15}");
		Matcher matcher = pattern.matcher(value);
		return matcher.find();
	}

	public static boolean isBankNumber(String value) {
		Pattern pattern = Pattern.compile("\\d{16,19}");
		Matcher matcher = pattern.matcher(value);
		return matcher.find();
	}

	public static Double objectToDouble(Object o) {
		if (o == null || o.toString().equals("")) {
			return 0.0;
		}
		return Double.parseDouble(o.toString());
	}

	public static Integer objectToInteger(Object o) {
		if (o == null || o.toString().equals("")) {
			return 0;
		}
		// 测试用
		if (o.toString().contains("-")) {
			return getIntDate(o.toString());
		}
		return Integer.parseInt(o.toString());
	}

	public static String objectToString(Object o) {
		if (o == null) {
			return "";
		}
		return o.toString();
	}

	public static String toTimeString(Object o) {
		if (o == null || o.toString().equals("")) {
			return "00";
		}
		if (o.toString().length() < 2) {
			return "0" + o.toString();
		}
		return o.toString();
	}

	public static int getIntDate(String date) {
		date = date.replace("-", "");
		return Integer.parseInt(date);
	}

	public static String sToh(int s) {
		String result = "";
		int N = s / 3600;
		s = s % 3600;
		int K = s / 60;
		s = s % 60;
		int M = s;
		result = N + "小时" + K + "分钟" + M + "秒";
		result = N + ":" + K + ":" + M;
		return result;
	}

	public static String md5(String str) {

		return str == null ? null : DigestUtils.md5Hex(str);
	}

	/**
	 * 字符串前面填充0到指定长度
	 * 
	 * @param str
	 * @param len
	 * @return
	 */
	public static String addZero(String str, int len) {
		if (isBlank(str))
			return null;
		else {
			if (str.length() > len)
				return str;
			else {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < len - str.length(); i++) {
					sb.append(0);
				}
				sb.append(str);
				return sb.toString();
			}
		}
	}

	/**
	 * @param parentId
	 * @param sequence 排序
	 * @param level
	 * @return
	 */
	public static String generatorKeyByParentAndLevel(String parentId,
			Integer sequence, Integer level) {
		if (parentId == null || sequence == null || level == null || level < 1)
			return null;
		String[] KeyGeneratorRule = PropertiesUtil.getStringValue(
				"KeyGeneratorRule").split(",");
		if (KeyGeneratorRule.length < level)
			return null;
		int len = Integer.parseInt(KeyGeneratorRule[level - 1]);
		return parentId + StringHelper.addZero(Integer.toString(sequence), len);
	}

	public static void main(String[] args) {
		System.out.println(formatString("12323.00"));
	}
}
