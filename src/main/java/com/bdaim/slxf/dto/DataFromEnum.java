package com.bdaim.slxf.dto;

/**
 * @author duanliying
 * @date 2019/3/15
 * @description
 */
public enum DataFromEnum {
    SYSTEM("系统添加", 1),
    O2O("O2O同步", 2);

    private String name;

    private int value;

    private DataFromEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public static String getNameByValue(int value) {
        String name = null;
        DataFromEnum[] ems = DataFromEnum.values();
        if (ems != null && ems.length > 0) {
            for (DataFromEnum em : ems) {
                if (em.getValue() == value) {
                    name = em.getName();
                    break;
                }
            }
        }
        return name;
    }

}