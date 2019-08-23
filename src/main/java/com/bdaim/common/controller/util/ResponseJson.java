package com.bdaim.common.controller.util;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/27
 * @description
 */
public class ResponseJson<T> extends ResponseCommon {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
}
