package com.bdaim.callcenter.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019/2/20
 * @description
 */
public class CallCenterConfig {


    private String callCenterType;

    private String callCenterId;

    private String callCenterIp;

    private String apparentNumber;

    private String xfCallSipPort;

    private String xfCallSipPwd;

    private String callCenterAppIp;

    public String getCallCenterType() {
        return callCenterType;
    }

    public void setCallCenterType(String callCenterType) {
        this.callCenterType = callCenterType;
    }

    public String getCallCenterId() {
        return callCenterId;
    }

    public void setCallCenterId(String callCenterId) {
        this.callCenterId = callCenterId;
    }

    public String getCallCenterIp() {
        return callCenterIp;
    }

    public void setCallCenterIp(String callCenterIp) {
        this.callCenterIp = callCenterIp;
    }

    public String getApparentNumber() {
        return apparentNumber;
    }

    public void setApparentNumber(String apparentNumber) {
        this.apparentNumber = apparentNumber;
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

    public String getCallCenterAppIp() {
        return callCenterAppIp;
    }

    public void setCallCenterAppIp(String callCenterAppIp) {
        this.callCenterAppIp = callCenterAppIp;
    }
}
