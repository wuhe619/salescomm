package com.bdaim.rbac.dto;

import java.io.Serializable;

/**
 * @author duanliying
 * @date 2019/3/13
 * 部门管理类
 */
public class DeptDto implements Serializable {
    //部门id
    private String id;
    //部门名字
    private String name;
    //操作用户
    private String optUser;
    //创建时间
    private String createTime;
    //修改时间
    private String modifyTime;
    //角色数量
    private int roleNum;

    public DeptDto() {
    }

    public int getRoleNum() {
        return roleNum;
    }

    public void setRoleNum(int roleNum) {
        this.roleNum = roleNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOptuser() {
        return optUser;
    }

    public void setOptuser(String optuser) {
        this.optUser = optUser;
    }

    public String getOptUser() {
        return optUser;
    }

    public void setOptUser(String optUser) {
        this.optUser = optUser;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public String toString() {
        return "DeptDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", optUser='" + optUser + '\'' +
                ", createTime='" + createTime + '\'' +
                ", modifyTime='" + modifyTime + '\'' +
                ", roleNum=" + roleNum +
                '}';
    }
}
