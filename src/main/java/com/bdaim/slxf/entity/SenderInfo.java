package com.bdaim.slxf.entity;

/**
 * @author wangxx@bdaim.com
 * @Description:
 * @date 2018/12/27 10:31
 */
public class SenderInfo {
    private String id;
    private String cust_id;
    private String sender_name;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String address;
    private String postcodes;
    private Integer status;
    private Integer type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCust_id() {
        return cust_id;
    }

    public void setCust_id(String cust_id) {
        this.cust_id = cust_id;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostcodes() {
        return postcodes;
    }

    public void setPostcodes(String postcodes) {
        this.postcodes = postcodes;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public SenderInfo(String id, String cust_id, String sender_name, String phone, String province, String city, String district, String address, String postcodes, Integer status, Integer type) {
        this.id = id;
        this.cust_id = cust_id;
        this.sender_name = sender_name;
        this.phone = phone;
        this.province = province;
        this.city = city;
        this.district = district;
        this.address = address;
        this.postcodes = postcodes;
        this.status = status;
        this.type = type;
    }

    public SenderInfo() {
    }
}
