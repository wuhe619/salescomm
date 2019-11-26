package com.bdaim.customer.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class Deposit {
    private int custId;
    @NotNull(message = "money参数必填")
    private String money;
    @NotNull(message = "pageSize参数必填")
    private String picId;

    public int getCustId() {
        return custId;
    }

    public void setCustId(int custId) {
        this.custId = custId;
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
