package com.bdaim.express.dto.yto;


import com.bdaim.express.dto.Receiver;

public class YTOReceiver {
    private String name;
    private String phone;
    private String mobile;
    private String prov;//省份
    private String city;//城市与区县， 城市与区县用英文逗号隔开
    private String address;//详细地址
    private int postCode;

    public int getPostCode() {
        return postCode;
    }

    public void setPostCode(int postCode) {
        this.postCode = postCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public YTOReceiver(Receiver receiver) {
        this.name = receiver.getName();
        this.phone = receiver.getPhone();
        this.mobile = receiver.getMobile();
        this.prov = receiver.getProv();
        this.city = receiver.getCity();
        this.address = receiver.getAddress();
        this.postCode = receiver.getPostCode();
    }

    public YTOReceiver() {
    }
}
