package com.bdaim.batch;

/**
 * @description 失联修复资源枚举类
 * @author:duanliying
 * @date: 2019/2/21 18:19
 */
public enum ResourceEnum {
    ADDRESS(1, "地址修复", "addressPrice", "", "seatMinute", "configId", "", "apparentNumber", "activityId", "callCenterId"),
    EXPRESS(2, "快递", "successPrice", "", "seatMinute", "configId", "", "", "activityId", "callCenterId"),
    SMS(3, "短信", "smsPrice", "", "seatMinute", "configId", "mianNumber", "apparentNumber", "activityId", "callCenterId"),
    CALL(4, "通话", "callPrice", "seatPrice", "seatMinute", "configId", "mianNumber", "apparentNumber", "activityId", "callCenterId"),
    MAC(5, "mac修复", "macPrice", "seatPrice", "seatMinute", "configId", "", "", "activityId", "callCenterId"),
    IDCARD(6, "身份证修复", "idCardPrice", "seatPrice", "seatMinute", "configId", "", "", "activityId", "callCenterId"),
    IMEI(7, "imei修复", "imeiPrice", "", "seatMinute", "configId", "", "", "activityId", "callCenterId");
    private int type;
    private String typeName;
    private String price;
    private String seatPrice;
    private String seatMinute;
    private String config;
    private String mianNumber;
    private String apparentNumber;
    private String activityId;
    private String callCenterId;


    ResourceEnum(int type, String typeName, String price, String seatPrice, String seatMinute, String config, String mianNumber, String apparentNumber, String activityId, String callCenterId) {
        this.type = type;
        this.typeName = typeName;
        this.price = price;
        this.seatPrice = seatPrice;
        this.seatMinute = seatMinute;
        this.config = config;
        this.mianNumber = mianNumber;
        this.apparentNumber = apparentNumber;
        this.activityId = activityId;
        this.callCenterId = callCenterId;
    }

    public int getType() {
        return type;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getMianNumber() {
        return mianNumber;
    }

    public String getSeatMinute() {
        return seatMinute;
    }

    public String getApparentNumber() {
        return apparentNumber;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getCallCenterId() {
        return callCenterId;
    }

    public String getPrice() {
        return price;
    }

    public String getConfig() {
        return config;
    }

    public String getSeatPrice() {
        return seatPrice;
    }

    public static ResourceEnum getResource(int type) {
        for (ResourceEnum s : ResourceEnum.values()) {
            if (s.type == type) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ResourceEnum{" +
                "type=" + type +
                ", typeName='" + typeName + '\'' +
                ", price='" + price + '\'' +
                ", config='" + config + '\'' +
                ", mianNumber='" + mianNumber + '\'' +
                ", apparentNumber='" + apparentNumber + '\'' +
                ", activityId='" + activityId + '\'' +
                ", callCenterId='" + callCenterId + '\'' +
                '}';
    }
}
