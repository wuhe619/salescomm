package com.bdaim.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2019/4/11
 * @description
 */
public class JsonUtil {

    /**
     * 转换为JSONArray
     *
     * @param value
     * @return
     */
    public static JSONArray convertJsonArray(String value) {
        JSONArray jsonArray = null;
        Object object = JSON.parse(value);
        if (object instanceof JSONObject) {
            JSONObject jsonObject = JSON.parseObject(value);
            jsonArray = new JSONArray();
            jsonArray.add(jsonObject);
        } else if (object instanceof JSONArray) {
            jsonArray = (JSONArray) object;
        }
        return jsonArray;
    }

    public static List convertJsonArray(String value, Class t) {
        JSONArray jsonArray = convertJsonArray(value);
        if (jsonArray != null) {
            return JSON.parseArray(jsonArray.toJSONString(), t);
        }
        return new JSONArray();
    }
}
