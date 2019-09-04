package com.bdaim.supplier.dto;

import com.bdaim.supplier.entity.SupplierEntity;

import java.sql.Timestamp;

/**
 * @author wangxx
 * @Description: TODO
 * @date 2018/9/17 17:34
 */
public class SupplierDTO {
    private Integer supplierId;
    private String name;
    private String person;
    private String phone;
    private String position;
    private Integer status;
    private Timestamp createTime;

    private String config;
    /**
     * 关联资源
     */
    private String RelationResource;

    private String type;

    /**
     * 结算类型 1-预付费 2-授信
     */
    private Integer settlementType;

    /**
     * 联系人姓名
     */
    private String contactPerson;
    /**
     * 联系人岗位
     */
    private String contactPosition;
    /**
     * 联系人手机
     */
    private String contactPhone;
    /**
     * 资源类型 1-数据，2-呼叫，3-短信,多个逗号隔开
     */
    private String serviceResource;

    private String dataConfig;

    private String callConfig;

    private String smsConfig;


    /**
     * 授信额度(元)
     */
    private String creditAmount;

    public SupplierDTO(Integer supplierId, String name, String person, String phone, String position, Integer status, Timestamp createTime, String config, String type, Integer settlementType) {
        this.supplierId = supplierId;
        this.name = name;
        this.person = person;
        this.phone = phone;
        this.position = position;
        this.status = status;
        this.createTime = createTime;
        this.config = config;
        this.type = type;
        this.settlementType = settlementType;
    }

    public SupplierDTO() {
    }

    public SupplierDTO(SupplierEntity supplierEntity) {
        this.supplierId = supplierEntity.getSupplierId();
        this.name = supplierEntity.getName();
        this.createTime = supplierEntity.getCreateTime();
        this.settlementType = supplierEntity.getSettlementType();
        this.person = supplierEntity.getContactPerson();
        this.position = supplierEntity.getContactPosition();
        this.phone = supplierEntity.getContactPhone();
        this.config = supplierEntity.getContactPerson();
        this.status = supplierEntity.getStatus();
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Integer getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(Integer settlementType) {
        this.settlementType = settlementType;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getRelationResource() {
        return RelationResource;
    }

    public void setRelationResource(String RelationResource) {
        this.RelationResource = RelationResource;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPosition() {
        return contactPosition;
    }

    public void setContactPosition(String contactPosition) {
        this.contactPosition = contactPosition;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getServiceResource() {
        return serviceResource;
    }

    public void setServiceResource(String serviceResource) {
        this.serviceResource = serviceResource;
    }

    public String getDataConfig() {
        return dataConfig;
    }

    public void setDataConfig(String dataConfig) {
        this.dataConfig = dataConfig;
    }

    public String getCallConfig() {
        return callConfig;
    }

    public void setCallConfig(String callConfig) {
        this.callConfig = callConfig;
    }

    public String getSmsConfig() {
        return smsConfig;
    }

    public void setSmsConfig(String smsConfig) {
        this.smsConfig = smsConfig;
    }

    public String getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(String creditAmount) {
        this.creditAmount = creditAmount;
    }

    @Override
    public String toString() {
        return "SupplierDTO{" +
                "supplierId=" + supplierId +
                ", name='" + name + '\'' +
                ", person='" + person + '\'' +
                ", phone='" + phone + '\'' +
                ", position='" + position + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", config='" + config + '\'' +
                ", RelationResource='" + RelationResource + '\'' +
                ", type='" + type + '\'' +
                ", settlementType=" + settlementType +
                ", settlementType=" + settlementType +
                ", contactPerson='" + contactPerson + '\'' +
                ", contactPosition='" + contactPosition + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", serviceResource='" + serviceResource + '\'' +
                ", dataConfig='" + dataConfig + '\'' +
                ", callConfig='" + callConfig + '\'' +
                ", smsConfig='" + smsConfig + '\'' +
                ", creditAmount='" + creditAmount + '\'' +
                '}';
    }
}
