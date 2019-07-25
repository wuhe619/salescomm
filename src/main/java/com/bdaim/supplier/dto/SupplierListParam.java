package com.bdaim.supplier.dto;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/9/17 17:34
 */
public class SupplierListParam {
    private String supplierId;
    private String name;
    private String person;
    private String phone;

    private String sourceid;
    private String seatPrice;
    private String seaMinutes;
    private String callPrice;
    private String fixPrice;
    private Double fixpriceInsurance;
    private Double fixpriceBank;
    private Double fixpriceOnline;
    private Double fixpriceCourt;
    private String smsPrice;
    private String jdFixPrice;
    //快递价格
    private String expressPrice;
    //1查询所有  2 查询快递的
    private Integer type;
    private String imeiFixPrice;
    private String macFixPrice;

    public String getImeiFixPrice() {
        return imeiFixPrice;
    }

    public void setImeiFixPrice(String imeiFixPrice) {
        this.imeiFixPrice = imeiFixPrice;
    }

    public String getMacFixPrice() {
        return macFixPrice;
    }

    public void setMacFixPrice(String macFixPrice) {
        this.macFixPrice = macFixPrice;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getJdFixPrice() {
        return jdFixPrice;
    }

    public void setJdFixPrice(String jdFixPrice) {
        this.jdFixPrice = jdFixPrice;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSourceid() {
        return sourceid;
    }

    public void setSourceid(String sourceid) {
        this.sourceid = sourceid;
    }

    public String getSeatPrice() {
        return seatPrice;
    }

    public void setSeatPrice(String seatPrice) {
        this.seatPrice = seatPrice;
    }

    public String getSeaMinutes() {
        return seaMinutes;
    }

    public void setSeaMinutes(String seaMinutes) {
        this.seaMinutes = seaMinutes;
    }

    public String getCallPrice() {
        return callPrice;
    }

    public void setCallPrice(String callPrice) {
        this.callPrice = callPrice;
    }

    public String getFixPrice() {
        return fixPrice;
    }

    public void setFixPrice(String fixPrice) {
        this.fixPrice = fixPrice;
    }

    public Double getFixpriceInsurance() {
        return fixpriceInsurance;
    }

    public void setFixpriceInsurance(Double fixpriceInsurance) {
        this.fixpriceInsurance = fixpriceInsurance;
    }

    public Double getFixpriceBank() {
        return fixpriceBank;
    }

    public void setFixpriceBank(Double fixpriceBank) {
        this.fixpriceBank = fixpriceBank;
    }

    public Double getFixpriceOnline() {
        return fixpriceOnline;
    }

    public void setFixpriceOnline(Double fixpriceOnline) {
        this.fixpriceOnline = fixpriceOnline;
    }

    public Double getFixpriceCourt() {
        return fixpriceCourt;
    }

    public void setFixpriceCourt(Double fixpriceCourt) {
        this.fixpriceCourt = fixpriceCourt;
    }

    public String getSmsPrice() {
        return smsPrice;
    }

    public void setSmsPrice(String smsPrice) {
        this.smsPrice = smsPrice;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getExpressPrice() {
        return expressPrice;
    }

    public void setExpressPrice(String expressPrice) {
        this.expressPrice = expressPrice;
    }

    @Override
    public String toString() {
        return "SupplierListParam{" +
                "supplierId='" + supplierId + '\'' +
                ", name='" + name + '\'' +
                ", person='" + person + '\'' +
                ", phone='" + phone + '\'' +
                ", sourceid='" + sourceid + '\'' +
                ", seatPrice='" + seatPrice + '\'' +
                ", seaMinutes='" + seaMinutes + '\'' +
                ", callPrice='" + callPrice + '\'' +
                ", fixPrice='" + fixPrice + '\'' +
                ", fixpriceInsurance=" + fixpriceInsurance +
                ", fixpriceBank=" + fixpriceBank +
                ", fixpriceOnline=" + fixpriceOnline +
                ", fixpriceCourt=" + fixpriceCourt +
                ", smsPrice='" + smsPrice + '\'' +
                ", jdFixPrice='" + jdFixPrice + '\'' +
                ", expressPrice='" + expressPrice + '\'' +
                ", type=" + type +
                '}';
    }
}
