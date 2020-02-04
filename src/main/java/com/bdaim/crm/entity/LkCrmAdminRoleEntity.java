package com.bdaim.crm.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_admin_role", schema = "", catalog = "")
public class LkCrmAdminRoleEntity {
    private int roleId;
    private String roleName;
    private Integer roleType;
    private String remark;
    private Integer status;
    private int dataType;
    private int isHidden;
    private Integer label;

    @Id
    @Column(name = "role_id")
    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    @Basic
    @Column(name = "role_name")
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Basic
    @Column(name = "role_type")
    public Integer getRoleType() {
        return roleType;
    }

    public void setRoleType(Integer roleType) {
        this.roleType = roleType;
    }

    @Basic
    @Column(name = "remark")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Basic
    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "data_type")
    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    @Basic
    @Column(name = "is_hidden")
    public int getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(int isHidden) {
        this.isHidden = isHidden;
    }

    @Basic
    @Column(name = "label")
    public Integer getLabel() {
        return label;
    }

    public void setLabel(Integer label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmAdminRoleEntity that = (LkCrmAdminRoleEntity) o;
        return roleId == that.roleId &&
                dataType == that.dataType &&
                isHidden == that.isHidden &&
                Objects.equals(roleName, that.roleName) &&
                Objects.equals(roleType, that.roleType) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(status, that.status) &&
                Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, roleName, roleType, remark, status, dataType, isHidden, label);
    }
}
