package com.bdaim.bill.dto;

/**
 * @author duanliying
 * @date 2018/12/6
 * @description 话单推送
 */
public class CallBackInfoParam {
    //老版本呼叫流水
    private String requestId;
    //老版本呼叫流水（通话的唯一标识）
    private String sessionId;
    //呼叫流水（通话的唯一标识）V1
    private String uuid;
    //呼叫中心流水 v1
    private String callId;
    //坐席分机
    private String localUrl;
    //客户电话
    private String remoteUrl;
    //企业ID
    private String entId;
    //坐席ID
    private String agentId;
    //通话开始时间
    private String startTime;
    //通话结束时间
    private String endTime;
    //结束类型：0：通话未完成 1：通话完成2：通话失败
    private String type;
    //呼叫类型：0：呼入1：呼出
    private String callType;
    //挂断类型 ：0：客户挂断1：坐席挂断
    private String hangupType;
    //总时长（可以不用）
    private String duration;
    //通话时长
    private String callDuration;
    //IVR结果（用户授权收取的按键：0没有授权   1同意授权）
    private String accredit;
    //录音地址（企业类型为0不含此字段）
    private String recordUrl;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public CallBackInfoParam() {
    }

    public CallBackInfoParam(String requestId, String sessionId, String localUrl, String remoteUrl, String entId, String agentId, String startTime, String endTime, String type, String callType, String hangupType, String duration, String callDuration, String accredit, String recordUrl, String record_URL, String RECORD_URL, String remoteUri) {
        this.requestId = requestId;
        this.sessionId = sessionId;
        this.localUrl = localUrl;
        this.remoteUrl = remoteUrl;
        this.entId = entId;
        this.agentId = agentId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.callType = callType;
        this.hangupType = hangupType;
        this.duration = duration;
        this.callDuration = callDuration;
        this.accredit = accredit;
        this.recordUrl = recordUrl;
    }


    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getLocalUrl() {
        return localUrl;
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getEntId() {
        return entId;
    }

    public void setEntId(String entId) {
        this.entId = entId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getHangupType() {
        return hangupType;
    }

    public void setHangupType(String hangupType) {
        this.hangupType = hangupType;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(String callDuration) {
        this.callDuration = callDuration;
    }

    public String getAccredit() {
        return accredit;
    }

    public void setAccredit(String accredit) {
        this.accredit = accredit;
    }

    public String getRecordUrl() {
        return recordUrl;
    }

    public void setRecordUrl(String recordUrl) {
        this.recordUrl = recordUrl;
    }

    @Override
    public String toString() {
        return "CallBackInfoParam{" +
                "requestId='" + requestId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", localUrl='" + localUrl + '\'' +
                ", remoteUrl='" + remoteUrl + '\'' +
                ", entId='" + entId + '\'' +
                ", agentId='" + agentId + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", type='" + type + '\'' +
                ", callType='" + callType + '\'' +
                ", hangupType='" + hangupType + '\'' +
                ", duration='" + duration + '\'' +
                ", callDuration='" + callDuration + '\'' +
                ", accredit='" + accredit + '\'' +
                ", recordUrl='" + recordUrl + '\'' +
                '}';
    }
}
