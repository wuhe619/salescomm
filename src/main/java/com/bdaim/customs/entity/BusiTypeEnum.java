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
    CD_HZ("CD_HZ", "舱单-海关回执", "cd_hz"),
    TAX_MANAGE("TAX_MANAGE","纳税管理","tax_manage"),
    PARAM_PROXY("PARAM_PROXY","地面代理参数","param_proxy");

    private String key;
    private String name;
    private String type;

    BusiTypeEnum(String key, String name, String type) {
        this.key = key;
        this.name = name;
        this.type = type;
    }


    public static BusiTypeEnum get(String busiType) {
        for (BusiTypeEnum v : BusiTypeEnum.values()) {
            if (v.getType().equals(busiType)) {
                return v;
            }
        }
        return null;
    }

    public static String getEsIndex(String busiType) {
        BusiTypeEnum busiTypeEnum = get(busiType);
        switch (busiTypeEnum) {
            case SZ:
                return Constants.SZ_INFO_INDEX;
            case CZ:
                return Constants.CZ_INFO_INDEX;
            case BZ:
                return Constants.BZ_INFO_INDEX;
            case SF:
                return Constants.SF_INFO_INDEX;
            case CF:
                return Constants.CF_INFO_INDEX;
            case BF:
                return Constants.BF_INFO_INDEX;
            case SS:
                return Constants.SS_INFO_INDEX;
            case CS:
                return Constants.CS_INFO_INDEX;
            case BS:
                return Constants.BS_INFO_INDEX;
            default:
                return "-1";
        }
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
