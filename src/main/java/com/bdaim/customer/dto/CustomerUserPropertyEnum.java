package com.bdaim.customer.dto;

/** 用户属性枚举
 * @author chengning@salescomm.net
 * @date 2019/4/11
 * @description
 */
public enum CustomerUserPropertyEnum {

    CALL_CHANNEL("call_channel", "呼叫中心渠道ID"),
    CALL_TYPE("call_type", "呼叫中心类型"),
    RESOURCE_MENU("resource","菜单权限");

    private String key;
    private String name;

    CustomerUserPropertyEnum(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
}
