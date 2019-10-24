package com.bdaim.supplier.fund.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @date 2019/6/5
 * @description 意见反馈表
 */
@Entity
@Table(name = "t_feedback")
public class FeedBack {
    private Integer id;
    private String nickName;
    private String mobilePhone;
    private String feedback;
    private Timestamp createTime;
    private String handler;
    private Timestamp handleTime;
    private String handleResult;
    private String history;
    private String client;
    private String userAgent;
    private String ip;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Basic
    @Column(name = "nick_name")

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Basic
    @Column(name = "mobile_phone")
    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    @Basic
    @Column(name = "feedback")
    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
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
    @Column(name = "handler")
    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    @Basic
    @Column(name = "handle_time")

    public Timestamp getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(Timestamp handleTime) {
        this.handleTime = handleTime;
    }


    @Basic
    @Column(name = "handle_result")
    public String getHandleResult() {
        return handleResult;
    }

    public void setHandleResult(String handleResult) {
        this.handleResult = handleResult;
    }

    @Basic
    @Column(name = "history")
    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    @Basic
    @Column(name = "client")
    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    @Basic
    @Column(name = "userAgent")
    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Basic
    @Column(name = "ip")
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public FeedBack() {
    }

    @Override
    public String toString() {
        return "FeedBack{" +
                "id=" + id +
                ", nickName='" + nickName + '\'' +
                ", mobilePhone='" + mobilePhone + '\'' +
                ", feedback='" + feedback + '\'' +
                ", createTime=" + createTime +
                ", handler='" + handler + '\'' +
                ", handleTime='" + handleTime + '\'' +
                ", handleResult='" + handleResult + '\'' +
                ", history='" + history + '\'' +
                ", client='" + client + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
