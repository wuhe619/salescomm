package com.bdaim.customs.entity;

import java.util.List;

public class PartyDan {
    private String Main_bill_NO;//主单号
    private String BILL_NO;//分单号
    private String WEIGHT;//重量
    private String PACK_NO ;//件数
    private String MainGName ;//主要货物名称
    private String ID_TYPE ;// 证件类型
    private String TOTAL_VALUE ;//价值
    private String CURR_CODE ;//币制
    private String TRADE_COUNTRY ;//贸易国别
    private String ID_NO ;// 证件号码
    private String RECEIVE_NAME ;// 收件人
    private String RECEIVE_TEL ;//收件人电话
    private String RECEIVE_ADDRESS ;// 收件人地址
    private String RECEIVE_PRO ;// 收件人省份
    private String RECEIVE_CITY ;// 收件人城市
    private String RECEIVE_COUNTRY ;//收件人国别
    private List<Product> PRODUCTS;

    public String getMain_bill_NO() {
        return Main_bill_NO;
    }

    public void setMain_bill_NO(String main_bill_NO) {
        Main_bill_NO = main_bill_NO;
    }

    public String getBILL_NO() {
        return BILL_NO;
    }

    public void setBILL_NO(String BILL_NO) {
        this.BILL_NO = BILL_NO;
    }

    public String getWEIGHT() {
        return WEIGHT;
    }

    public void setWEIGHT(String WEIGHT) {
        this.WEIGHT = WEIGHT;
    }

    public String getPACK_NO() {
        return PACK_NO;
    }

    public void setPACK_NO(String PACK_NO) {
        this.PACK_NO = PACK_NO;
    }

    public String getMainGName() {
        return MainGName;
    }

    public void setMainGName(String mainGName) {
        MainGName = mainGName;
    }

    public String getID_TYPE() {
        return ID_TYPE;
    }

    public void setID_TYPE(String ID_TYPE) {
        this.ID_TYPE = ID_TYPE;
    }

    public String getTOTAL_VALUE() {
        return TOTAL_VALUE;
    }

    public void setTOTAL_VALUE(String TOTAL_VALUE) {
        this.TOTAL_VALUE = TOTAL_VALUE;
    }

    public String getCURR_CODE() {
        return CURR_CODE;
    }

    public void setCURR_CODE(String CURR_CODE) {
        this.CURR_CODE = CURR_CODE;
    }

    public String getTRADE_COUNTRY() {
        return TRADE_COUNTRY;
    }

    public void setTRADE_COUNTRY(String TRADE_COUNTRY) {
        this.TRADE_COUNTRY = TRADE_COUNTRY;
    }

    public String getID_NO() {
        return ID_NO;
    }

    public void setID_NO(String ID_NO) {
        this.ID_NO = ID_NO;
    }

    public String getRECEIVE_NAME() {
        return RECEIVE_NAME;
    }

    public void setRECEIVE_NAME(String RECEIVE_NAME) {
        this.RECEIVE_NAME = RECEIVE_NAME;
    }

    public String getRECEIVE_TEL() {
        return RECEIVE_TEL;
    }

    public void setRECEIVE_TEL(String RECEIVE_TEL) {
        this.RECEIVE_TEL = RECEIVE_TEL;
    }

    public String getRECEIVE_ADDRESS() {
        return RECEIVE_ADDRESS;
    }

    public void setRECEIVE_ADDRESS(String RECEIVE_ADDRESS) {
        this.RECEIVE_ADDRESS = RECEIVE_ADDRESS;
    }

    public String getRECEIVE_PRO() {
        return RECEIVE_PRO;
    }

    public void setRECEIVE_PRO(String RECEIVE_PRO) {
        this.RECEIVE_PRO = RECEIVE_PRO;
    }

    public String getRECEIVE_CITY() {
        return RECEIVE_CITY;
    }

    public void setRECEIVE_CITY(String RECEIVE_CITY) {
        this.RECEIVE_CITY = RECEIVE_CITY;
    }

    public String getRECEIVE_COUNTRY() {
        return RECEIVE_COUNTRY;
    }

    public void setRECEIVE_COUNTRY(String RECEIVE_COUNTRY) {
        this.RECEIVE_COUNTRY = RECEIVE_COUNTRY;
    }

    public List<Product> getPRODUCTS() {
        return PRODUCTS;
    }

    public void setPRODUCTS(List<Product> PRODUCTS) {
        this.PRODUCTS = PRODUCTS;
    }
}
