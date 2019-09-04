package com.bdaim.bill.dto;

/**
 * 交易类型枚举
 *
 * @author chengning@salescomm.net
 * @date 2018/12/20
 * @description
 */
public enum TransactionTypeEnum {

    BALANCE_RECHARGE(1, "充值"),
    BALANCE_DEDUCTION(2, "扣减"),
    SMS_DEDUCTION(3, "短信扣费"),
    CALL_DEDUCTION(4, "通话扣费"),
    SEAT_DEDUCTION(5, "坐席扣费"),
    //REPAIR_DEDUCTION(6, "修复扣费"),
    LABEL_DEDUCTION(7, "数据(客户群)扣费"),
    APPARENT_NUM_DEDUCTION(8, "外显扣费");

    private int type;
    private String name;

    TransactionTypeEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }


    public static TransactionTypeEnum getType(int type) {
        for (TransactionTypeEnum s : TransactionTypeEnum.values()) {
            if (s.type == type) {
                return s;
            }
        }
        return null;
    }

    public static String getName(int type) {
        for (TransactionTypeEnum s : TransactionTypeEnum.values()) {
            if (s.type == type) {
                return s.getName();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "TransactionTypeEnum{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
