package com.bdaim.smscenter.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**短信队列
 * @author chengning@salescomm.net
 * @date 2019/7/4
 * @description
 */
@Entity
@Table(name = "t_touch_sms_queue", schema = "", catalog = "")
public class TouchSmsQueue {
    private int id;
    private Integer templateId;
    private String custId;
    private Integer customerGroupId;
    private String superid;
    private String batchNumber;
    private Timestamp createTime;
    private String resourceId;
    private String userId;
    private String marketTaskId;

    public TouchSmsQueue() {
    }

    public TouchSmsQueue(Integer templateId, String custId, Integer customerGroupId, String superid, String batchNumber, Timestamp createTime, String resourceId, String userId, String marketTaskId) {
        this.templateId = templateId;
        this.custId = custId;
        this.customerGroupId = customerGroupId;
        this.superid = superid;
        this.batchNumber = batchNumber;
        this.createTime = createTime;
        this.resourceId = resourceId;
        this.userId = userId;
        this.marketTaskId = marketTaskId;
    }

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "template_id")
    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
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
    @Column(name = "customer_group_id")
    public Integer getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(Integer customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    @Basic
    @Column(name = "superid")
    public String getSuperid() {
        return superid;
    }

    public void setSuperid(String superid) {
        this.superid = superid;
    }

    @Basic
    @Column(name = "batch_number")
    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
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
    @Column(name = "resource_id")
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    @Basic
    @Column(name = "user_id")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "market_task_id")
    public String getMarketTaskId() {
        return marketTaskId;
    }

    public void setMarketTaskId(String marketTaskId) {
        this.marketTaskId = marketTaskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TouchSmsQueue that = (TouchSmsQueue) o;
        return id == that.id &&
                Objects.equals(templateId, that.templateId) &&
                Objects.equals(custId, that.custId) &&
                Objects.equals(customerGroupId, that.customerGroupId) &&
                Objects.equals(superid, that.superid) &&
                Objects.equals(batchNumber, that.batchNumber) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(resourceId, that.resourceId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(marketTaskId, that.marketTaskId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, templateId, custId, customerGroupId, superid, batchNumber, createTime, resourceId, userId, marketTaskId);
    }
}
