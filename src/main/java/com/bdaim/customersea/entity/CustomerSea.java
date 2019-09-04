package com.bdaim.customersea.entity;

import com.bdaim.customersea.dto.CustomerSeaParam;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * 公海基础表
 *
 * @author chengning@salescomm.net
 * @date 2019/6/19
 * @description
 */
@Entity
@Table(name = "t_customer_sea", schema = "", catalog = "")
public class CustomerSea {
    private long id;
    private Integer marketProjectId;
    private String custId;
    private String name;
    private String description;
    private Integer status;
    private Long createUid;
    private Timestamp createTime;
    private Long updateUid;
    private Timestamp updateTime;
    private String remark;
    private String quantity;
    private String taskId;
    private Integer taskPhoneIndex;
    private Integer taskType;
    private Timestamp taskEndTime;
    private Timestamp taskCreateTime;
    private Integer taskSmsIndex;
    private String smsTemplateId;

    public CustomerSea() {
    }

    public CustomerSea(CustomerSeaParam param) {
        this.setMarketProjectId(param.getMarketProjectId());
        this.setCustId(param.getCustId());
        this.setName(param.getName());
        this.setDescription(param.getDescription());
        this.setRemark(param.getRemark());
        this.setTaskId(param.getTaskId());
        this.setTaskType(param.getTaskType());
        if (param.getTaskEndTime() != null) {
            this.setTaskEndTime(new Timestamp(param.getTaskEndTime()));
        }
        if (param.getTaskCreateTime() != null) {
            this.setTaskCreateTime(new Timestamp(param.getTaskCreateTime()));
        }
        this.setSmsTemplateId(param.getSmsTemplateId());
        this.setCreateUid(param.getCreateUid());
        this.setUpdateUid(param.getUpdateUid());
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "market_project_id")
    public Integer getMarketProjectId() {
        return marketProjectId;
    }

    public void setMarketProjectId(Integer marketProjectId) {
        this.marketProjectId = marketProjectId;
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
        CustomerSea customerSea = (CustomerSea) o;
        return Objects.equals(id, customerSea.id) &&
                Objects.equals(marketProjectId, customerSea.marketProjectId) &&
                Objects.equals(custId, customerSea.custId) &&
                Objects.equals(name, customerSea.name) &&
                Objects.equals(description, customerSea.description) &&
                Objects.equals(status, customerSea.status) &&
                Objects.equals(createUid, customerSea.createUid) &&
                Objects.equals(createTime, customerSea.createTime) &&
                Objects.equals(updateUid, customerSea.updateUid) &&
                Objects.equals(updateTime, customerSea.updateTime) &&
                Objects.equals(remark, customerSea.remark) &&
                Objects.equals(quantity, customerSea.quantity) &&
                Objects.equals(taskId, customerSea.taskId) &&
                Objects.equals(taskPhoneIndex, customerSea.taskPhoneIndex) &&
                Objects.equals(taskType, customerSea.taskType) &&
                Objects.equals(taskEndTime, customerSea.taskEndTime) &&
                Objects.equals(taskCreateTime, customerSea.taskCreateTime) &&
                Objects.equals(taskSmsIndex, customerSea.taskSmsIndex) &&
                Objects.equals(smsTemplateId, customerSea.smsTemplateId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, marketProjectId, custId, name, description, status, createUid, createTime, updateUid, updateTime, remark, quantity, taskId, taskPhoneIndex, taskType, taskEndTime, taskCreateTime, taskSmsIndex, smsTemplateId);
    }


    @Override
    public String toString() {
        return "CustomerSea{" +
                "id='" + id + '\'' +
                ", marketProjectId=" + marketProjectId +
                ", custId='" + custId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", createUid=" + createUid +
                ", createTime=" + createTime +
                ", updateUid=" + updateUid +
                ", updateTime=" + updateTime +
                ", remark='" + remark + '\'' +
                ", quantity='" + quantity + '\'' +
                ", taskId='" + taskId + '\'' +
                ", taskPhoneIndex=" + taskPhoneIndex +
                ", taskType=" + taskType +
                ", taskEndTime=" + taskEndTime +
                ", taskCreateTime=" + taskCreateTime +
                ", taskSmsIndex=" + taskSmsIndex +
                ", smsTemplateId='" + smsTemplateId + '\'' +
                '}';
    }
}
