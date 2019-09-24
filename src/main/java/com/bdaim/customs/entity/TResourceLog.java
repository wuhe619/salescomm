package com.bdaim.customs.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/22
 * @description
 */
@Entity
@Table(name = "t_resource_log", schema = "", catalog = "")
public class TResourceLog {
    private long id;
    private Integer resourceId;
    private String supplierId;
    private String custId;
    private Timestamp createTime;
    private Integer prodAmount;
    private Integer amount;
    private Integer busiType;
    private String batchId;
    private String busiId;
    private Long custUserId;
    private String content;

    @Id
    @Column(name = "id")
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "resource_id")
    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    @Basic
    @Column(name = "supplier_id")
    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
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
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "prod_amount")
    public Integer getProdAmount() {
        return prodAmount;
    }

    public void setProdAmount(Integer prodAmount) {
        this.prodAmount = prodAmount;
    }

    @Basic
    @Column(name = "amount")
    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    @Basic
    @Column(name = "busi_type")
    public Integer getBusiType() {
        return busiType;
    }

    public void setBusiType(Integer busiType) {
        this.busiType = busiType;
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
    @Column(name = "busi_id")
    public String getBusiId() {
        return busiId;
    }

    public void setBusiId(String busiId) {
        this.busiId = busiId;
    }

    @Basic
    @Column(name = "cust_user_id")
    public Long getCustUserId() {
        return custUserId;
    }

    public void setCustUserId(Long custUserId) {
        this.custUserId = custUserId;
    }

    @Basic
    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TResourceLog that = (TResourceLog) o;
        return id == that.id &&
                Objects.equals(resourceId, that.resourceId) &&
                Objects.equals(supplierId, that.supplierId) &&
                Objects.equals(custId, that.custId) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(prodAmount, that.prodAmount) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(busiType, that.busiType) &&
                Objects.equals(batchId, that.batchId) &&
                Objects.equals(busiId, that.busiId) &&
                Objects.equals(custUserId, that.custUserId) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, resourceId, supplierId, custId, createTime, prodAmount, amount, busiType, batchId, busiId, custUserId, content);
    }
}
