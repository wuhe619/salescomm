package com.bdaim.util;

/**
 * BP业务平台
 */
public enum BusinessEnum {
    ONLINE("ONLINE","精准营销"),
    NOLOSE("NOLOSE","失联复联"),
    EXPRESS_PLATFORM("EXPRESS_PLATFORM","沃快递聚合平台"),
    BEAR_CLASS("BEAR_CLASS","教育超市"),
    RONG_360("RONG_360","金融超市"),
    CUSTOMS("CUSTOMS","海关"),
    CRM("CRM","CRM");

    private String key;
    private String name;

    BusinessEnum(String key, String name) {
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
