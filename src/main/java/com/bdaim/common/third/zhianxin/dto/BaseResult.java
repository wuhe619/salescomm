package com.bdaim.common.third.zhianxin.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/17 11:21
 */
public class BaseResult<T> {

    private String code;
    private String orderNo;
    private boolean charge;
    private T data;
    private String pcode;
    private Object param;
    private String time;
    private String message;

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setCharge(boolean charge) {
        this.charge = charge;
    }

    public boolean getCharge() {
        return charge;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setPcode(String pcode) {
        this.pcode = pcode;
    }

    public String getPcode() {
        return pcode;
    }

    public Object getParam() {
        return param;
    }

    public void setParam(Object param) {
        this.param = param;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "BaseResult{" +
                "code='" + code + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", charge=" + charge +
                ", data=" + data +
                ", pcode='" + pcode + '\'' +
                ", param=" + param +
                ", time='" + time + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}