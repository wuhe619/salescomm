package com.bdaim.customs.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Product {
    private String id ;
     private String party_No;//分单单号
    @JsonProperty(value = "CODE_TS")
     private String code_ts ;//商品编号
    @JsonProperty(value = "G_NAME")
	 private String g_name ;//商品名称
    @JsonProperty(value = "G_NAME_EN")
	 private String g_name_en ;//商品英文名称
    @JsonProperty(value = "G_MODEL")
	 private String g_model ;//商品规格、型号
    @JsonProperty(value = "ORIGIN_COUNTRY")
	 private String origin_country;// 产销国
	 private String tradeCurr ;//成交币制

    @JsonProperty(value = "Trade_total")
	 private String trade_total ;//成交总价
    @JsonProperty(value = "declPrice")
	 private String declPrice ;//申报单价
    @JsonProperty(value = "DECL_TOTAL")
	 private String decl_total;//申报总价
    @JsonProperty(value = "USE_TO")
	 private String  use_to;//用途
    @JsonProperty(value = "DUTY_MODE")
	 private String duty_mode;//征减免税方式
    @JsonProperty(value = "G_QTY")
	 private String g_qty;//申报数量
    @JsonProperty(value = "G_UNIT")
	 private String g_unit;//申报计量单位
    @JsonProperty(value = "QTY_1")
	 private String qty_1;//第一（法定）数量
    @JsonProperty(value = "UNIT_1")
	 private String unit_1;//第一(法定)计量单位
    @JsonProperty(value = "QTY_2")
	 private String qty_2;//第二数量
    @JsonProperty(value = "UNIT_2")
	 private String unit_2 ;//第二计量单位
    @JsonProperty(value = "GGrossWt")
	 private String ggrossWt ;//商品毛重

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParty_No() {
        return party_No;
    }

    public void setParty_No(String party_No) {
        this.party_No = party_No;
    }

    public String getCode_ts() {
        return code_ts;
    }

    public void setCode_ts(String code_ts) {
        this.code_ts = code_ts;
    }

    public String getG_name() {
        return g_name;
    }

    public void setG_name(String g_name) {
        this.g_name = g_name;
    }

    public String getG_name_en() {
        return g_name_en;
    }

    public void setG_name_en(String g_name_en) {
        this.g_name_en = g_name_en;
    }

    public String getG_model() {
        return g_model;
    }

    public void setG_model(String g_model) {
        this.g_model = g_model;
    }

    public String getOrigin_country() {
        return origin_country;
    }

    public void setOrigin_country(String origin_country) {
        this.origin_country = origin_country;
    }

    public String getTradeCurr() {
        return tradeCurr;
    }

    public void setTradeCurr(String tradeCurr) {
        this.tradeCurr = tradeCurr;
    }

    public String getTrade_total() {
        return trade_total;
    }

    public void setTrade_total(String trade_total) {
        this.trade_total = trade_total;
    }

    public String getDeclPrice() {
        return declPrice;
    }

    public void setDeclPrice(String declPrice) {
        this.declPrice = declPrice;
    }

    public String getDecl_total() {
        return decl_total;
    }

    public void setDecl_total(String decl_total) {
        this.decl_total = decl_total;
    }

    public String getUse_to() {
        return use_to;
    }

    public void setUse_to(String use_to) {
        this.use_to = use_to;
    }

    public String getDuty_mode() {
        return duty_mode;
    }

    public void setDuty_mode(String duty_mode) {
        this.duty_mode = duty_mode;
    }

    public String getG_qty() {
        return g_qty;
    }

    public void setG_qty(String g_qty) {
        this.g_qty = g_qty;
    }

    public String getG_unit() {
        return g_unit;
    }

    public void setG_unit(String g_unit) {
        this.g_unit = g_unit;
    }

    public String getQty_1() {
        return qty_1;
    }

    public void setQty_1(String qty_1) {
        this.qty_1 = qty_1;
    }

    public String getUnit_1() {
        return unit_1;
    }

    public void setUnit_1(String unit_1) {
        this.unit_1 = unit_1;
    }

    public String getQty_2() {
        return qty_2;
    }

    public void setQty_2(String qty_2) {
        this.qty_2 = qty_2;
    }

    public String getUnit_2() {
        return unit_2;
    }

    public void setUnit_2(String unit_2) {
        this.unit_2 = unit_2;
    }

    public String getGgrossWt() {
        return ggrossWt;
    }

    public void setGgrossWt(String ggrossWt) {
        this.ggrossWt = ggrossWt;
    }
}
