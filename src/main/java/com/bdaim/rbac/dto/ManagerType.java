package com.bdaim.rbac.dto;

/**
 * @author duanliying
 * @date 2019/3/15
 * @description
 */
public enum ManagerType {
    ADMINISTRATOR(0,"超级管理员"),
    ADMIN(1,"管理员"),
    USER(2,"普通用户"),
    GUEST(3,"访客");
    private int id;
    private String CnName;

    private ManagerType(int id, String cnName) {
        this.id = id;
        CnName = cnName;
    }

    public int getId() {
        return id;
    }

    public String getCnName() {
        return CnName;
    }

    public static ManagerType getManagerType(int id){
        if (id==ADMINISTRATOR.id)return ADMINISTRATOR;
        else if (id==ADMIN.id) return ADMIN;
        else if (id==USER.id)return USER;
        else if (id==GUEST.id)return GUEST;
        else return null;
    }
}
