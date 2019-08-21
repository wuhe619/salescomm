package com.bdaim.marketproject.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2019/6/26
 * @description
 */
@Entity
@Table(name = "t_market_project_property", schema = "", catalog = "")
@IdClass(MarketProjectPropertyPK.class)
public class MarketProjectProperty {
    private String marketProjectId;
    private String propertyName;
    private String propertyValue;
    private Timestamp createTime;

    public MarketProjectProperty() {
    }

    public MarketProjectProperty(String marketProjectId, String propertyName, String propertyValue, Timestamp createTime) {
        this.marketProjectId = marketProjectId;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.createTime = createTime;
    }

    @Id
    @Column(name = "market_project_id")
    public String getMarketProjectId() {
        return marketProjectId;
    }

    public void setMarketProjectId(String marketProjectId) {
        this.marketProjectId = marketProjectId;
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
        MarketProjectProperty that = (MarketProjectProperty) o;
        return Objects.equals(marketProjectId, that.marketProjectId) &&
                Objects.equals(propertyName, that.propertyName) &&
                Objects.equals(propertyValue, that.propertyValue) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(marketProjectId, propertyName, propertyValue, createTime);
    }
}
