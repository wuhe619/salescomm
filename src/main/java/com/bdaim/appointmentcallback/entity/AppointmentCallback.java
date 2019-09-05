package com.bdaim.appointmentcallback.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/** 预约回拨
 * @author chengning@salescomm.net
 * @date 2019/1/30
 * @description
 */
@Entity
@Table(name = "t_appointment_callback")
public class AppointmentCallback {

    private int id;
    private String superid;
    private Integer customerGroupId;
    private String custId;
    private String backupPhone;
    private Timestamp createTime;
    private Timestamp appointmentTime;
    private String operator;
    private String remark;
    private Integer status;
    private String marketTaskId;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "superid")
    public String getSuperid() {
        return superid;
    }

    public void setSuperid(String superid) {
        this.superid = superid;
    }

    @Basic
    @Column(name = "customer_group_id")
    public Integer getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(Integer customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    @Basic
    @Column(name = "cust_id")
    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    @Basic
    @Column(name = "backup_phone")
    public String getBackupPhone() {
        return backupPhone;
    }

    public void setBackupPhone(String backupPhone) {
        this.backupPhone = backupPhone;
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
    @Column(name = "appointment_time")
    public Timestamp getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(Timestamp appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    @Basic
    @Column(name = "operator")
    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Basic
    @Column(name = "remark")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }
    @Basic
    @Column(name = "status")
    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "market_task_id")
    public String getMarketTaskId() {
        return marketTaskId;
    }

    public void setMarketTaskId(String marketTaskId) {
        this.marketTaskId = marketTaskId;
    }

    @Override
    public String toString() {
        return "AppointmentCallback{" +
                "id=" + id +
                ", superid='" + superid + '\'' +
                ", customerGroupId=" + customerGroupId +
                ", custId='" + custId + '\'' +
                ", backupPhone='" + backupPhone + '\'' +
                ", createTime=" + createTime +
                ", appointmentTime=" + appointmentTime +
                ", operator='" + operator + '\'' +
                ", remark='" + remark + '\'' +
                ", status=" + status +
                ", marketTaskId=" + marketTaskId +
                '}';
    }
}
