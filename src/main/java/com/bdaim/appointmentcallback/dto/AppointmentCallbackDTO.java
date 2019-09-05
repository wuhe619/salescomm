package com.bdaim.appointmentcallback.dto;

import com.bdaim.appointmentcallback.entity.AppointmentCallback;

import java.sql.Timestamp;

/**
 * 预约回拨
 *
 * @author chengning@salescomm.net
 * @date 2019/2/12 16:41
 */
public class AppointmentCallbackDTO {
    private int id;
    private String superid;
    private Integer customerGroupId;
    private String custId;
    private String customerGroupName;
    private String backupPhone;
    private Timestamp createTime;
    private Timestamp appointmentTime;
    private String operator;
    private String operatorName;
    private String remark;
    private Integer status;
    private String super_name;
    private String super_age;
    private String super_sex;
    private String super_telphone;
    private String super_phone;
    private String super_address_province_city;
    private String super_address_street;
    private String marketTaskId;
    private String marketTaskName;

    public AppointmentCallbackDTO(AppointmentCallback appointmentCallback) {
        this.id = appointmentCallback.getId();
        this.superid = appointmentCallback.getSuperid();
        this.customerGroupId = appointmentCallback.getCustomerGroupId();
        this.custId = appointmentCallback.getCustId();
        this.backupPhone = appointmentCallback.getBackupPhone();
        this.createTime = appointmentCallback.getCreateTime();
        this.appointmentTime = appointmentCallback.getAppointmentTime();
        this.operator = appointmentCallback.getOperator();
        this.remark = appointmentCallback.getRemark();
        this.status = appointmentCallback.getStatus();
        this.marketTaskId = appointmentCallback.getMarketTaskId();
    }

    public String getSuperid() {
        return superid;
    }

    public void setSuperid(String superid) {
        this.superid = superid;
    }

    public Integer getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(Integer customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    public String getBackupPhone() {
        return backupPhone;
    }

    public void setBackupPhone(String backupPhone) {
        this.backupPhone = backupPhone;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(Timestamp appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCustomerGroupName() {
        return customerGroupName;
    }

    public void setCustomerGroupName(String customerGroupName) {
        this.customerGroupName = customerGroupName;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getSuper_name() {
        return super_name;
    }

    public void setSuper_name(String super_name) {
        this.super_name = super_name;
    }

    public String getSuper_age() {
        return super_age;
    }

    public void setSuper_age(String super_age) {
        this.super_age = super_age;
    }

    public String getSuper_sex() {
        return super_sex;
    }

    public void setSuper_sex(String super_sex) {
        this.super_sex = super_sex;
    }

    public String getSuper_telphone() {
        return super_telphone;
    }

    public void setSuper_telphone(String super_telphone) {
        this.super_telphone = super_telphone;
    }

    public String getSuper_phone() {
        return super_phone;
    }

    public void setSuper_phone(String super_phone) {
        this.super_phone = super_phone;
    }

    public String getSuper_address_province_city() {
        return super_address_province_city;
    }

    public void setSuper_address_province_city(String super_address_province_city) {
        this.super_address_province_city = super_address_province_city;
    }

    public String getSuper_address_street() {
        return super_address_street;
    }

    public void setSuper_address_street(String super_address_street) {
        this.super_address_street = super_address_street;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMarketTaskId() {
        return marketTaskId;
    }

    public void setMarketTaskId(String marketTaskId) {
        this.marketTaskId = marketTaskId;
    }

    public String getMarketTaskName() {
        return marketTaskName;
    }

    public void setMarketTaskName(String marketTaskName) {
        this.marketTaskName = marketTaskName;
    }

    @Override
    public String toString() {
        return "AppointmentCallbackDTO{" +
                "id=" + id +
                ", superid='" + superid + '\'' +
                ", customerGroupId=" + customerGroupId +
                ", custId='" + custId + '\'' +
                ", customerGroupName='" + customerGroupName + '\'' +
                ", backupPhone='" + backupPhone + '\'' +
                ", createTime=" + createTime +
                ", appointmentTime=" + appointmentTime +
                ", operator='" + operator + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", remark='" + remark + '\'' +
                ", status=" + status +
                ", super_name='" + super_name + '\'' +
                ", super_age='" + super_age + '\'' +
                ", super_sex='" + super_sex + '\'' +
                ", super_telphone='" + super_telphone + '\'' +
                ", super_phone='" + super_phone + '\'' +
                ", super_address_province_city='" + super_address_province_city + '\'' +
                ", super_address_street='" + super_address_street + '\'' +
                ", marketTaskId='" + marketTaskId + '\'' +
                ", marketTaskName='" + marketTaskName + '\'' +
                '}';
    }
}
