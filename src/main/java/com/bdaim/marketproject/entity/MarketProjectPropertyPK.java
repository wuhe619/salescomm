package com.bdaim.marketproject.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2019/6/26
 * @description
 */
public class MarketProjectPropertyPK implements Serializable {
    private String marketProjectId;
    private String propertyName;

    @Column(name = "market_project_id")
    @Id
    public String getMarketProjectId() {
        return marketProjectId;
    }

    public void setMarketProjectId(String marketProjectId) {
        this.marketProjectId = marketProjectId;
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
        MarketProjectPropertyPK that = (MarketProjectPropertyPK) o;
        return Objects.equals(marketProjectId, that.marketProjectId) &&
                Objects.equals(propertyName, that.propertyName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(marketProjectId, propertyName);
    }
}
