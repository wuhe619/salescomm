package com.bdaim.callcenter.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019-12-03 10:03
 */
public enum CallTypeParamEnum {
    /**
     * 呼叫中心
     */
    CALL_CENTER(1, "call_center"),
    /**
     * 讯众双呼
     */
    CALL2_WAY(2, "call2way"),
    /**
     * 联通双呼
     */
    UNICOM_CALL2_WAY(4, "unicomCall2way");

    private int type;
    private String propertyName;

    CallTypeParamEnum(int type, String propertyName) {
        this.type = type;
        this.propertyName = propertyName;
    }

    public int getType() {
        return type;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
