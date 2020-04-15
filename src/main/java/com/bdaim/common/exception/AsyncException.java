package com.bdaim.common.exception;

/**
 * @author chengning@salescomm.net
 * @description TODO
 * @date 2020/4/15 17:15
 */
public class AsyncException extends Exception {
    private int code;
    private String errorMessage;

    public AsyncException() {
    }

    public AsyncException(int code, String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
