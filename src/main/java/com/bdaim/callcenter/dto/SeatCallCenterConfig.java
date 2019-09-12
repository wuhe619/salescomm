package com.bdaim.callcenter.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019/2/20
 * @description
 */
public class SeatCallCenterConfig {

    private String userId;

    private String callCenterType;

    private String callCenterId;

    private String callCenterIp;

    private String apparentNumber;

    private String xfCallSipPort;

    private String xfCallSipPwd;

    private String xfSeatsAccount;

    private String account;

    private String password;

    private String extensionNumber;

    private String extensionPassword;

    private String callCenterAppIp;

    private String workNum;

    public SeatCallCenterConfig() {
    }

    public SeatCallCenterConfig(CallCenterConfig callCenterConfig, SeatCallConfig seatCallConfig) {
        this.userId = seatCallConfig.getUserId();
        this.callCenterType = callCenterConfig.getCallCenterType();
        this.callCenterId = callCenterConfig.getCallCenterId();
        this.callCenterIp = callCenterConfig.getCallCenterIp();
        this.apparentNumber = callCenterConfig.getApparentNumber();
        this.xfCallSipPort = callCenterConfig.getXfCallSipPort();
        this.xfCallSipPwd = callCenterConfig.getXfCallSipPwd();
        this.xfSeatsAccount = seatCallConfig.getXfSeatsAccount();
        this.account = seatCallConfig.getAccount();
        this.password = seatCallConfig.getPassword();
        this.extensionNumber = seatCallConfig.getExtensionNumber();
        this.extensionPassword = seatCallConfig.getExtensionPassword();
        this.callCenterAppIp = callCenterConfig.getCallCenterAppIp();
        this.workNum = seatCallConfig.getWorkNum();
    }

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

    public String getCallCenterId() {
        return callCenterId;
    }

    public void setCallCenterId(String callCenterId) {
        this.callCenterId = callCenterId;
    }

    public String getCallCenterIp() {
        return callCenterIp;
    }

    public void setCallCenterIp(String callCenterIp) {
        this.callCenterIp = callCenterIp;
    }

    public String getApparentNumber() {
        return apparentNumber;
    }

    public void setApparentNumber(String apparentNumber) {
        this.apparentNumber = apparentNumber;
    }

    public String getXfCallSipPort() {
        return xfCallSipPort;
    }

    public void setXfCallSipPort(String xfCallSipPort) {
        this.xfCallSipPort = xfCallSipPort;
    }

    public String getXfCallSipPwd() {
        return xfCallSipPwd;
    }

    public void setXfCallSipPwd(String xfCallSipPwd) {
        this.xfCallSipPwd = xfCallSipPwd;
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

    public String getXfSeatsAccount() {
        return xfSeatsAccount;
    }

    public void setXfSeatsAccount(String xfSeatsAccount) {
        this.xfSeatsAccount = xfSeatsAccount;
    }

    public String getCallCenterAppIp() {
        return callCenterAppIp;
    }

    public void setCallCenterAppIp(String callCenterAppIp) {
        this.callCenterAppIp = callCenterAppIp;
    }

    public String getWorkNum() {
        return workNum;
    }

    public void setWorkNum(String workNum) {
        this.workNum = workNum;
    }

}
