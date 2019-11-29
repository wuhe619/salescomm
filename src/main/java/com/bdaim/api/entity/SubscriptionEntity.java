package com.bdaim.api.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

@Entity
@Table(name = "am_subscription")
public class SubscriptionEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "SUBSCRIPTION_ID")
    private int id;

    @Column(name = "TIER_ID")
    private String tierId;

    @Column(name = "API_ID")
    private int apiId;

    @Column(name = "LAST_ACCESSED")
    private Date lastAccessed;

    @Column(name = "APPLICATION_ID")
    private int applicationId;

    @Column(name = "SUB_STATUS")
    private String subStatus;

    @Column(name = "SUBS_CREATE_STATE")
    private String subsCreateState;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_TIME")
    private Date createdTime;


    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @Column(name = "UPDATED_TIME")
    private Date updatedTime;


    @Column(name = "UUID")
    private String UUID;

    @Column(name = "CHARGE_MODE")
    private int chargeMode;


    @Column(name = "ACCUMULATION")
    private long accumulation;

    @Column(name = "LAST_CHARGE_TIME")
    private Date lastChargeTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTierId() {
        return tierId;
    }

    public void setTierId(String tierId) {
        this.tierId = tierId;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public Date getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(Date lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(String subStatus) {
        this.subStatus = subStatus;
    }

    public String getSubsCreateState() {
        return subsCreateState;
    }

    public void setSubsCreateState(String subsCreateState) {
        this.subsCreateState = subsCreateState;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public int getChargeMode() {
        return chargeMode;
    }

    public void setChargeMode(int chargeMode) {
        this.chargeMode = chargeMode;
    }

    public long getAccumulation() {
        return accumulation;
    }

    public void setAccumulation(long accumulation) {
        this.accumulation = accumulation;
    }

    public Date getLastChargeTime() {
        return lastChargeTime;
    }

    public void setLastChargeTime(Date lastChargeTime) {
        this.lastChargeTime = lastChargeTime;
    }
}
