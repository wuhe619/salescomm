package com.bdaim.crm.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "lkcrm_crm_business", schema = "", catalog = "")
public class LkCrmBusinessEntity {
    private Integer businessId;
    private String custId;
    private Integer typeId;
    private Integer statusId;
    private Timestamp nextTime;
    private Integer customerId;
    private Timestamp dealDate;
    private String businessName;
    private BigDecimal money;
    private BigDecimal discountRate;
    private BigDecimal totalPrice;
    private String remark;
    private Long createUserId;
    private Long ownerUserId;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String batchId;
    private String roUserId;
    private String rwUserId;
    private int isEnd;
    private String statusRemark;

    //移出方式（1.移除2.转为团队成员）
    private Integer transferType;
    //权限（1.只读2.读写）
    private Integer power;
    private String ids;
    private Long newOwnerUserId;
    private String memberIds;
    private Integer orderNum;
    private String businessIds;

    @Id
    @Column(name = "business_id")
    @GeneratedValue
    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
        this.businessId = businessId;
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
    @Column(name = "type_id")
    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    @Basic
    @Column(name = "status_id")
    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    @Basic
    @Column(name = "next_time")
    public Timestamp getNextTime() {
        return nextTime;
    }

    public void setNextTime(Timestamp nextTime) {
        this.nextTime = nextTime;
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
    @Column(name = "deal_date")
    public Timestamp getDealDate() {
        return dealDate;
    }

    public void setDealDate(Timestamp dealDate) {
        this.dealDate = dealDate;
    }

    @Basic
    @Column(name = "business_name")
    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
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
    @Column(name = "is_end")
    public int getIsEnd() {
        return isEnd;
    }

    public void setIsEnd(int isEnd) {
        this.isEnd = isEnd;
    }

    @Basic
    @Column(name = "status_remark")
    public String getStatusRemark() {
        return statusRemark;
    }

    public void setStatusRemark(String statusRemark) {
        this.statusRemark = statusRemark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmBusinessEntity that = (LkCrmBusinessEntity) o;
        return businessId == that.businessId &&
                customerId == that.customerId &&
                createUserId == that.createUserId &&
                isEnd == that.isEnd &&
                Objects.equals(typeId, that.typeId) &&
                Objects.equals(statusId, that.statusId) &&
                Objects.equals(nextTime, that.nextTime) &&
                Objects.equals(dealDate, that.dealDate) &&
                Objects.equals(businessName, that.businessName) &&
                Objects.equals(money, that.money) &&
                Objects.equals(discountRate, that.discountRate) &&
                Objects.equals(totalPrice, that.totalPrice) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(ownerUserId, that.ownerUserId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(batchId, that.batchId) &&
                Objects.equals(roUserId, that.roUserId) &&
                Objects.equals(rwUserId, that.rwUserId) &&
                Objects.equals(statusRemark, that.statusRemark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessId, typeId, statusId, nextTime, customerId, dealDate, businessName, money, discountRate, totalPrice, remark, createUserId, ownerUserId, createTime, updateTime, batchId, roUserId, rwUserId, isEnd, statusRemark);
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
    public Long getNewOwnerUserId() {
        return newOwnerUserId;
    }

    public void setNewOwnerUserId(Long newOwnerUserId) {
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
    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
    @Transient
    public String getBusinessIds() {
        return businessIds;
    }

    public void setBusinessIds(String businessIds) {
        this.businessIds = businessIds;
    }
}
