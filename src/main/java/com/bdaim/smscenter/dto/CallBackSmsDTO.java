package com.bdaim.smscenter.dto;

/**
 * @author duanliying
 * @date 2018/10/8
 * @description
 */
public class CallBackSmsDTO {
    private String requestid;
    private String appid;
    private String mobileList;
    private String contents;
    private String templateId;
    private String sendTime;
    private String smsAmount;
    private String sendSts;


    public String getRequestid() {
        return requestid;
    }

    public void setRequestid(String requestid) {
        this.requestid = requestid;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getMobileList() {
        return mobileList;
    }

    public void setMobileList(String mobileList) {
        this.mobileList = mobileList;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getSmsAmount() {
        return smsAmount;
    }

    public void setSmsAmount(String smsAmount) {
        this.smsAmount = smsAmount;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getSendSts() {
        return sendSts;
    }

    public void setSendSts(String sendSts) {
        this.sendSts = sendSts;
    }

    @Override
    public String toString() {
        return "CallBackSmsDTO{" +
                "requestid='" + requestid + '\'' +
                ", appid='" + appid + '\'' +
                ", mobileList='" + mobileList + '\'' +
                ", contents='" + contents + '\'' +
                ", templateId='" + templateId + '\'' +
                ", sendTime='" + sendTime + '\'' +
                ", smsAmount='" + smsAmount + '\'' +
                ", sendSts='" + sendSts + '\'' +
                '}';
    }
}
