package com.bdaim.crm.common.interceptor;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.crm.common.annotation.ClassTypeCheck;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.exception.ParamValidateException;
import com.bdaim.crm.erp.admin.service.LkAdminRoleService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.aop.Aop;
import com.jfinal.json.Json;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.*;
import java.util.*;

/**
 * CRM注解配置 @Permissions @NotNullValidate @ClassTypeCheck
 *
 * @author Chacker
 * @date 2020/3/12
 */
@Component
@Aspect
public class CrmAnnotationsConfiguration {

    public static final Logger LOGGER = LoggerFactory.getLogger(CrmAnnotationsConfiguration.class);
    public static final int BUFFER_SIZE = 1024 * 8;
    public static final String EXECUTE_ROUTES = "execution(* com.bdaim.crm.erp..*Controller.*(..))";
    @Autowired
    private LkAdminRoleService roleService;

    @Around(EXECUTE_ROUTES)
    public Object doAnnotationsConfiguration(ProceedingJoinPoint joinPoint) throws Throwable {
        //是否是application/json格式的请求
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        boolean isJson = request.getHeader("Content-Type") != null &&
                request.getHeader("Content-Type").toLowerCase().contains("application/json");
        //从切点上获取目标方法
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        LOGGER.debug("methodSignature :" + methodSignature);
        Method method = methodSignature.getMethod();

        //权限功能后台拦截
        permissionsCheck(method);

        //通过注解 @NotNullValidate 对入参进行非空校验
        notNullValidate(method, isJson, request);

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
                String bodyStr = getRequestBody(request);
                args[0] = new BasePageRequest(bodyStr, clazz);
                return joinPoint.proceed(args);
            }
        } else {
            return joinPoint.proceed();
        }
    }

    /**
     * 通过注解 @NotNullValidate 对入参进行非空校验
     *
     * @param method
     * @param isJson
     * @param request
     * @author Chacker
     */
    private void notNullValidate(Method method, Boolean isJson, HttpServletRequest request) throws IOException {
        NotNullValidate[] validates = method.getAnnotationsByType(NotNullValidate.class);
        if (ArrayUtil.isNotEmpty(validates)) {
            for (NotNullValidate validate : validates) {
                //获取注解参数值
                if (!isJson) {
                    //请求数据类型为 request params
                    if (request.getParameter(validate.value()) == null) {
                        throw new ParamValidateException("500", validate.message());
                    }
                } else {
                    //请求数据类型为 application/json   读取 raw 参数
                    String bodyStr = getRequestBody(request);
                    JSONObject jsonObject = JSONObject.parseObject(bodyStr);
                    if (!jsonObject.containsKey(validate.value()) || jsonObject.get(validate.value()) == null) {
                        throw new ParamValidateException("500", validate.message());
                    }
                }
            }
        }
    }

    /**
     * 权限功能后台拦截
     *
     * @param method
     * @author Chacker
     */
    private void permissionsCheck(Method method) {
        if (method.isAnnotationPresent(Permissions.class)) {
            Permissions permissions = method.getAnnotation(Permissions.class);
            if (permissions != null && permissions.value().length > 0) {
                JSONObject jsonObject = roleService.auth(BaseUtil.getUserId());
                //组装应有权限列表
                List<String> arr = queryAuth(jsonObject, "");
                boolean isRelease = false;
                for (String key : permissions.value()) {
                    if (!isRelease) {
                        if (arr.contains(key)) {
                            isRelease = true;
                        }
                    }
                }
                if (!isRelease) {
                    throw new ParamValidateException("500", "无权访问");
                }
            }
        }
    }

    /**
     * 请求数据类型为 application/json   读取 raw 参数
     *
     * @param request
     * @return
     * @throws IOException
     * @author Chacker
     */
    private String getRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
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
        return writer.getBuffer().toString();
    }

    @SuppressWarnings("unchecked")
    private List<String> queryAuth(Map<String, Object> map, String key) {
        List<String> permissions = new ArrayList<>();
        map.keySet().forEach(str -> {
            if (map.get(str) instanceof Map) {
                permissions.addAll(this.queryAuth((Map<String, Object>) map.get(str), key + str + ":"));
            } else {
                permissions.add(key + str);
            }
        });
        return permissions;
    }
}


