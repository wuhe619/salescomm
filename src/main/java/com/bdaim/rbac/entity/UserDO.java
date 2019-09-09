package com.bdaim.rbac.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Mr.YinXin on 2017/2/21.
 */
@Entity
@Table(name = "t_user", schema = "", catalog = "")
public class UserDO {
    private long id;
    private String custId;
    private Integer userType;
    private String name;
    private String password;
    private String realname;
    private String optuser;
    private Long deptid;
    private Integer source;
    private Integer status;
    private String email;
    private String mobileNum;
    private String emailGroup;
    private String connectionInfo;
    private String title;
    private String workNum;
    private String fixedNum;
    private String remark;
    private Timestamp createTime;
    private Timestamp modifyTime;
    private Timestamp activeTime;
    private String enterpriseName;
    private String workNumStatus;
    private Integer userPwdLevel;


    private String newPassword;
    /**
     * 授权平台 1-精准营销 2-金融超市
     */
    @Basic
    @Column(name = "authorize")
    private Integer authorize;

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "cust_id")
    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    @Basic
    @Column(name = "user_type")
    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    @Basic
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "PASSWORD")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Basic
    @Column(name = "realname")
    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    @Basic
    @Column(name = "optuser")
    public String getOptuser() {
        return optuser;
    }

    public void setOptuser(String optuser) {
        this.optuser = optuser;
    }

    @Basic
    @Column(name = "deptid")
    public Long getDeptid() {
        return deptid;
    }

    public void setDeptid(Long deptid) {
        this.deptid = deptid;
    }

    @Basic
    @Column(name = "source")
    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    @Basic
    @Column(name = "STATUS")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Basic
    @Column(name = "mobile_num")
    public String getMobileNum() {
        return mobileNum;
    }

    public void setMobileNum(String mobileNum) {
        this.mobileNum = mobileNum;
    }

    @Basic
    @Column(name = "email_group")
    public String getEmailGroup() {
        return emailGroup;
    }

    public void setEmailGroup(String emailGroup) {
        this.emailGroup = emailGroup;
    }

    @Basic
    @Column(name = "connection_info")
    public String getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(String connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Basic
    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Basic
    @Column(name = "work_num")
    public String getWorkNum() {
        return workNum;
    }

    public void setWorkNum(String workNum) {
        this.workNum = workNum;
    }

    @Basic
    @Column(name = "fixed_num")
    public String getFixedNum() {
        return fixedNum;
    }

    public void setFixedNum(String fixedNum) {
        this.fixedNum = fixedNum;
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
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "modify_time")
    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Basic
    @Column(name = "active_time")
    public Timestamp getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(Timestamp activeTime) {
        this.activeTime = activeTime;
    }

    @Basic
    @Column(name = "enterprise_name")
    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    @Basic
    @Column(name = "work_num_status")
    public String getWorkNumStatus() {
        return workNumStatus;
    }

    public void setWorkNumStatus(String workNumStatus) {
        this.workNumStatus = workNumStatus;
    }

    @Transient
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public Integer getAuthorize() {
        return authorize;
    }

    public void setAuthorize(Integer authorize) {
        this.authorize = authorize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDO userDO = (UserDO) o;

        if (id != userDO.id) return false;
        if (custId != null ? !custId.equals(userDO.custId) : userDO.custId != null) return false;
        if (userType != null ? !userType.equals(userDO.userType) : userDO.userType != null) return false;
        if (name != null ? !name.equals(userDO.name) : userDO.name != null) return false;
        if (password != null ? !password.equals(userDO.password) : userDO.password != null) return false;
        if (realname != null ? !realname.equals(userDO.realname) : userDO.realname != null) return false;
        if (optuser != null ? !optuser.equals(userDO.optuser) : userDO.optuser != null) return false;
        if (deptid != null ? !deptid.equals(userDO.deptid) : userDO.deptid != null) return false;
        if (source != null ? !source.equals(userDO.source) : userDO.source != null) return false;
        if (status != null ? !status.equals(userDO.status) : userDO.status != null) return false;
        if (email != null ? !email.equals(userDO.email) : userDO.email != null) return false;
        if (mobileNum != null ? !mobileNum.equals(userDO.mobileNum) : userDO.mobileNum != null) return false;
        if (emailGroup != null ? !emailGroup.equals(userDO.emailGroup) : userDO.emailGroup != null) return false;
        if (connectionInfo != null ? !connectionInfo.equals(userDO.connectionInfo) : userDO.connectionInfo != null)
            return false;
        if (title != null ? !title.equals(userDO.title) : userDO.title != null) return false;
        if (workNum != null ? !workNum.equals(userDO.workNum) : userDO.workNum != null) return false;
        if (fixedNum != null ? !fixedNum.equals(userDO.fixedNum) : userDO.fixedNum != null) return false;
        if (remark != null ? !remark.equals(userDO.remark) : userDO.remark != null) return false;
        if (createTime != null ? !createTime.equals(userDO.createTime) : userDO.createTime != null) return false;
        if (modifyTime != null ? !modifyTime.equals(userDO.modifyTime) : userDO.modifyTime != null) return false;
        if (activeTime != null ? !activeTime.equals(userDO.activeTime) : userDO.activeTime != null) return false;
        if (enterpriseName != null ? !enterpriseName.equals(userDO.enterpriseName) : userDO.enterpriseName != null)
            return false;
        if (workNumStatus != null ? !workNumStatus.equals(userDO.workNumStatus) : userDO.workNumStatus != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (custId != null ? custId.hashCode() : 0);
        result = 31 * result + (userType != null ? userType.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (realname != null ? realname.hashCode() : 0);
        result = 31 * result + (optuser != null ? optuser.hashCode() : 0);
        result = 31 * result + (deptid != null ? deptid.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (mobileNum != null ? mobileNum.hashCode() : 0);
        result = 31 * result + (emailGroup != null ? emailGroup.hashCode() : 0);
        result = 31 * result + (connectionInfo != null ? connectionInfo.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (workNum != null ? workNum.hashCode() : 0);
        result = 31 * result + (fixedNum != null ? fixedNum.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (modifyTime != null ? modifyTime.hashCode() : 0);
        result = 31 * result + (activeTime != null ? activeTime.hashCode() : 0);
        result = 31 * result + (enterpriseName != null ? enterpriseName.hashCode() : 0);
        result = 31 * result + (workNumStatus != null ? workNumStatus.hashCode() : 0);
        return result;
    }

    @Basic
    @Column(name = "user_pwd_level")
    public Integer getUserPwdLevel() {
        return userPwdLevel;
    }

    public void setUserPwdLevel(Integer userPwdLevel) {
        this.userPwdLevel = userPwdLevel;
    }
}
