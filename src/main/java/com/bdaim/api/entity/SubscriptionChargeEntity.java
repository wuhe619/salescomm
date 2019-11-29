package com.bdaim.api.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "am_subcription_charge")
public class SubscriptionChargeEntity implements Serializable {
    @Id
    @Column(name = "SUBSCRIPTION_ID")
    private int id;

    @Column(name = "CHARGE_ID")
    private int chargeId;

    @Column(name = "EFFECTIVE_DATE")
    private Date effectiveDate;
    @Column(name = "EXPIRE_DATE")
    private Date expireDate;
    @Column(name = "START_VOLUME")
    private long startVolume;

    @Column(name = "TIER_VOLUME")
    private long tierVolume;

    @Column(name = "unit_price")
    private int unitPrice;

    @Column(name = "CREATE_TIME")
    private Date createTime;

    @Column(name = "CREATE_BY")
    private String createBy;

    @Column(name = "UPDATE_TIME")
    private Date update_time;

    @Column(name = "UPDATE_BY")
    private String updateBy;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChargeId() {
        return chargeId;
    }

    public void setChargeId(int chargeId) {
        this.chargeId = chargeId;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public long getStartVolume() {
        return startVolume;
    }

    public void setStartVolume(long startVolume) {
        this.startVolume = startVolume;
    }

    public long getTierVolume() {
        return tierVolume;
    }

    public void setTierVolume(long tierVolume) {
        this.tierVolume = tierVolume;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }
}
