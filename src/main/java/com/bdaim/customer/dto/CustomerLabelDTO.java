package com.bdaim.customer.dto;

import com.bdaim.customer.entity.CustomerLabelDO;

import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2018/11/16
 * @description
 */
public class CustomerLabelDTO {
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
    private String marketProjectId;

    public CustomerLabelDTO(CustomerLabelDO customerLabel) {
        this.id = customerLabel.getId();
        this.custId = customerLabel.getCustId();
        this.userId = customerLabel.getCustId();
        this.labelId = customerLabel.getLabelId();
        this.labelName = customerLabel.getLabelName();
        this.createTime = customerLabel.getCreateTime();
        this.status = customerLabel.getStatus();
        this.labelDesc = customerLabel.getLabelDesc();
        this.type = customerLabel.getType();
        this.option = customerLabel.getOption();
        this.marketProjectId = customerLabel.getMarketProjectId();
    }

    public CustomerLabelDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
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

    public String getLabelDesc() {
        return labelDesc;
    }

    public void setLabelDesc(String labelDesc) {
        this.labelDesc = labelDesc;
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

    public String getMarketProjectId() {
        return marketProjectId;
    }

    public void setMarketProjectId(String marketProjectId) {
        this.marketProjectId = marketProjectId;
    }

    @Override
    public String toString() {
        return "CustomerLabelDTO{" +
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
                ", marketProjectId='" + marketProjectId + '\'' +
                '}';
    }
}
