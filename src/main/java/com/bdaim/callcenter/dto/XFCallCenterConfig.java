package com.bdaim.callcenter.dto;

import com.alibaba.fastjson.JSON;

/** 新方呼叫中心配置
 * @author chengning@salescomm.net
 * @date 2018/10/24
 * @description
 */
public class XFCallCenterConfig {

    private String custId = "";
    private String xfCallCenterIp= "";
    private String xfCallCenterId= "";
    private String xfCallSipPort= "";
    private String xfCallSipPwd= "";
    private String callCenterType= "";
    private String xfCallCenterPwd= "";
    private String xfCallCenterRecordPort= "";
    private String xfCallCenterVoicePort= "";
    private String xfCallCenterVoiceServer= "";
    private String xfCallCenterRecordIp= "";

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getXfCallCenterIp() {
        return xfCallCenterIp;
    }

    public void setXfCallCenterIp(String xfCallCenterIp) {
        this.xfCallCenterIp = xfCallCenterIp;
    }

    public String getXfCallCenterId() {
        return xfCallCenterId;
    }

    public void setXfCallCenterId(String xfCallCenterId) {
        this.xfCallCenterId = xfCallCenterId;
    }

    public String getXfCallSipPort() {
        return xfCallSipPort;
    }

    public void setXfCallSipPort(String xfCallSipPort) {
        this.xfCallSipPort = xfCallSipPort;
    }

    public String getXfCallSipPwd() {
        return xfCallSipPwd;
    }

    public void setXfCallSipPwd(String xfCallSipPwd) {
        this.xfCallSipPwd = xfCallSipPwd;
    }

    public String getCallCenterType() {
        return callCenterType;
    }

    public void setCallCenterType(String callCenterType) {
        this.callCenterType = callCenterType;
    }

    public String getXfCallCenterPwd() {
        return xfCallCenterPwd;
    }

    public void setXfCallCenterPwd(String xfCallCenterPwd) {
        this.xfCallCenterPwd = xfCallCenterPwd;
    }

    public String getXfCallCenterRecordPort() {
        return xfCallCenterRecordPort;
    }

    public void setXfCallCenterRecordPort(String xfCallCenterRecordPort) {
        this.xfCallCenterRecordPort = xfCallCenterRecordPort;
    }

    public String getXfCallCenterVoicePort() {
        return xfCallCenterVoicePort;
    }

    public void setXfCallCenterVoicePort(String xfCallCenterVoicePort) {
        this.xfCallCenterVoicePort = xfCallCenterVoicePort;
    }

    public String getXfCallCenterVoiceServer() {
        return xfCallCenterVoiceServer;
    }

    public void setXfCallCenterVoiceServer(String xfCallCenterVoiceServer) {
        this.xfCallCenterVoiceServer = xfCallCenterVoiceServer;
    }

    public String getXfCallCenterRecordIp() {
        return xfCallCenterRecordIp;
    }

    public void setXfCallCenterRecordIp(String xfCallCenterRecordIp) {
        this.xfCallCenterRecordIp = xfCallCenterRecordIp;
    }

    @Override
    public String toString() {
        return "XFCallCenterConfig{" +
                "custId='" + custId + '\'' +
                ", xfCallCenterIp='" + xfCallCenterIp + '\'' +
                ", xfCallCenterId='" + xfCallCenterId + '\'' +
                ", xfCallSipPort='" + xfCallSipPort + '\'' +
                ", xfCallSipPwd='" + xfCallSipPwd + '\'' +
                ", callCenterType='" + callCenterType + '\'' +
                ", xfCallCenterPwd='" + xfCallCenterPwd + '\'' +
                ", xfCallCenterRecordPort='" + xfCallCenterRecordPort + '\'' +
                ", xfCallCenterVoicePort='" + xfCallCenterVoicePort + '\'' +
                ", xfCallCenterVoiceServer='" + xfCallCenterVoiceServer + '\'' +
                ", xfCallCenterRecordIp='" + xfCallCenterRecordIp + '\'' +
                '}';
    }

    public static void main(String[] args) {
        System.out.println(JSON.toJSONString(new XFCallCenterConfig()));
    }
}
