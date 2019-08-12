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
    private String name;
    private String custId;
    private String enterpriseName;
    private String userType;
    private String role;
    private String mobileNum;
    private String tokenid;
    //职位
    private String position;
    //职位id
    private String positionId;
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
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
}
