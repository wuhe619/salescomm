package com.bdaim.common;

/**
 * @author duanliying
 * @date 2019/3/18
 * @description
 */
public enum CommonInfoCodeEnum {
    CUST_SALES_PRICE("1001", "企业销售定价设置");

    private String key;
    private String name;

    CommonInfoCodeEnum(String key, String name) {
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
