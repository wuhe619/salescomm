package com.bdaim.bill.dto;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/10/8 10:39
 */
public class SupplierBillQueryParam {
    /**
     * 快递id
     */
    private String expressId;
    /**
     * 收件人id
     */
    private String peopleId;
    /**
     * 姓名
     */
    private String name;
    private String phone;
    private String supplierName;
    private String resourceType;
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
    private String batchName;

    public String getExpressId() {
        return expressId;
    }

    public void setExpressId(String expressId) {
        this.expressId = expressId;
    }

    public String getPeopleId() {
        return peopleId;
    }

    public void setPeopleId(String peopleId) {
        this.peopleId = peopleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

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
