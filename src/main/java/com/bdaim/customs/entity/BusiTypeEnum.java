package com.bdaim.customs.entity;

public enum BusiTypeEnum {
    SZ("SZ","申报单-主单"),
    SF("SF","申报单-分单"),
    SS("SS","申报单-税单"),
    CZ("CZ","舱单-主单"),
    CF("CF","舱单-分单"),
    CS("CS","舱单-税单"),
    BZ("BZ","报关单-主单"),
    BF("BF","报关单-分单"),
    BS("BS","报关单-税单");

    private String key;
    private String name;

    BusiTypeEnum(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }



    public String getName() {
        return name;
    }


}
