package com.bdaim.customer.dto;

import java.sql.Timestamp;

/** 外显号管理
 * @author chengning@salescomm.net
 * @date 2019/2/13
 * @description
 */
public class ApparentNumberQueryParam {
    private int id;
    private String custId;
    private Integer type;
    private String apparentNumber;
    private String operator;
    private String province;
    private Integer status;
    private Timestamp createTime;
    private Timestamp stopTime;
    private Integer stopStatus;
    private String callType;
    private String callChannel;
    private int pageNum;
    private int pageSize;


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

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
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

    @Override
    public String toString() {
        return "ApparentNumberQueryParam{" +
                "id=" + id +
                ", custId='" + custId + '\'' +
                ", type=" + type +
                ", apparentNumber='" + apparentNumber + '\'' +
                ", operator='" + operator + '\'' +
                ", province='" + province + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", stopTime=" + stopTime +
                ", stopStatus=" + stopStatus +
                ", callType='" + callType + '\'' +
                ", callChannel='" + callChannel + '\'' +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                '}';
    }
}
