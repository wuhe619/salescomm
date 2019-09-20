package com.bdaim.common.dto;

/**
 * @author duanliying
 * @date 2019/9/20
 * @description 业务类型枚举类
 */
public enum ServiceEnum {
    SZ("A", "申报单主单", "sbd_z"),
    SF("B", "申报单分单", "sbd_f"),
    SS("C", "申报单税单", "sbd_s"),
    CZ("E", "舱单主单", "cd_z"),
    CF("F", "舱单分单", "cd_f"),
    CS("G", "舱单税单", "cd_s"),
    BZ("H", "报关单主单", "bgd_z"),
    BF("I", "报关单分单", "bgd_f"),
    BS("J", "报关单税单", "bgd_s"),
    ST("K", "场站", "cz");

    private String id;
    private String name;
    private String type;

    ServiceEnum(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    /**
     * 获取type
     *
     * @param id
     * @return
     */
    public static String getType(String id) {
        for (ServiceEnum v : ServiceEnum.values()) {
            if (v.getId().equals(id)) {
                return v.getType();
            }
        }
        return "";
    }
}
