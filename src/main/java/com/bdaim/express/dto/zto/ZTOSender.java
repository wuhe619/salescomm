package com.bdaim.express.dto.zto;

import com.bdaim.express.dto.Sender;

public class ZTOSender {
    private String name;
    private String mobile;
    private String phone;
    private String city;
    private String address;
    private String zipcode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public ZTOSender() {
    }

    public ZTOSender(Sender sender) {
        this.name = sender.getName();
        this.mobile = sender.getMobile();
        this.phone = sender.getPhone();
        this.city = sender.getProv() + sender.getCity();
        this.address = sender.getAddress();
        this.zipcode = sender.getPostCode() + "".trim();
    }
}
