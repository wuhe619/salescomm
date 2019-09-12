package com.bdaim.callcenter.dto;

/**
 * 讯众自动外呼任务监控数据
 * @author chengning@salescomm.net
 * @date 2019/8/7 14:57
 */
public class XzAutoTaskMonitor0 {

    private String taskId;
    /**
     * 用户总数
     */
    private String allCustomers;
    /**
     * 外呼进度
     */
    private String calloutProgress;
    /**
     * 接通数量
     */
    private String successNumber;
    /**
     * 未接通数量
     */
    private String failedNumber;
    /**
     * 接通率
     */
    private String successRate;


    /**
     * 呼损数量
     */
    private String lossyNumber;
    /**
     * 呼损率
     */
    private String lossyRate;
    /**
     * 外呼系数
     */
    private String calloutCoefficient;


    /**
     * 坐席数量
     */
    private String allAgents;
    /**
     * 坐席空闲数量
     */
    private String agentFreed;
    /**
     * 坐席后处理数量
     */
    private String agentProcessed;
    /**
     * 坐席振铃数量
     */
    private String agentRinging;
    /**
     * 坐席应答数量
     */
    private String agentAnswered;


    /**
     * 号码呼叫实时数量
     */
    private String dialDevices;
    /**
     * 用户振铃数量
     */
    private String deviceRinging;
    /**
     * 用户接通数量
     */
    private String deviceAnswered;


    /**
     * 平均通话时长（秒）
     */
    private String averageCalltime;
    /**
     * 平均空闲时长
     */
    private String averageFreetime;
    /**
     * 平均后处理时长
     */
    private String averageProcessTime;
    /**
     * 创建时间
     */
    private String createTime;

    public String getAllCustomers() {
        return allCustomers;
    }

    public void setAllCustomers(String allCustomers) {
        this.allCustomers = allCustomers;
    }

    public String getCalloutProgress() {
        return calloutProgress;
    }

    public void setCalloutProgress(String calloutProgress) {
        this.calloutProgress = calloutProgress;
    }

    public String getSuccessNumber() {
        return successNumber;
    }

    public void setSuccessNumber(String successNumber) {
        this.successNumber = successNumber;
    }

    public String getFailedNumber() {
        return failedNumber;
    }

    public void setFailedNumber(String failedNumber) {
        this.failedNumber = failedNumber;
    }

    public String getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(String successRate) {
        this.successRate = successRate;
    }

    public String getLossyNumber() {
        return lossyNumber;
    }

    public void setLossyNumber(String lossyNumber) {
        this.lossyNumber = lossyNumber;
    }

    public String getAllAgents() {
        return allAgents;
    }

    public void setAllAgents(String allAgents) {
        this.allAgents = allAgents;
    }

    public String getAgentFreed() {
        return agentFreed;
    }

    public void setAgentFreed(String agentFreed) {
        this.agentFreed = agentFreed;
    }

    public String getAgentProcessed() {
        return agentProcessed;
    }

    public void setAgentProcessed(String agentProcessed) {
        this.agentProcessed = agentProcessed;
    }


    public String getAgentRinging() {
        return agentRinging;
    }

    public void setAgentRinging(String agentRinging) {
        this.agentRinging = agentRinging;
    }

    public String getLossyRate() {
        return lossyRate;
    }

    public void setLossyRate(String lossyRate) {
        this.lossyRate = lossyRate;
    }

    public String getCalloutCoefficient() {
        return calloutCoefficient;
    }

    public void setCalloutCoefficient(String calloutCoefficient) {
        this.calloutCoefficient = calloutCoefficient;
    }

    public String getAgentAnswered() {
        return agentAnswered;
    }

    public void setAgentAnswered(String agentAnswered) {
        this.agentAnswered = agentAnswered;
    }

    public String getDialDevices() {
        return dialDevices;
    }

    public void setDialDevices(String dialDevices) {
        this.dialDevices = dialDevices;
    }

    public String getDeviceRinging() {
        return deviceRinging;
    }

    public void setDeviceRinging(String deviceRinging) {
        this.deviceRinging = deviceRinging;
    }

    public String getDeviceAnswered() {
        return deviceAnswered;
    }

    public void setDeviceAnswered(String deviceAnswered) {
        this.deviceAnswered = deviceAnswered;
    }

    public String getAverageCalltime() {
        return averageCalltime;
    }

    public void setAverageCalltime(String averageCalltime) {
        this.averageCalltime = averageCalltime;
    }

    public String getAverageFreetime() {
        return averageFreetime;
    }

    public void setAverageFreetime(String averageFreetime) {
        this.averageFreetime = averageFreetime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getAverageProcessTime() {
        return averageProcessTime;
    }

    public void setAverageProcessTime(String averageProcessTime) {
        this.averageProcessTime = averageProcessTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}