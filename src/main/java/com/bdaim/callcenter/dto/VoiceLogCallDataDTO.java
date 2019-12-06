package com.bdaim.callcenter.dto;

/**
 * @author chengning@salescomm.net
 * @date 2018/11/13
 * @description
 */
public class VoiceLogCallDataDTO {

    private String caller;
    private String callerStartTime;
    private String callerEndTime;
    private String callerDuration;
    private String called;
    private String calledStartTime;
    private String calledEndTime;
    private String calledDuration;
    private String duration;
    private String recordUrl;
    private String level = "";

    public VoiceLogCallDataDTO(String caller, String callerStartTime, String callerEndTime, String callerDuration, String called, String calledStartTime, String calledEndTime, String calledDuration, String duration, String recordUrl, String level) {
        this.caller = caller;
        this.callerStartTime = callerStartTime;
        this.callerEndTime = callerEndTime;
        this.callerDuration = callerDuration;
        this.called = called;
        this.calledStartTime = calledStartTime;
        this.calledEndTime = calledEndTime;
        this.calledDuration = calledDuration;
        this.duration = duration;
        this.recordUrl = recordUrl;
        this.level = level;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getCallerStartTime() {
        return callerStartTime;
    }

    public void setCallerStartTime(String callerStartTime) {
        this.callerStartTime = callerStartTime;
    }

    public String getCallerEndTime() {
        return callerEndTime;
    }

    public void setCallerEndTime(String callerEndTime) {
        this.callerEndTime = callerEndTime;
    }

    public String getCallerDuration() {
        return callerDuration;
    }

    public void setCallerDuration(String callerDuration) {
        this.callerDuration = callerDuration;
    }

    public String getCalled() {
        return called;
    }

    public void setCalled(String called) {
        this.called = called;
    }

    public String getCalledStartTime() {
        return calledStartTime;
    }

    public void setCalledStartTime(String calledStartTime) {
        this.calledStartTime = calledStartTime;
    }

    public String getCalledEndTime() {
        return calledEndTime;
    }

    public void setCalledEndTime(String calledEndTime) {
        this.calledEndTime = calledEndTime;
    }

    public String getCalledDuration() {
        return calledDuration;
    }

    public void setCalledDuration(String calledDuration) {
        this.calledDuration = calledDuration;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRecordUrl() {
        return recordUrl;
    }

    public void setRecordUrl(String recordUrl) {
        this.recordUrl = recordUrl;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "VoiceLogCallDataDTO{" +
                "caller='" + caller + '\'' +
                ", callerStartTime='" + callerStartTime + '\'' +
                ", callerEndTime='" + callerEndTime + '\'' +
                ", callerDuration='" + callerDuration + '\'' +
                ", called='" + called + '\'' +
                ", calledStartTime='" + calledStartTime + '\'' +
                ", calledEndTime='" + calledEndTime + '\'' +
                ", calledDuration='" + calledDuration + '\'' +
                ", duration='" + duration + '\'' +
                ", recordUrl='" + recordUrl + '\'' +
                ", level='" + level + '\'' +
                '}';
    }
}
