package com.bdaim.label.dto;

/** 品类的类型1表示媒体，0表示电商
 */
public enum CategoryType {
    MEDIA(0),
    PRODUCT(1);

    private int type;

    CategoryType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
