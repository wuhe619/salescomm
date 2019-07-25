package com.bdaim.bill.dto;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/10/8 10:39
 */
public class SupplierBillQueryParam {

    private String supplierName;
    private String supplierId;
    private String type;
    private String billDate;
    private int pageNum;
    private int pageSize;
    private String enterpriseName;
    private String batchId;
    private String transActionId;
    private String custId;
    private String thirdPartyNum;
    private String startTime;
    private String endTime;
    private String resourceId;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getTransActionId() {
        return transActionId;
    }

    public void setTransActionId(String transActionId) {
        this.transActionId = transActionId;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getThirdPartyNum() {
        return thirdPartyNum;
    }

    public void setThirdPartyNum(String thirdPartyNum) {
        this.thirdPartyNum = thirdPartyNum;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "SupplierBillQueryParam{" +
                "supplierName='" + supplierName + '\'' +
                ", supplierId='" + supplierId + '\'' +
                ", type='" + type + '\'' +
                ", billDate='" + billDate + '\'' +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", enterpriseName='" + enterpriseName + '\'' +
                ", batchId='" + batchId + '\'' +
                ", transActionId='" + transActionId + '\'' +
                ", custId='" + custId + '\'' +
                ", thirdPartyNum='" + thirdPartyNum + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", resourceId='" + resourceId + '\'' +
                '}';
    }
}
