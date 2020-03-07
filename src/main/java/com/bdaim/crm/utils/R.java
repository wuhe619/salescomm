package com.bdaim.crm.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.crm.common.config.json.ErpJsonFactory;
import com.jfinal.json.Json;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 返回数据
 */
public class R extends LinkedHashMap<String, Object> implements Serializable {
    private static final long serialVersionUID = 1L;

    Json json = ErpJsonFactory.me().getJson();

    public R() {
        put("code", 0);
    }

    public static R error() {
        return error(500, "未知异常，请联系管理员");
    }

    public static R error(String msg) {
        return error(500, msg);
    }

    public static R noAuth() {
        return error("没有权限");
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R ok() {
        return new R();
    }

    private Object handleLongValue(Object value) {
        if (value instanceof JSONObject) {
            JSONObject v = ((JSONObject) value);
            for (Map.Entry<String, Object> k : v.entrySet()) {
                if (k.getValue() instanceof Long) {
                    k.setValue(String.valueOf(v.get(k.getKey())));
                } else if (k.getValue() instanceof JSONArray) {
                    JSONArray list = ((JSONArray) k.getValue() );
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i) instanceof JSONObject) {
                            JSONObject object = list.getJSONObject(i);
                            for (Map.Entry<String, Object> m : object.entrySet()) {
                                if (m.getValue() instanceof Long) {
                                    m.setValue(String.valueOf(m.getValue()));
                                }
                            }
                        }
                    }
                    return list;
                }
            }
            return v;
        } else if (value instanceof JSONArray) {
            JSONArray list = ((JSONArray) value);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof JSONObject) {
                    JSONObject v = list.getJSONObject(i);
                    for (Map.Entry<String, Object> k : v.entrySet()) {
                        if (k.getValue() instanceof Long) {
                            k.setValue(String.valueOf(k.getValue()));
                        }
                    }
                }
            }
            return list;
        }
        return null;
    }

    @Override
    public R put(String key, Object value) {
        if (!"excel".equals(key)) {
            value = JSON.parse(json.toJson(value));
        }
        handleLongValue(value);
        super.put(key, value);
        return this;
    }

    public boolean isSuccess() {
        return super.containsKey("code") && super.get("code").equals(0);
    }

    public static R isSuccess(boolean b, String msg) {
        return b ? R.ok(msg) : R.error(msg);
    }

    public static R isSuccess(boolean b) {
        return isSuccess(b, null);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
