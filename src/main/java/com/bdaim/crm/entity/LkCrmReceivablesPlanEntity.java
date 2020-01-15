package com.bdaim.crm.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_crm_receivables_plan", schema = "", catalog = "")
public class LkCrmReceivablesPlanEntity {
    private int planId;
    private String num;
    private Integer receivablesId;
    private Integer status;
    private BigDecimal money;
    private Timestamp returnDate;
    private String returnType;
    private Integer remind;
    private Timestamp remindDate;
    private String remark;
    private int createUserId;
    private Integer ownerUserId;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String fileBatch;
    private int contractId;
    private int customerId;

    @Id
    @Column(name = "plan_id")
    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    @Basic
    @Column(name = "num")
    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    @Basic
    @Column(name = "receivables_id")
    public Integer getReceivablesId() {
        return receivablesId;
    }

    public void setReceivablesId(Integer receivablesId) {
        this.receivablesId = receivablesId;
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
    @Column(name = "money")
    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    @Basic
    @Column(name = "return_date")
    public Timestamp getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Timestamp returnDate) {
        this.returnDate = returnDate;
    }

    @Basic
    @Column(name = "return_type")
    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    @Basic
    @Column(name = "remind")
    public Integer getRemind() {
        return remind;
    }

    public void setRemind(Integer remind) {
        this.remind = remind;
    }

    @Basic
    @Column(name = "remind_date")
    public Timestamp getRemindDate() {
        return remindDate;
    }

    public void setRemindDate(Timestamp remindDate) {
        this.remindDate = remindDate;
    }

    @Basic
    @Column(name = "remark")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Basic
    @Column(name = "create_user_id")
    public int getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(int createUserId) {
        this.createUserId = createUserId;
    }

    @Basic
    @Column(name = "owner_user_id")
    public Integer getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Integer ownerUserId) {
        this.ownerUserId = ownerUserId;
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
    @Column(name = "update_time")
    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Basic
    @Column(name = "file_batch")
    public String getFileBatch() {
        return fileBatch;
    }

    public void setFileBatch(String fileBatch) {
        this.fileBatch = fileBatch;
    }

    @Basic
    @Column(name = "contract_id")
    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    @Basic
    @Column(name = "customer_id")
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmReceivablesPlanEntity that = (LkCrmReceivablesPlanEntity) o;
        return planId == that.planId &&
                createUserId == that.createUserId &&
                contractId == that.contractId &&
                customerId == that.customerId &&
                Objects.equals(num, that.num) &&
                Objects.equals(receivablesId, that.receivablesId) &&
                Objects.equals(status, that.status) &&
                Objects.equals(money, that.money) &&
                Objects.equals(returnDate, that.returnDate) &&
                Objects.equals(returnType, that.returnType) &&
                Objects.equals(remind, that.remind) &&
                Objects.equals(remindDate, that.remindDate) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(ownerUserId, that.ownerUserId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(fileBatch, that.fileBatch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, num, receivablesId, status, money, returnDate, returnType, remind, remindDate, remark, createUserId, ownerUserId, createTime, updateTime, fileBatch, contractId, customerId);
    }
}
