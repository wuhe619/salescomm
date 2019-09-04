package com.bdaim.callcenter.dto;

import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2018/9/21
 * @description 用于存储创建坐席信息
 */
public class SeatsInfo {
    //渠道/供应商 0-联通 1-移动 2-电信
    private String channel;
    //类型前台修改1  后台修改2
    private String type;
    //真实姓名
    private String realName;
    //用户id
    private String userId;
    //电话号码
    private String phoneNum;
    //用户登陆信息
    private String account;
    //坐席登陆密码
    private String password;
    //坐席id（账号）
    private String seatId;
    //坐席名字
    private String seatName;
    //坐席密码
    private String seatPassword;
    //主叫号码
    private String mainNumber;
    //企业id
    private String custId;

    //坐席真实姓名
    private String seatsName;
    private String seatsAccount;
    private String seatsPassword;
    private String extensionNumber;
    private String extensionPassword;
    //坐席信息集合
    private List<Map<String, Object>> userPropertyInfoList;

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeatName() {
        return seatName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getSeatId() {
        return seatId;
    }

    public String getSeatPassword() {
        return seatPassword;
    }

    public void setSeatPassword(String seatPassword) {
        this.seatPassword = seatPassword;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getSeatName(String seatName) {
        return this.seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public List<Map<String, Object>> getUserPropertyInfoList() {
        return userPropertyInfoList;
    }

    public void setUserPropertyInfoList(List<Map<String, Object>> userPropertyInfoList) {
        this.userPropertyInfoList = userPropertyInfoList;
    }

    public String getMainNumber() {
        return mainNumber;
    }

    public void setMainNumber(String mainNumber) {
        this.mainNumber = mainNumber;
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

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }


    public String getSeatsName() {
        return seatsName;
    }

    public void setSeatsName(String seatsName) {
        this.seatsName = seatsName;
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

    @Override
    public String toString() {
        return "SeatsInfo{" +
                "channel='" + channel + '\'' +
                ", type='" + type + '\'' +
                ", realName='" + realName + '\'' +
                ", userId='" + userId + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                ", seatId='" + seatId + '\'' +
                ", seatName='" + seatName + '\'' +
                ", seatPassword='" + seatPassword + '\'' +
                ", mainNumber='" + mainNumber + '\'' +
                ", custId='" + custId + '\'' +
                ", seatsName='" + seatsName + '\'' +
                ", seatsAccount='" + seatsAccount + '\'' +
                ", seatsPassword='" + seatsPassword + '\'' +
                ", extensionNumber='" + extensionNumber + '\'' +
                ", extensionPassword='" + extensionPassword + '\'' +
                ", userPropertyInfoList=" + userPropertyInfoList +
                '}';
    }
}
