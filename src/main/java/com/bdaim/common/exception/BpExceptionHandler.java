package com.bdaim.common.exception;

import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @description:
 * @auther: Chacker
 * @date: 2019/8/2 09:12
 */
@RestControllerAdvice
public class BpExceptionHandler {
    public static final Logger log = LoggerFactory.getLogger(BpExceptionHandler.class);

    /**
     * 参数解析失败
     *
     * @param
     * @return
     * @auther Chacker
     * @date 2019/8/2 9:17
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseInfo handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("could_not_read_json", e);
        return new ResponseInfoAssemble().failure(HttpStatus.BAD_REQUEST.value(), "could_not_read_json");
    }

    /**
     * 请求方式不支持，如，为该方法指定了Get请求方式，当客户端发送该方法的POST请求时，抛出该异常
     *
     * @param
     * @return
     * @auther Chacker
     * @date 2019/8/2 9:22
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseInfo handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("request_method_not_supported", e);
        return new ResponseInfoAssemble().failure(HttpStatus.BAD_REQUEST.value(), "request_method_not_supported");
    }

    /**
     * 参数验证失败
     *
     * @param
     * @return
     * @auther Chacker
     * @date 2019/8/2 9:19
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseInfo handleValidationException(MethodArgumentNotValidException e) {
        log.error("parameter_validation_exception...", e);
        return new ResponseInfoAssemble().failure(HttpStatus.BAD_REQUEST.value(), "parameter_validation_exception");
    }

    /**
     * 500 异常，服务器
     *
     * @param
     * @return
     * @auther Chacker
     * @date 2019/8/2 9:24
     */
    @ExceptionHandler(Exception.class)
    public ResponseInfo handleException(Exception e) {
        log.error("Internal Server Error...", e);
        if (e instanceof org.springframework.web.servlet.NoHandlerFoundException) {
            return new ResponseInfoAssemble().failure(HttpStatus.NOT_FOUND.value(),
                    "Not Found Exception");
        } else {
            return new ResponseInfoAssemble().failure(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e.getMessage());
        }
    }

}
