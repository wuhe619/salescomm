package com.bdaim.crm.common.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.util.StringUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Aspect
public class PageRequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(PageRequestInterceptor.class);
    private final String executeRoutes = "execution(* com.bdaim.crm.erp..*Controller.*(..))";

    @Before(executeRoutes)
    public void doBasePageRequest(JoinPoint joinPoint){
        boolean isJSON = false;
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        logger.error("进来了吗？");
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getName();
        if("init".equals(methodName)){
            return;
        }
        String contentType=request.getHeader("Content-Type");
        if(StringUtil.isNotEmpty(contentType)){
            if("application/json".equals(request.getHeader("Content-Type").toLowerCase())){
                isJSON = true;
            }
        }
        logger.error(" is JSON {}",isJSON);
        LocalVariableTableParameterNameDiscoverer paramNames = new LocalVariableTableParameterNameDiscoverer();
        Object[] args = joinPoint.getArgs();
        String[] params = paramNames.getParameterNames(method);
        List<Object> filteredArgs = Arrays.stream(args)
                .filter(arg -> (!(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse)))
                .collect(Collectors.toList());

//        boolean isJson=controller.getHeader("Content-Type")!=null&&controller.getHeader("Content-Type").toLowerCase().contains("application/json");
        JSONObject rqsJson = new JSONObject();
        rqsJson.put("rqsMethod", methodName);
        if (CollectionUtils.isEmpty(filteredArgs)) {
            rqsJson.put("rqsParams", null);
        } else {
            //拼接请求参数
//            Map<String, Object> rqsParams = IntStream.range(0, filteredArgs.size())
//                    .boxed()
//                    .collect(Collectors.toMap(j -> params[j], j -> filteredArgs.get(j)));
//            rqsJson.put("rqsParams", rqsParams);
            for(int i=0;i<filteredArgs.size();i++){
                Object object = filteredArgs.get(i);
                if(object instanceof BasePageRequest){
                    Class clazz=null;
                    Parameter[] parameters=  method.getParameters();
                    for (Parameter parameter:parameters){
                        if(BasePageRequest.class.isAssignableFrom(parameter.getType())){
                            Type parameterizedType=parameter.getParameterizedType();
                            if (parameterizedType instanceof ParameterizedType) {
                                Type[] paramsType = ((ParameterizedType) parameterizedType).getActualTypeArguments();
                                clazz= TypeUtils.getClass(paramsType[0]);
                            }
                            break;
                        }
                    }
                    logger.error(JSON.toJSONString((BasePageRequest)object));
//                    return isJson?new BasePageRequest(controller.getRawData(),clazz):new BasePageRequest(controller.getKv(),clazz);
//                    object = isJSON?new BasePageRequest(request.getReader(),clazz)
//                            :new BasePageRequest(controller.getKv(),clazz);

                }
            }
        }
        logger.info(methodName + "请求信息为：" + rqsJson.toJSONString());

    }


}
