package com.bdaim.rbac.dto;


import java.util.Date;
import java.util.List;

/**
 */
public class RolesResource {
    private RoleDTO role;
    private List<AbstractTreeResource> resources;
    private String user;
    private Date createDate;
    private Date modifyDate;

    public RoleDTO getRole() {
        return role;
    }

    public void setRole(RoleDTO role) {
        this.role = role;
    }

    public List<AbstractTreeResource> getResources() {
        return resources;
    }

    public void setResources(List<AbstractTreeResource> resources) {
        this.resources = resources;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }
}
