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
    private String customerId;
    @Column(name = "api_id")
    private String apiId;

    @Column(name = "resounse_id")
    private String resounseId;
    @Column(name = "CREATED_BY")
    private String createdBy;
    @Column(name = "UPDATED_BY")
    private String updateBy;
    @Column(name = "CREATED_TIME")
    private Date createdTime;
    @Column(name = "UPDATED_TIME")
    private Date updateTime;
    @Column(name = "begin_percent")
    private String  beginPercent;
    @Column(name = "end_percent")
    private String  endPercent;
    @Column(name = "sub_id")
    private String subId;

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public void setResounseId(String resounseId) {
        this.resounseId = resounseId;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

    @Column(name = "percent")
    private String percent;

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
