package com.bdaim.fund.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 字典属性
 */
@Entity
@Table(name = "t_settlement_property", schema = "", catalog = "")
@IdClass(SettlementPropertyPK.class)
public class SettlementProperty implements Serializable {


    private Long settlementId;
    private String propertyName;

    private String propertyValue;
    private Date createTime;

    public SettlementProperty() {
    }

    public SettlementProperty(Long settlementId, String propertyName, String propertyValue, Date createTime) {
        this.settlementId = settlementId;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.createTime = createTime;
    }

    @Id
    @Column(name = "settlement_id")

    public Long getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(Long settlementId) {
        this.settlementId = settlementId;
    }

    @Id
    @Column(name = "property_name")
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Column(name = "property_value")
    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    @Column(name = "create_time")

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "SettlementProperty{" +
                "settlementId=" + settlementId +
                ", propertyName='" + propertyName + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
