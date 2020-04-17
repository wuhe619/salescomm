package com.bdaim.crm.ent.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * 企查查数据导入实体类
 */
public class ImportQCCEnt {
    @Excel(name = "企业名称", orderNum = "0")
    private String entName;
    @Excel(name = "法定代表人", orderNum = "1")
    private String legalName;
    @Excel(name = "法定代表人Key")
    private String legalNameKey;
    @Excel(name = "注册资本", orderNum = "2")
    private String regCap;
    private String regCapCur;
    @Excel(name = "成立日期", orderNum = "3")
    private String establishTime;
    @Excel(name = "注吊销日期", orderNum = "4")
    private String cancelDate;
    @Excel(name = "经营状态", orderNum = "5")
    private String entStatus;
    @Excel(name = "工商注册号", orderNum = "6")
    private String regNo;
    @Excel(name = "机构组织代码", orderNum = "7")
    private String orgNo;
    @Excel(name = "统一社会信用代码", orderNum = "8")
    private String creditCode;
    @Excel(name = "纳税人识别号", orderNum = "9")
    private String taxpayerNo;
    private String entTypeCode;
    @Excel(name = "企业类型", orderNum = "10")
    private String entType;
    private String industryCode;
    @Excel(name = "所属行业", orderNum = "11")
    private String industry;
    @Excel(name = "营业期限开始时间", orderNum = "12")
    private String fromTime;
    @Excel(name = "营业期限结束时间", orderNum = "13")
    private String toTime;
    @Excel(name = "核准日期", orderNum = "14")
    private String approvedTime;
    @Excel(name = "登记机关", orderNum = "15")
    private String regInstitute;
    @Excel(name = "省份代码", orderNum = "16")
    private String regProvinceCode;
    @Excel(name = "所属地区", orderNum = "17")
    private String regProvinceName;
    @Excel(name = "详细地址", orderNum = "18")
    private String regLocation;
    private String regCityCode;
    @Excel(name = "注册地址城市", orderNum = "19")
    private String regCityName;
    @Excel(name = "经营范围", orderNum = "20")
    private String opScope;
    @Excel(name = "电话", orderNum = "21")
    private String phoneNumbers;
    @Excel(name = "更多电话")
    private String phoneNumbers_1;
    @Excel(name = "邮箱", orderNum = "22")
    private String email;

    @Excel(name = "参保人数", orderNum = "22")
    private String insuredPersonCount;
    @Excel(name = "行业领域", orderNum = "22")
    private String industryField;
    @Excel(name = "是否上市", orderNum = "22")
    private String  isLListedCompany;
    @Excel(name = "融资轮次", orderNum = "22")
    private String financingRound;
    @Excel(name = "融资金额", orderNum = "22")
    private String financingAmount;
    @Excel(name = "融资日期", orderNum = "22")
    private String financingTime;
    @Excel(name = "潜在标签", orderNum = "22")
    private String potentialLabel;

    private Long id;
    /**
     * 手机号数量
     */
    private int phoneCount;
    /**
     * 固话数量
     */
    private int telPhoneCount;
    /**
     * 邮箱数量
     */
    private int emailCount;
    /**
     * 网站数量
     */
    private int webCount;
    /**
     * 专利数量
     */
    private int patentCount;
    /**
     * 商标数量
     */
    private int trademarkCount;
    /**
     * 联系人数量
     */
    private int contactManCount;
    private int ext1Count;
    private int ext2Count;
    private int ext3Count;
    private int ext4Count;
    private int ext5Count;

    /**
     * 审核日期
     */
    private String auditDate;

    /**
     * 公司介绍
     */
    private String entIntroduction;

    /**
     * 联系人
     */
    private String contactMan;

    /**
     * 公司主页
     */
    private String entIndex;


    public String getEntName() {
        return entName;
    }

