package com.bdaim.rbac.dto;

import java.util.Date;

/**
 * 数据权限
 */
//@Entity
//@Table(name = "t_role_data_permission")
//@IdClass(RoleDataPermissonPK.class)
public class RoleDataPermissonDTO {

    //用户id
//    @Id
//    @Column(name = "role_id")
    private String roleId;
    //类型 5-供应商 4-客户
//    @Id
//    @Column(name = "type")
    private Integer type;
    //供应商/客户id
//    @Id
//    @Column(name = "rel_id")
    private String rId;

//    @Column(name="status")
//    private Integer status;

//    @Column(name = "create_time")
    private Date createTime;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getrId() {
        return rId;
    }

    public void setrId(String rId) {
        this.rId = rId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "UserDataPermisson{" +
                "roleId='" + roleId + '\'' +
                ", type=" + type +
                ", relId='" + rId + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
