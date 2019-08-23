package com.bdaim.customer.dto;

import com.bdaim.customer.entity.ApparentNumber;

import java.sql.Timestamp;

/**
 * 外显号管理
 *
 * @author chengning@salescomm.net
 * @date 2019/2/13
 * @description
 */
public class ApparentNumberDTO {
    private int id;
    private String custId;
    private Integer type;
    private String apparentNumber;
    private String operator;
    private String province;
    private Integer status;
    private Integer stopStatus;
    private Timestamp createTime;
    private Timestamp stopTime;
    private String areaCode;
    private String callType;
    private String callChannel;
    private String callChannelName;
    private String signInfo;

    public ApparentNumberDTO(ApparentNumber model) {
        this.id = model.getId();
        this.custId = model.getCustId();
        this.type = model.getType();
        this.apparentNumber = model.getApparentNumber();
        this.operator = model.getOperator();
        this.province = model.getProvince();
        this.status = model.getStatus();
        this.createTime = model.getCreateTime();
        this.stopTime = model.getStopTime();
        this.areaCode = model.getAreaCode();
        this.stopStatus = model.getStopStatus();
        this.callType = model.getCallType();
        this.callChannel = model.getCallChannel();
        this.signInfo = model.getSignInfo();
    }

    public String getSignInfo() {
        return signInfo;
    }

    public void setSignInfo(String signInfo) {
        this.signInfo = signInfo;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getApparentNumber() {
        return apparentNumber;
    }

    public void setApparentNumber(String apparentNumber) {
        this.apparentNumber = apparentNumber;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getStopTime() {
        return stopTime;
    }

    public void setStopTime(Timestamp stopTime) {
        this.stopTime = stopTime;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public Integer getStopStatus() {
        return stopStatus;
    }

    public void setStopStatus(Integer stopStatus) {
        this.stopStatus = stopStatus;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallChannel() {
        return callChannel;
    }

    public void setCallChannel(String callChannel) {
        this.callChannel = callChannel;
    }

    public String getCallChannelName() {
        return callChannelName;
    }

    public void setCallChannelName(String callChannelName) {
        this.callChannelName = callChannelName;
    }

    @Override
    public String toString() {
        return "ApparentNumberDTO{" +
                "id=" + id +
                ", custId='" + custId + '\'' +
                ", type=" + type +
                ", apparentNumber='" + apparentNumber + '\'' +
                ", operator='" + operator + '\'' +
                ", province='" + province + '\'' +
                ", status=" + status +
                ", stopStatus=" + stopStatus +
                ", createTime=" + createTime +
                ", stopTime=" + stopTime +
                ", areaCode='" + areaCode + '\'' +
                ", callType='" + callType + '\'' +
                ", callChannel='" + callChannel + '\'' +
                ", callChannelName='" + callChannelName + '\'' +
                ", signInfo='" + signInfo + '\'' +
                '}';
    }
}
