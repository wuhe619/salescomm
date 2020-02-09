package com.bdaim.crm.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_crm_owner_record", schema = "", catalog = "")
public class LkCrmOwnerRecordEntity {
    private int recordId;
    private int typeId;
    private int type;
    private Integer preOwnerUserId;
    private Integer postOwnerUserId;
    private Timestamp createTime;

    @Id
    @Column(name = "record_id")
    @GeneratedValue
    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    @Basic
    @Column(name = "type_id")
    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    @Basic
    @Column(name = "type")
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Basic
    @Column(name = "pre_owner_user_id")
    public Integer getPreOwnerUserId() {
        return preOwnerUserId;
    }

    public void setPreOwnerUserId(Integer preOwnerUserId) {
        this.preOwnerUserId = preOwnerUserId;
    }

    @Basic
    @Column(name = "post_owner_user_id")
    public Integer getPostOwnerUserId() {
        return postOwnerUserId;
    }

    public void setPostOwnerUserId(Integer postOwnerUserId) {
        this.postOwnerUserId = postOwnerUserId;
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
        LkCrmOwnerRecordEntity that = (LkCrmOwnerRecordEntity) o;
        return recordId == that.recordId &&
                typeId == that.typeId &&
                type == that.type &&
                Objects.equals(preOwnerUserId, that.preOwnerUserId) &&
                Objects.equals(postOwnerUserId, that.postOwnerUserId) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId, typeId, type, preOwnerUserId, postOwnerUserId, createTime);
    }
}
