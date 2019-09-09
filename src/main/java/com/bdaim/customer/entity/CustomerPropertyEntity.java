package com.bdaim.customer.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/7
 * @description
 */
@Entity
@Table(name = "t_customer_label", schema = "", catalog = "")
public class CustomerPropertyEntity {
    private Long id;
    private String custId;
    private String userId;
    private String labelId;
    private String labelName;
    private Timestamp createTime;
    private String status;
    private String labelDesc;
    private Integer type;
    private String option;

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

    @Override
    public String toString() {
        return "CustomerPropertyEntity{" +
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
                '}';
    }
}
