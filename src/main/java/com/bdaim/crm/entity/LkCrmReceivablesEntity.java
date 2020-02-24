package com.bdaim.crm.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_crm_receivables", schema = "", catalog = "")
public class LkCrmReceivablesEntity {
    private Integer receivablesId;
    private String custId;
    private String number;
    private Integer planId;
    private Integer customerId;
    private Integer contractId;
    private Integer checkStatus;
    private Integer examineRecordId;
    private Date returnTime;
    private String returnType;
    private BigDecimal money;
    private String remark;
    private Long createUserId;
    private Long ownerUserId;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String remarks;
    private String batchId;

    @Id
    @Column(name = "receivables_id")
    @GeneratedValue
    public Integer getReceivablesId() {
        return receivablesId;
    }

    public void setReceivablesId(Integer receivablesId) {
        this.receivablesId = receivablesId;
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
    @Column(name = "number")
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Basic
    @Column(name = "plan_id")
    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    @Basic
    @Column(name = "customer_id")
    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    @Basic
    @Column(name = "contract_id")
    public Integer getContractId() {
        return contractId;
    }

    public void setContractId(Integer contractId) {
        this.contractId = contractId;
    }

    @Basic
    @Column(name = "check_status")
    public Integer getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(Integer checkStatus) {
        this.checkStatus = checkStatus;
    }

    @Basic
    @Column(name = "examine_record_id")
    public Integer getExamineRecordId() {
        return examineRecordId;
    }

    public void setExamineRecordId(Integer examineRecordId) {
        this.examineRecordId = examineRecordId;
    }

    @Basic
    @Column(name = "return_time")
    public Date getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(Date returnTime) {
        this.returnTime = returnTime;
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
    @Column(name = "money")
    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
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
    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    @Basic
    @Column(name = "owner_user_id")
    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
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
    @Column(name = "remarks")
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Basic
    @Column(name = "batch_id")
    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmReceivablesEntity that = (LkCrmReceivablesEntity) o;
        return receivablesId == that.receivablesId &&
                createUserId == that.createUserId &&
                Objects.equals(number, that.number) &&
                Objects.equals(planId, that.planId) &&
                Objects.equals(customerId, that.customerId) &&
                Objects.equals(contractId, that.contractId) &&
                Objects.equals(checkStatus, that.checkStatus) &&
                Objects.equals(examineRecordId, that.examineRecordId) &&
                Objects.equals(returnTime, that.returnTime) &&
                Objects.equals(returnType, that.returnType) &&
                Objects.equals(money, that.money) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(ownerUserId, that.ownerUserId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(remarks, that.remarks) &&
                Objects.equals(batchId, that.batchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receivablesId, number, planId, customerId, contractId, checkStatus, examineRecordId, returnTime, returnType, money, remark, createUserId, ownerUserId, createTime, updateTime, remarks, batchId);
    }
}
