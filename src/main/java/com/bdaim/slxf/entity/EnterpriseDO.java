package com.bdaim.slxf.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Mr.YinXin on 2017/2/21.
 */
@Entity
@Table(name = "t_enterprise", schema = "", catalog = "")
public class EnterpriseDO {
    private String enterpriseId;
    private String custId;
    private String name;
    private String industry;
    private String website;
    private Integer number;
    private String nature;
    private String contactAddress;
    private String bliNumber;
    private String regAddress;
    private String bliPath;
    private Timestamp establishDate;
    private Integer regCapital;
    private String description;
    private Integer status;
    private String taxpayerId;
    private String taxpayerCertificatePath;
    private String bank;
    private String bankAccount;
    private String bankAccountCertificate;
    private String province;
    private String city;
    private String county;
    private String state;
    private Timestamp createTime;
    private Timestamp modifyTime;

    @Id
    @Column(name = "enterprise_id")
    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    @Basic
    @Column(name = "cust_id")
    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    @Basic
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "industry")
    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    @Basic
    @Column(name = "website")
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Basic
    @Column(name = "number")
    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    @Basic
    @Column(name = "nature")
    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    @Basic
    @Column(name = "contact_address")
    public String getContactAddress() {
        return contactAddress;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    @Basic
    @Column(name = "bli_number")
    public String getBliNumber() {
        return bliNumber;
    }

    public void setBliNumber(String bliNumber) {
        this.bliNumber = bliNumber;
    }

    @Basic
    @Column(name = "reg_address")
    public String getRegAddress() {
        return regAddress;
    }

    public void setRegAddress(String regAddress) {
        this.regAddress = regAddress;
    }

    @Basic
    @Column(name = "bli_path")
    public String getBliPath() {
        return bliPath;
    }

    public void setBliPath(String bliPath) {
        this.bliPath = bliPath;
    }

    @Basic
    @Column(name = "establish_date")
    public Timestamp getEstablishDate() {
        return establishDate;
    }

    public void setEstablishDate(Timestamp establishDate) {
        this.establishDate = establishDate;
    }

    @Basic
    @Column(name = "reg_capital")
    public Integer getRegCapital() {
        return regCapital;
    }

    public void setRegCapital(Integer regCapital) {
        this.regCapital = regCapital;
    }

    @Basic
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "STATUS")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "taxpayer_id")
    public String getTaxpayerId() {
        return taxpayerId;
    }

    public void setTaxpayerId(String taxpayerId) {
        this.taxpayerId = taxpayerId;
    }

    @Basic
    @Column(name = "taxpayer_certificate_path")
    public String getTaxpayerCertificatePath() {
        return taxpayerCertificatePath;
    }

    public void setTaxpayerCertificatePath(String taxpayerCertificatePath) {
        this.taxpayerCertificatePath = taxpayerCertificatePath;
    }

    @Basic
    @Column(name = "bank")
    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    @Basic
    @Column(name = "bank_account")
    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    @Basic
    @Column(name = "bank_account_certificate")
    public String getBankAccountCertificate() {
        return bankAccountCertificate;
    }

    public void setBankAccountCertificate(String bankAccountCertificate) {
        this.bankAccountCertificate = bankAccountCertificate;
    }

    @Basic
    @Column(name = "province")
    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    @Basic
    @Column(name = "city")
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Basic
    @Column(name = "county")
    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    @Basic
    @Column(name = "state")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Basic
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "modify_time")
    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnterpriseDO that = (EnterpriseDO) o;

        if (enterpriseId != null ? !enterpriseId.equals(that.enterpriseId) : that.enterpriseId != null) return false;
        if (custId != null ? !custId.equals(that.custId) : that.custId != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (industry != null ? !industry.equals(that.industry) : that.industry != null) return false;
        if (website != null ? !website.equals(that.website) : that.website != null) return false;
        if (number != null ? !number.equals(that.number) : that.number != null) return false;
        if (nature != null ? !nature.equals(that.nature) : that.nature != null) return false;
        if (contactAddress != null ? !contactAddress.equals(that.contactAddress) : that.contactAddress != null)
            return false;
        if (bliNumber != null ? !bliNumber.equals(that.bliNumber) : that.bliNumber != null) return false;
        if (regAddress != null ? !regAddress.equals(that.regAddress) : that.regAddress != null) return false;
        if (bliPath != null ? !bliPath.equals(that.bliPath) : that.bliPath != null) return false;
        if (establishDate != null ? !establishDate.equals(that.establishDate) : that.establishDate != null)
            return false;
        if (regCapital != null ? !regCapital.equals(that.regCapital) : that.regCapital != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (taxpayerId != null ? !taxpayerId.equals(that.taxpayerId) : that.taxpayerId != null) return false;
        if (taxpayerCertificatePath != null ? !taxpayerCertificatePath.equals(that.taxpayerCertificatePath) : that.taxpayerCertificatePath != null)
            return false;
        if (bank != null ? !bank.equals(that.bank) : that.bank != null) return false;
        if (bankAccount != null ? !bankAccount.equals(that.bankAccount) : that.bankAccount != null) return false;
        if (bankAccountCertificate != null ? !bankAccountCertificate.equals(that.bankAccountCertificate) : that.bankAccountCertificate != null)
            return false;
        if (province != null ? !province.equals(that.province) : that.province != null) return false;
        if (city != null ? !city.equals(that.city) : that.city != null) return false;
        if (county != null ? !county.equals(that.county) : that.county != null) return false;
        if (state != null ? !state.equals(that.state) : that.state != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        if (modifyTime != null ? !modifyTime.equals(that.modifyTime) : that.modifyTime != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = enterpriseId != null ? enterpriseId.hashCode() : 0;
        result = 31 * result + (custId != null ? custId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (industry != null ? industry.hashCode() : 0);
        result = 31 * result + (website != null ? website.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (nature != null ? nature.hashCode() : 0);
        result = 31 * result + (contactAddress != null ? contactAddress.hashCode() : 0);
        result = 31 * result + (bliNumber != null ? bliNumber.hashCode() : 0);
        result = 31 * result + (regAddress != null ? regAddress.hashCode() : 0);
        result = 31 * result + (bliPath != null ? bliPath.hashCode() : 0);
        result = 31 * result + (establishDate != null ? establishDate.hashCode() : 0);
        result = 31 * result + (regCapital != null ? regCapital.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (taxpayerId != null ? taxpayerId.hashCode() : 0);
        result = 31 * result + (taxpayerCertificatePath != null ? taxpayerCertificatePath.hashCode() : 0);
        result = 31 * result + (bank != null ? bank.hashCode() : 0);
        result = 31 * result + (bankAccount != null ? bankAccount.hashCode() : 0);
        result = 31 * result + (bankAccountCertificate != null ? bankAccountCertificate.hashCode() : 0);
        result = 31 * result + (province != null ? province.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (county != null ? county.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (modifyTime != null ? modifyTime.hashCode() : 0);
        return result;
    }
}
