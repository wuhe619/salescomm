package com.bdaim.customs.entity;


import java.util.ArrayList;
import java.util.List;

public enum BusiTypeEnum {
    SZ("SZ", "申报单-主单", "sbd_z"),
    SF("SF", "申报单-分单", "sbd_f"),
    SS("SS", "申报单-税单", "sbd_s"),
    CZ("CZ", "舱单-主单", "cd_z"),
    CF("CF", "舱单-分单", "cd_f"),
    CS("CS", "舱单-税单", "cd_s"),
    BZ("BZ", "报关单-主单", "bgd_z"),
    BF("BF", "报关单-分单", "bgd_f"),
    BS("BS", "报关单-税单", "bgd_s"),
    ST("ST", "场站", "station"),
    BGD_HZ("BGD_HZ", "报关单-海关回执", "bgd_hz"),
    CD_HZ("CD_HZ", "舱单-海关回执", "cd_hz");

    private String key;
    private String name;
    private String type;

    BusiTypeEnum(String key, String name, String type) {
        this.key = key;
        this.name = name;
        this.type = type;
    }


    /**
     * 获取type
     *
     * @param key
     * @return
     */
    public static String getType(String key) {
        for (BusiTypeEnum v : BusiTypeEnum.values()) {
            if (v.getKey().equals(key)) {
                return v.getType();
            }
        }
        return "";
    }

    public String getKey() {
        return key;
    }


    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public static List<String> getTypeList(){
        List types = new ArrayList();
        for (BusiTypeEnum v : BusiTypeEnum.values()) {
            types.add(v.getType());
        }
        return types;
    }

}
