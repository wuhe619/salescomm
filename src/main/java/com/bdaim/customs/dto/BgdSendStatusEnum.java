package com.bdaim.customs.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019-11-26 14:52
 */
public enum BgdSendStatusEnum {

    B0("B0","未申报"),
    BACK("00","退单");

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    BgdSendStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
