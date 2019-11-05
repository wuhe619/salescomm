package com.bdaim.customer.dto;

import com.bdaim.customer.entity.CustomerMsg;
import org.joda.time.DateTime;

import java.util.Date;

public class ContentDTO {
    private int id;
    private String custId;
    private String custUserId;
    private Date createTime;
    private String msgType;
    private String custName;

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
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

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public ContentDTO() {
    }

    public ContentDTO(ContentData entity) {
        this.id = entity.getId();
        this.custId = entity.getCustId();
        this.custUserId = entity.getCustUserId();
        this.createTime = entity.getCreateTime();
        this.msgType = entity.getMsgType();
        this.custName = entity.getCustName();
    }
}
