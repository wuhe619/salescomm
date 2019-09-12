package com.bdaim.rbac.dto;

import java.io.Serializable;

/**
 * 
 */
public class RegisterDTO implements Serializable{
    private String userId;
    private String userName;
    private String password;
    private int userPwdLevel;
    private String enterpriseName;
    private String province;
    private String city;
    private String county;
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
    private String title;
    private int source;

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

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }
}
