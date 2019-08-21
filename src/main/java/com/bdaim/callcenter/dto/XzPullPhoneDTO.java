package com.bdaim.callcenter.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019/5/6
 * @description
 */
public class XzPullPhoneDTO {

    private String phone;
    private String param;

    public XzPullPhoneDTO() {
    }

    public XzPullPhoneDTO(String phone, String param) {
        this.phone = phone;
        this.param = param;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "XzPullPhoneDTO{" +
                "phone='" + phone + '\'' +
                ", param='" + param + '\'' +
                '}';
    }
}
