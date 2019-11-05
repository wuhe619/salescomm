package com.bdaim.customer.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "h_customer_msg")
public class CustomerMsg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "cust_id")
    private String custId;
    @Column(name = "cust_user_id")
    private String custUserId;
    @Column(name = "content")
    private String content;
    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "status")
    private int status;
    @Column(name = "level")
    private int level;
    @Column(name = "msg_type")
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

    public String getCustUserId() {
        return custUserId;
    }

    public void setCustUserId(String custUserId) {
        this.custUserId = custUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
}
