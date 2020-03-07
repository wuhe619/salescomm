package com.bdaim.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.CharsetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    public static String postForm(String url, String _file, Map<String, File> files, Map<String, InputStream> is, Map<String, String> texts) {
        CloseableHttpClient client = HttpClients.createDefault();
        InputStream input = null;
        BufferedReader reader = null;
        try {
            // 要上传的文件的路径
            HttpPost httpPost = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.setCharset(CharsetUtils.get("UTF-8"));
            for (String key : is.keySet()) {
                InputStreamBody ib = new InputStreamBody(is.get(key), key);
                builder.addPart(_file, ib);
            }
            for (String key : files.keySet()) {
                FileBody fb = new FileBody(files.get(key));
                builder.addPart(_file, fb);
            }
            for (String key : texts.keySet()) {
                builder.addTextBody(key, texts.get(key));
            }
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            HttpResponse response = client.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) { // 成功
                // 获取服务器返回值
                HttpEntity responseEntity = response.getEntity();
                input = responseEntity.getContent();
                reader = new BufferedReader(
                        new InputStreamReader(input, "utf-8"));
                String result = reader.readLine();
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("error:", e);
            e.printStackTrace();
        } finally {
            if (null != input)
                try {
                    input.close();
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

}
