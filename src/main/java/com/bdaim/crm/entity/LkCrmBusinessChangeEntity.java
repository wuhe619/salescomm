package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_crm_business_change", schema = "", catalog = "")
public class LkCrmBusinessChangeEntity {
    private int changeId;
    private int businessId;
    private int statusId;
    private Timestamp createTime;
    private int createUserId;

    @Id
    @Column(name = "change_id")
    public int getChangeId() {
        return changeId;
    }

    public void setChangeId(int changeId) {
        this.changeId = changeId;
    }

    @Basic
    @Column(name = "business_id")
    public int getBusinessId() {
        return businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    @Basic
    @Column(name = "status_id")
    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    @Basic
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "create_user_id")
    public int getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(int createUserId) {
        this.createUserId = createUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmBusinessChangeEntity that = (LkCrmBusinessChangeEntity) o;
        return changeId == that.changeId &&
                businessId == that.businessId &&
                statusId == that.statusId &&
                createUserId == that.createUserId &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(changeId, businessId, statusId, createTime, createUserId);
    }
}
