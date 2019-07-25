package com.bdaim.resource.entity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "t_market_resource", schema = "", catalog = "")
public class MarketResourceEntity {
    private Integer resourceId;
    private String supplierId;
    private Integer typeCode;
    private String resname;
    private Integer salePrice;
    private Integer costPrice;
    private String description;
    private Integer status;
    private Timestamp createTime;
    private Timestamp validDate;
    private Timestamp expireDate;
    private String resPicPath;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "resource_id")
    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }
    @Basic
    @Column(name = "supplier_id")

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    @Basic
    @Column(name = "type_code")
    public Integer getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(Integer typeCode) {
        this.typeCode = typeCode;
    }

    @Basic
    @Column(name = "resname")
    public String getResname() {
        return resname;
    }

    public void setResname(String resname) {
        this.resname = resname;
    }

    @Basic
    @Column(name = "sale_price")
    public Integer getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Integer salePrice) {
        this.salePrice = salePrice;
    }

    @Basic
    @Column(name = "cost_price")
    public Integer getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Integer costPrice) {
        this.costPrice = costPrice;
    }

    @Basic
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
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

    @Basic
    @Column(name = "valid_date")
    public Timestamp getValidDate() {
        return validDate;
    }

    public void setValidDate(Timestamp validDate) {
        this.validDate = validDate;
    }

    @Basic
    @Column(name = "expire_date")
    public Timestamp getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Timestamp expireDate) {
        this.expireDate = expireDate;
    }
    @Basic
    @Column(name = "res_pic_path")

    public String getResPicPath() {
        return resPicPath;
    }

    public void setResPicPath(String resPicPath) {
        this.resPicPath = resPicPath;
    }


    @Override
    public String toString() {
        return "MarketResourceEntity{" +
                "resourceId=" + resourceId +
                ", supplierId='" + supplierId + '\'' +
                ", typeCode=" + typeCode +
                ", resname='" + resname + '\'' +
                ", salePrice=" + salePrice +
                ", costPrice=" + costPrice +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", validDate=" + validDate +
                ", expireDate=" + expireDate +
                ", resPicPath='" + resPicPath + '\'' +
                '}';
    }
}

