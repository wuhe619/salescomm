package com.bdaim.callcenter.dto;

/**
 * 通话状态
 *
 * @author chengning@salescomm.net
 * @date 2019/1/30 10:15
 * @return
 */
public enum CallStatusEnum {

    /**
     * 成功
     */
    SUCCESS(1001, "成功"),
    /**
     * 失败
     */
    FAIL(1002, "失败"),
    /**
     * 用户不在服务区
     */
    NOT_SERVICE_AREA(1003, "用户不在服务区"),
    /**
     * 关机
     */
    SHUTDOWN(1004, "关机"),
    /**
     * 空号
     */
    SPACE_PHONE(1005, "空号"),
    /**
     * 停机
     */
    PHONE_DOWN(1006, "停机"),
    /**
     * 忙音
     */
    BUSY(1007, "忙音"),
    /**
     * 其他
     */
    OTHER(1008, "其他"),

    DEFAULT(0, "未知");

    private int status;
    private String name;

    CallStatusEnum(int status, String name) {
        this.status = status;
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static CallStatusEnum getByType(int i) {
        switch (i) {
            case 1:
                return SUCCESS;
            case 2:
                return FAIL;
            case 3:
                return NOT_SERVICE_AREA;
            case 4:
                return SHUTDOWN;
            case 5:
                return SPACE_PHONE;
            case 6:
                return PHONE_DOWN;
            case 7:
                return BUSY;
            case 8:
                return OTHER;
            default:
                return OTHER;
        }
    }
}
