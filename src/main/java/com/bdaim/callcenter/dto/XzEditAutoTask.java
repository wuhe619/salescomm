package com.bdaim.callcenter.dto;

import java.util.Date;

/**
 * 讯众呼叫中心修改自动外呼任务实体
 *
 * @author chengning@salescomm.net
 * @date 2019/4/28 19:22
 */
public class XzEditAutoTask {

    private String taskname;
    private String expirdatebegin;
    private String expirdateend;
    private String shownum;
    private String dailmodel;
    private String calloutspeed;
    private String counttype;
    private String callinterval;
    private String ringingduration;
    private String timeruleid;
    private String waitvoiceid;
    private String seatallocationmodel;
    private String maxconcurrentnumber;
    private String intelligence_num;
    private String modifier;
    private String compid;
    private String autoid;
    private String noticeurl;

    public String getTaskname() {
        return taskname;
    }

    public void setTaskname(String taskname) {
        this.taskname = taskname;
    }

    public String getExpirdatebegin() {
        return expirdatebegin;
    }

    public void setExpirdatebegin(String expirdatebegin) {
        this.expirdatebegin = expirdatebegin;
    }

    public String getExpirdateend() {
        return expirdateend;
    }

    public void setExpirdateend(String expirdateend) {
        this.expirdateend = expirdateend;
    }

    public String getShownum() {
        return shownum;
    }

    public void setShownum(String shownum) {
        this.shownum = shownum;
    }

    public String getDailmodel() {
        return dailmodel;
    }

    public void setDailmodel(String dailmodel) {
        this.dailmodel = dailmodel;
    }

    public String getCalloutspeed() {
        return calloutspeed;
    }

    public void setCalloutspeed(String calloutspeed) {
        this.calloutspeed = calloutspeed;
    }

    public String getCounttype() {
        return counttype;
    }

    public void setCounttype(String counttype) {
        this.counttype = counttype;
    }

    public String getCallinterval() {
        return callinterval;
    }

    public void setCallinterval(String callinterval) {
        this.callinterval = callinterval;
    }

    public String getRingingduration() {
        return ringingduration;
    }

    public void setRingingduration(String ringingduration) {
        this.ringingduration = ringingduration;
    }

    public String getTimeruleid() {
        return timeruleid;
    }

    public void setTimeruleid(String timeruleid) {
        this.timeruleid = timeruleid;
    }

    public String getWaitvoiceid() {
        return waitvoiceid;
    }

    public void setWaitvoiceid(String waitvoiceid) {
        this.waitvoiceid = waitvoiceid;
    }

    public String getSeatallocationmodel() {
        return seatallocationmodel;
    }

    public void setSeatallocationmodel(String seatallocationmodel) {
        this.seatallocationmodel = seatallocationmodel;
    }

    public String getMaxconcurrentnumber() {
        return maxconcurrentnumber;
    }

    public void setMaxconcurrentnumber(String maxconcurrentnumber) {
        this.maxconcurrentnumber = maxconcurrentnumber;
    }

    public String getIntelligence_num() {
        return intelligence_num;
    }

    public void setIntelligence_num(String intelligence_num) {
        this.intelligence_num = intelligence_num;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getCompid() {
        return compid;
    }

    public void setCompid(String compid) {
        this.compid = compid;
    }

    public String getAutoid() {
        return autoid;
    }

    public void setAutoid(String autoid) {
        this.autoid = autoid;
    }

    public String getNoticeurl() {
        return noticeurl;
    }

    public void setNoticeurl(String noticeurl) {
        this.noticeurl = noticeurl;
    }

    @Override
    public String toString() {
        return "XzEditAutoTask{" +
                "taskname='" + taskname + '\'' +
                ", expirdatebegin=" + expirdatebegin +
                ", expirdateend=" + expirdateend +
                ", shownum='" + shownum + '\'' +
                ", dailmodel='" + dailmodel + '\'' +
                ", calloutspeed='" + calloutspeed + '\'' +
                ", counttype='" + counttype + '\'' +
                ", callinterval='" + callinterval + '\'' +
                ", ringingduration='" + ringingduration + '\'' +
                ", timeruleid='" + timeruleid + '\'' +
                ", waitvoiceid='" + waitvoiceid + '\'' +
                ", seatallocationmodel='" + seatallocationmodel + '\'' +
                ", maxconcurrentnumber='" + maxconcurrentnumber + '\'' +
                ", intelligence_num='" + intelligence_num + '\'' +
                ", modifier='" + modifier + '\'' +
                ", compid='" + compid + '\'' +
                ", autoid='" + autoid + '\'' +
                ", noticeurl='" + noticeurl + '\'' +
                '}';
    }
}