package com.bdaim.callcenter.dto;

/**
 * 讯众呼叫中心添加自动外呼任务实体
 *
 * @author chengning@salescomm.net
 * @date 2019/4/28 19:22
 */
public class XzAddAutoTask {

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
    private String creator;
    private String compid;
    private String noticeurl;

    public void setTaskname(String taskname) {
        this.taskname = taskname;
    }

    public String getTaskname() {
        return taskname;
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

    public void setShownum(String shownum) {
        this.shownum = shownum;
    }

    public String getShownum() {
        return shownum;
    }

    public void setDailmodel(String dailmodel) {
        this.dailmodel = dailmodel;
    }

    public String getDailmodel() {
        return dailmodel;
    }

    public void setCalloutspeed(String calloutspeed) {
        this.calloutspeed = calloutspeed;
    }

    public String getCalloutspeed() {
        return calloutspeed;
    }

    public void setCounttype(String counttype) {
        this.counttype = counttype;
    }

    public String getCounttype() {
        return counttype;
    }

    public void setCallinterval(String callinterval) {
        this.callinterval = callinterval;
    }

    public String getCallinterval() {
        return callinterval;
    }

    public void setRingingduration(String ringingduration) {
        this.ringingduration = ringingduration;
    }

    public String getRingingduration() {
        return ringingduration;
    }

    public void setTimeruleid(String timeruleid) {
        this.timeruleid = timeruleid;
    }

    public String getTimeruleid() {
        return timeruleid;
    }

    public void setWaitvoiceid(String waitvoiceid) {
        this.waitvoiceid = waitvoiceid;
    }

    public String getWaitvoiceid() {
        return waitvoiceid;
    }

    public void setSeatallocationmodel(String seatallocationmodel) {
        this.seatallocationmodel = seatallocationmodel;
    }

    public String getSeatallocationmodel() {
        return seatallocationmodel;
    }

    public void setMaxconcurrentnumber(String maxconcurrentnumber) {
        this.maxconcurrentnumber = maxconcurrentnumber;
    }

    public String getMaxconcurrentnumber() {
        return maxconcurrentnumber;
    }

    public void setIntelligence_num(String intelligence_num) {
        this.intelligence_num = intelligence_num;
    }

    public String getIntelligence_num() {
        return intelligence_num;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    public void setCompid(String compid) {
        this.compid = compid;
    }

    public String getCompid() {
        return compid;
    }

    public void setNoticeurl(String noticeurl) {
        this.noticeurl = noticeurl;
    }

    public String getNoticeurl() {
        return noticeurl;
    }

    @Override
    public String toString() {
        return "XzAddAutoTask{" +
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
                ", creator='" + creator + '\'' +
                ", compid='" + compid + '\'' +
                ", noticeurl='" + noticeurl + '\'' +
                '}';
    }
}