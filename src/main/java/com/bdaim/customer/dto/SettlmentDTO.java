package com.bdaim.customer.dto;

/**
 * 职场结算价DTO
 */
public class SettlmentDTO {
    private String zid;
    private String projectName;
    private String projectId;
    private String enterpriseName;
    private String custId;
    private String jobId;
    private String jobName;
    private String settlementPrice;
    private String settlementPriceLog;
    private String remark;
    private String serviceCode;
    private String status;
    private String type;
    private String billDate;//账期
    private String jobSignNum;//职场标记量
    private String jobSettlementNum;//职场结算单
    private String createTime;//创建时间
    private String operator; //操作人
    private String unitPrice;//单价
    private String batchNo;//批次
    private String totalPrice;//总价
    private String totalSettlementNum;//结算单总量
    private String confirmPerson;//确认人
    private String confirmTime;//确认时间
    private String paymentTime;//到账时间
    private String confirmRemark;//确认备注

    @Override
    public String toString() {
        return "SettlmentDTO{" +
                "zid='" + zid + '\'' +
                ", projectName='" + projectName + '\'' +
                ", projectId='" + projectId + '\'' +
                ", enterpriseName='" + enterpriseName + '\'' +
                ", custId='" + custId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", jobName='" + jobName + '\'' +
                ", settlementPrice='" + settlementPrice + '\'' +
                ", settlementPriceLog='" + settlementPriceLog + '\'' +
                ", remark='" + remark + '\'' +
                ", serviceCode='" + serviceCode + '\'' +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                ", billDate='" + billDate + '\'' +
                ", jobSignNum='" + jobSignNum + '\'' +
                ", jobSettlementNum='" + jobSettlementNum + '\'' +
                ", createTime='" + createTime + '\'' +
                ", operator='" + operator + '\'' +
                ", unitPrice='" + unitPrice + '\'' +
                ", batchNo='" + batchNo + '\'' +
                ", totalPrice='" + totalPrice + '\'' +
                ", totalSettlementNum='" + totalSettlementNum + '\'' +
                ", confirmPerson='" + confirmPerson + '\'' +
                ", confirmTime='" + confirmTime + '\'' +
                ", paymentTime='" + paymentTime + '\'' +
                ", confirmRemark='" + confirmRemark + '\'' +
                '}';
    }

    public String getConfirmRemark() {
        return confirmRemark;
    }

    public void setConfirmRemark(String confirmRemark) {
        this.confirmRemark = confirmRemark;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getConfirmPerson() {
        return confirmPerson;
    }

    public void setConfirmPerson(String confirmPerson) {
        this.confirmPerson = confirmPerson;
    }

    public String getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(String confirmTime) {
        this.confirmTime = confirmTime;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getTotalSettlementNum() {
        return totalSettlementNum;
    }

    public void setTotalSettlementNum(String totalSettlementNum) {
        this.totalSettlementNum = totalSettlementNum;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
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

    public String getJobSignNum() {
        return jobSignNum;
    }

    public void setJobSignNum(String jobSignNum) {
        this.jobSignNum = jobSignNum;
    }

    public String getJobSettlementNum() {
        return jobSettlementNum;
    }

    public void setJobSettlementNum(String jobSettlementNum) {
        this.jobSettlementNum = jobSettlementNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getZid() {
        return zid;
    }

    public void setZid(String zid) {
        this.zid = zid;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getSettlementPrice() {
        return settlementPrice;
    }

    public void setSettlementPrice(String settlementPrice) {
        this.settlementPrice = settlementPrice;
    }

    public String getSettlementPriceLog() {
        return settlementPriceLog;
    }

    public void setSettlementPriceLog(String settlementPriceLog) {
        this.settlementPriceLog = settlementPriceLog;
    }

}
