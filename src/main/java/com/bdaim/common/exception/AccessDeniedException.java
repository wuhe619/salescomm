package com.bdaim.common.exception;

/**
 * @author chengning@salescomm.net
 * @date 2018/12/13
 * @description
 */
public class AccessDeniedException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = -8365673423244275982L;
    private String code;
    private String errMsg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public AccessDeniedException(String code, String errMsg) {
        super(errMsg);
        this.code = code;
        this.errMsg = errMsg;
    }

}
