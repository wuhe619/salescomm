package com.bdaim.rbac.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "agent_account_recorde")
public class AgentAccountRecorde {

    @Id
    @Column(name = "id")
    private  Long id;
    @Basic
    @Column(name = "customer_Id")
    private  String customerId;
    @Basic
    @Column(name = "account_type")
    private  Integer account_type;
    @Basic
    @Column(name = "account_cost")
    private BigDecimal accountCost;
    @Basic
    @Column(name = "account_profit")
    private BigDecimal accountProfit;
    @Basic
    @Column(name = "start_banlance")
    private BigDecimal startBanlance;
    @Basic
    @Column(name = "end_banlance")
    private BigDecimal endBanlance;
    @Basic
    @Column(name = "fail_money")
    private BigDecimal failMoney;

    @Basic
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    @Basic
    @Column(name = "confirm_re_msg")
    private String confirmReMsg;
    @Basic
    @Column(name = "account_time")
    private String accountTime;
    @Basic
    @Column(name = "INSERT_TIME")
    private Date insertTime;
    @Basic
    @Column(name = "syc_TIME")
    private Date sycTime;
    @Basic
    @Column(name = "syc_state")
    private int sycstate;
    @Basic
    @Column(name = "confirm_state")
    private int confirmState;
    @Basic
    @Column(name = "confirm_time")
    private Date confirmTime;
    @Basic
    @Column(name = "confirm_user")
    private String confirmUser;
    @Basic
    @Column(name = "send_re_msg")
    private String sendReMsg;
    @Basic
    @Column(name = "total_count")
    private Long totalCount;
    @Basic
    @Column(name = "profit_rate")
    private String profitRate;
    @Basic
    @Column(name = "fai_remark")
    private String faiRemark;

    public String getFaiRemark() {
        return faiRemark;
    }

    public void setFaiRemark(String faiRemark) {
        this.faiRemark = faiRemark;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setAccount_type(Integer account_type) {
        this.account_type = account_type;
    }

    public void setAccountCost(BigDecimal accountCost) {
        this.accountCost = accountCost;
    }

    public void setAccountProfit(BigDecimal accountProfit) {
        this.accountProfit = accountProfit;
    }

    public void setStartBanlance(BigDecimal startBanlance) {
        this.startBanlance = startBanlance;
    }

    public void setEndBanlance(BigDecimal endBanlance) {
        this.endBanlance = endBanlance;
    }

    public void setFailMoney(BigDecimal failMoney) {
        this.failMoney = failMoney;
    }

    public void setConfirmReMsg(String confirmReMsg) {
        this.confirmReMsg = confirmReMsg;
    }

    public void setAccountTime(String accountTime) {
        this.accountTime = accountTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public void setSycTime(Date sycTime) {
        this.sycTime = sycTime;
    }

    public void setSycstate(int sycstate) {
        this.sycstate = sycstate;
    }

    public void setConfirmState(int confirmState) {
        this.confirmState = confirmState;
    }

    public void setConfirmTime(Date confirmTime) {
        this.confirmTime = confirmTime;
    }

    public void setConfirmUser(String confirmUser) {
        this.confirmUser = confirmUser;
    }

    public void setSendReMsg(String sendReMsg) {
        this.sendReMsg = sendReMsg;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public void setProfitRate(String profitRate) {
        this.profitRate = profitRate;
    }








    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Integer getAccount_type() {
        return account_type;
    }

    public BigDecimal getAccountCost() {
        return accountCost;
    }

    public BigDecimal getAccountProfit() {
        return accountProfit;
    }

    public BigDecimal getStartBanlance() {
        return startBanlance;
    }

    public BigDecimal getEndBanlance() {
        return endBanlance;
    }

    public BigDecimal getFailMoney() {
        return failMoney;
    }

    public String getConfirmReMsg() {
        return confirmReMsg;
    }

    public String getAccountTime() {
        return accountTime;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public Date getSycTime() {
        return sycTime;
    }

    public int getSycstate() {
        return sycstate;
    }

    public int getConfirmState() {
        return confirmState;
    }

    public Date getConfirmTime() {
        return confirmTime;
    }

    public String getConfirmUser() {
        return confirmUser;
    }

    public String getSendReMsg() {
        return sendReMsg;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public String getProfitRate() {
        return profitRate;
    }
}
