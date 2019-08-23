package com.bdaim.customer.dto;

/**
 * 客户属性属性名称枚举
 *
 * @author chengning@salescomm.net
 * @date 2019/2/27
 * @description
 */
public enum CustomerPropertyEnum {
    CALL_BACK_APP_ID("app_id", "双向呼叫appId"),
    MARKET_PROJECT_ID_PREFIX("marketProjectId_", "企业下关联项目的ID前缀"),
    SERVICE_MODE("service_mode","服务权限");

    private String key;
    private String name;

    CustomerPropertyEnum(String key, String name) {
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
