package com.bdaim.customer.dto;

import com.bdaim.customer.entity.Customer;

public class CustomerDTO {
    private String id;
    private String enterpriseName;
    private String username;
    private String userId;
    private String mobileNum;
    private String source;
    private int status;
    private String createTime;

    private String enterpriseAddress;
    private String bliNumber;
    private String bliPic;
    private String taxPayerNum;
    private String taxPic;
    private String bankName;
    private String bankAccount;
    private String bankAccountPic;

    private String province;
    private String city;
    private String county;
    private String realname;
    private String email;
    private String title;

    private String industryId;
    private String industryName;
    private Integer settlementType;
    private Integer callType;
    private String callTypeNames;
    private String serviceMode;//服务模式
    /**
     * 有效外显个数
     */
    private Integer apparentNumberNum;
    /**
     * 外显使用规则
     */
    private String apparentNumberRule;

    private String apparentNumber;
    /**
     * 授信额度(元)
     */
    private String creditAmount;


    public CustomerDTO() {

    }

    public CustomerDTO(Customer cust) {
        this.id = cust.getCustId();
        this.enterpriseName = cust.getEnterpriseName();
        this.status = cust.getStatus();
        this.createTime = cust.getCreateTime().toString();
        this.source = cust.getSource();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnterpriseAddress() {
        return enterpriseAddress;
    }

    public void setEnterpriseAddress(String enterpriseAddress) {
        this.enterpriseAddress = enterpriseAddress;
    }

    public String getBliNumber() {
        return bliNumber;
    }

    public void setBliNumber(String bliNumber) {
        this.bliNumber = bliNumber;
    }

    public String getBliPic() {
        return bliPic;
    }

    public void setBliPic(String bliPic) {
        this.bliPic = bliPic;
    }

    public String getTaxPayerNum() {
        return taxPayerNum;
    }

    public void setTaxPayerNum(String taxPayerNum) {
        this.taxPayerNum = taxPayerNum;
    }

    public String getTaxPic() {
        return taxPic;
    }

    public void setTaxPic(String taxPic) {
        this.taxPic = taxPic;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankAccountPic() {
        return bankAccountPic;
    }

    public void setBankAccountPic(String bankAccountPic) {
        this.bankAccountPic = bankAccountPic;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMobileNum() {
        return mobileNum;
    }

    public void setMobileNum(String mobileNum) {
        this.mobileNum = mobileNum;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIndustryId() {
        return industryId;
    }

    public void setIndustryId(String industryId) {
        this.industryId = industryId;
    }

    public String getIndustryName() {
        return industryName;
    }

    public void setIndustryName(String industryName) {
        this.industryName = industryName;
    }


    public Integer getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(Integer settlementType) {
        this.settlementType = settlementType;
    }

    public String getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(String creditAmount) {
        this.creditAmount = creditAmount;
    }

    public Integer getCallType() {
        return callType;
    }

    public void setCallType(Integer callType) {
        this.callType = callType;
    }

    public Integer getApparentNumberNum() {
        return apparentNumberNum;
    }

    public void setApparentNumberNum(Integer apparentNumberNum) {
        this.apparentNumberNum = apparentNumberNum;
    }

    public String getApparentNumberRule() {
        return apparentNumberRule;
    }

    public void setApparentNumberRule(String apparentNumberRule) {
        this.apparentNumberRule = apparentNumberRule;
    }

    public String getCallTypeNames() {
        return callTypeNames;
    }

    public void setCallTypeNames(String callTypeNames) {
        this.callTypeNames = callTypeNames;
    }

    public String getApparentNumber() {
        return apparentNumber;
    }

    public void setApparentNumber(String apparentNumber) {
        this.apparentNumber = apparentNumber;
    }

    public String getServiceMode() {
        return serviceMode;
    }

    public void setServiceMode(String serviceMode) {
        this.serviceMode = serviceMode;
    }
}
