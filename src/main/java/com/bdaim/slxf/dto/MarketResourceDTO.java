package com.bdaim.slxf.dto;

import java.util.Date;

import com.bdaim.resource.entity.MarketResourceEntity;

/**
 * 营销资源实体类
 *
 * @author duanliying@salescomm.net
 * @date 2018/12/18 9:44
 */
public class MarketResourceDTO {

    private Long id;

    /**
     * 资源id
     */
    private Integer resourceId;

    /**
     * 供应商
     */
    private String supplierId;

    private String supplierName;

    /**
     * 资源类型
     */
    private Integer typeCode;

    /**
     * 资源名称
     */
    private String resname;

    /**
     * 销售单价(分)
     */
    private Integer salePrice;

    /**
     * 供应商
     */
    private Integer costPrice;

    /**
     * 资源说明
     */
    private String description;

    /**
     * 资源状态(1.可用 2.不可用)
     */
    private Integer status;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 有效期
     */
    private Date validDate;

    /**
     * 失效期
     */
    private Date expireDate;

    /**
     * 资源图片path
     */
    private String resPicPath;

    private String resourceProperty;

    private Integer chargingType;

    public MarketResourceDTO() {
    }

    public MarketResourceDTO(MarketResourceEntity marketResource) {
        this.resourceId = marketResource.getResourceId();
        this.supplierId = marketResource.getSupplierId();
        this.typeCode = marketResource.getTypeCode();
        this.resname = marketResource.getResname();
        this.salePrice = marketResource.getSalePrice();
        this.costPrice = marketResource.getCostPrice();
        this.description = marketResource.getDescription();
        this.status = marketResource.getStatus();
        this.createTime = marketResource.getCreateTime();
        this.validDate = marketResource.getValidDate();
        this.expireDate = marketResource.getExpireDate();
        this.resPicPath = marketResource.getResPicPath();
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public Integer getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(Integer typeCode) {
        this.typeCode = typeCode;
    }

    public String getResname() {
        return resname;
    }

    public void setResname(String resname) {
        this.resname = resname;
    }

    public Integer getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Integer salePrice) {
        this.salePrice = salePrice;
    }

    public Integer getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Integer costPrice) {
        this.costPrice = costPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getValidDate() {
        return validDate;
    }

    public void setValidDate(Date validDate) {
        this.validDate = validDate;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public String getResPicPath() {
        return resPicPath;
    }

    public void setResPicPath(String resPicPath) {
        this.resPicPath = resPicPath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceProperty() {
        return resourceProperty;
    }

    public void setResourceProperty(String resourceProperty) {
        this.resourceProperty = resourceProperty;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Integer getChargingType() {
        return chargingType;
    }

    public void setChargingType(Integer chargingType) {
        this.chargingType = chargingType;
    }
}
