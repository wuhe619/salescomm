package com.bdaim.common.exception;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author chengning@salescomm.net
 * @description TODO
 * @date 2020/4/15 17:14
 */
@Component
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    public static final Logger LOG = LoggerFactory.getLogger(AsyncExceptionHandler.class);

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        LOG.info("Async method: {} has uncaught exception,params:{}", method.getName(), JSON.toJSONString(params));

        if (ex instanceof AsyncException) {
            AsyncException asyncException = (AsyncException) ex;
            LOG.info("asyncException:{}", asyncException.getErrorMessage());
        }

        LOG.info("Exception :");
        ex.printStackTrace();
    }
}
