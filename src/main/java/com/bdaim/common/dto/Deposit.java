package com.bdaim.common.dto;

import javax.validation.constraints.NotNull;

public class Deposit {
    @NotNull(message = "pageNum参数必填")
    private long id;
    @NotNull(message = "pageNum参数必填")
    private String money;
//    @NotNull(message = "充值凭证必填")
    private String repaidVoucher;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getRepaidVoucher() {
        return repaidVoucher;
    }

    public void setRepaidVoucher(String repaidVoucher) {
        this.repaidVoucher = repaidVoucher;
    }
}
