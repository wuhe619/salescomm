package com.bdaim.account.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Mr.YinXin on 2017/2/21.
 */
@Entity
@Table(name = "t_account", schema = "", catalog = "")
public class AccountDO {
    private String acctId;
    private String custId;
    private Integer payType;
    private String payPassword;
    private Integer usedAmount;
    private Integer remainAmount;
    private Integer creditLimit;
    private Timestamp createTime;
    private Timestamp modifyTime;
    private Timestamp activeTime;
    private Integer status;
    private Integer pwdStatus;

    @Id
    @Column(name = "acct_id")
    public String getAcctId() {
        return acctId;
    }

    public void setAcctId(String acctId) {
        this.acctId = acctId;
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
    @Column(name = "pay_type")
    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    @Basic
    @Column(name = "pay_password")
    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }

    @Basic
    @Column(name = "used_amount")
    public Integer getUsedAmount() {
        return usedAmount;
    }

    public void setUsedAmount(Integer usedAmount) {
        this.usedAmount = usedAmount;
    }

    @Basic
    @Column(name = "remain_amount")
    public Integer getRemainAmount() {
        return remainAmount;
    }

    public void setRemainAmount(Integer remainAmount) {
        this.remainAmount = remainAmount;
    }

    @Basic
    @Column(name = "credit_limit")
    public Integer getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(Integer creditLimit) {
        this.creditLimit = creditLimit;
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
    @Column(name = "modify_time")
    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Basic
    @Column(name = "active_time")
    public Timestamp getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(Timestamp activeTime) {
        this.activeTime = activeTime;
    }

    @Basic
    @Column(name = "STATUS")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "pwd_status")
    public Integer getPwdStatus() {
        return pwdStatus;
    }

    public void setPwdStatus(Integer pwdStatus) {
        this.pwdStatus = pwdStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountDO accountDO = (AccountDO) o;

        if (acctId != null ? !acctId.equals(accountDO.acctId) : accountDO.acctId != null) return false;
        if (custId != null ? !custId.equals(accountDO.custId) : accountDO.custId != null) return false;
        if (payType != null ? !payType.equals(accountDO.payType) : accountDO.payType != null) return false;
        if (payPassword != null ? !payPassword.equals(accountDO.payPassword) : accountDO.payPassword != null)
            return false;
        if (usedAmount != null ? !usedAmount.equals(accountDO.usedAmount) : accountDO.usedAmount != null) return false;
        if (remainAmount != null ? !remainAmount.equals(accountDO.remainAmount) : accountDO.remainAmount != null)
            return false;
        if (creditLimit != null ? !creditLimit.equals(accountDO.creditLimit) : accountDO.creditLimit != null)
            return false;
        if (createTime != null ? !createTime.equals(accountDO.createTime) : accountDO.createTime != null) return false;
        if (modifyTime != null ? !modifyTime.equals(accountDO.modifyTime) : accountDO.modifyTime != null) return false;
        if (activeTime != null ? !activeTime.equals(accountDO.activeTime) : accountDO.activeTime != null) return false;
        if (status != null ? !status.equals(accountDO.status) : accountDO.status != null) return false;
        if (pwdStatus != null ? !pwdStatus.equals(accountDO.pwdStatus) : accountDO.pwdStatus != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = acctId != null ? acctId.hashCode() : 0;
        result = 31 * result + (custId != null ? custId.hashCode() : 0);
        result = 31 * result + (payType != null ? payType.hashCode() : 0);
        result = 31 * result + (payPassword != null ? payPassword.hashCode() : 0);
        result = 31 * result + (usedAmount != null ? usedAmount.hashCode() : 0);
        result = 31 * result + (remainAmount != null ? remainAmount.hashCode() : 0);
        result = 31 * result + (creditLimit != null ? creditLimit.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (modifyTime != null ? modifyTime.hashCode() : 0);
        result = 31 * result + (activeTime != null ? activeTime.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (pwdStatus != null ? pwdStatus.hashCode() : 0);
        return result;
    }
}
