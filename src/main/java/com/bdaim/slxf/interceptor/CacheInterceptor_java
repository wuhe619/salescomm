package com.bdaim.slxf.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.bdaim.common.util.CacheHelper;
import com.bdaim.common.util.ConfigUtil;
import com.bdaim.common.util.LogUtil;
import com.bdaim.slxf.annotation.CacheAnnotation;
import com.bdaim.slxf.annotation.CacheAnnotation.CacheType;

@Aspect
@Component
public class CacheInterceptor {
	private static final Log log = LogFactory.getLog(CacheInterceptor.class);
	String[] querynames = {"query", "get", "find", "load"};
	String RETURN_NULL_STRING = "RETURN_NULL_STRING";
	
	@Pointcut("@annotation(com.bdaim.slxf.annotation.CacheAnnotation)")
	private void pointCutMethod() {
	}

	@AfterThrowing(pointcut = "pointCutMethod()", throwing = "e")
	public void doAfterException(JoinPoint joinPoint, Throwable e) {
	}

	@Around("pointCutMethod()")
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		boolean is_cache = ConfigUtil.getInstance().getBoolean("bfd.tag.cache.enable", false);
		if (is_cache)
		{
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
					.getRequestAttributes()).getRequest();
			String uri = request.getRequestURI();
			String[] urisegs = uri.trim().split("/");
			String actionname = urisegs[2];
			String funname = urisegs[urisegs.length - 1];
			if (isQueryFunction(funname))
			{//it is a query function , query from cache first...
				Map<String, List<String>> keys = new HashMap<String, List<String>>();
				Map<String, String[]> map = request.getParameterMap();
				for (String s : map.keySet()) {
					keys.put(s, Arrays.asList(map.get(s)));
				}
				String url = uri + keys;
				//the key is actionname + md5(url), to clear by actionname...
				String key = urisegs[2] + "_" + DigestUtils.md2Hex(url);
				String result = null;
				//缓存结果，格式为:{"url":url,"cacheType":cacheType,"cacheKey":cacheKey,"value":value}
//				String _result = CacheHelper.getStringValue(key);
				String _result = CacheHelper.getStringValue(key);
				if (_result != null){
//					JSONObject json = JSONObject.parseObject(_result);
//					String cacheType = json.getString("cacheType");
//					String cacheKey = json.getString("cacheKey");
//					String value =  json.getString("value");
					LogUtil.info("get one value from cache, key:" + key);
					if (RETURN_NULL_STRING.equals(_result))
						return null;
					else
						return _result;
				}
				LogUtil.info("can not get one value from cache, key:" + key + ",to query from service");
				Object o = pjp.proceed();
				if (null != o)
					CacheHelper.setValue(key, o.toString());
				else
					CacheHelper.setValue(key, RETURN_NULL_STRING);
				return o;
			}
			else
			{//it is not a query function,then clear cache , which start with actionname
				CacheHelper.removePreKey(actionname);
				LogUtil.info("clear datas from cache, actionname:" + actionname);
				Object o = pjp.proceed();
				return o;
			}
		}
		return pjp.proceed();
	}

	public boolean isQueryFunction(String name)
	{
		for (String onename : querynames)
		{
			if (name.startsWith(onename))
				return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param joinPoint
	 *            切点
	 * @return 方法描述
	 * @throws Exception
	 */
	public static Map<String, Object> getMethodDescription(JoinPoint joinPoint)
			throws Exception {
		String targetName = joinPoint.getTarget().getClass().getName();
		String methodName = joinPoint.getSignature().getName();
		Object[] arguments = joinPoint.getArgs();
		Class targetClass = Class.forName(targetName);
		Method[] methods = targetClass.getMethods();
		Map<String, Object> result = new HashMap<String, Object>();
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				Class[] clazzs = method.getParameterTypes();
				if (clazzs.length == arguments.length) {
					String description = method.getAnnotation(
							CacheAnnotation.class).description();
					CacheType cacheType = method.getAnnotation(
							CacheAnnotation.class).cacheType();
					String[] cacheKey = method.getAnnotation(
							CacheAnnotation.class).cacheKey();
					result.put("description", description);
					result.put("cacheType", cacheType);
					result.put("cacheKey", cacheKey);
					break;
				}
			}
		}
		return result;
	}
	
	public static void main(String[] args) {
		Object o = null;
		Object a = (Serializable)o;
		System.out.println("d");
		
	}
}
