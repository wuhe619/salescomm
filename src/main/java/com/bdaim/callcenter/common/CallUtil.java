package com.bdaim.callcenter.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.util.ConfigUtil;
import com.bdaim.common.util.http.HttpUtil;
import com.bdaim.common.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * @author chengning@salescomm.net
 * @date 2019/4/10
 * @description
 */
public class CallUtil {

    private static Logger log = LoggerFactory.getLogger(CallUtil.class);

    /**
     * 从HBase获取录音文件base64字符串
     *
     * @param fileName
     * @return
     */
    public static String getVoiceBase64Data(String fileName) throws Exception {
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        String result = HttpUtil.httpGet(PropertiesUtil.getStringValue("hbase_audio_url") + fileName + "/f1:file", param, headers);
        String base64Str = null;
        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(result);
        } catch (JSONException e) {
            log.error("解析HBase录音文件返回Json出错,fileName:" + fileName + ",返回结果:" + result, e);
            return "";
        }
        JSONArray row = jsonObject.getJSONArray("Row");
        if (row != null && row.size() > 0) {
            JSONObject rowData = row.getJSONObject(0);
            JSONArray cell = rowData.getJSONArray("Cell");
            if (cell != null && cell.size() > 0) {
                base64Str = cell.getJSONObject(0).getString("$");
            }
        }
        return base64Str;
    }

    /**
     * 生成录音文件名称
     *
     * @param monthYear
     * @param touchId
     * @return
     */
    public static String generateRecordNameToMp3(String monthYear, Object touchId) {
        return generateRecordUrl(monthYear, String.valueOf(touchId), "mp3");
    }

    /**
     * 生成录音文件服务器全路径
     *
     * @param monthYear
     * @param touchId
     * @return
     */
    public static String generateRecordUrlMp3(String monthYear, Object userId, Object touchId) {
        return ConfigUtil.getInstance().get("audio_server_url") + "/" + userId + "/" + generateRecordUrl(monthYear, String.valueOf(touchId), "mp3");
    }

    private static String generateRecordUrl(String monthYear, String touchId, String fileType) {
        return monthYear + touchId + "." + fileType;
    }
}
