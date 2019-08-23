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
@Table(name = "t_market_task_user_rel", schema = "", catalog = "")
@IdClass(MarketTaskUserRelPK.class)
public class MarketTaskUserRel {
    private String marketTaskId;
    private String userId;
    private int status;
    private Timestamp createTime;

    public MarketTaskUserRel() {
    }

    public MarketTaskUserRel(String marketTaskId, String userId, int status, Timestamp createTime) {
        this.marketTaskId = marketTaskId;
        this.userId = userId;
        this.status = status;
        this.createTime = createTime;
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
    @Column(name = "user_id")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "status")
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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
        MarketTaskUserRel that = (MarketTaskUserRel) o;
        return marketTaskId == that.marketTaskId &&
                status == that.status &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(marketTaskId, userId, status, createTime);
    }
}
