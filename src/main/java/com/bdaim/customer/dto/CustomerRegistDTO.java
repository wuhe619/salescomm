package com.bdaim.customer.dto;

import java.io.Serializable;

/**
 * Created by Mr.YinXin on 2017/2/21.
 */
public class CustomerRegistDTO implements Serializable{
    private String userId;
    //企业帐号
    private String name;
    private String password;
    private int userPwdLevel;
    private String enterpriseName;
    private String province;
    private String city;
    private String country;
    private String address;
    private String bliNumber;
    private String bliPath;
    private String taxPayerId;
    private String taxpayerCertificatePath;
    private String bank;
    private String bankAccount;
    private String bankAccountCertificate;
    private String realName;
    private String mobile;
    private String email;
    private String custId;
    private String startTime;
    private String endTime;
    private String dealType;
    private String status;
    //联系人职位
    private String title;
    //所属行业
    private String industry;
    //销售负责人
    private String salePerson;
    //渠道
    private String channel;
    //打印员
    private String printer;
    //封装员
    private String packager;
    //身份证正面
    private String idCardFront;
    //身份证反面
    private String idCardBack;

    public String getIdCardFront() {
        return idCardFront;
    }

    public void setIdCardFront(String idCardFront) {
        this.idCardFront = idCardFront;
    }

    public String getIdCardBack() {
        return idCardBack;
    }

    public void setIdCardBack(String idCardBack) {
        this.idCardBack = idCardBack;
    }

    public String getPrinter() {
        return printer;
    }

    public void setPrinter(String printer) {
        this.printer = printer;
    }

    public String getPackager() {
        return packager;
    }

    public void setPackager(String packager) {
        this.packager = packager;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDealType() {
        return dealType;
    }

    public void setDealType(String dealType) {
        this.dealType = dealType;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getSalePerson() {
        return salePerson;
    }

    public void setSalePerson(String salePerson) {
        this.salePerson = salePerson;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBliNumber() {
        return bliNumber;
    }

    public void setBliNumber(String bliNumber) {
        this.bliNumber = bliNumber;
    }

    public String getBliPath() {
        return bliPath;
    }

    public void setBliPath(String bliPath) {
        this.bliPath = bliPath;
    }

    public String getTaxPayerId() {
        return taxPayerId;
    }

    public void setTaxPayerId(String taxPayerId) {
        this.taxPayerId = taxPayerId;
    }

    public String getTaxpayerCertificatePath() {
        return taxpayerCertificatePath;
    }

    public void setTaxpayerCertificatePath(String taxpayerCertificatePath) {
        this.taxpayerCertificatePath = taxpayerCertificatePath;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankAccountCertificate() {
        return bankAccountCertificate;
    }

    public void setBankAccountCertificate(String bankAccountCertificate) {
        this.bankAccountCertificate = bankAccountCertificate;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
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

    public int getUserPwdLevel() {
        return userPwdLevel;
    }

    public void setUserPwdLevel(int userPwdLevel) {
        this.userPwdLevel = userPwdLevel;
    }
}
