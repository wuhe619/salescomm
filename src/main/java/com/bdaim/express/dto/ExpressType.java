package com.bdaim.express.dto;

public enum ExpressType {

    YTO(0, "yto", "圆通","http://114.80.61.11:8020/CommonOrderModeBPlusServlet","http://gwmarketinginterface.yto.net.cn:7000/standard");

    int type;
    String expressCode;
    String expressName;
    String orderUrl;
    String rajectoryUrl;

    public static ExpressType getExpressTypeByCode(String code) {
        for (ExpressType sexEnum : ExpressType.values()) {
            if (code.equals(sexEnum.getExpressCode())) {
                return sexEnum;
            }
        }
        return null;
    }

    ExpressType(int type, String expressCode, String expressName,String orderUrl,String rajectoryUrl) {
        this.type = type;
        this.expressName = expressName;
        this.expressCode = expressCode;
        this.orderUrl=orderUrl;
        this.rajectoryUrl=rajectoryUrl;
    }


    public String getOrderUrl() {
        return orderUrl;
    }

    public void setOrderUrl(String orderUrl) {
        this.orderUrl = orderUrl;
    }

    public String getRajectoryUrl() {
        return rajectoryUrl;
    }

    public void setRajectoryUrl(String rajectoryUrl) {
        this.rajectoryUrl = rajectoryUrl;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getExpressCode() {
        return expressCode;
    }

    public void setExpressCode(String expressCode) {
        this.expressCode = expressCode;
    }

    public String getExpressName() {
        return expressName;
    }

    public void setExpressName(String expressName) {
        this.expressName = expressName;
    }
}
