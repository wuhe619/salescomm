package com.bdaim.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.util.http.HttpUtil;

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

/**
 * 短链接生成
 *
 * @author chengning@salescomm.net
 * @date 2019/7/11
 * @description
 */
public class ShortUrlUtil {

    private static Logger logger = Logger.getLogger(ShortUrlUtil.class);

    public static String generateShortUrl(String longUrl) {
        try {
            logger.info("原始url:" + longUrl);
            String apiUrl = "https://api.weibo.com/2/short_url/shorten.json?source=2849184197&url_long={0}";
            logger.info("encode url:" + URLEncoder.encode(longUrl, "UTF-8"));
            String result = HttpUtil.httpGet(MessageFormat.format(apiUrl, URLEncoder.encode(longUrl, "UTF-8")), null, null);
            if (StringUtil.isNotEmpty(result)) {
                JSONObject jsonObject = JSON.parseObject(result);
                if (jsonObject == null) {
                    return "";
                }
                if (jsonObject.getJSONArray("urls") == null) {
                    return "";
                }
                if (jsonObject.getJSONArray("urls").size() == 0) {
                    return "";
                }
                logger.info("短链接:" + jsonObject.getJSONArray("urls").getJSONObject(0).getString("url_short"));
                return jsonObject.getJSONArray("urls").getJSONObject(0).getString("url_short");
            }
        } catch (Exception e) {
            logger.error("短链接转换异常", e);
        }
        return "";
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(generateShortUrl("http://financedev.datau.top/h5/#/creditDetail?id=1907101846550001&activityId=1907111821370000&channelId=1907111718280001"));
    }
}
