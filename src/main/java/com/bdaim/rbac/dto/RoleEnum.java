package com.bdaim.rbac.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019/1/3
 * @description
 */
public enum RoleEnum {
    ADMIN("admin"),
    ROLE_USER("ROLE_USER"),
    ROLE_CUSTOMER("ROLE_CUSTOMER");

    private String role;

    RoleEnum(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
