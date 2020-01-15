package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_oa_log_relation", schema = "", catalog = "")
public class LkCrmOaLogRelationEntity {
    private int rId;
    private Integer logId;
    private String customerIds;
    private String contactsIds;
    private String businessIds;
    private String contractIds;
    private Integer status;
    private Timestamp createTime;

    @Id
    @Column(name = "r_id")
    public int getrId() {
        return rId;
    }

    public void setrId(int rId) {
        this.rId = rId;
    }

    @Basic
    @Column(name = "log_id")
    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    @Basic
    @Column(name = "customer_ids")
    public String getCustomerIds() {
        return customerIds;
    }

    public void setCustomerIds(String customerIds) {
        this.customerIds = customerIds;
    }

    @Basic
    @Column(name = "contacts_ids")
    public String getContactsIds() {
        return contactsIds;
    }

    public void setContactsIds(String contactsIds) {
        this.contactsIds = contactsIds;
    }

    @Basic
    @Column(name = "business_ids")
    public String getBusinessIds() {
        return businessIds;
    }

    public void setBusinessIds(String businessIds) {
        this.businessIds = businessIds;
    }

    @Basic
    @Column(name = "contract_ids")
    public String getContractIds() {
        return contractIds;
    }

    public void setContractIds(String contractIds) {
        this.contractIds = contractIds;
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
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmOaLogRelationEntity that = (LkCrmOaLogRelationEntity) o;
        return rId == that.rId &&
                Objects.equals(logId, that.logId) &&
                Objects.equals(customerIds, that.customerIds) &&
                Objects.equals(contactsIds, that.contactsIds) &&
                Objects.equals(businessIds, that.businessIds) &&
                Objects.equals(contractIds, that.contractIds) &&
                Objects.equals(status, that.status) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rId, logId, customerIds, contactsIds, businessIds, contractIds, status, createTime);
    }
}
