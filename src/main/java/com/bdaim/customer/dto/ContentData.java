package com.bdaim.customer.dto;

import java.util.Date;

public class ContentData {
    private int id;
    private String custId;
    private String custName;
    private String custUserId;
    private Date createTime;
    private int status;
    private int level;
    private String msgType;

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

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getCustUserId() {
        return custUserId;
    }

    public void setCustUserId(String custUserId) {
        this.custUserId = custUserId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    @Override
    public String toString() {
        return "ContentData{" +
                "id=" + id +
                ", custId='" + custId + '\'' +
                ", custName='" + custName + '\'' +
                ", custUserId='" + custUserId + '\'' +
                ", createTime=" + createTime +
                ", status=" + status +
                ", level=" + level +
                ", msgType='" + msgType + '\'' +
                '}';
    }
}
