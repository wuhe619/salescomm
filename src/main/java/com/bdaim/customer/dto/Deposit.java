package com.bdaim.customer.dto;

import javax.validation.constraints.NotNull;

public class Deposit {
    private String custId;
    @NotNull(message = "money参数必填")
    private String money;
    @NotNull(message = "pageSize参数必填")
    private String picId;
    private int id;
    private String payTime;
    private String realname;
    private String userId;
    private String preMoney;

    public String getPreMoney() {
        return preMoney;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public void setPreMoney(String preMoney) {
        this.preMoney = preMoney;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }


    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getPicId() {
        return picId;
    }

    public void setPicId(String picId) {
        this.picId = picId;
    }
}
