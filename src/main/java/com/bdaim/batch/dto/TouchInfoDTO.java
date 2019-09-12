package com.bdaim.batch.dto;

import java.io.Serializable;

public class TouchInfoDTO implements Serializable {

    private String voice_info_id;
    private String cust_id;
    private String user_id;
    private String cust_group_id;
    private String super_id;
    private String super_name;
    private String super_age;
    private String super_sex; //(男1，女2)
    private String super_telphone;
    private String super_phone;
    private String super_address_province_city;
    private String super_address_street;
    private String bantch_id;
    private String labelData;
    private String market_task_id;

    public TouchInfoDTO() {
        super();
        // TODO Auto-generated constructor stub
    }

    public TouchInfoDTO(String voice_info_id, String cust_id, String user_id, String cust_group_id, String super_id, String super_name, String super_age, String super_sex, String super_telphone, String super_phone, String super_address_province_city, String super_address_street, String bantch_id) {
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
        this.bantch_id = bantch_id;
    }

    public String getBantch_id() {
        return bantch_id;
    }

    public void setBantch_id(String bantch_id) {
        this.bantch_id = bantch_id;
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

    public String getLabelData() {
        return labelData;
    }

    public void setLabelData(String labelData) {
        this.labelData = labelData;
    }

    public String getMarket_task_id() {
        return market_task_id;
    }

    public void setMarket_task_id(String market_task_id) {
        this.market_task_id = market_task_id;
    }

    @Override
    public String toString() {
        return "TouchInfoDTO{" +
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
                ", bantch_id='" + bantch_id + '\'' +
                ", labelData='" + labelData + '\'' +
                ", market_task_id='" + market_task_id + '\'' +
                '}';
    }
}
