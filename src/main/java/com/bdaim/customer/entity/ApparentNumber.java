package com.bdaim.customer.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 外显号管理
 *
 * @author chengning@salescomm.net
 * @date 2019/2/13
 * @description
 */
@Entity
@Table(name = "t_apparent_number")
public class ApparentNumber {
    private int id;
    private String custId;
    private Integer type;
    private String apparentNumber;
    private String areaCode;
    private String operator;
    private String province;
    private Integer status;
    private Integer stopStatus;
    private Timestamp createTime;
    private Timestamp stopTime;
    private String callType;
    private String callChannel;
    private String signInfo;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "cust_id")
    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    @Basic
    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Basic
    @Column(name = "apparent_number")
    public String getApparentNumber() {
        return apparentNumber;
    }

    public void setApparentNumber(String apparentNumber) {
        this.apparentNumber = apparentNumber;
    }

    @Basic
    @Column(name = "operator")
    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Basic
    @Column(name = "province")
    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    @Basic
    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "stop_time")
    public Timestamp getStopTime() {
        return stopTime;
    }

    public void setStopTime(Timestamp stopTime) {
        this.stopTime = stopTime;
    }

    @Basic
    @Column(name = "stop_status")
    public Integer getStopStatus() {
        return stopStatus;
    }

    public void setStopStatus(Integer stopStatus) {
        this.stopStatus = stopStatus;
    }

    @Basic
    @Column(name = "area_code")
    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    @Basic
    @Column(name = "call_type")
    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    @Basic
    @Column(name = "call_channel")
    public String getCallChannel() {
        return callChannel;
    }

    public void setCallChannel(String callChannel) {
        this.callChannel = callChannel;
    }

    @Basic
    @Column(name = "sign_info")
    public String getSignInfo() {
        return signInfo;
    }

    public void setSignInfo(String signInfo) {
        this.signInfo = signInfo;
    }

    @Override
    public String toString() {
        return "ApparentNumber{" +
                "id=" + id +
                ", custId='" + custId + '\'' +
                ", type=" + type +
                ", apparentNumber='" + apparentNumber + '\'' +
                ", areaCode='" + areaCode + '\'' +
                ", operator='" + operator + '\'' +
                ", province='" + province + '\'' +
                ", status=" + status +
                ", stopStatus=" + stopStatus +
                ", createTime=" + createTime +
                ", stopTime=" + stopTime +
                ", callType='" + callType + '\'' +
                ", callChannel='" + callChannel + '\'' +
                ", signInfo='" + signInfo + '\'' +
                '}';
    }
}
