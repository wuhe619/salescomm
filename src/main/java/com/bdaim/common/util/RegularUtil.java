package com.bdaim.common.util;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 正则校验公共方法
 * 2017-02-13
 * @author 李超(d大)
 *
 */
public class RegularUtil {
	
	/**
	 * 目前只验证是否为11位作为登录的校验
	 * @param mobiles 参数手机号
	 * @return
	 */
	public static boolean isMobileNO(String mobiles){  
		Pattern p = Pattern.compile("^\\d{11}$");  
		if(StringUtils.isEmpty(mobiles)){
			return false;
		}
		Matcher m = p.matcher(mobiles);
		return m.matches();  
		  
	}
	/**
	 * 是否为纯数字校验
	 * @param mobiles 参数手机号
	 * @return
	 */
	public static boolean isNum(String num){  
		Pattern p = Pattern.compile("^\\d$");  
		if(StringUtils.isEmpty(num)){
			return false;
		}
		Matcher m = p.matcher(num);
		return m.matches();  
		  
	}

}
