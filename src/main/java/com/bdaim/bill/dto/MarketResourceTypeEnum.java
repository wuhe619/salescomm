package com.bdaim.bill.dto;

/**
 * 营销资源类型
 *
 * @author chengning@salescomm.net
 * @date 2018/12/28
 * @description
 */
public enum MarketResourceTypeEnum {

    CALL(1, "call_config"),
    SMS(2, "sms_config"),
    EMAIL(3, "email_config"),
    LABEL(4, "data_config"),
    SEATS(5, "seat_config"),
    APPARENT_NUMBER(6, "apparent_config");
    private int type;
    private String propertyName;

    MarketResourceTypeEnum(int type, String propertyName) {
        this.type = type;
        this.propertyName = propertyName;
    }

    public int getType() {
        return type;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public static MarketResourceTypeEnum getType(int type) {
        for (MarketResourceTypeEnum s : MarketResourceTypeEnum.values()) {
            if (s.type == type) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "MarketResourceTypeEnum{" +
                "type=" + type +
                ", propertyName='" + propertyName + '\'' +
                '}';
    }
}
