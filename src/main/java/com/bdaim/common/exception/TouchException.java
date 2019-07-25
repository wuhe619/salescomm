package com.bdaim.common.exception;

/**
 * Created by Mr.YinXin on 2017/2/21.
 */
public class TouchException extends Exception {

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

    public TouchException() {
        // TODO Auto-generated constructor stub
    }

    public TouchException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public TouchException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public TouchException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public TouchException(String code, String message) {
        super(message);
        this.code = code;
        this.errMsg=message;
    }

}
