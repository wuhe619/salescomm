package com.bdaim.api.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="customer_api_resouse_precent")
public class CustomerApiResourcePrecent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;
    @Column(name = "customer_id")
    private int customerId;
    @Column(name = "api_id")
    private int apiId;

    @Column(name = "resounse_id")
    private int resounseId;
    @Column(name = "CREATED_BY")
    private int createdBy;
    @Column(name = "UPDATED_BY")
    private int updateBy;
    @Column(name = "CREATED_TIME")
    private Date createdTime;
    @Column(name = "UPDATED_TIME")
    private Date updateTime;
    @Column(name = "begin_percent")
    private String  beginPercent;
    @Column(name = "end_percent")
    private String  endPercent;
    @Column(name = "sub_id")
    private int subId;

    @Column(name = "percent")
    private String percent;

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public int getSubId() {
        return subId;
    }

    public void setSubId(int subId) {
        this.subId = subId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public int getResounseId() {
        return resounseId;
    }

    public void setResounseId(int resounseId) {
        this.resounseId = resounseId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(int updateBy) {
        this.updateBy = updateBy;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getBeginPercent() {
        return beginPercent;
    }

    public void setBeginPercent(String beginPercent) {
        this.beginPercent = beginPercent;
    }

    public String getEndPercent() {
        return endPercent;
    }

    public void setEndPercent(String endPercent) {
        this.endPercent = endPercent;
    }
}
