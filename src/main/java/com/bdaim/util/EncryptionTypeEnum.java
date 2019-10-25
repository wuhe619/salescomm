package com.bdaim.util;

/**
 * 加密类型枚举
 *
 */
public enum EncryptionTypeEnum {

    AES(1, "AES"),
    DES(2, "DES"),
    RAS(3, "RSA");


    private int type;
    private String name;

    EncryptionTypeEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }


    public static EncryptionTypeEnum getType(int type) {
        for (EncryptionTypeEnum s : EncryptionTypeEnum.values()) {
            if (s.type == type) {
                return s;
            }
        }
        return null;
    }

    public static String getName(int type) {
        for (EncryptionTypeEnum s : EncryptionTypeEnum.values()) {
            if (s.type == type) {
                return s.getName();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "EncryptionTypeEnum{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
