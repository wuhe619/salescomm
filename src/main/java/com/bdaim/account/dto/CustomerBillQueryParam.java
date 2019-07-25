package com.bdaim.account.dto;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/10/8 10:36
 */
public class CustomerBillQueryParam {
    private String customerId;
    private String enterpriseName;
    private String account;

    private String phone;
    private String type;
    //0全部  1半年 2 一年  
    private String billDate;
    private String transactionId;
    private String supplierId;
    private String date;
    private String batchId;

    private String startTime;
    private String endTime;
    private int pageNum;
    private int pageSize;
    private String industry;
    private String realname;

    private Double amount;
    private String path;
    private String remark;
    private Integer action;//action 0 充值   1扣减
    private Integer payMode;
    private Long userId;
    private String dealType;

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDealType() {
        return dealType;
    }

    public void setDealType(String dealType) {
        this.dealType = dealType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public Integer getPayMode() {
        return payMode;
    }

    public void setPayMode(Integer payMode) {
        this.payMode = payMode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }


    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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

    public String getDate() {
        return date;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "CustomerBillQueryParam{" +
                "customerId='" + customerId + '\'' +
                ", enterpriseName='" + enterpriseName + '\'' +
                ", account='" + account + '\'' +
                ", phone='" + phone + '\'' +
                ", type='" + type + '\'' +
                ", billDate='" + billDate + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", supplierId='" + supplierId + '\'' +
                ", date='" + date + '\'' +
                ", batchId='" + batchId + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", industry='" + industry + '\'' +
                ", realname='" + realname + '\'' +
                ", amount=" + amount +
                ", path='" + path + '\'' +
                ", remark='" + remark + '\'' +
                ", action=" + action +
                ", payMode=" + payMode +
                ", userId=" + userId +
                ", dealType='" + dealType + '\'' +
                '}';
    }
}
