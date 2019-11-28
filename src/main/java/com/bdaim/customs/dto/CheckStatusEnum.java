package com.bdaim.customs.dto;

/**
 * 身份核验状态枚举 对应分单的check_status字段
 * @author chengning@salescomm.net
 * @date 2019-11-28 11:10
 */
public enum CheckStatusEnum {

    PENDING("0","待校验"),
    PASS("1","校验通过"),
    NOT_PASS("2","校验未通过"),
    IN_PROGRESS("3","校验中"),
    UN_AUTH("4","无法认证");

    private String code;
    private String name;

    CheckStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
