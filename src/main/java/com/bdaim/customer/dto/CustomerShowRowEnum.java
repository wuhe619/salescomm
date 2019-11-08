package com.bdaim.customer.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义显示字段列枚举
 */
public enum CustomerShowRowEnum {

    NAME("super_name", "姓名"),
    SEX("super_sex", "性别"),
    AGE("super_age", "年龄"),
    PRODRSSION("profession", "职业"),
    PHONE("super_telphone", "手机号"),
    COMPANY("company", "所在公司"),
    TELPHONE("super_phone", "固话号"),
    WECHAT("weChat", "微信号"),
    QQ("qq", "QQ号"),
    EMAIL("email", "邮箱"),
    PROVINCE("super_address_province_city", "省市县"),
    STREET("super_address_street", "街道"),
    ID("id", "线索ID"),
    CREAT_TIME("create_time", "进入公海时间"),
    LAST_MARK_TIME("last_mark_time", "最后跟进时间"),
    FOLLOW_STATUS("followStatus", "跟进状态"),
    DATA_SOURCE("data_source", "线索来源"),
    PRE_USER_ID("pre_user_id", "前负责人"),
    CALL_COUNT("callCount", "致电次数"),
    //numbers(2, "进入公海次数"),
    SMS_SUCCESS_COUNT("sms_success_count", "短信营销次数"),
    INVALIDREASON("invalidReason", "退回公海原因"),
    //REPLACE(2, "负责人更换次数"),
    USER_GET_TIME("user_get_time", "领取时间"),
    PULL_STATUS("pull_status", "拉取标记"),
    LAST_CALL_STATUS("last_call_status", "最近一次的呼叫结果"),
    INTENT_LEVEL("intent_level", "意向度"),
    LAST_CALL_TIME("last_call_time", "最后呼叫时间"),
    USER_ID("realname", "当前责任人"),
    REG_LOCATION("regLocation", "注册地址"),
    REG_CAPITAL("regCapital", "注册资金"),
    REG_TIME("regTime", "注册时间"),
    REG_STATUS("regStatus", "经营状态"),
    ENT_PERSON_NUM("entPersonNum", "企业联系人数量"),
    ENT_ID("entId", "企业ID");


    private String key;
    private String value;

    CustomerShowRowEnum(String key, String value) {
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
        for (CustomerShowRowEnum s : CustomerShowRowEnum.values()) {
            if (s.key.equals(key)) {
                return s.getValue();
            }
        }
        return null;
    }

    public static List<Map<String, Object>> getAllRow() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CustomerShowRowEnum s : CustomerShowRowEnum.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("key", s.getKey());
            map.put("value", s.getValue());
            list.add(map);
        }
        return list;
    }


    @Override

    public String toString() {
        return "CustomerShowRowEnum{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

}
