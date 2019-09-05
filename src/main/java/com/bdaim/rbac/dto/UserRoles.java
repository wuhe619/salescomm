package com.bdaim.rbac.dto;

import java.util.Date;
import java.util.List;

/** 这个是数据库的一种映射关系
 */
public class UserRoles {
    private UserDTO user;
    private List<RoleDTO> roles;
    private List<Integer> level;
    private String optUser;
    private Date createDate;

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public List<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
    }

    public List<Integer> getLevel() {
        return level;
    }

    public void setLevel(List<Integer> level) {
        this.level = level;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getOptUser() {
        return optUser;
    }

    public void setOptUser(String optUser) {
        this.optUser = optUser;
    }
   
}
