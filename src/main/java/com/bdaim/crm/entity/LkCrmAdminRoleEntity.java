package com.bdaim.crm.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "lkcrm_admin_role", schema = "", catalog = "")
public class LkCrmAdminRoleEntity {
    private Integer roleId;
    private String roleName;
    private Integer roleType;
    private String remark;
    private Integer status;
    private Integer dataType;
    private Integer isHidden;
    private Integer label;
    private String custId;
    private Date createTime;
    private Date updateTime;

    private String menuIds;

    public LkCrmAdminRoleEntity() {
    }

    public LkCrmAdminRoleEntity(String roleName, Integer roleType, String remark, Integer status, int dataType, int isHidden, Integer label, String custId) {
        this.roleName = roleName;
        this.roleType = roleType;
        this.remark = remark;
        this.status = status;
        this.dataType = dataType;
        this.isHidden = isHidden;
        this.label = label;
        this.custId = custId;
    }

    @Id
    @Column(name = "role_id")
    @GeneratedValue
    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
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
    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    @Basic
    @Column(name = "is_hidden")
    public Integer getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Integer isHidden) {
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

    @Basic
    @Column(name = "cust_id")
    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    @Transient
    public String getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(String menuIds) {
        this.menuIds = menuIds;
    }
    @Basic
    @Column(name = "create_time")
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "update_time")
    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
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
