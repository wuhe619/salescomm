package com.bdaim.crm.common.interceptor;

import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.jfinal.json.Json;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.lang.reflect.*;
import java.util.*;

/**
 * 分页参数拦截
 *
 * @author Chacker
 * @date 2020/3/12
 */
@Component
@Aspect
public class PageRequestInterceptor {

    public static final Logger LOGGER = LoggerFactory.getLogger(PageRequestInterceptor.class);
    public static final int BUFFER_SIZE = 1024 * 8;
    public static final String EXECUTE_ROUTES = "execution(* com.bdaim.crm.erp..*Controller.*(..))";

    @Around(EXECUTE_ROUTES)
    public Object doBasePageRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        //是否是application/json格式的请求
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        boolean isJson = request.getHeader("Content-Type") != null &&
                request.getHeader("Content-Type").toLowerCase().contains("application/json");
        //从切点上获取目标方法
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        LOGGER.debug("methodSignature :" + methodSignature);
        Method method = methodSignature.getMethod();
        //如果设置了泛型注解 @ClassTypeCheck
        if (method.isAnnotationPresent(ClassTypeCheck.class)) {
            //获取注解对象
            ClassTypeCheck classTypeCheck = method.getAnnotation(ClassTypeCheck.class);
            //获取注解参数值
            Class clazz = classTypeCheck.classType();
            //把controller类的初始化方法排除
            if ("init".equals(method.getName())) {
                return joinPoint.proceed();
            }
            if (!isJson) {
                Object[] args = joinPoint.getArgs();
                //请求数据类型为 request params
                Enumeration<String> parameters = request.getParameterNames();
                Map<String, Object> parameterMap = new HashMap<>();
                while (parameters.hasMoreElements()) {
                    String parameter = parameters.nextElement();
                    LOGGER.info(parameter);
                    parameterMap.put(parameter, request.getParameter(parameter));
                }
                args[0] = new BasePageRequest(Json.getJson().toJson(parameterMap), clazz);
                Object result = joinPoint.proceed(args);
                return result;
            } else {
                //请求数据类型为 application/json   读取 raw 参数
                Object[] args = joinPoint.getArgs();
                BufferedReader bufferedReader = request.getReader();
                StringWriter writer = new StringWriter();
                try {
                    int read;
                    char[] buf = new char[BUFFER_SIZE];
                    while ((read = bufferedReader.read(buf)) != -1) {
                        writer.write(buf, 0, read);
                    }
                } finally {
                    writer.close();
                }
                String bodyStr = writer.getBuffer().toString();
                args[0] = new BasePageRequest(bodyStr, clazz);
                return joinPoint.proceed(args);
            }
        } else {
            return joinPoint.proceed();
        }
    }

}


