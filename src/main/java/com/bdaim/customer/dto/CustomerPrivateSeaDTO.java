package com.bdaim.customer.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum CustomerPrivateSeaDTO {
    NAME("super_name", "姓名"),
    SEX("super_sex", "性别"),
    AGE("super_age", "年龄"),
    PRODRSSION("SYS004", "职业"),
    PHONE("super_telphone", "手机号"),
    COMPANY("company", "所在公司"),
    POSITION("position", "岗位"),
    TELPHONE("super_phone", "固话号"),
    WECHAT("SYS001", "微信号"),
    QQ("SYS002", "QQ号"),
    EMAIL("SYS003", "邮箱"),
    PROVINCE("super_address_province_city", "省份"),
//    CITY("super_address_province_city", "城市"),
    COUNTY("super_address_street ", "区县"),
    ID("id", "线索ID"),
    USER_GET_TIME("user_get_time", "领取时间"),
    INTENT_LEVEL("intentLevel", "意向度"),
    REG_LOCATION("regLocation", "注册地址"),
    REG_CAPITAL("regCapital", "注册资金"),
    DATA_CREATE_TIME("create_time", "线索创建时间"),
    CREATE_USER("create_user", "创建人"),
    CREAT_TIME("create_time", "进入公海时间"),
    LAST_MARK_TIME("last_mark_time", "最后跟进时间"),
    FOLLOW_STATUS("followStatus", "跟进状态"),
    DATA_SOURCE("data_source", "线索来源"),
    PRE_USER_ID("pre_user_id", "前负责人"),
    PUBLIC_SEA_NUMBERS("public_sea_numbers", "进入公海次数"),
    SMS_SUCCESS_COUNT("sms_success_count", "短信营销次数"),
    INVALIDREASON("SYS006", "退回公海原因"),
    REPLACE("replace", "负责人更换次数"),
    CALL_COUNT("callCount", "致电次数"),
    REMARK("remark", "备注");
    private String key;
    private String value;

    CustomerPrivateSeaDTO(String key, String value) {
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
        for (CustomerPrivateSeaDTO s : CustomerPrivateSeaDTO.values()) {
            if (s.key.equals(key)) {
                return s.getValue();
            }
        }
        return null;
    }

    public static List<Map<String, Object>> getAllRow() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CustomerPrivateSeaDTO s : CustomerPrivateSeaDTO.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("key", s.getKey());
            map.put("value", s.getValue());
            list.add(map);
        }
        return list;
    }

}
