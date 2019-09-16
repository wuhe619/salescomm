package com.bdaim.customs.entity;

public class Product {
     private String party_No;//分单单号
     private String CODE_TS ;//商品编号
	 private String G_NAME ;//商品名称
	 private String G_NAME_EN ;//商品英文名称
	 private String G_MODEL ;//商品规格、型号
	 private String ORIGIN_COUNTRY ;// 产销国
	 private String tradeCurr ;//成交币制
	 private String Trade_total ;//成交总价
	 private String DeclPrice ;//申报单价
	 private String DECL_TOTAL ;//申报总价
	 private String USE_TO ;//用途
	 private String DUTY_MODE ;//征减免税方式
	 private String G_QTY ;//申报数量
	 private String G_UNIT ;//申报计量单位
	 private String QTY_1 ;//第一（法定）数量
	 private String UNIT_1 ;//第一(法定)计量单位
	 private String QTY_2 ;//第二数量
	 private String UNIT_2 ;//第二计量单位
	 private String GGrossWt ;//商品毛重

    public String getParty_No() {
        return party_No;
    }

    public void setParty_No(String party_No) {
        this.party_No = party_No;
    }

    public String getCODE_TS() {
        return CODE_TS;
    }

    public void setCODE_TS(String CODE_TS) {
        this.CODE_TS = CODE_TS;
    }

    public String getG_NAME() {
        return G_NAME;
    }

    public void setG_NAME(String g_NAME) {
        G_NAME = g_NAME;
    }

    public String getG_NAME_EN() {
        return G_NAME_EN;
    }

    public void setG_NAME_EN(String g_NAME_EN) {
        G_NAME_EN = g_NAME_EN;
    }

    public String getG_MODEL() {
        return G_MODEL;
    }

    public void setG_MODEL(String g_MODEL) {
        G_MODEL = g_MODEL;
    }

    public String getORIGIN_COUNTRY() {
        return ORIGIN_COUNTRY;
    }

    public void setORIGIN_COUNTRY(String ORIGIN_COUNTRY) {
        this.ORIGIN_COUNTRY = ORIGIN_COUNTRY;
    }

    public String getTradeCurr() {
        return tradeCurr;
    }

    public void setTradeCurr(String tradeCurr) {
        this.tradeCurr = tradeCurr;
    }

    public String getTrade_total() {
        return Trade_total;
    }

    public void setTrade_total(String trade_total) {
        Trade_total = trade_total;
    }

    public String getDeclPrice() {
        return DeclPrice;
    }

    public void setDeclPrice(String declPrice) {
        DeclPrice = declPrice;
    }

    public String getDECL_TOTAL() {
        return DECL_TOTAL;
    }

    public void setDECL_TOTAL(String DECL_TOTAL) {
        this.DECL_TOTAL = DECL_TOTAL;
    }

    public String getUSE_TO() {
        return USE_TO;
    }

    public void setUSE_TO(String USE_TO) {
        this.USE_TO = USE_TO;
    }

    public String getDUTY_MODE() {
        return DUTY_MODE;
    }

    public void setDUTY_MODE(String DUTY_MODE) {
        this.DUTY_MODE = DUTY_MODE;
    }

    public String getG_QTY() {
        return G_QTY;
    }

    public void setG_QTY(String g_QTY) {
        G_QTY = g_QTY;
    }

    public String getG_UNIT() {
        return G_UNIT;
    }

    public void setG_UNIT(String g_UNIT) {
        G_UNIT = g_UNIT;
    }

    public String getQTY_1() {
        return QTY_1;
    }

    public void setQTY_1(String QTY_1) {
        this.QTY_1 = QTY_1;
    }

    public String getUNIT_1() {
        return UNIT_1;
    }

    public void setUNIT_1(String UNIT_1) {
        this.UNIT_1 = UNIT_1;
    }

    public String getQTY_2() {
        return QTY_2;
    }

    public void setQTY_2(String QTY_2) {
        this.QTY_2 = QTY_2;
    }

    public String getUNIT_2() {
        return UNIT_2;
    }

    public void setUNIT_2(String UNIT_2) {
        this.UNIT_2 = UNIT_2;
    }

    public String getGGrossWt() {
        return GGrossWt;
    }

    public void setGGrossWt(String GGrossWt) {
        this.GGrossWt = GGrossWt;
    }
}
