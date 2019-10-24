package com.bdaim.supplier.fund.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2019/6/5
 * @description
 */
@Entity
@Table(name = "t_fund_product_apply")
public class FundProductApply {
    private Long id;
    private Long userId;
    private String mobilePhone;
    private String productId;
    private String productType;
    private Timestamp applyTime;
    private String fromClient;
    private String channel;
    private String activityId;
    private String loanAmount;
    private String loanTerm;
    private String loanLate;
    private Integer matchStatus;
    private String applyValue;

    @Id
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "user_id")
    public Long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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
    @Column(name = "product_id")
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Basic
    @Column(name = "product_type")
    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    @Basic
    @Column(name = "apply_time")
    public Timestamp getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Timestamp applyTime) {
        this.applyTime = applyTime;
    }

    @Basic
    @Column(name = "from_client")
    public String getFromClient() {
        return fromClient;
    }

    public void setFromClient(String fromClient) {
        this.fromClient = fromClient;
    }

    @Basic
    @Column(name = "channel")
    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Basic
    @Column(name = "activity_id")
    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    @Basic
    @Column(name = "loan_amount")
    public String getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(String loanAmount) {
        this.loanAmount = loanAmount;
    }

    @Basic
    @Column(name = "loan_term")
    public String getLoanTerm() {
        return loanTerm;
    }

    public void setLoanTerm(String loanTerm) {
        this.loanTerm = loanTerm;
    }

    @Basic
    @Column(name = "loan_late")
    public String getLoanLate() {
        return loanLate;
    }

    public void setLoanLate(String loanLate) {
        this.loanLate = loanLate;
    }

    @Basic
    @Column(name = "match_status")
    public Integer getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(Integer matchStatus) {
        this.matchStatus = matchStatus;
    }

    @Basic
    @Column(name = "apply_value")
    public String getApplyValue() {
        return applyValue;
    }

    public void setApplyValue(String applyValue) {
        this.applyValue = applyValue;
    }

    @Override
    public String toString() {
        return "FundOrder{" +
                "id=" + id +
                ", userId=" + userId +
                ", mobilePhone='" + mobilePhone + '\'' +
                ", productId='" + productId + '\'' +
                ", productType='" + productType + '\'' +
                ", applyTime=" + applyTime +
                ", fromClient='" + fromClient + '\'' +
                ", channel='" + channel + '\'' +
                ", activityId='" + activityId + '\'' +
                ", loanAmount='" + loanAmount + '\'' +
                ", loanTerm='" + loanTerm + '\'' +
                ", loanLate='" + loanLate + '\'' +
                ", matchStatus=" + matchStatus +
                ", applyValue='" + applyValue + '\'' +
                '}';
    }
}
