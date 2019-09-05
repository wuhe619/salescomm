package com.bdaim.customer.dto;

/**
 * 服务权限
 * @author chengning@salescomm.net
 * @date 2019/6/18
 * @description
 */
public enum ServiceModeEnum {

    MARKET_TASK("1"),
    PUBLIC_SEA("2");

    private String code;

    public String getCode() {
        return code;
    }

    ServiceModeEnum(String code) {
        this.code = code;
    }

    public static void main(String[] args) {
        for (ServiceModeEnum v:ServiceModeEnum.values()){
            System.out.println(v.code);
        }
    }
}
