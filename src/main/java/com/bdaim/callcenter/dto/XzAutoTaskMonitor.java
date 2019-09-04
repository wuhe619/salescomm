package com.bdaim.callcenter.dto;

/**
 * 讯众自动外呼任务监控数据
 *
 * @author chengning@salescomm.net
 * @date 2019/8/7 14:57
 */
public class XzAutoTaskMonitor {

    private String compid;
    private String taskidentity;
    private String taskname;
    private String allcustomers = "0";
    private String callout_progress = "0";
    private String successnumber = "0";
    private String failednumber = "0";
    private String successrate = "0";
    private String lossynumber = "0";
    private String allagents = "0";
    private String agentfreed = "0";
    private String agentprocessed = "0";
    private String agentinviteed = "0";
    private String agentringing = "0";
    private String lossyrate = "0";
    private String calloutcoefficient = "0";
    private String agentanswered = "0";
    private String dialdevices = "0";
    private String deviceinvited = "0";
    private String deviceringing = "0";
    private String deviceanswered = "0";
    private String averagecalltime = "0";
    private String averagefreetime = "0";
    private String averageprocesstime = "0";
    private String createdate;

    public void setCompid(String compid) {
        this.compid = compid;
    }

    public String getCompid() {
        return compid;
    }

    public void setTaskidentity(String taskidentity) {
        this.taskidentity = taskidentity;
    }

    public String getTaskidentity() {
        return taskidentity;
    }

    public void setTaskname(String taskname) {
        this.taskname = taskname;
    }

    public String getTaskname() {
        return taskname;
    }

    public void setAllcustomers(String allcustomers) {
        this.allcustomers = allcustomers;
    }

    public String getAllcustomers() {
        return allcustomers;
    }

    public void setCallout_progress(String callout_progress) {
        this.callout_progress = callout_progress;
    }

    public String getCallout_progress() {
        return callout_progress;
    }

    public void setSuccessnumber(String successnumber) {
        this.successnumber = successnumber;
    }

    public String getSuccessnumber() {
        return successnumber;
    }

    public void setFailednumber(String failednumber) {
        this.failednumber = failednumber;
    }

    public String getFailednumber() {
        return failednumber;
    }

    public void setSuccessrate(String successrate) {
        this.successrate = successrate;
    }

    public String getSuccessrate() {
        return successrate;
    }

    public void setLossynumber(String lossynumber) {
        this.lossynumber = lossynumber;
    }

    public String getLossynumber() {
        return lossynumber;
    }

    public void setAllagents(String allagents) {
        this.allagents = allagents;
    }

    public String getAllagents() {
        return allagents;
    }

    public void setAgentfreed(String agentfreed) {
        this.agentfreed = agentfreed;
    }

    public String getAgentfreed() {
        return agentfreed;
    }

    public void setAgentprocessed(String agentprocessed) {
        this.agentprocessed = agentprocessed;
    }

    public String getAgentprocessed() {
        return agentprocessed;
    }

    public void setAgentinviteed(String agentinviteed) {
        this.agentinviteed = agentinviteed;
    }

    public String getAgentinviteed() {
        return agentinviteed;
    }

    public void setAgentringing(String agentringing) {
        this.agentringing = agentringing;
    }

    public String getAgentringing() {
        return agentringing;
    }

    public void setLossyrate(String lossyrate) {
        this.lossyrate = lossyrate;
    }

    public String getLossyrate() {
        return lossyrate;
    }

    public void setCalloutcoefficient(String calloutcoefficient) {
        this.calloutcoefficient = calloutcoefficient;
    }

    public String getCalloutcoefficient() {
        return calloutcoefficient;
    }

    public void setAgentanswered(String agentanswered) {
        this.agentanswered = agentanswered;
    }

    public String getAgentanswered() {
        return agentanswered;
    }

    public void setDialdevices(String dialdevices) {
        this.dialdevices = dialdevices;
    }

    public String getDialdevices() {
        return dialdevices;
    }

    public void setDeviceinvited(String deviceinvited) {
        this.deviceinvited = deviceinvited;
    }

    public String getDeviceinvited() {
        return deviceinvited;
    }

    public void setDeviceringing(String deviceringing) {
        this.deviceringing = deviceringing;
    }

    public String getDeviceringing() {
        return deviceringing;
    }

    public void setDeviceanswered(String deviceanswered) {
        this.deviceanswered = deviceanswered;
    }

    public String getDeviceanswered() {
        return deviceanswered;
    }

    public void setAveragecalltime(String averagecalltime) {
        this.averagecalltime = averagecalltime;
    }

    public String getAveragecalltime() {
        return averagecalltime;
    }

    public void setAveragefreetime(String averagefreetime) {
        this.averagefreetime = averagefreetime;
    }

    public String getAveragefreetime() {
        return averagefreetime;
    }

    public void setAverageprocesstime(String averageprocesstime) {
        this.averageprocesstime = averageprocesstime;
    }

    public String getAverageprocesstime() {
        return averageprocesstime;
    }

    public void setCreatedate(String createdate) {
        this.createdate = createdate;
    }

    public String getCreatedate() {
        return createdate;
    }
}