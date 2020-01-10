package com.bdaim.industry.dto;

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
    APPARENT_NUMBER(6, "apparent_config"),
    B2B_PRICE(7, "b2b_price_config"),
    B2B_TCB(8, "b2b_resource_config"),
    XZ_CALL_API_CONFIG(9, "xz_call_api"),
    API_CONFIG(10, "api_config");
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
