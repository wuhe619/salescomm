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
public class MarketTaskUserRelPK implements Serializable {
    private String marketTaskId;
    private String userId;

    @Column(name = "market_task_id")
    @Id
    public String getMarketTaskId() {
        return marketTaskId;
    }

    public void setMarketTaskId(String marketTaskId) {
        this.marketTaskId = marketTaskId;
    }

    @Column(name = "user_id")
    @Id
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketTaskUserRelPK that = (MarketTaskUserRelPK) o;
        return marketTaskId == that.marketTaskId &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(marketTaskId, userId);
    }
}
