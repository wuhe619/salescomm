package com.bdaim.callcenter.dto;

/**
 * 呼叫中心类型
 *
 * @author chengning@salescomm.net
 * @date 2019/2/21
 * @description
 */
public enum CallCenterTypeEnum {

    XF(1, "xf"),
    XZ_CC(2, "xz"),
    XZ_SH(3, "xz_sh"),
    UNICOM_SH(4, "unicom_sh");

    private int type;

    private String propertyName;

    CallCenterTypeEnum(int type, String propertyName) {
        this.type = type;
        this.propertyName = propertyName;
    }

    public int getType() {return type; }

    public String getPropertyName() {
        return propertyName;
    }
}
