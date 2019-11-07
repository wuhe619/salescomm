package com.bdaim.express.dto.yto;


import com.bdaim.express.dto.Sender;

public class YTOSender {
    private String name;
    private String phone;
    private String mobile;
    private String prov;//省份
    private String city;//城市与区县， 城市与区县用英文逗号隔开
    private String address;//详细地址
    private int postCode;

    public String getName() {
        return name;
    }

    public int getPostCode() {
        return postCode;
    }

    public void setPostCode(int postCode) {
        this.postCode = postCode;
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


    public YTOSender() {
    }

    public YTOSender(Sender sender) {
//        this.name = sender.getSenderName()==null?sender.getName():sender.getSenderName();
        this.name=sender.getName();
        this.phone = sender.getPhone();
        this.mobile = sender.getMobile();
        this.prov = sender.getProv();
        this.city = sender.getCity();
        this.address = sender.getAddress();
        this.postCode=sender.getPostCode();
    }
}
