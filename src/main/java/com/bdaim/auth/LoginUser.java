package com.bdaim.auth;

import com.bdaim.common.auth.Token;
import com.bdaim.rbac.entity.User;

import java.util.List;

/**
 *
 */
public class LoginUser extends Token {
	public LoginUser() {
        super();
    }
	public LoginUser(Long userid, String username, String tokenid) {
        this.id = userid;
        this.userName=username;
        this.stateCode = "200";
        this.msg = "SUCCESS";
        this.user_id = String.valueOf(userid);
        this.id = userid;
        this.tokenid = tokenid;
        super.setUsername(username);
        super.setTokenid(tokenid);
    }

	public LoginUser(String username, String tokenid, String msg, String stateCode) {
        this.msg = msg;
        this.stateCode = stateCode;
        this.userName = username;
        this.tokenid = tokenid;
        super.setUsername(username);
        super.setTokenid(tokenid);
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
    /**
     *前台菜单资源
     */
    private String resourceMenu;
    /**
     *意向行业
     */
    private String inten_industry;

    /**
     * api_token
     * @return
     */
    private String api_token;

    /**
     * 营销类型:1-B2C营销  2-B2B营销
     */
    private Integer marketingType;

    //查询开始时间
    private String startTime;
    //查询结束时间
    private String endTime;
    //用户角色列表
    private List<Integer> roles;

    private Long userId;

    private Integer deptId;

    private String salt;

    public String getApi_token() {
        return api_token;
    }

    public void setApi_token(String api_token) {
        this.api_token = api_token;
    }

    public String getResourceMenu() {
        return resourceMenu;
    }

    public void setResourceMenu(String resourceMenu) {
        this.resourceMenu = resourceMenu;
    }

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

    public String getInten_industry() {
        return inten_industry;
    }

    public void setInten_industry(String inten_industry) {
        this.inten_industry = inten_industry;
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

    public Integer getMarketingType() {
        return marketingType;
    }

    public void setMarketingType(Integer marketingType) {
        this.marketingType = marketingType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public List<Integer> getRoles() {
        return roles;
    }

    public void setRoles(List<Integer> roles) {
        this.roles = roles;
    }

    public Long getUserId() {
        return id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }
}
