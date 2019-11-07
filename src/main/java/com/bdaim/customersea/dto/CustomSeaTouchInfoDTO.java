package com.bdaim.customersea.dto;

import java.io.Serializable;
import java.util.Map;

public class CustomSeaTouchInfoDTO implements Serializable {

    private String voice_info_id;
    private String cust_id;
    private String user_id;
    private String cust_group_id;
    private String super_id;
    private String super_name;
    private String super_age;
    private String super_sex;
    private String super_telphone;
    private String super_phone;
    private String super_address_province_city;
    private String super_address_street;
    /**
     * 公海ID
     */
    private String customerSeaId;
    /**
     * 自建属性值
     */
    private Map<String, Object> superData;

    private String email;
    private String profession;
    private String weChat;
    private String company;
    private String qq;
    /**
     * 跟进状态
     */
    private String followStatus;
    /**
     * 无线原因
     */
    private String invalidReason;

    private String regLocation;
    private String regCapital;
    private String regTime;
    private String regStatus;
    private Integer entPersonNum;

    public CustomSeaTouchInfoDTO() {
        super();
    }

    public CustomSeaTouchInfoDTO(String voice_info_id, String cust_id, String user_id, String cust_group_id, String super_id,
                                 String super_name, String super_age, String super_sex, String super_telphone, String super_phone,
                                 String super_address_province_city, String super_address_street) {
        super();
        this.voice_info_id = voice_info_id;
        this.cust_id = cust_id;
        this.user_id = user_id;
        this.cust_group_id = cust_group_id;
        this.super_id = super_id;
        this.super_name = super_name;
        this.super_age = super_age;
        this.super_sex = super_sex;
        this.super_telphone = super_telphone;
        this.super_phone = super_phone;
        this.super_address_province_city = super_address_province_city;
        this.super_address_street = super_address_street;
    }

    public CustomSeaTouchInfoDTO(String voice_info_id, String cust_id, String user_id, String cust_group_id, String super_id,
                                 String super_name, String super_age, String super_sex, String super_telphone, String super_phone,
                                 String super_address_province_city, String super_address_street, String customerSeaId,
                                 Map<String, Object> superData, String qq, String email, String profession, String weChat, String followStatus, String invalidReason, String company) {
        super();
        this.voice_info_id = voice_info_id;
        this.cust_id = cust_id;
        this.user_id = user_id;
        this.cust_group_id = cust_group_id;
        this.super_id = super_id;
        this.super_name = super_name;
        this.super_age = super_age;
        this.super_sex = super_sex;
        this.super_telphone = super_telphone;
        this.super_phone = super_phone;
        this.super_address_province_city = super_address_province_city;
        this.super_address_street = super_address_street;
        this.customerSeaId = customerSeaId;
        this.superData = superData;
        this.email = email;
        this.profession = profession;
        this.weChat = weChat;
        this.company = company;
        this.qq = qq;
        this.followStatus = followStatus;
        this.invalidReason = invalidReason;
    }

    public String getVoice_info_id() {
        return voice_info_id;
    }

    public void setVoice_info_id(String voice_info_id) {
        this.voice_info_id = voice_info_id;
    }

    public String getCust_id() {
        return cust_id;
    }

    public void setCust_id(String cust_id) {
        this.cust_id = cust_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCust_group_id() {
        return cust_group_id;
    }

    public void setCust_group_id(String cust_group_id) {
        this.cust_group_id = cust_group_id;
    }

    public String getSuper_id() {
        return super_id;
    }

    public void setSuper_id(String super_id) {
        this.super_id = super_id;
    }

    public String getSuper_name() {
        return super_name;
    }

    public void setSuper_name(String super_name) {
        this.super_name = super_name;
    }

    public String getSuper_age() {
        return super_age;
    }

    public void setSuper_age(String super_age) {
        this.super_age = super_age;
    }

    public String getSuper_sex() {
        return super_sex;
    }

    public void setSuper_sex(String super_sex) {
        this.super_sex = super_sex;
    }

    public String getSuper_telphone() {
        return super_telphone;
    }

    public void setSuper_telphone(String super_telphone) {
        this.super_telphone = super_telphone;
    }

    public String getSuper_phone() {
        return super_phone;
    }

    public void setSuper_phone(String super_phone) {
        this.super_phone = super_phone;
    }

    public String getSuper_address_province_city() {
        return super_address_province_city;
    }

    public void setSuper_address_province_city(String super_address_province_city) {
        this.super_address_province_city = super_address_province_city;
    }

    public String getSuper_address_street() {
        return super_address_street;
    }

    public void setSuper_address_street(String super_address_street) {
        this.super_address_street = super_address_street;
    }

    public String getCustomerSeaId() {
        return customerSeaId;
    }

    public void setCustomerSeaId(String customerSeaId) {
        this.customerSeaId = customerSeaId;
    }

    public Map<String, Object> getSuperData() {
        return superData;
    }

    public void setSuperData(Map<String, Object> superData) {
        this.superData = superData;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getWeChat() {
        return weChat;
    }

    public void setWeChat(String weChat) {
        this.weChat = weChat;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getFollowStatus() {
        return followStatus;
    }

    public void setFollowStatus(String followStatus) {
        this.followStatus = followStatus;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }

    public String getRegLocation() {
        return regLocation;
    }

    public void setRegLocation(String regLocation) {
        this.regLocation = regLocation;
    }

    public String getRegCapital() {
        return regCapital;
    }

    public void setRegCapital(String regCapital) {
        this.regCapital = regCapital;
    }

    public String getRegTime() {
        return regTime;
    }

    public void setRegTime(String regTime) {
        this.regTime = regTime;
    }

    public String getRegStatus() {
        return regStatus;
    }

    public void setRegStatus(String regStatus) {
        this.regStatus = regStatus;
    }

    public Integer getEntPersonNum() {
        return entPersonNum;
    }

    public void setEntPersonNum(Integer entPersonNum) {
        this.entPersonNum = entPersonNum;
    }

    @Override
    public String toString() {
        return "CustomSeaTouchInfoDTO{" +
                "voice_info_id='" + voice_info_id + '\'' +
                ", cust_id='" + cust_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", cust_group_id='" + cust_group_id + '\'' +
                ", super_id='" + super_id + '\'' +
                ", super_name='" + super_name + '\'' +
                ", super_age='" + super_age + '\'' +
                ", super_sex='" + super_sex + '\'' +
                ", super_telphone='" + super_telphone + '\'' +
                ", super_phone='" + super_phone + '\'' +
                ", super_address_province_city='" + super_address_province_city + '\'' +
                ", super_address_street='" + super_address_street + '\'' +
                ", customerSeaId='" + customerSeaId + '\'' +
                ", superData=" + superData +
                ", email='" + email + '\'' +
                ", profession='" + profession + '\'' +
                ", weChat='" + weChat + '\'' +
                ", company='" + company + '\'' +
                ", qq='" + qq + '\'' +
                ", followStatus='" + followStatus + '\'' +
                ", invalidReason='" + invalidReason + '\'' +
                ", regLocation='" + regLocation + '\'' +
                ", regCapital='" + regCapital + '\'' +
                ", regTime='" + regTime + '\'' +
                ", regStatus='" + regStatus + '\'' +
                ", entPersonNum=" + entPersonNum +
                '}';
    }
}
