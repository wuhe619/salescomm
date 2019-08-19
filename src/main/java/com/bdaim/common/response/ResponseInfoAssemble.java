package com.bdaim.common.response;

import org.springframework.http.HttpStatus;

/**
 * @description: 聚合了 返回值对象，其中包括自定义的返回值消息体
 * 使用此类来定义返回值
 * @auther: Chacker
 * @date: 2019/8/2 09:00
 */
public class ResponseInfoAssemble {
    /**
     * 接口调用成功后的返回内容
     *
     * @param obj 返回值对象，可以是任意基本类型或引用类型
     * @param <T>
     * @return
     */
    public <T> ResponseInfo success(T obj) {
        ResponseInfo<T> rb = new ResponseInfo<T>();
        rb.setCode(HttpStatus.OK.value());
        rb.setData(obj);
        return rb;
    }

    /**
     * 失败，异常消息
     *
     * @param code  自定义状态码(建议使用Http状态码常量)
     * @param message 错误信息
     * @return
     * @auther Chacker
     * @date 2019/8/2 9:07
     */
    public <T> ResponseInfo failure(int code, String message) {
        ResponseInfo<T> rb = new ResponseInfo<T>();
        rb.setCode(code);
        rb.setMessage(message);
        return rb;
    }
}
