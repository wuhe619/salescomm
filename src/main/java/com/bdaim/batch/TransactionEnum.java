package com.bdaim.batch;

/**
 * @author duanliying
 * @date 2019/3/5
 * @description
 */
public enum TransactionEnum {
    BALANCE_RECHARGE(1, "充值", 0),
    EXPRESS_DEDUCTION(2, "快递", 2),
    SMS_DEDUCTION(3, "短信", 3),
    CALL_DEDUCTION(4, "通话", 4),
    SEAT_DEDUCTION(5, "座席", 4),
    IDCARD_DEDUCTION(6, "身份证修复", 6),
    BALANCE_DEDUCTION(7, "扣减", 0),
    SUPPLIER_RECHARGE(8, "供应商资金添加", 0),
    SUPPLIER_ADJUSTMENT(9, "供应商调账", 0),
    MAC_NUM_DEDUCTION(10, "mac修复", 5),
    IMEI_DEDUCTION(11, "imei修复", 7),
    ADDRESS_DEDUCTION(12, "地址修复", 1),
    SUPPLIER_DEDUCTION(13, "供应商资金扣减", 0);

    private int type;
    private String name;
    private int resourctType;

    TransactionEnum(int type, String name, int resourctType) {
        this.type = type;
        this.name = name;
        this.resourctType = resourctType;
    }

    public int getResourctType() {
        return resourctType;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }


    public static TransactionEnum getType(int resourctType) {
        for (TransactionEnum s : TransactionEnum.values()) {
            if (s.resourctType == resourctType) {
                return s;
            }
        }
        return null;
    }

    public static String getName(int type) {
        for (TransactionEnum s : TransactionEnum.values()) {
            if (s.type == type) {
                return s.getName();
            }
        }
        return null;
    }

    public static int getResourceType(int type) {
        for (TransactionEnum s : TransactionEnum.values()) {
            if (s.type == type) {
                return s.getResourctType();
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return "TransactionEnum{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", resourctType=" + resourctType +
                '}';
    }
}
