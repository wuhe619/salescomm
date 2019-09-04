package com.bdaim.customeruser.dto;

import java.io.Serializable;

public class UserCallConfigDTO implements Serializable{
	
	String Id;
	String userName;
	String realName;
	Integer userType;
	String customerId;
	String title;
	String remark;
	String password;
	String mobileNumber;
	Integer status;
	String email;
	String workNum; //主叫号
	String seatsAccount;
	String seatsPassword;
	String extensionNumber;
	String extensionPassword;
	String callCenterId;
	String appId;
	String lockedTime;
    String callType;
    String callChannel;
    String hasMarketProject;
    String addAgentMethod; //0:默认：1：api方式
	String showNumber;//外显号

	public String getShowNumber() {
		return showNumber;
	}

	public void setShowNumber(String showNumber) {
		this.showNumber = showNumber;
	}

	public String getAddAgentMethod() {
		return addAgentMethod;
	}

	public void setAddAgentMethod(String addAgentMethod) {
		this.addAgentMethod = addAgentMethod;
	}

	public String getHasMarketProject() {
		return hasMarketProject;
	}

	public void setHasMarketProject(String hasMarketProject) {
		this.hasMarketProject = hasMarketProject;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public String getCallChannel() {
		return callChannel;
	}

	public void setCallChannel(String callChannel) {
		this.callChannel = callChannel;
	}

	public String getLockedTime() {
		return lockedTime;
	}

	public void setLockedTime(String lockedTime) {
		this.lockedTime = lockedTime;
	}

	public UserCallConfigDTO() {
	}

	@Override
	public String toString() {
		return "UserDTO2{" +
				"Id='" + Id + '\'' +
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
				", workNum='" + workNum + '\'' +
				", seatsAccount='" + seatsAccount + '\'' +
				", seatsPassword='" + seatsPassword + '\'' +
				", extensionNumber='" + extensionNumber + '\'' +
				", extensionPassword='" + extensionPassword + '\'' +
				", callCenterId='" + callCenterId + '\'' +
				", appId='" + appId + '\'' +
				", lockedTime='" + lockedTime + '\'' +
				", callType='" + callType + '\'' +
				", callChannel='" + callChannel + '\'' +
				'}';
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
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

	public String getWorkNum() {
		return workNum;
	}

	public void setWorkNum(String workNum) {
		this.workNum = workNum;
	}

	public String getSeatsAccount() {
		return seatsAccount;
	}

	public void setSeatsAccount(String seatsAccount) {
		this.seatsAccount = seatsAccount;
	}

	public String getSeatsPassword() {
		return seatsPassword;
	}

	public void setSeatsPassword(String seatsPassword) {
		this.seatsPassword = seatsPassword;
	}

	public String getExtensionNumber() {
		return extensionNumber;
	}

	public void setExtensionNumber(String extensionNumber) {
		this.extensionNumber = extensionNumber;
	}

	public String getExtensionPassword() {
		return extensionPassword;
	}

	public void setExtensionPassword(String extensionPassword) {
		this.extensionPassword = extensionPassword;
	}

	public String getCallCenterId() {
		return callCenterId;
	}

	public void setCallCenterId(String callCenterId) {
		this.callCenterId = callCenterId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

}
