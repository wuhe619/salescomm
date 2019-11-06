package com.bdaim.express.dto;

import javax.validation.constraints.NotBlank;

public class Sender {
    private String name;
    private String phone;
    private String mobile;
    @NotBlank(message = "prov参数不能为空")
    private String prov;//省份
    @NotBlank(message = "city参数不能为空")
    private String city;//城市与区县， 城市与区县用英文逗号隔开
    @NotBlank(message = "address参数不能为空")
    private String address;//详细地址
    private int postCode;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPostCode() {
        return postCode;
    }

    public void setPostCode(int postCode) {
        this.postCode = postCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getProv() {
        return prov;
    }

    public void setProv(String prov) {
        this.prov = prov;
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
}
