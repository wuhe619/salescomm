package com.bdaim.customer.user.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/9
 * @description
 */
public enum CustomerUserTypeEnum {
    ADMIN_USER("1"),
    PROJECT_USER("3"),
    STAFF_USER("2");
    private String type;

    CustomerUserTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "CustomerUserTypeEnum{" +
                "type='" + type + '\'' +
                '}';
    }
}
