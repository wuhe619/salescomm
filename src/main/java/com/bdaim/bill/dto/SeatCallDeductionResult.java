package com.bdaim.bill.dto;

/** 坐席扣减结果实体类
 * @author chengning@salescomm.net
 * @date 2018/12/25
 * @description
 */
public class SeatCallDeductionResult {

    private int summMinute;
    private int custAmount;
    private int prodAmount;

    public SeatCallDeductionResult() {
    }

    public SeatCallDeductionResult(int summMinute, int custAmount, int prodAmount) {
        this.summMinute = summMinute;
        this.custAmount = custAmount;
        this.prodAmount = prodAmount;
    }

    public int getSummMinute() {
        return summMinute;
    }

    public void setSummMinute(int summMinute) {
        this.summMinute = summMinute;
    }

    public int getCustAmount() {
        return custAmount;
    }

    public void setCustAmount(int custAmount) {
        this.custAmount = custAmount;
    }

    public int getProdAmount() {
        return prodAmount;
    }

    public void setProdAmount(int prodAmount) {
        this.prodAmount = prodAmount;
    }

    @Override
    public String toString() {
        return "SeatCallDeductionResult{" +
                "summMinute=" + summMinute +
                ", custAmount=" + custAmount +
                ", prodAmount=" + prodAmount +
                '}';
    }
}
