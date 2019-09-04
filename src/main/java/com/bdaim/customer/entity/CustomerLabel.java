package com.bdaim.customer.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2018/10/19
 * @description
 */
/*@Entity
@Table(name = "t_customer_label", schema = "", catalog = "")*/
public class CustomerLabel {
    private int id;
    private String custId;
    private String userId;
    private String labelId;
    private String labelName;
    private Timestamp createTime;
    private String status;
    private String labelDesc;
    private Integer type;
    private String option;
    private Timestamp updateTime;
    private String marketProjectId;
    private Integer sort;
    private Integer required;

    @Id
    @Column(name = "id")
    @GeneratedValue
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
    @Column(name = "user_id")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "label_id")
    public String getLabelId() {
        return labelId;
    }

    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }

    @Basic
    @Column(name = "label_name")
    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
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
    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Basic
    @Column(name = "label_desc")
    public String getLabelDesc() {
        return labelDesc;
    }

    public void setLabelDesc(String labelDesc) {
        this.labelDesc = labelDesc;
    }

    @Basic
    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Basic
    @Column(name = "`option`")
    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    @Basic
    @Column(name = "`update_time`")
    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Basic
    @Column(name = "market_project_id")
    public String getMarketProjectId() {
        return marketProjectId;
    }

    public void setMarketProjectId(String marketProjectId) {
        this.marketProjectId = marketProjectId;
    }

    @Basic
    @Column(name = "sort")
    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @Basic
    @Column(name = "required")
    public Integer getRequired() {
        return required;
    }

    public void setRequired(Integer required) {
        this.required = required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerLabel that = (CustomerLabel) o;
        return id == that.id &&
                Objects.equals(custId, that.custId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(labelId, that.labelId) &&
                Objects.equals(labelName, that.labelName) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(status, that.status) &&
                Objects.equals(labelDesc, that.labelDesc) &&
                Objects.equals(type, that.type) &&
                Objects.equals(option, that.option);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, custId, userId, labelId, labelName, createTime, status, labelDesc, type, option);
    }

    @Override
    public String toString() {
        return "CustomerLabelDO{" +
                "id=" + id +
                ", custId='" + custId + '\'' +
                ", userId='" + userId + '\'' +
                ", labelId='" + labelId + '\'' +
                ", labelName='" + labelName + '\'' +
                ", createTime=" + createTime +
                ", status='" + status + '\'' +
                ", labelDesc='" + labelDesc + '\'' +
                ", type=" + type +
                ", option='" + option + '\'' +
                ", updateTime=" + updateTime +
                ", marketProjectId='" + marketProjectId + '\'' +
                '}';
    }
}
