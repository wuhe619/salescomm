package com.bdaim.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.AppConfig;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.crm.utils.R;
import com.bdaim.util.StringUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Aspect
@Component
public class ResponseBodyInterceptor {
	/*@Resource
	private LogService logService;*/

    private static final Logger log = LoggerFactory.getLogger("Response");

    private static final String APP_NAME = AppConfig.getApp();

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
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("pointCutMethod()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        JSONObject result = new JSONObject();
        result.put("code", 200);
//		result.put("data", new JSONArray());
        result.put("_message", "成功");
        String className = null;
        String method = null;
        long begin = System.currentTimeMillis();
        try {
            method = pjp.getSignature().getName();
            className = pjp.getTarget().getClass().getSimpleName();

            Object o = pjp.proceed();
            if (o instanceof ResponseInfo || o instanceof R || "ErrorController".equals(className)) {
                return o;
            }
            //else if (o instanceof Map) {
            //  return o;
            //}
            if (o != null) {
                String str = null;
                if (o instanceof String) {
                    str = o.toString();
                } else {
                    str = JSONObject.toJSONString(o);
                }

                String reg0 = "\\{.*\\}";
                String reg1 = "\\[.*\\]";
                if (str.matches(reg0)) {
                    log.debug("json string!");
                    JSONObject json = JSONObject.parseObject(str);
                    if (json.containsKey("code")) {
                        result.put("code", json.get("code"));
                    }
                    if (json.containsKey("_message")) {
                        result.put("_message", json.getString("_message"));
                    }
                    if (json.containsKey("message")) {
                        result.put("msg", json.getString("message"));
                    }
                    if (json.containsKey("data")) {
                        result.put("data", json.get("data"));
                    } else {
                        result.put("data", json);
                    }
                } else if (str.matches(reg1)) {
                    log.debug("json array string!");
                    result.put("data", JSONArray.parseArray(str));
                } else {
                    result.put("data", str);
                }
            }
        } catch (TouchException e) {
            if (StringUtil.isEmpty(e.getCode())) {
                result.put("code", 300);
            } else {
                result.put("code", e.getCode());
            }
            result.put("_message", "失败");
            result.put("msg", "失败");
            log.error(e.getMessage());
        }
        log.info("method:" + className + "." + method + " time:" + (System.currentTimeMillis() - begin) + "ms " + "response:" + result);
        return JSON.toJSONString(result);
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param joinPoint 切点
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
