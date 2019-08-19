package com.bdaim.supplier.dto;

import java.sql.Timestamp;

import com.bdaim.supplier.entity.SupplierEntity;

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


    private Integer settlementType;

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
                ", type='" + type + '\'' +
                ", settlementType=" + settlementType +
                '}';
    }
}
