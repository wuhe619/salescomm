package com.bdaim.customer.dto;

/**
 * 客户营销类型枚举
 * @author chengning@salescomm.net
 * @date 2019-10-28 16:35
 */
public enum MarketTypeEnum {
    B2C(1),
    B2B(2);

    private int code;

    MarketTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static void main(String[] args) {
        for (ServiceModeEnum v : ServiceModeEnum.values()) {
            System.out.println(v.getCode());
        }
    }
}
