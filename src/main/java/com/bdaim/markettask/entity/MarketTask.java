package com.bdaim.markettask.entity;

import com.bdaim.markettask.dto.MarketTaskParam;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2019/4/23
 * @description
 */
@Entity
@Table(name = "t_market_task", schema = "", catalog = "")
public class MarketTask {
    private String id;
    private Integer customerGroupId;
    private String custId;
    private String name;
    private String description;
    private Integer status;
    private Long createUid;
    private Timestamp createTime;
    private Long updateUid;
    private Timestamp updateTime;
    private String groupCondition;
    private String remark;
    private String quantity;
    private String taskId;
    private Integer taskPhoneIndex;
    /**
     * 1-自动 2-手动 3-机器人外呼 4-短信
     */
    private Integer taskType;
    private Timestamp taskEndTime;
    private Timestamp taskCreateTime;
    private Integer taskSmsIndex;
    private String smsTemplateId;

    public MarketTask(MarketTaskParam m) {
        this.customerGroupId = m.getCustomerGroupId();
        this.custId = m.getCustId();
        this.name = m.getName();
        this.createUid = m.getCreateUid();
        this.groupCondition = m.getGroupCondition();
        this.remark = m.getRemark();
        this.taskId = m.getTaskId();
        this.taskType = m.getTaskType();
        if (m.getTaskEndTime() != null) {
            this.taskEndTime = new Timestamp(m.getTaskEndTime());
        }
        if (m.getTaskCreateTime() != null) {
            this.taskCreateTime = new Timestamp(m.getTaskCreateTime());
        }
        this.smsTemplateId = m.getSmsTemplateId();
    }

    public MarketTask() {
    }

    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
    @Column(name = "cust_id")
    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
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
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
    @Column(name = "create_uid")
    public Long getCreateUid() {
        return createUid;
    }

    public void setCreateUid(Long createUid) {
        this.createUid = createUid;
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
    @Column(name = "update_uid")
    public Long getUpdateUid() {
        return updateUid;
    }

    public void setUpdateUid(Long updateUid) {
        this.updateUid = updateUid;
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
    @Column(name = "group_condition")
    public String getGroupCondition() {
        return groupCondition;
    }

    public void setGroupCondition(String groupCondition) {
        this.groupCondition = groupCondition;
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
    @Column(name = "quantity")
    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    @Basic
    @Column(name = "task_id")
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Basic
    @Column(name = "task_phone_index")
    public Integer getTaskPhoneIndex() {
        return taskPhoneIndex;
    }

    public void setTaskPhoneIndex(Integer taskPhoneIndex) {
        this.taskPhoneIndex = taskPhoneIndex;
    }

    @Basic
    @Column(name = "task_type")
    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    @Basic
    @Column(name = "task_end_time")
    public Timestamp getTaskEndTime() {
        return taskEndTime;
    }

    public void setTaskEndTime(Timestamp taskEndTime) {
        this.taskEndTime = taskEndTime;
    }

    @Basic
    @Column(name = "task_create_time")
    public Timestamp getTaskCreateTime() {
        return taskCreateTime;
    }

    public void setTaskCreateTime(Timestamp taskCreateTime) {
        this.taskCreateTime = taskCreateTime;
    }

    @Basic
    @Column(name = "task_sms_index")
    public Integer getTaskSmsIndex() {
        return taskSmsIndex;
    }

    public void setTaskSmsIndex(Integer taskSmsIndex) {
        this.taskSmsIndex = taskSmsIndex;
    }

    @Basic
    @Column(name = "sms_template_id")
    public String getSmsTemplateId() {
        return smsTemplateId;
    }

    public void setSmsTemplateId(String smsTemplateId) {
        this.smsTemplateId = smsTemplateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketTask that = (MarketTask) o;
        return id == that.id &&
                Objects.equals(customerGroupId, that.customerGroupId) &&
                Objects.equals(custId, that.custId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(status, that.status) &&
                Objects.equals(createUid, that.createUid) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateUid, that.updateUid) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(groupCondition, that.groupCondition) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(taskId, that.taskId) &&
                Objects.equals(taskPhoneIndex, that.taskPhoneIndex) &&
                Objects.equals(taskType, that.taskType) &&
                Objects.equals(taskEndTime, that.taskEndTime) &&
                Objects.equals(taskCreateTime, that.taskCreateTime) &&
                Objects.equals(taskSmsIndex, that.taskSmsIndex) &&
                Objects.equals(smsTemplateId, that.smsTemplateId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, customerGroupId, custId, name, description, status, createUid, createTime, updateUid, updateTime, groupCondition, remark, quantity, taskId, taskPhoneIndex, taskType, taskEndTime, taskCreateTime, taskSmsIndex, smsTemplateId);
    }
}
