package com.bdaim.rbac.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class UserDTO implements Serializable, Manager<Long> {
    public Long Id;
    public String userName;
    public String realName;
    public Integer userType;
    public String customerId;
    public String title;
    public String remark;
    public String password;
    public String mobileNumber;
    public Integer status;
    public String email;
    public String mainNumber;
    public Long deptId;
    public String deptName;
    public String roles;
    public List<RoleDTO> roleList;
    public String roleName;
    public String optuser;
    public Date createTime;
    private Date modifyTime;
    public int source;
    /**
     * 场站id
     */
    public String stationId;
    /**
     * 授权平台 1-精准营销 2-金融超市
     */
    private String authorize;
    private String name;

    public UserDTO() {
    }

    public UserDTO(Long id, String userName, String realName, Integer userType, String customerId, String title, String remark, String password, String mobileNumber, Integer status, String email, String mainNumber, Long deptId, String deptName, String roles, String roleName, String optuser, Date createTime, int source) {
        Id = id;
        this.userName = userName;
        this.realName = realName;
        this.userType = userType;
        this.customerId = customerId;
        this.title = title;
        this.remark = remark;
        this.password = password;
        this.mobileNumber = mobileNumber;
        this.status = status;
        this.email = email;
        this.mainNumber = mainNumber;
        this.deptId = deptId;
        this.deptName = deptName;
        this.roles = roles;
        this.roleName = roleName;
        this.optuser = optuser;
        this.createTime = createTime;
        this.source = source;
    }

    @Override
    public Long getKey() {
        return this.Id;
    }

    public Long getId() {
        return Id;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMainNumber() {
        return mainNumber;
    }

    public void setMainNumber(String mainNumber) {
        this.mainNumber = mainNumber;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getOptuser() {
        return optuser;
    }

    public void setOptuser(String optuser) {
        this.optuser = optuser;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getAuthorize() {
        return authorize;
    }

    public void setAuthorize(String authorize) {
        this.authorize = authorize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RoleDTO> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<RoleDTO> roleList) {
        this.roleList = roleList;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "Id=" + Id +
                ", userName='" + userName + '\'' +
                ", realName='" + realName + '\'' +
                ", userType=" + userType +
                ", customerId='" + customerId + '\'' +
                ", title='" + title + '\'' +
                ", remark='" + remark + '\'' +
                ", password='" + password + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", status=" + status +
                ", email='" + email + '\'' +
                ", mainNumber='" + mainNumber + '\'' +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", roles='" + roles + '\'' +
                ", roleList=" + roleList +
                ", roleName='" + roleName + '\'' +
                ", optuser='" + optuser + '\'' +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", source=" + source +
                ", authorize='" + authorize + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
