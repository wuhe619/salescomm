package com.bdaim.markettask.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2019/4/24
 * @description
 */
@Entity
@Table(name = "t_market_task_property", schema = "", catalog = "")
@IdClass(MarketTaskPropertyPK.class)
public class MarketTaskProperty {
    private String marketTaskId;
    private String propertyName;
    private String propertyValue;
    private Timestamp createTime;

    public MarketTaskProperty() {
    }

    public MarketTaskProperty(String marketTaskId, String propertyName, String propertyValue, Timestamp createTime) {
        this.marketTaskId = marketTaskId;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.createTime = createTime;
    }

    public MarketTaskProperty(String marketTaskId, String propertyName, String propertyValue) {
        this.marketTaskId = marketTaskId;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    @Id
    @Column(name = "market_task_id")
    public String getMarketTaskId() {
        return marketTaskId;
    }

    public void setMarketTaskId(String marketTaskId) {
        this.marketTaskId = marketTaskId;
    }

    @Id
    @Column(name = "property_name")
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Basic
    @Column(name = "property_value")
    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    @Basic
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketTaskProperty that = (MarketTaskProperty) o;
        return marketTaskId == that.marketTaskId &&
                Objects.equals(propertyName, that.propertyName) &&
                Objects.equals(propertyValue, that.propertyValue) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(marketTaskId, propertyName, propertyValue, createTime);
    }

    @Override
    public String toString() {
        return "MarketTaskProperty{" +
                "marketTaskId='" + marketTaskId + '\'' +
                ", propertyName='" + propertyName + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
