package com.bdaim.batch.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by yanls on 2018/9/6.
 */
@Entity
@Table(name = "nl_active_file", schema = "", catalog = "")
public class ActiveFileInfo implements Serializable {
    private String activityName;
    private String huashu;
    private String activityBegindate;
    private String activityEnddate;
    private String activityId;
    private String customerId;
    private String provodeId;
    private String custId;
    private String batchId;



    @Basic
    @Column(name = "huashu")
    public String getHuashu() {
        return huashu;
    }

    public void setHuashu(String huashu) {
        this.huashu = huashu;
    }

    @Basic
    @Column(name = "activity_name")
    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    @Basic
    @Column(name = "activity_begindate")
    public String getActivityBegindate() {
        return activityBegindate;
    }

    public void setActivityBegindate(String activityBegindate) {
        this.activityBegindate = activityBegindate;
    }

    @Basic
    @Column(name = "activity_enddate")
    public String getActivityEnddate() {
        return activityEnddate;
    }

    public void setActivityEnddate(String activityEnddate) {
        this.activityEnddate = activityEnddate;
    }

    @Id
    @Column(name = "activity_id")
    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    @Basic
    @Column(name = "customer_id")
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @Basic
    @Column(name = "provode_id")
    public String getProvodeId() {
        return provodeId;
    }

    public void setProvodeId(String provodeId) {
        this.provodeId = provodeId;
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
    @Column(name = "batch_id")
    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

}
