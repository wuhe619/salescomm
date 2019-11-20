package com.bdaim.customer.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum CustomerPublicSeaDTO {

    CUST_NAME("company", "企业名称"),
    REG_LOCATION("regLocation", "注册地址"),
    REG_CAPITAL("regCapital", "注册资金"),
    REG_TIME("regTime", "注册时间"),
    REG_STATUS("regStatus", "经营状态"),
    ENT_PERSON_NUM("entPersonNum", "企业联系人数量"),
    USER_GET_TIME("user_get_time", "领取时间"),
    CREAT_TIME("create_time", "进入公海时间"),
    LAST_MARK_TIME("last_mark_time", "最后跟进时间"),
    FOLLOW_STATUS("followStatus", "跟进状态"),
    DATA_SOURCE("data_source", "线索来源"),
    PRE_USER_ID("pre_user_id", "前负责人"),
    CALL_COUNT("callCount", "致电次数"),
    SMS_SUCCESS_COUNT("sms_success_count", "短信营销次数"),
    INVALIDREASON("SYS006", "退回公海原因"),
    LAST_CALL_STATUS("last_call_status", "最近一次的呼叫结果"),
    INTENT_LEVEL("intentLevel", "意向度"),
    LAST_CALL_TIME("lastCallTime", "最后呼叫时间"),
    USER_ID("realname", "当前责任人");
    private String key;
    private String value;

    CustomerPublicSeaDTO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static String getValue(String key) {
        for (CustomerPublicSeaDTO s : CustomerPublicSeaDTO.values()) {
            if (s.key.equals(key)) {
                return s.getValue();
            }
        }
        return null;
    }

    public static List<Map<String, Object>> getAllRow() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CustomerPublicSeaDTO s : CustomerPublicSeaDTO.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("key", s.getKey());
            map.put("value", s.getValue());
            list.add(map);
        }
        return list;
    }
    }
