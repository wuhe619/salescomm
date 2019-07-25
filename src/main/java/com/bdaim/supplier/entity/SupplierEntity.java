package com.bdaim.supplier.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author duanliying
 * @date 2018/12/26
 * @description
 */
@Entity
@Table(name = "t_supplier", schema = "", catalog = "")
public class SupplierEntity implements Serializable {
    private Integer supplierId;
    private String name;
    private Integer type;
    private Integer billCycle;
    private Timestamp createTime;
    private Integer settlementType;
    private String contactPerson;
    private String contactPosition;
    private String contactPhone;
    private Integer status;
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "supplier_id")
    public Integer getSupplierId() {
        return supplierId;
    }
    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }
    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @Basic
    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
    @Basic
    @Column(name = "bill_cycle")
    public Integer getBillCycle() {
        return billCycle;
    }

    public void setBillCycle(Integer billCycle) {
        this.billCycle = billCycle;
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
    @Column(name = "settlement_type")
    public Integer getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(Integer settlementType) {
        this.settlementType = settlementType;
    }
    @Basic
    @Column(name = "contact_person")
    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }
    @Basic
    @Column(name = "contact_position")
    public String getContactPosition() {
        return contactPosition;
    }

    public void setContactPosition(String contactPosition) {
        this.contactPosition = contactPosition;
    }

    @Basic
    @Column(name = "contact_phone")
    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    @Basic
    @Column(name = "status")

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SupplierEntity{" +
                "supplierId=" + supplierId +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", billCycle=" + billCycle +
                ", createTime=" + createTime +
                ", settlementType=" + settlementType +
                ", contactPerson='" + contactPerson + '\'' +
                ", contactPosition='" + contactPosition + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", status=" + status +
                '}';
    }
}
