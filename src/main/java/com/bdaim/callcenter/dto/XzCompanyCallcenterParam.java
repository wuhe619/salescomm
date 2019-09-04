package com.bdaim.callcenter.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * 呼叫中心企业账号参数
 */
public class XzCompanyCallcenterParam implements Serializable {
    private String compid;
    private String companyname;
    private String begintime;
    private String endtime;
    private int amountagentauth;
    private int expirerecord;
    private int enable;
    private  int maxconcurrentnumber;
    private String source = "huoke";
    private String account = "8888";
    private String pwd = "huoke123456";


    public int getMaxconcurrentnumber() {
        return maxconcurrentnumber;
    }

    public void setMaxconcurrentnumber(int maxconcurrentnumber) {
        this.maxconcurrentnumber = maxconcurrentnumber;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getCompid() {
        return compid;
    }

    public void setCompid(String compid) {
        this.compid = compid;
    }

    public String getCompanyname() {
        return companyname;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public String getBegintime() {
        return begintime;
    }

    public void setBegintime(String begintime) {
        this.begintime = begintime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public int getAmountagentauth() {
        return amountagentauth;
    }

    public void setAmountagentauth(int amountagentauth) {
        this.amountagentauth = amountagentauth;
    }

    public int getExpirerecord() {
        return expirerecord;
    }

    public void setExpirerecord(int expirerecord) {
        this.expirerecord = expirerecord;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XzCompanyCallcenterParam param = (XzCompanyCallcenterParam) o;
        return amountagentauth == param.amountagentauth &&
                expirerecord == param.expirerecord &&
                enable == param.enable &&
                maxconcurrentnumber == param.maxconcurrentnumber &&
                Objects.equals(compid, param.compid) &&
                Objects.equals(companyname, param.companyname) &&
                Objects.equals(begintime, param.begintime) &&
                Objects.equals(endtime, param.endtime) &&
                Objects.equals(source, param.source) &&
                Objects.equals(account, param.account) &&
                Objects.equals(pwd, param.pwd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compid, companyname, begintime, endtime, amountagentauth, expirerecord, enable, maxconcurrentnumber, source, account, pwd);
    }
}
