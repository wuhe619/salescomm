package com.bdaim.rbac.dto;

import com.bdaim.rbac.dto.Manager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author duanliying
 * @date 2019/3/15
 * 角色
 */
public class RoleDTO implements Manager<Long> {
    private Long key;
    private String user;
    private Date createDate;
    private Date modifyDate;
    private String name;
    private ManagerType type;
    private Long deptId;
    private Long id;

    private String deptName;

    public ManagerType getType() {
        return type;
    }

    public void setType(ManagerType type) {
        this.type = type;
    }

    @Override
    public Long getKey() {
        return this.key;
    }

    public void setKey(Long key) {
        this.key = key;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoleDTO() {
        super();
    }

    public RoleDTO(Long key) {
        this.key = key;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public static List<RoleDTO> pop(ResultSet rs) {
        List<RoleDTO> roles = new ArrayList<RoleDTO>();
        RoleDTO role = null;
        try {
            while (rs.next()) {
                role = new RoleDTO();
                role.setKey(rs.getLong("ID"));
                role.setName(rs.getString("NAME"));
                role.setUser(rs.getString("OPTUSER"));
                role.setCreateDate(rs.getDate("CREATE_TIME"));
                role.setModifyDate(rs.getDate("MODIFY_TIME"));
                roles.add(role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}

