package com.bdaim.smscenter.dto;

import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2018/7/26
 * @description
 */
public class YxtSmsParam {

    private String action;
    private String mobile;
    private String appid;
    private String templateId;
    private List<String> datas;
    private String spuid;
    private String sppwd;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public List<String> getDatas() {
        return datas;
    }

    public void setDatas(List<String> datas) {
        this.datas = datas;
    }

    public String getSpuid() {
        return spuid;
    }

    public void setSpuid(String spuid) {
        this.spuid = spuid;
    }

    public String getSppwd() {
        return sppwd;
    }

    public void setSppwd(String sppwd) {
        this.sppwd = sppwd;
    }

    @Override
    public String toString() {
        return "YxtSmsParam{" +
                "action='" + action + '\'' +
                ", mobile='" + mobile + '\'' +
                ", appid='" + appid + '\'' +
                ", templateId='" + templateId + '\'' +
                ", datas=" + datas +
                ", spuid='" + spuid + '\'' +
                ", sppwd='" + sppwd + '\'' +
                '}';
    }
}
