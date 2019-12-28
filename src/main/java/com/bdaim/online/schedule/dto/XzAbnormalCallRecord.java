package com.bdaim.online.schedule.dto;

public class XzAbnormalCallRecord {

    private long id;
    private String requestid;
    private int calltype;
    private String callid;
    private String compid;
    private int cdrtype;
    private int grouptype;
    private String caller;
    private String callee;
    private String callstatus;
    private String App;
    private int respcode;
    private String stime;
    private String rtime;
    private String atime;
    private String etime;
    private String rec_path;
    private String agentid;
    private int ring_time;
    private int duration_time;
    private int fee;
    private int feetime;
    private int totaltime;
    private String caller_area;
    private String callee_area;
    private String dh;
    private String AgentGrpId;
    private int Isfee;
    private String Duration;
    private String channel;
    private String TaskId;
    private String serverid;
    private int callresult;


    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setRequestid(String requestid) {
        this.requestid = requestid;
    }

    public String getRequestid() {
        return requestid;
    }

    public void setCalltype(int calltype) {
        this.calltype = calltype;
    }

    public int getCalltype() {
        return calltype;
    }

    public void setCallid(String callid) {
        this.callid = callid;
    }

    public String getCallid() {
        return callid;
    }

    public void setCompid(String compid) {
        this.compid = compid;
    }

    public String getCompid() {
        return compid;
    }

    public void setCdrtype(int cdrtype) {
        this.cdrtype = cdrtype;
    }

    public int getCdrtype() {
        return cdrtype;
    }

    public void setGrouptype(int grouptype) {
        this.grouptype = grouptype;
    }

    public int getGrouptype() {
        return grouptype;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getCaller() {
        return caller;
    }

    public void setCallee(String callee) {
        this.callee = callee;
    }

    public String getCallee() {
        return callee;
    }

    public void setCallstatus(String callstatus) {
        this.callstatus = callstatus;
    }

    public String getCallstatus() {
        return callstatus;
    }

    public void setApp(String App) {
        this.App = App;
    }

    public String getApp() {
        return App;
    }

    public void setRespcode(int respcode) {
        this.respcode = respcode;
    }

    public int getRespcode() {
        return respcode;
    }

    public void setRtime(String rtime) {
        this.rtime = rtime;
    }

    public String getRtime() {
        return rtime;
    }

    public void setAtime(String atime) {
        this.atime = atime;
    }

    public String getAtime() {
        return atime;
    }

    public void setRec_path(String rec_path) {
        this.rec_path = rec_path;
    }

    public String getRec_path() {
        return rec_path;
    }

    public void setAgentid(String agentid) {
        this.agentid = agentid;
    }

    public String getAgentid() {
        return agentid;
    }

    public void setRing_time(int ring_time) {
        this.ring_time = ring_time;
    }

    public int getRing_time() {
        return ring_time;
    }

    public void setDuration_time(int duration_time) {
        this.duration_time = duration_time;
    }

    public int getDuration_time() {
        return duration_time;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }

    public int getFee() {
        return fee;
    }

    public void setTotaltime(int totaltime) {
        this.totaltime = totaltime;
    }

    public int getTotaltime() {
        return totaltime;
    }

    public void setCaller_area(String caller_area) {
        this.caller_area = caller_area;
    }

    public String getCaller_area() {
        return caller_area;
    }

    public void setCallee_area(String callee_area) {
        this.callee_area = callee_area;
    }

    public String getCallee_area() {
        return callee_area;
    }

    public void setDh(String dh) {
        this.dh = dh;
    }

    public String getDh() {
        return dh;
    }

    public void setAgentGrpId(String AgentGrpId) {
        this.AgentGrpId = AgentGrpId;
    }

    public String getAgentGrpId() {
        return AgentGrpId;
    }

    public void setIsfee(int Isfee) {
        this.Isfee = Isfee;
    }

    public int getIsfee() {
        return Isfee;
    }

    public void setDuration(String Duration) {
        this.Duration = Duration;
    }

    public String getDuration() {
        return Duration;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public void setTaskId(String TaskId) {
        this.TaskId = TaskId;
    }

    public String getTaskId() {
        return TaskId;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }

    public String getServerid() {
        return serverid;
    }

    public String getStime() {
        return stime;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public String getEtime() {
        return etime;
    }

    public void setEtime(String etime) {
        this.etime = etime;
    }

    public int getFeetime() {
        return feetime;
    }

    public void setFeetime(int feetime) {
        this.feetime = feetime;
    }

    public int getCallresult() {
        return callresult;
    }

    public void setCallresult(int callresult) {
        this.callresult = callresult;
    }

    @Override
    public String toString() {
        return "XzAbnormalCallRecord{" +
                "id=" + id +
                ", requestid='" + requestid + '\'' +
                ", calltype=" + calltype +
                ", callid='" + callid + '\'' +
                ", compid='" + compid + '\'' +
                ", cdrtype=" + cdrtype +
                ", grouptype=" + grouptype +
                ", caller='" + caller + '\'' +
                ", callee='" + callee + '\'' +
                ", callstatus='" + callstatus + '\'' +
                ", App='" + App + '\'' +
                ", respcode=" + respcode +
                ", stime='" + stime + '\'' +
                ", rtime='" + rtime + '\'' +
                ", atime='" + atime + '\'' +
                ", etime='" + etime + '\'' +
                ", rec_path='" + rec_path + '\'' +
                ", agentid='" + agentid + '\'' +
                ", ring_time=" + ring_time +
                ", duration_time=" + duration_time +
                ", fee=" + fee +
                ", feetime=" + feetime +
                ", totaltime=" + totaltime +
                ", caller_area='" + caller_area + '\'' +
                ", callee_area='" + callee_area + '\'' +
                ", dh='" + dh + '\'' +
                ", AgentGrpId='" + AgentGrpId + '\'' +
                ", Isfee=" + Isfee +
                ", Duration='" + Duration + '\'' +
                ", channel='" + channel + '\'' +
                ", TaskId='" + TaskId + '\'' +
                ", serverid='" + serverid + '\'' +
                ", callresult=" + callresult +
                '}';
    }
}