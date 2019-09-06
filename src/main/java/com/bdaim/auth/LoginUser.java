package com.bdaim.auth;

import com.bdaim.common.auth.Token;
import com.bdaim.rbac.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 *
 */
public class LoginUser extends Token {
    public LoginUser(Long userid, String username, String tokenid, Collection<? extends GrantedAuthority> authorities) {
        super(username, tokenid, authorities);
        this.id = userid;

        this.stateCode = "200";
        this.msg = "SUCCESS";
        this.auth = authorities.toArray()[0].toString();
        this.user_id = String.valueOf(userid);
        this.id = userid;
        this.tokenid = tokenid;
        this.tokentime = tokentime;
    }


    private User user;
    private Long id;
    private Long type;
    private String name;
    private String custId;
    private String enterpriseName;
    private String userType;
    private String role;
    private String mobileNum;
    private String tokenid;
    private long tokentime = 0;

    private String refurl;
    private String redirecturl;
    private String msg = "ok";
    private String stateCode = "0";
    private Object resultData;
    private String auth;
    private String userName;
    private String mobile_num;
    private String user_id;

    /**
     * 用户组角色 1-组长 2-组员
     */
    private String userGroupRole;

    private String userGroupId;

    /**
     * 职场ID
     */
    private String jobMarketId;

    private String defaultUrl;

    /**
     * 授权平台 1-精准营销 2-金融超市
     */
    private String authorize;

    private String status;

    /**
     * 服务权限 1-营销任务 2-公海 多个逗号隔开
     */
    private String serviceMode;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        this.id = user.getId();
        this.name = user.getName();
        this.custId = user.getCust_Id();
        this.enterpriseName = user.getEnterprise_name();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isAdmin() {
        if ("admin".equals(name))
            return true;
        return false;
    }

    public String getCustId() {
        if ("admin".equals(this.role) || "ROLE_USER".equals(role))
            return null;
        if (custId == null)
            return "";
        return custId;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMobileNum() {
        return mobileNum;
    }

    public void setMobileNum(String mobileNum) {
        this.mobileNum = mobileNum;
    }

    public String getTokenid() {
        return tokenid;
    }

    public void setTokenid(String tokenid) {
        this.tokenid = tokenid;
    }

    public long getTokentime() {
        return tokentime;
    }

    public void setTokentime(long tokentime) {
        this.tokentime = tokentime;
    }

    public String getStateCode() {
        return stateCode;
    }

    public String getMsg() {
        return msg;
    }

    public String getUserGroupRole() {
        return userGroupRole;
    }

    public void setUserGroupRole(String userGroupRole) {
        this.userGroupRole = userGroupRole;
    }

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }

    public String getJobMarketId() {
        return jobMarketId;
    }

    public void setJobMarketId(String jobMarketId) {
        this.jobMarketId = jobMarketId;
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public String getAuthorize() {
        return authorize;
    }

    public void setAuthorize(String authorize) {
        this.authorize = authorize;
    }

    public String getRefurl() {
        return refurl;
    }

    public void setRefurl(String refurl) {
        this.refurl = refurl;
    }

    public String getRedirecturl() {
        return redirecturl;
    }

    public void setRedirecturl(String redirecturl) {
        this.redirecturl = redirecturl;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public Object getResultData() {
        return resultData;
    }

    public void setResultData(Object resultData) {
        this.resultData = resultData;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMobile_num() {
        return mobile_num;
    }

    public void setMobile_num(String mobile_num) {
        this.mobile_num = mobile_num;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getServiceMode() {
        return serviceMode;
    }

    public void setServiceMode(String serviceMode) {
        this.serviceMode = serviceMode;
    }
}
