package com.bdaim.crm;

import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * 企业基本信息
 */
public class EntInfo {
    @Excel(name = "企业名称", orderNum = "0")
    private String entName;
    @Excel(name = "法定代表人", orderNum = "1")
    private String legalName;
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
    private  String fromTime;
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
    @Excel(name = "更多电话", orderNum = "21")
    private String phoneNumbers;
    @Excel(name = "邮箱", orderNum = "22")
    private String email;
    private Long id;

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
}