    public void setEntName(String entName) {
        this.entName = entName;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getRegCap() {
        return regCap;
    }

    public void setRegCap(String regCap) {
        this.regCap = regCap;
    }

    public String getRegCapCur() {
        return regCapCur;
    }

    public void setRegCapCur(String regCapCur) {
        this.regCapCur = regCapCur;
    }

    public String getEstablishTime() {
        return establishTime;
    }

    public void setEstablishTime(String establishTime) {
        this.establishTime = establishTime;
    }

    public String getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(String cancelDate) {
        this.cancelDate = cancelDate;
    }

    public String getEntStatus() {
        return entStatus;
    }

    public void setEntStatus(String entStatus) {
        this.entStatus = entStatus;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getOrgNo() {
        return orgNo;
    }

    public void setOrgNo(String orgNo) {
        this.orgNo = orgNo;
    }

    public String getCreditCode() {
        return creditCode;
    }

    public void setCreditCode(String creditCode) {
        this.creditCode = creditCode;
    }

    public String getTaxpayerNo() {
        return taxpayerNo;
    }

    public void setTaxpayerNo(String taxpayerNo) {
        this.taxpayerNo = taxpayerNo;
    }

    public String getEntTypeCode() {
        return entTypeCode;
    }

    public void setEntTypeCode(String entTypeCode) {
        this.entTypeCode = entTypeCode;
    }

    public String getEntType() {
        return entType;
    }

    public void setEntType(String entType) {
        this.entType = entType;
    }

    public String getIndustryCode() {
        return industryCode;
    }

    public void setIndustryCode(String industryCode) {
        this.industryCode = industryCode;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }

    public String getApprovedTime() {
        return approvedTime;
    }

    public void setApprovedTime(String approvedTime) {
        this.approvedTime = approvedTime;
    }

    public String getRegInstitute() {
        return regInstitute;
    }

    public void setRegInstitute(String regInstitute) {
        this.regInstitute = regInstitute;
    }

    public String getRegProvinceCode() {
        return regProvinceCode;
    }

    public void setRegProvinceCode(String regProvinceCode) {
        this.regProvinceCode = regProvinceCode;
    }

    public String getRegProvinceName() {
        return regProvinceName;
    }

    public void setRegProvinceName(String regProvinceName) {
        this.regProvinceName = regProvinceName;
    }

    public String getRegLocation() {
        return regLocation;
    }

    public void setRegLocation(String regLocation) {
        this.regLocation = regLocation;
    }

    public String getRegCityCode() {
        return regCityCode;
    }

    public void setRegCityCode(String regCityCode) {
        this.regCityCode = regCityCode;
    }

    public String getRegCityName() {
        return regCityName;
    }

    public void setRegCityName(String regCityName) {
        this.regCityName = regCityName;
    }

    public String getOpScope() {
        return opScope;
    }

    public void setOpScope(String opScope) {
        this.opScope = opScope;
    }

    public String getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(String phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPhoneCount() {
        return phoneCount;
    }

    public void setPhoneCount(int phoneCount) {
        this.phoneCount = phoneCount;
    }

    public int getTelPhoneCount() {
        return telPhoneCount;
    }

    public void setTelPhoneCount(int telPhoneCount) {
        this.telPhoneCount = telPhoneCount;
    }

    public int getEmailCount() {
        return emailCount;
    }

    public void setEmailCount(int emailCount) {
        this.emailCount = emailCount;
    }

    public int getWebCount() {
        return webCount;
    }

    public void setWebCount(int webCount) {
        this.webCount = webCount;
    }

    public int getPatentCount() {
        return patentCount;
    }

    public void setPatentCount(int patentCount) {
        this.patentCount = patentCount;
    }

    public int getTrademarkCount() {
        return trademarkCount;
    }

    public void setTrademarkCount(int trademarkCount) {
        this.trademarkCount = trademarkCount;
    }

    public int getExt1Count() {
        return ext1Count;
    }

    public void setExt1Count(int ext1Count) {
        this.ext1Count = ext1Count;
    }

    public int getExt2Count() {
        return ext2Count;
    }

    public void setExt2Count(int ext2Count) {
        this.ext2Count = ext2Count;
    }

    public int getExt3Count() {
        return ext3Count;
    }

    public void setExt3Count(int ext3Count) {
        this.ext3Count = ext3Count;
    }

    public int getExt4Count() {
        return ext4Count;
    }

    public void setExt4Count(int ext4Count) {
        this.ext4Count = ext4Count;
    }

    public int getExt5Count() {
        return ext5Count;
    }

    public void setExt5Count(int ext5Count) {
        this.ext5Count = ext5Count;
    }

    public String getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(String auditDate) {
        this.auditDate = auditDate;
    }

    public String getEntIntroduction() {
        return entIntroduction;
    }

    public void setEntIntroduction(String entIntroduction) {
        this.entIntroduction = entIntroduction;
    }

    public String getContactMan() {
        return contactMan;
    }

    public void setContactMan(String contactMan) {
        this.contactMan = contactMan;
    }

    public String getEntIndex() {
        return entIndex;
    }

    public void setEntIndex(String entIndex) {
        this.entIndex = entIndex;
    }

    public int getContactManCount() {
        return contactManCount;
    }

    public void setContactManCount(int contactManCount) {
        this.contactManCount = contactManCount;
    }

    public String getLegalNameKey() {
        return legalNameKey;
    }

    public void setLegalNameKey(String legalNameKey) {
        this.legalNameKey = legalNameKey;
    }

    public String getInsuredPersonCount() {
        return insuredPersonCount;
    }

    public void setInsuredPersonCount(String insuredPersonCount) {
        this.insuredPersonCount = insuredPersonCount;
    }

    public String getIndustryField() {
        return industryField;
    }

    public void setIndustryField(String industryField) {
        this.industryField = industryField;
    }

    public String getIsLListedCompany() {
        return isLListedCompany;
    }

    public void setIsLListedCompany(String isLListedCompany) {
        this.isLListedCompany = isLListedCompany;
    }

    public String getFinancingRound() {
        return financingRound;
    }

    public void setFinancingRound(String financingRound) {
        this.financingRound = financingRound;
    }

    public String getFinancingAmount() {
        return financingAmount;
    }

    public void setFinancingAmount(String financingAmount) {
        this.financingAmount = financingAmount;
    }

    public String getFinancingTime() {
        return financingTime;
    }

    public void setFinancingTime(String financingTime) {
        this.financingTime = financingTime;
    }

    public String getPotentialLabel() {
        return potentialLabel;
    }

    public void setPotentialLabel(String potentialLabel) {
        this.potentialLabel = potentialLabel;
    }

    public String getPhoneNumbers_1() {
        return phoneNumbers_1;
    }

    public void setPhoneNumbers_1(String phoneNumbers_1) {
        this.phoneNumbers_1 = phoneNumbers_1;
    }
}
