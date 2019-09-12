package com.bdaim.fund.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 *
 */
public class SettlementPropertyPK implements Serializable {
    private Long settlementId;
    private String propertyName;

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
}
