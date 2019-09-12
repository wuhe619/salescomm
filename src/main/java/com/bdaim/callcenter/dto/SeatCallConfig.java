package com.bdaim.callcenter.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019/2/20
 * @description
 */
public class SeatCallConfig {

    private String userId;

    private String callCenterType;

    private String xfSeatsAccount;

    private String account;

    private String password;

    private String extensionNumber;

    private String extensionPassword;

    private String workNum;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCallCenterType() {
        return callCenterType;
    }

    public void setCallCenterType(String callCenterType) {
        this.callCenterType = callCenterType;
    }

    public String getXfSeatsAccount() {
        return xfSeatsAccount;
    }

    public void setXfSeatsAccount(String xfSeatsAccount) {
        this.xfSeatsAccount = xfSeatsAccount;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getWorkNum() {
        return workNum;
    }

    public void setWorkNum(String workNum) {
        this.workNum = workNum;
    }
}
