package com.bdaim.crm;

import java.util.Date;
import java.util.List;

/**
 * 企业基本信息
 */
public class EntDataEntity {

    /**
     * 企业ID
     */
    private String id;
    /**
     * 企业名称
     */
    private String entName;

    /**
     * 曾用名
     */
    private String otherName;

    /**
     * 英文名称
     */
    private String entEnName;
    /**
     * 法定代表人
     */
    private String legalName;
    /**
     * 注册资金
     */
    private String regCap;

    /**
     * 注册资金单位(元/万元)
     */
    private String regCapUnit;
    /**
     * 注册资金币种
     */
    private String regCapCur;
    /**
     * 成立日期
     */
    private Date establishTime;
    /**
     * 注吊销日期
     */
    private Date cancelDate;
    /**
     * 审核日期
     */
    private Date auditDate;
    /**
     * 经营状态
     */
    private String entStatus;
    /**
     * 工商注册号
     */
    private String regNo;
    /**
     * 机构组织代码
     */
    private String orgNo;
    /**
     * 统一社会信用代码
     */
    private String creditCode;
    /**
     * 纳税人识别号
     */
    private String taxpayerNo;
    /**
     * 企业类型代码
     */
    private String entTypeCode;
    /**
     * 企业类型
     */
    private String entType;
    /**
     * 国际行业代码
     */
    private String industryCode;
    /**
     * 行业名称
     */
    private String industry;
    /**
     * 公司介绍
     */
    private String entIntroduction;
    /**
     * 营业期限开始时间
     */
    private Date fromTime;
    /**
     * 营业期限结束时间
     */
    private Date toTime;
    /**
     * 核准日期
     */
    private Date approvedTime;
    /**
     * 登记机关
     */
    private String regInstitute;
    /**
     * 省份代码
     */
    private String regProvinceCode;
    /**
     * 所属地区
     */
    private String regProvinceName;
    /**
     * 详细地址
     */
    private String regLocation;
    /**
     * 注册城市代码
     */
    private String regCityCode;
    /**
     * 注册地址城市
     */
    private String regCityName;
    /**
     * 经营范围
     */
    private String opScope;
    /**
     * 手机号
     */
    private List<PhoneEntity> phoneNumbers;

    /**
     * 固话
     */
    private List<TelPhoneEntity> telPhoneNumbers;
    /**
     * 邮箱
     */
    private List<EmailEntity> email;

    /**
     * 网站
     */
    private List<EntWebEntity> web;
    /**
     * 专利
     */
    private List<PatentEntity> patents;
    /**
     * 商标
     */
    private List<TrademarkEntity> trademarks;
    /**
     * 联系人
     */
    private List<ContactsEntity> contactMan;

    private Long createTime;
    private Long updateTime;

    private String s_tag;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntName() {
        return entName;
    }

    public void setEntName(String entName) {
        this.entName = entName;
    }

    public String getOtherName() {
        return otherName;
    }

    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

    public String getEntEnName() {
        return entEnName;
    }

    public void setEntEnName(String entEnName) {
        this.entEnName = entEnName;
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

    public String getRegCapUnit() {
        return regCapUnit;
    }

    public void setRegCapUnit(String regCapUnit) {
        this.regCapUnit = regCapUnit;
    }

    public String getRegCapCur() {
        return regCapCur;
    }

    public void setRegCapCur(String regCapCur) {
        this.regCapCur = regCapCur;
    }

    public Date getEstablishTime() {
        return establishTime;
    }

    public void setEstablishTime(Date establishTime) {
        this.establishTime = establishTime;
    }

    public Date getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(Date cancelDate) {
        this.cancelDate = cancelDate;
    }

    public Date getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(Date auditDate) {
        this.auditDate = auditDate;
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

    public String getEntIntroduction() {
        return entIntroduction;
    }

    public void setEntIntroduction(String entIntroduction) {
        this.entIntroduction = entIntroduction;
    }

    public Date getFromTime() {
        return fromTime;
    }

    public void setFromTime(Date fromTime) {
        this.fromTime = fromTime;
    }

    public Date getToTime() {
        return toTime;
    }

    public void setToTime(Date toTime) {
        this.toTime = toTime;
    }

    public Date getApprovedTime() {
        return approvedTime;
    }

    public void setApprovedTime(Date approvedTime) {
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

    public List<PhoneEntity> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<PhoneEntity> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public List<TelPhoneEntity> getTelPhoneNumbers() {
        return telPhoneNumbers;
    }

    public void setTelPhoneNumbers(List<TelPhoneEntity> telPhoneNumbers) {
        this.telPhoneNumbers = telPhoneNumbers;
    }

    public List<EmailEntity> getEmail() {
        return email;
    }

    public void setEmail(List<EmailEntity> email) {
        this.email = email;
    }

    public List<EntWebEntity> getWeb() {
        return web;
    }

    public void setWeb(List<EntWebEntity> web) {
        this.web = web;
    }

    public List<PatentEntity> getPatents() {
        return patents;
    }

    public void setPatents(List<PatentEntity> patents) {
        this.patents = patents;
    }

    public List<TrademarkEntity> getTrademarks() {
        return trademarks;
    }

    public void setTrademarks(List<TrademarkEntity> trademarks) {
        this.trademarks = trademarks;
    }

    public List<ContactsEntity> getContactMan() {
        return contactMan;
    }

    public void setContactMan(List<ContactsEntity> contactMan) {
        this.contactMan = contactMan;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getS_tag() {
        return s_tag;
    }

    public void setS_tag(String s_tag) {
        this.s_tag = s_tag;
    }
}
