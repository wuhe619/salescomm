package com.bdaim.slxf.entity;

import java.util.List;

import com.bdaim.callcenter.dto.SeatsInfo;

/**
 * @author duanliying
 * @date 2018/9/19
 * @description
 */
public class SeatsMessageParam {
    //坐席id
    private String id;
    //坐席名字
    private String realName;
    //坐席手机号
    private String mobileNum;

    private String createTime;
    //坐席登陆账号
    private String account;
    //坐席登陆密码
    private String password;
    //坐席状态1-有效  0-无效
    private Integer status;
    //企业id
    private String custId;
    //企业密码
    private String enterprisePassword;
    //外显号码
    private String apparentNumbera;
    //案件ID
    private String activityId;
    //外呼企业ID
    private String callEnterpriseId;
    //渠道/2-联通 3-电信 4-移动
    private String channel;
    //userId
    private String userId;
    //属性值
    private String propertyValue;
    //属性key
    private String propertyName;
    //坐席集合
    private List<SeatsInfo> seatsInfoList;
    //地址渠道配置信息（暂时未确定）
    private String configId;

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getMobileNum() {
        return mobileNum;
    }

    public void setMobileNum(String mobileNum) {
        this.mobileNum = mobileNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getEnterprisePassword() {
        return enterprisePassword;
    }

    public void setEnterprisePassword(String enterprisePassword) {
        this.enterprisePassword = enterprisePassword;
    }

    public String getApparentNumbera() {
        return apparentNumbera;
    }

    public void setApparentNumbera(String apparentNumbera) {
        this.apparentNumbera = apparentNumbera;
    }

    public String getCallEnterpriseId() {
        return callEnterpriseId;
    }

    public void setCallEnterpriseId(String callEnterpriseId) {
        this.callEnterpriseId = callEnterpriseId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public List<SeatsInfo> getSeatsInfoList() {
        return seatsInfoList;
    }

    public void setSeatsInfoList(List<SeatsInfo> seatsInfoList) {
        this.seatsInfoList = seatsInfoList;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    @Override
    public String toString() {
        return "SeatsMessageParam{" +
                "id='" + id + '\'' +
                ", realName='" + realName + '\'' +
                ", mobileNum='" + mobileNum + '\'' +
                ", createTime='" + createTime + '\'' +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                ", status=" + status +
                ", custId='" + custId + '\'' +
                ", enterprisePassword='" + enterprisePassword + '\'' +
                ", apparentNumbera='" + apparentNumbera + '\'' +
                ", activityId='" + activityId + '\'' +
                ", callEnterpriseId='" + callEnterpriseId + '\'' +
                ", channel='" + channel + '\'' +
                ", userId='" + userId + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", propertyName='" + propertyName + '\'' +
                ", seatsInfoList=" + seatsInfoList +
                ", configId='" + configId + '\'' +
                '}';
    }
}
