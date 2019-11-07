package com.bdaim.express.dto.zto;

import com.bdaim.express.dto.Receiver;

public class ZTOReceiver {
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

    public ZTOReceiver() {
    }

    public ZTOReceiver(Receiver receiver) {
        this.name = receiver.getName();
        this.mobile = receiver.getMobile();
        this.phone = receiver.getPhone();
        this.city = receiver.getProv()+receiver.getCity();
        this.address = receiver.getAddress();
        this.zipcode = receiver.getPostCode()+"".trim();
    }
}
