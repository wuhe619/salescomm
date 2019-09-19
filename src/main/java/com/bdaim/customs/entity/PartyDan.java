package com.bdaim.customs.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PartyDan {
    private String id;
    @JsonProperty(value = "Main_bill_NO")
    private String main_bill_NO;//主单号
    @JsonProperty(value = "BILL_NO")
    private String bill_NO;//分单号
    @JsonProperty(value = "WEIGHT")
    private String weight;//重量
    @JsonProperty(value = "PACK_NO")
    private String pack_NO ;//件数
    @JsonProperty(value = "MainGName")
    private String main_gName ;//主要货物名称
    @JsonProperty(value = "ID_TYPE")
    private String id_type ;// 证件类型
    @JsonProperty(value = "TOTAL_VALUE")
    private String total_value ;//价值
    @JsonProperty(value = "CURR_CODE")
    private String curr_code ;//币制
    @JsonProperty(value = "TRADE_COUNTRY")
    private String trade_country;//贸易国别
    @JsonProperty(value = "ID_NO")
    private String id_NO ;// 证件号码
    @JsonProperty(value = "RECEIVE_NAME")
    private String receive_name ;// 收件人
    @JsonProperty(value = "RECEIVE_TEL")
    private String receive_tel ;//收件人电话
    @JsonProperty(value = "RECEIVE_ADDRESS")
    private String receive_address;// 收件人地址
    @JsonProperty(value = "RECEIVE_PRO")
    private String receive_pro;// 收件人省份
    @JsonProperty(value = "RECEIVE_CITY")
    private String receive_city ;// 收件人城市
    @JsonProperty(value = "RECEIVE_COUNTRY")
    private String receive_country ;//收件人国别
    @JsonProperty(value = "PRODUCTS")
    private List<Product> products;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMain_bill_NO() {
        return main_bill_NO;
    }

    public void setMain_bill_NO(String main_bill_NO) {
        this.main_bill_NO = main_bill_NO;
    }

    public String getBill_NO() {
        return bill_NO;
    }

    public void setBill_NO(String bill_NO) {
        this.bill_NO = bill_NO;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getPack_NO() {
        return pack_NO;
    }

    public void setPack_NO(String pack_NO) {
        this.pack_NO = pack_NO;
    }

    public String getMain_gName() {
        return main_gName;
    }

    public void setMain_gName(String main_gName) {
        this.main_gName = main_gName;
    }

    public String getId_type() {
        return id_type;
    }

    public void setId_type(String id_type) {
        this.id_type = id_type;
    }

    public String getTotal_value() {
        return total_value;
    }

    public void setTotal_value(String total_value) {
        this.total_value = total_value;
    }

    public String getCurr_code() {
        return curr_code;
    }

    public void setCurr_code(String curr_code) {
        this.curr_code = curr_code;
    }

    public String getTrade_country() {
        return trade_country;
    }

    public void setTrade_country(String trade_country) {
        this.trade_country = trade_country;
    }

    public String getId_NO() {
        return id_NO;
    }

    public void setId_NO(String id_NO) {
        this.id_NO = id_NO;
    }

    public String getReceive_name() {
        return receive_name;
    }

    public void setReceive_name(String receive_name) {
        this.receive_name = receive_name;
    }

    public String getReceive_tel() {
        return receive_tel;
    }

    public void setReceive_tel(String receive_tel) {
        this.receive_tel = receive_tel;
    }

    public String getReceive_address() {
        return receive_address;
    }

    public void setReceive_address(String receive_address) {
        this.receive_address = receive_address;
    }

    public String getReceive_pro() {
        return receive_pro;
    }

    public void setReceive_pro(String receive_pro) {
        this.receive_pro = receive_pro;
    }

    public String getReceive_city() {
        return receive_city;
    }

    public void setReceive_city(String receive_city) {
        this.receive_city = receive_city;
    }

    public String getReceive_country() {
        return receive_country;
    }

    public void setReceive_country(String receive_country) {
        this.receive_country = receive_country;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
