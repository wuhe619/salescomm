package com.bdaim.markettask.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2019/4/24
 * @description
 */
public class MarketTaskPropertyPK implements Serializable {
    private String marketTaskId;
    private String propertyName;

    @Column(name = "market_task_id")
    @Id
    public String getMarketTaskId() {
        return marketTaskId;
    }

    public void setMarketTaskId(String marketTaskId) {
        this.marketTaskId = marketTaskId;
    }

    @Column(name = "property_name")
    @Id
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketTaskPropertyPK that = (MarketTaskPropertyPK) o;
        return marketTaskId == that.marketTaskId &&
                Objects.equals(propertyName, that.propertyName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(marketTaskId, propertyName);
    }
}
