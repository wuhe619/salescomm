package com.bdaim.customer.dto;


import java.util.Date;

/**
 * 客户营销资源导出
 *
 * @author chengning@salescomm.net
 * @date 2018/9/4
 * @description
 */
public class CustomerMarketExport {

    private Long id;

    private Integer type;

    private String customerId;

    private String customerGroupId;

    private String fileName;

    private Date applyTime;

    private Long potentialPersonSum;

    private Integer downloadSum;

    private Date downloadLastTime;

    private String downloadUrl;

    private Date createTime;

    private Integer status;

    private Date modifyTime;

    private Date passTime;

    private String remark;

    private String operator;

    private String userId;

    private String enterpriseName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public Long getPotentialPersonSum() {
        return potentialPersonSum;
    }

    public void setPotentialPersonSum(Long potentialPersonSum) {
        this.potentialPersonSum = potentialPersonSum;
    }

    public Integer getDownloadSum() {
        return downloadSum;
    }

    public void setDownloadSum(Integer downloadSum) {
        this.downloadSum = downloadSum;
    }

    public Date getDownloadLastTime() {
        return downloadLastTime;
    }

    public void setDownloadLastTime(Date downloadLastTime) {
        this.downloadLastTime = downloadLastTime;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Date getPassTime() {
        return passTime;
    }

    public void setPassTime(Date passTime) {
        this.passTime = passTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(String customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    @Override
    public String toString() {
        return "CustomerMarketExportDO{" +
                "id=" + id +
                ", type=" + type +
                ", customerId='" + customerId + '\'' +
                ", customerGroupId='" + customerGroupId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", applyTime=" + applyTime +
                ", potentialPersonSum=" + potentialPersonSum +
                ", downloadSum=" + downloadSum +
                ", downloadLastTime=" + downloadLastTime +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", createTime=" + createTime +
                ", status=" + status +
                ", modifyTime=" + modifyTime +
                ", passTime=" + passTime +
                ", remark='" + remark + '\'' +
                ", operator='" + operator + '\'' +
                ", userId='" + userId + '\'' +
                ", enterpriseName='" + enterpriseName + '\'' +
                '}';
    }
}
