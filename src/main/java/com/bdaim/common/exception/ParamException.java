package com.bdaim.common.exception;

/**
 * @author chengning@salescomm.net
 * @date 2018/12/19
 * @description
 */
public class ParamException extends RuntimeException {
    private int code;

    public ParamException() {
    }

    public ParamException(String message) {
        super(message);
    }

    public ParamException(String message, int code) {
        super(message);
        this.code = code;
    }
}
