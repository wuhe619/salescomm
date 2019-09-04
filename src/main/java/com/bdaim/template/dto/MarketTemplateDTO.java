package com.bdaim.template.dto;

import com.bdaim.template.entity.MarketTemplate;

import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2019/01/08
 * @description
 */
public class MarketTemplateDTO {
    private int id;
    private String custId;
    private String custName;
    private String title;
    private Integer typeCode;
    private String mouldContent;
    private Timestamp createTime;
    private Integer status;
    private Timestamp modifyTime;
    private Timestamp passTime;
    private String remark;
    private String smsSignatures;
    private String emailMouldContent;
    private String operator;
    private String templateCode;
    private String resourceId;
    private String resourceName;
    private String supplierName;
    private Integer marketProjectId;
    private String marketProjectName;

    public MarketTemplateDTO() {
    }

    public MarketTemplateDTO(MarketTemplate marketTemplate) {
        this.id = marketTemplate.getId();
        this.custId = marketTemplate.getCustId();
        this.title = marketTemplate.getTitle();
        this.typeCode = marketTemplate.getTypeCode();
        this.mouldContent = marketTemplate.getMouldContent();
        this.createTime = marketTemplate.getCreateTime();
        this.status = marketTemplate.getStatus();
        this.modifyTime = marketTemplate.getModifyTime();
        this.passTime = marketTemplate.getPassTime();
        this.remark = marketTemplate.getRemark();
        this.smsSignatures = marketTemplate.getSmsSignatures();
        this.emailMouldContent = marketTemplate.getEmailMouldContent();
        this.operator = marketTemplate.getOperator();
        this.templateCode = marketTemplate.getTemplateCode();
        this.resourceId = marketTemplate.getResourceId();
        this.marketProjectId = marketTemplate.getMarketProjectId();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(Integer typeCode) {
        this.typeCode = typeCode;
    }

    public String getMouldContent() {
        return mouldContent;
    }

    public void setMouldContent(String mouldContent) {
        this.mouldContent = mouldContent;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Timestamp getPassTime() {
        return passTime;
    }

    public void setPassTime(Timestamp passTime) {
        this.passTime = passTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getSmsSignatures() {
        return smsSignatures;
    }

    public void setSmsSignatures(String smsSignatures) {
        this.smsSignatures = smsSignatures;
    }

    public String getEmailMouldContent() {
        return emailMouldContent;
    }

    public void setEmailMouldContent(String emailMouldContent) {
        this.emailMouldContent = emailMouldContent;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Integer getMarketProjectId() {
        return marketProjectId;
    }

    public void setMarketProjectId(Integer marketProjectId) {
        this.marketProjectId = marketProjectId;
    }

    public String getMarketProjectName() {
        return marketProjectName;
    }

    public void setMarketProjectName(String marketProjectName) {
        this.marketProjectName = marketProjectName;
    }
}
