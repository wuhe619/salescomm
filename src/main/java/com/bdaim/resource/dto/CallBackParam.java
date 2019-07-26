package com.bdaim.resource.dto;

/**
 * @author duanliying
 * @date 2019/1/14
 * 讯众外呼参数类
 * @description
 */
public class CallBackParam {
    private String action;
    private String appid;
    private String src;
    private String dst;
    private String credit;
    private String customParm;
    private String srcclid;
    private String dstclid;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getCustomParm() {
        return customParm;
    }

    public void setCustomParm(String customParm) {
        this.customParm = customParm;
    }

    public String getSrcclid() {
        return srcclid;
    }

    public void setSrcclid(String srcclid) {
        this.srcclid = srcclid;
    }

    public String getDstclid() {
        return dstclid;
    }

    public void setDstclid(String dstclid) {
        this.dstclid = dstclid;
    }

    @Override
    public String toString() {
        return "CallBackParam{" +
                "action='" + action + '\'' +
                ", appid='" + appid + '\'' +
                ", src='" + src + '\'' +
                ", dst='" + dst + '\'' +
                ", credit='" + credit + '\'' +
                ", customParm='" + customParm + '\'' +
                ", srcclid='" + srcclid + '\'' +
                ", dstclid='" + dstclid + '\'' +
                '}';
    }

}