package com.bdaim.common.controller.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/27
 * @description
 */
public class ResponseCommon {
    public static final int TOKEN_MISS = 10001;
    public static final String TOKEN_MISS_MSG = "Please enter valid access token";
    public static final int SUCCESS_CODE = 1;
    public static final String SUCCESS_MSG = "success";
    public static final boolean SUCCESS_STATUS = true;
    public static final String ERROR_MSG = "error";
    public static final int ERROR_CODE = -1;
    public static final boolean ERROR_STATUS = false;
    public static final int EMPTY_CODE = 0;
    public static final String EMPTY_MSG = "empty";
    public static final int HTTP_STATUS_CODE = 200;

    public ResponseCommon fail() {
        this.code = ERROR_CODE;
        this.message = ERROR_MSG;
        return this;
    }
    public ResponseCommon fail(String msg) {
        this.code = ERROR_CODE;
        this.message = msg;
        return this;
    }
    public ResponseCommon fail(int code, String msg) {
        this.code = code;
        this.message = msg;
        return this;
    }
    public ResponseCommon success() {
        this.code = SUCCESS_CODE;
        this.message = SUCCESS_MSG;
        return this;
    }
    public ResponseCommon success(String message) {
        this.code = SUCCESS_CODE;
        this.message = message;
        return this;
    }
    public ResponseCommon success(int code, String msg) {
        this.code = code;
        this.message = msg;
        return this;
    }

    public ResponseCommon success(int code) {
        this.code = code;
        return this;
    }

    private String message;
    private int code;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String msg;

    @JsonProperty(value = "_message")
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public ResponseCommon() {
        this.message = SUCCESS_MSG;
        this.code = SUCCESS_CODE;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ResponseCommon{" +
                "message='" + message + '\'' +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
