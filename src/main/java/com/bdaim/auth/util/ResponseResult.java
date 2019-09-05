package com.bdaim.auth.util;

/**
 * Created by Mr.YinXin on 2017/4/10.
 */


public class ResponseResult {

    private String refurl;
    private String redirecturl;
    private String msg = "ok";
    private String stateCode = "0";
    private Object resultData;
    private String auth;
    private String userName;
    
    private String userType;
    private String custId;
    private String mobile_num;
    private String enterpriseName;
    private String user_id;
    private String status;
    private String tokenid;
    /**
     * 用户组角色 1-组长 2-组员
     */
    private String userGroupRole;

    private String defaultUrl;

    /**
     * 服务权限 1-营销任务 2-公海 多个逗号隔开
     */
    private String serviceMode;

    public ResponseResult() {

    }

    public ResponseResult(String stateCode, String msg) {
        this.msg = msg;
        this.stateCode = stateCode;
    }

    public ResponseResult(String refurl, String redirecturl, Object resultData) {
        super();
        this.refurl = refurl;
        this.redirecturl = redirecturl;
        this.resultData = resultData;
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getStateCode() {
        return stateCode;
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

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getMobile_num() {
		return mobile_num;
	}

	public void setMobile_num(String mobile_num) {
		this.mobile_num = mobile_num;
	}

	public String getEnterpriseName() {
		return enterpriseName;
	}

	public void setEnterpriseName(String enterpriseName) {
		this.enterpriseName = enterpriseName;
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

    public String getUserGroupRole() {
        return userGroupRole;
    }

    public void setUserGroupRole(String userGroupRole) {
        this.userGroupRole = userGroupRole;
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public String getTokenid() {
		return tokenid;
	}

	public void setTokenid(String tokenid) {
		this.tokenid = tokenid;
	}

    public String getServiceMode() {
        return serviceMode;
    }

    public void setServiceMode(String serviceMode) {
        this.serviceMode = serviceMode;
    }

    @Override
    public String toString() {
        return "ResponseResult{" +
                "refurl='" + refurl + '\'' +
                ", redirecturl='" + redirecturl + '\'' +
                ", msg='" + msg + '\'' +
                ", stateCode='" + stateCode + '\'' +
                ", resultData=" + resultData +
                ", auth='" + auth + '\'' +
                ", userName='" + userName + '\'' +
                ", userType='" + userType + '\'' +
                ", custId='" + custId + '\'' +
                ", mobile_num='" + mobile_num + '\'' +
                ", enterpriseName='" + enterpriseName + '\'' +
                ", user_id='" + user_id + '\'' +
                ", status='" + status + '\'' +
                ", tokenid='" + tokenid + '\'' +
                ", userGroupRole='" + userGroupRole + '\'' +
                ", defaultUrl='" + defaultUrl + '\'' +
                ", serviceMode='" + serviceMode + '\'' +
                '}';
    }
}


