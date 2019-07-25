package com.bdaim.slxf.dto;

import org.springframework.security.core.GrantedAuthority;

import com.bdaim.rbac.entity.User;

import java.util.Collection;

/**
 *
 */
public class LoginUser extends org.springframework.security.core.userdetails.User{
	public LoginUser(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities){
		super(username,password,enabled,accountNonExpired,credentialsNonExpired,accountNonLocked,authorities);
	}

	public LoginUser(String username,String password,Collection<? extends GrantedAuthority> authorities){
		super(username,password,authorities);
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
	private long tokentime=0;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
		this.id=user.getId();
		this.name=user.getName();
		this.custId=user.getCust_Id();
		this.enterpriseName=user.getEnterprise_name();
	}

	public Long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public boolean isAdmin() {
		if("admin".equals(name))
			return true;
		return false;
	}
	public String getCustId() {
		if("admin".equals(this.role) || "ROLE_USER".equals(role))
			return null;
		if(custId==null)
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

	
}
