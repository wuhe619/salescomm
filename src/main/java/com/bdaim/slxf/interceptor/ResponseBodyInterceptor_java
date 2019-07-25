package com.bdaim.slxf.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.annotation.Resource;

import com.bdaim.common.util.StringUtil;
import com.bdaim.slxf.exception.TouchException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@Aspect
@Component
public class ResponseBodyInterceptor {
	/*@Resource
	private LogService logService;*/

	private static final Log log = LogFactory
			.getLog(ResponseBodyInterceptor.class);

	// @Pointcut("execution(* com.bfd.controller.*.*(..))")
	@Pointcut("@annotation(org.springframework.web.bind.annotation.ResponseBody)")
	private void pointCutMethod() {
	}


	@AfterReturning(pointcut = "pointCutMethod()", returning = "result")
	public void doAfterReturn(JoinPoint joinPoint, String result) {
	}

	@AfterThrowing(pointcut = "pointCutMethod()", throwing = "e")
	public void doAfterException(JoinPoint joinPoint, Throwable e) {
//		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
//				.getRequestAttributes()).getRequest();
//		HttpSession session = request.getSession();
//		// 读取session中的用户
//		User user = (User) session.getAttribute(Constant.CURRENT_USER);
//		// 获取请求ip
//		String ip = request.getRemoteAddr();
//		// 获取用户请求方法的参数并序列化为JSON格式字符串
//		String params = "";
//		if (joinPoint.getArgs() != null && joinPoint.getArgs().length > 0) {
//			for (int i = 0; i < joinPoint.getArgs().length; i++) {
//			}
//		}
	}

	@After("pointCutMethod()")
	public void doAfter() {
	}

	/**
	 * 
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	@Around("pointCutMethod()")
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		JSONObject result = new JSONObject();
		result.put("code", 200);
		result.put("data", new JSONArray());
		result.put("_message", "接口调用成功");
		String className = null;
		String method = null;
		long begin = System.currentTimeMillis();
		try {
			method = pjp.getSignature().getName();
			className = pjp.getTarget().getClass().getSimpleName();
			
			Object o = pjp.proceed();
			if (o != null) {
				String str = o.toString();
				String reg0 = "\\{.*\\}";
				String reg1 = "\\[.*\\]";
				if (str.matches(reg0)) {
					JSONObject json = JSONObject.parseObject(str);
					if (json.containsKey("code")) {
						result.put("code",json.get("code"));
					} 
					if (json.containsKey("_message")) {
						result.put("_message", json.getString("_message"));
					} 
					if(json.containsKey("data")) {
						result.put("data", json.get("data"));
					}else{
						result.put("data", json);
					}
				} else if (str.matches(reg1)) {
					result.put("code", 200);
					result.put("data", JSONArray.parseArray(str));
					result.put("_message", "接口调用成功");
				} else{
					result.put("code", 200);
					result.put("data", str);
					result.put("_message", "接口调用成功");
				}
			}
		} catch (TouchException e) {
			if(StringUtil.isEmpty(e.getCode())) {
				result.put("code", 300);
			}
			result.put("code",e.getCode());
			result.put("_message", e.getMessage());
			e.printStackTrace();
		}
		log.info("method:" + className + "." + method + " time:"+ (System.currentTimeMillis() - begin) + "ms " + "response:"+result);
		return JSON.toJSONString(result);
	}

	/**
	 * 获取注解中对方法的描述信息 用于Controller层注解
	 * 
	 * @param joinPoint
	 *            切点
	 * @return 方法描述
	 * @throws Exception
	 */
	public static String getControllerMethodDescription(JoinPoint joinPoint)
			throws Exception {
		String targetName = joinPoint.getTarget().getClass().getName();
		String methodName = joinPoint.getSignature().getName();
		Object[] arguments = joinPoint.getArgs();
		Class targetClass = Class.forName(targetName);
		Method[] methods = targetClass.getMethods();
		String description = "";
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				Class[] clazzs = method.getParameterTypes();
				if (clazzs.length == arguments.length) {
					Annotation[] annotations = method.getAnnotations();
					for (Annotation a : annotations) {
						Class<?>[] interfaces = a.getClass().getInterfaces();
						for (Class inter : interfaces) {
							System.out.println(inter.getName());
						}
					}
					break;
				}
			}
		}
		return description;
	}
}
