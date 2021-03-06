package com.bdaim.customs.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Product {
    private String id;
    private String pid;
    private String bill_no;//分单单号
    /**
     * 主单号
     */
    private String main_bill_no;
    @JsonProperty(value = "CODE_TS")
    private String code_ts;//商品编号
    @JsonProperty(value = "G_NAME")
    private String g_name;//商品名称
    @JsonProperty(value = "G_NAME_EN")
    private String g_name_en;//商品英文名称
    @JsonProperty(value = "G_MODEL")
    private String g_model;//商品规格、型号
    @JsonProperty(value = "ORIGIN_COUNTRY")
    private String origin_country;// 产销国
    private String trade_curr;//成交币制

    @JsonProperty(value = "Trade_total")
    private String trade_total;//成交总价
    @JsonProperty(value = "declPrice")
    private String decl_price;//申报单价
    @JsonProperty(value = "DECL_TOTAL")
    private String decl_total;//申报总价
    @JsonProperty(value = "USE_TO")
    private String use_to;//用途
    @JsonProperty(value = "DUTY_MODE")
    private String duty_mode;//征减免税方式
    @JsonProperty(value = "G_QTY")
    private String g_qty;//申报数量
    @JsonProperty(value = "G_UNIT")
    private String g_unit;//申报单位
    @JsonProperty(value = "QTY_1")
    private String qty_1;//第一（法定）数量
    @JsonProperty(value = "UNIT_1")
    private String unit_1;//第一(法定)计量单位
    @JsonProperty(value = "QTY_2")
    private String qty_2;//第二数量
    @JsonProperty(value = "UNIT_2")
    private String unit_2;//第二计量单位
    @JsonProperty(value = "GGrossWt")
    private String ggrosswt;//商品毛重

    /**
     * 完税价格
     */
    private String duty_paid_price;

    /**
     * 预估税金
     */
    private String estimated_tax;

    /**
     * 税率
     */
    private String tax_rate;

    /**
     * 是否为低价商品 1-低价 0-正常
     */
    private Integer is_low_price;

    /**
     * 添加类型 ADD-添加 APD-追加
     */
    private String opt_type;

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

    public String getBill_no() {
        return bill_no;
    }

    public void setBill_no(String bill_no) {
        this.bill_no = bill_no;
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

    public String getTrade_curr() {
        return trade_curr;
    }

    public void setTrade_curr(String trade_curr) {
        this.trade_curr = trade_curr;
    }

    public String getTrade_total() {
        return trade_total;
    }

    public void setTrade_total(String trade_total) {
        this.trade_total = trade_total;
    }

    public String getDecl_price() {
        return decl_price;
    }

    public void setDecl_price(String decl_price) {
        this.decl_price = decl_price;
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

    public String getGgrosswt() {
        return ggrosswt;
    }

    public void setGgrosswt(String ggrosswt) {
        this.ggrosswt = ggrosswt;
    }

    public String getDuty_paid_price() {
        return duty_paid_price;
    }

    public void setDuty_paid_price(String duty_paid_price) {
        this.duty_paid_price = duty_paid_price;
    }

    public String getEstimated_tax() {
        return estimated_tax;
    }

    public void setEstimated_tax(String estimated_tax) {
        this.estimated_tax = estimated_tax;
    }

    public Integer getIs_low_price() {
        return is_low_price;
    }

    public void setIs_low_price(Integer is_low_price) {
        this.is_low_price = is_low_price;
    }

    public String getOpt_type() {
        return opt_type;
    }

    public void setOpt_type(String opt_type) {
        this.opt_type = opt_type;
    }

    public String getTax_rate() {
        return tax_rate;
    }

    public void setTax_rate(String tax_rate) {
        this.tax_rate = tax_rate;
    }

    public String getMain_bill_no() {
        return main_bill_no;
    }

    public void setMain_bill_no(String main_bill_no) {
        this.main_bill_no = main_bill_no;
    }
}
