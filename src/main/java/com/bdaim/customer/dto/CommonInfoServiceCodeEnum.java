package com.bdaim.customer.dto;

/**
 * 通用业务码枚举
 *
 * */
public enum CommonInfoServiceCodeEnum {
    SETTING_JOB_SETTLEMENT_PRICE("10001", "职场结算价设置"),
    SETTING_PROJECT_SETTLEMENT_PRICE("10002", "项目结算价设置"),
    JOB_SETTLEMENT_MANAGE("10003", "职场结算单管理"),
    JOB_SETTLEMENT_DETAIL("10004", "职场结算单详情"),
    PROJECT_SETTLEMENT_MANAGE("10005", "项目结算单管理"),
    PROJECT_SETTLEMENT_DETAIL("10006", "项目结算详情"),
    JOB_SETTLEMENT_APPLY_MANAGE("10007", "职场结算申请管理"),
    PROJECT_SETTLEMENT_APPLY_MANAGE("10008", "项目结算申请管理")
    ;

    private String key;
    private String name;

    CommonInfoServiceCodeEnum(String key, String name) {
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
