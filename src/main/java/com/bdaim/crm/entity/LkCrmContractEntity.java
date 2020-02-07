package com.bdaim.crm.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_crm_contract", schema = "", catalog = "")
public class LkCrmContractEntity {
    private int contractId;
    private String name;
    private Integer customerId;
    private Integer businessId;
    private Integer checkStatus;
    private Integer examineRecordId;
    private Timestamp orderDate;
    private int createUserId;
    private Integer ownerUserId;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String num;
    private Timestamp startTime;
    private Timestamp endTime;
    private BigDecimal money;
    private BigDecimal discountRate;
    private BigDecimal totalPrice;
    private String types;
    private String paymentType;
    private String batchId;
    private String roUserId;
    private String rwUserId;
    private Integer contactsId;
    private String remark;
    private Integer companyUserId;

    //移出方式（1.移除2.转为团队成员）
    private Integer transferType;
    //权限（1.只读2.只写）
    private Integer power;
    //变更模块（1.联系人2.商机3.合同）
    private String ids;
    private Integer newOwnerUserId;
    private String memberIds;
    private String contractIds;

    @Id
    @Column(name = "contract_id")
    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    @Column(name = "business_id")
    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
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
    @Column(name = "order_date")
    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
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
    @Column(name = "num")
    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    @Basic
    @Column(name = "start_time")
    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "end_time")
    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
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
    @Column(name = "discount_rate")
    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    @Basic
    @Column(name = "total_price")
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Basic
    @Column(name = "types")
    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    @Basic
    @Column(name = "payment_type")
    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    @Basic
    @Column(name = "batch_id")
    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    @Basic
    @Column(name = "ro_user_id")
    public String getRoUserId() {
        return roUserId;
    }

    public void setRoUserId(String roUserId) {
        this.roUserId = roUserId;
    }

    @Basic
    @Column(name = "rw_user_id")
    public String getRwUserId() {
        return rwUserId;
    }

    public void setRwUserId(String rwUserId) {
        this.rwUserId = rwUserId;
    }

    @Basic
    @Column(name = "contacts_id")
    public Integer getContactsId() {
        return contactsId;
    }

    public void setContactsId(Integer contactsId) {
        this.contactsId = contactsId;
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
    @Column(name = "company_user_id")
    public Integer getCompanyUserId() {
        return companyUserId;
    }

    public void setCompanyUserId(Integer companyUserId) {
        this.companyUserId = companyUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmContractEntity that = (LkCrmContractEntity) o;
        return contractId == that.contractId &&
                createUserId == that.createUserId &&
                Objects.equals(name, that.name) &&
                Objects.equals(customerId, that.customerId) &&
                Objects.equals(businessId, that.businessId) &&
                Objects.equals(checkStatus, that.checkStatus) &&
                Objects.equals(examineRecordId, that.examineRecordId) &&
                Objects.equals(orderDate, that.orderDate) &&
                Objects.equals(ownerUserId, that.ownerUserId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(num, that.num) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(money, that.money) &&
                Objects.equals(discountRate, that.discountRate) &&
                Objects.equals(totalPrice, that.totalPrice) &&
                Objects.equals(types, that.types) &&
                Objects.equals(paymentType, that.paymentType) &&
                Objects.equals(batchId, that.batchId) &&
                Objects.equals(roUserId, that.roUserId) &&
                Objects.equals(rwUserId, that.rwUserId) &&
                Objects.equals(contactsId, that.contactsId) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(companyUserId, that.companyUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractId, name, customerId, businessId, checkStatus, examineRecordId, orderDate, createUserId, ownerUserId, createTime, updateTime, num, startTime, endTime, money, discountRate, totalPrice, types, paymentType, batchId, roUserId, rwUserId, contactsId, remark, companyUserId);
    }

    @Transient
    public Integer getTransferType() {
        return transferType;
    }

    public void setTransferType(Integer transferType) {
        this.transferType = transferType;
    }
    @Transient
    public Integer getPower() {
        return power;
    }

    public void setPower(Integer power) {
        this.power = power;
    }
    @Transient
    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }
    @Transient
    public Integer getNewOwnerUserId() {
        return newOwnerUserId;
    }

    public void setNewOwnerUserId(Integer newOwnerUserId) {
        this.newOwnerUserId = newOwnerUserId;
    }
    @Transient
    public String getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(String memberIds) {
        this.memberIds = memberIds;
    }
    @Transient
    public String getContractIds() {
        return contractIds;
    }

    public void setContractIds(String contractIds) {
        this.contractIds = contractIds;
    }
}
