package com.bdaim.customs.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PartyDan {
    private String id;
    private String pid;
    @JsonProperty(value = "Main_bill_NO")
    private String main_bill_no;//主单号
    @JsonProperty(value = "BILL_NO")
    private String bill_no;//分单号
    @JsonProperty(value = "WEIGHT")
    private String weight;//重量
    @JsonProperty(value = "PACK_NO")
    private String pack_no ;//件数
    @JsonProperty(value = "MainGName")
    private String main_gname ;//主要货物名称
    @JsonProperty(value = "ID_TYPE")
    private String id_type ;// 证件类型
    @JsonProperty(value = "TOTAL_VALUE")
    private String total_value ;//价值
    @JsonProperty(value = "CURR_CODE")
    private String curr_code ;//币制
    @JsonProperty(value = "TRADE_COUNTRY")
    private String trade_country;//贸易国别
    @JsonProperty(value = "ID_NO")
    private String id_no;// 证件号码
    private String id_no_pic ;// 证件照片
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

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getMain_bill_no() {
        return main_bill_no;
    }

    public void setMain_bill_no(String main_bill_no) {
        this.main_bill_no = main_bill_no;
    }

    public String getBill_no() {
        return bill_no;
    }

    public void setBill_no(String bill_no) {
        this.bill_no = bill_no;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getPack_no() {
        return pack_no;
    }

    public void setPack_no(String pack_no) {
        this.pack_no = pack_no;
    }

    public String getMain_gname() {
        return main_gname;
    }

    public void setMain_gname(String main_gname) {
        this.main_gname = main_gname;
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

    public String getId_no() {
        return id_no;
    }

    public void setId_no(String id_no) {
        this.id_no = id_no;
    }

    public String getId_no_pic() {
        return id_no_pic;
    }

    public void setId_no_pic(String id_no_pic) {
        this.id_no_pic = id_no_pic;
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
