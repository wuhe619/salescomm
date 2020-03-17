package com.bdaim.crm.common.exception;

/**
 * 参数校验异常
 *
 * @author Chacker
 */
public class ParamValidateException extends RuntimeException {
    private String code;
    private String msg;

    public ParamValidateException(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ParamValidateException(String message, String code, String msg) {
        super(message);
        this.code = code;
        this.msg = msg;
    }

    public ParamValidateException(String message, Throwable cause, String code, String msg) {
        super(message, cause);
        this.code = code;
        this.msg = msg;
    }

    public ParamValidateException(Throwable cause, String code, String msg) {
        super(cause);
        this.code = code;
        this.msg = msg;
    }

    public ParamValidateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String code, String msg) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
