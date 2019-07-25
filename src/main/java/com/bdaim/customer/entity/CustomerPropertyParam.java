package com.bdaim.customer.entity;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/7
 * @description
 */
public class CustomerPropertyParam {
    private Long id;
    private String customerId;
    private String userId;
    private String labelId;
    private String labelName;
    private Timestamp createTime;
    private String status;
    private String labelDesc;
    private Integer type;
    private String option;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLabelId() {
        return labelId;
    }

    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public String getLabelDesc() {
        return labelDesc;
    }

    public void setLabelDesc(String labelDesc) {
        this.labelDesc = labelDesc;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    @Override
    public String toString() {
        return "CustomerPropertyParam{" +
                "id=" + id +
                ", customerId='" + customerId + '\'' +
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


