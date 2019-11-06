package com.bdaim.customer.dto;

public class Content {
    private String msg;
    private String bill_no;
    private String op_time;
    private String ma_bill_no;
    private String type;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getBill_no() {
        return bill_no;
    }

    public void setBill_no(String bill_no) {
        this.bill_no = bill_no;
    }

    public String getOp_time() {
        return op_time;
    }

    public void setOp_time(String op_time) {
        this.op_time = op_time;
    }

    public String getMa_bill_no() {
        return ma_bill_no;
    }

    public void setMa_bill_no(String ma_bill_no) {
        this.ma_bill_no = ma_bill_no;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Content{" +
                "msg='" + msg + '\'' +
                ", bill_no='" + bill_no + '\'' +
                ", op_time='" + op_time + '\'' +
                ", ma_bill_no='" + ma_bill_no + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
