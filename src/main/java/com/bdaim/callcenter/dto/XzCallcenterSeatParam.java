package com.bdaim.callcenter.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * 呼叫中心坐席参数
 */
public class XzCallcenterSeatParam implements Serializable {
    private String compid; //企业标识
    private String agentid;//座席id
    private Integer agentrole;//座席角色
    private String agentpwd;//座席密码
    private String extpwd;//分机密码
    private String shownumber;//外显号
    private String creator;//创建人
    /**
     * 话后处理方式。
     * 1：定时示闲
     * 2：立即进入空闲状态
     * 3：进入后处理状态
     * (必传)
     */
    private Integer istoafterstate;
    /**
     * 接续号码。(选传)
     */
    private String trannum;

    public String getCompid() {
        return compid;
    }

    public void setCompid(String compid) {
        this.compid = compid;
    }

    public String getAgentid() {
        return agentid;
    }

    public void setAgentid(String agentid) {
        this.agentid = agentid;
    }

    public Integer getAgentrole() {
        return agentrole;
    }

    public void setAgentrole(Integer agentrole) {
        this.agentrole = agentrole;
    }

    public String getAgentpwd() {
        return agentpwd;
    }

    public void setAgentpwd(String agentpwd) {
        this.agentpwd = agentpwd;
    }

    public String getExtpwd() {
        return extpwd;
    }

    public void setExtpwd(String extpwd) {
        this.extpwd = extpwd;
    }

    public String getShownumber() {
        return shownumber;
    }

    public void setShownumber(String shownumber) {
        this.shownumber = shownumber;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Integer getIstoafterstate() {
        return istoafterstate;
    }

    public void setIstoafterstate(Integer istoafterstate) {
        this.istoafterstate = istoafterstate;
    }

    public String getTrannum() {
        return trannum;
    }

    public void setTrannum(String trannum) {
        this.trannum = trannum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XzCallcenterSeatParam that = (XzCallcenterSeatParam) o;
        return Objects.equals(compid, that.compid) &&
                Objects.equals(agentid, that.agentid) &&
                Objects.equals(agentrole, that.agentrole) &&
                Objects.equals(agentpwd, that.agentpwd) &&
                Objects.equals(extpwd, that.extpwd) &&
                Objects.equals(shownumber, that.shownumber) &&
                Objects.equals(creator, that.creator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compid, agentid, agentrole, agentpwd, extpwd, shownumber, creator);
    }
}
