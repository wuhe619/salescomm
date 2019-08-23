package com.bdaim.common.util;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HttpUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    private static String httpPost(String strUrl, String strReq) throws Exception {
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        OutputStream out = conn.getOutputStream();
        out.write(strReq.getBytes("UTF-8"));
        out.flush();
        out.close();
        InputStream in = conn.getInputStream();
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        int i = -1;
        while ((i = in.read()) != -1) {
            res.write(i);
        }
        res.flush();
        res.close();
        conn.disconnect();
        return new String(new String(res.toByteArray(), "UTF-8"));
    }

    public static String httpsPost(String strUrl, String strReq) throws Exception {
        return httpsPost(strUrl, strReq, null);
    }

    public static String httpsPost(String strUrl, String strReq, String authorization) throws Exception {
        if (strUrl.startsWith("http://"))
            return httpPost(strUrl, strReq);

        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        HttpResponse response = null;
        try {
            httpClient = HttpsSSLClient.createSSLInsecureClient();
            httpPost = new HttpPost(strUrl);
            // httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json;charset=utf-8;");
            if (StringUtils.isNotBlank(authorization))
                httpPost.setHeader("Authorization", authorization);
            StringEntity stringEntity = new StringEntity(strReq);
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            result = EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static String httpPost(String strUrl, String strReq, Map<String, Object> headers) throws Exception {
        HttpClient httpClient;
        HttpPost httpPost;
        String result;
        HttpResponse response = null;
        try {
            httpClient = HttpClientBuilder.create().build();
            httpPost = new HttpPost(strUrl);
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                httpPost.setHeader(header.getKey(), String.valueOf(header.getValue()));
            }
            StringEntity stringEntity = new StringEntity(strReq, Charset.forName("UTF-8"));
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            result = EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
            throw new RuntimeException("发送http请求异常", e);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    throw new RuntimeException("发送http请求异常", e);
                }
            }
        }
        return result;
    }

    public static String httpPost(String strUrl, Map<String, String> params, Map<String, Object> headers) throws Exception {
        HttpClient httpClient;
        HttpPost httpPost;
        String result;
        HttpResponse response = null;
        try {
            httpClient = HttpClientBuilder.create().build();
            httpPost = new HttpPost(strUrl);
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, Object> header : headers.entrySet()) {
                    httpPost.setHeader(header.getKey(), String.valueOf(header.getValue()));
                }
            }
            //设置参数
            List<NameValuePair> list = new ArrayList<>();
            Iterator iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> elem = (Map.Entry<String, String>) iterator.next();
                list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
            }
            if (list.size() > 0) {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, Charset.forName("UTF-8"));
                httpPost.setEntity(entity);
            }
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            result = EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
            throw new RuntimeException("发送http请求异常", e);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    throw new RuntimeException("发送http请求异常", e);
                }
            }
        }
        return result;
    }


    public static String httpGet(String strUrl, Map<String, Object> params, Map<String, Object> headers) {
        HttpClient httpClient;
        HttpGet httpGet;
        String result;
        HttpResponse response = null;
        try {
            httpClient = HttpClientBuilder.create().build();
            List<NameValuePair> nameValuePairArrayList = new ArrayList<>();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                nameValuePairArrayList.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
            }
            //参数转换为字符串
            String paramsStr = EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairArrayList, "UTF-8"));
            httpGet = new HttpGet(strUrl + "?" + paramsStr);
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, Object> header : headers.entrySet()) {
                    httpGet.setHeader(header.getKey(), String.valueOf(header.getValue()));
                }
            }
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            result = EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
            throw new RuntimeException("发送http请求异常", e);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    throw new RuntimeException("发送http请求异常", e);
                }
            }
        }
        return result;
    }

    /**
     * 使用重试超时机制的post请求
     *
     * @param url  请求的地址
     * @param args 请求的参数
     * @return
     */
    public static InputStream _postForInputStream(String url, Map<String, Object> args) {
        InputStream is = null;
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        org.apache.commons.httpclient.HttpClient hc = new org.apache.commons.httpclient.HttpClient(connectionManager);
        hc.getHttpConnectionManager().getParams().setConnectionTimeout(50000);
        hc.getHttpConnectionManager().getParams().setSoTimeout(10000);
        PostMethod method = new PostMethod(url);
        List<org.apache.commons.httpclient.NameValuePair> nvps = new ArrayList<org.apache.commons.httpclient.NameValuePair>();
        for (String key : args.keySet()) {
            org.apache.commons.httpclient.NameValuePair nvp = new org.apache.commons.httpclient.NameValuePair(key, args.get(key).toString());
            nvps.add(nvp);
        }
        method.setRequestBody(nvps.toArray(new org.apache.commons.httpclient.NameValuePair[nvps.size()]));
        method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
       /* method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new HttpRetryHandler(retryCount));*/
        try {
            int status = hc.executeMethod(method);
            if (status == 200) {
                is = method.getResponseBodyAsStream();
            }
        } catch (HttpException e) {
            System.out.println("_post  网络请求错误");
        } catch (IOException e) {
            System.out.println("_post io读写错误");
        }

//		method.releaseConnection();
        return is;
    }

    /**
     * Http Get请求 使用超时重试机制的请求
     *
     * @param url
     * @param args
     * @return
     * @throws IOException
     */
    public static String _get(String url) {
        String content = null;
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        org.apache.commons.httpclient.HttpClient hc = new org.apache.commons.httpclient.HttpClient(connectionManager);
        hc.getHttpConnectionManager().getParams().setConnectionTimeout(8000);
        hc.getHttpConnectionManager().getParams().setSoTimeout(60000);
        HttpMethod method = new GetMethod(url);
      /*  method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new HttpRetryHandler(retryCount));*/
        try {
            int status = hc.executeMethod(method);
            if (status == 200) {
                content = extractContent(method);
            }
        } catch (HttpException e) {
            log.error("_get  网络请求错误", e);
        } catch (IOException e) {
            log.error("_get io读写错误", e);
        }
        method.releaseConnection();
        return content;
    }

    public static String _get(String url, Map<String, String> args) throws UnsupportedEncodingException {
        String content = null;
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        org.apache.commons.httpclient.HttpClient hc = new org.apache.commons.httpclient.HttpClient(connectionManager);
        hc.getHttpConnectionManager().getParams().setConnectionTimeout(8000);
        hc.getHttpConnectionManager().getParams().setSoTimeout(60000);
        StringBuilder sb = new StringBuilder(url);
        for (String oneo : args.keySet()) {
            sb.append("&");
            sb.append(oneo);
            sb.append("=");
            sb.append(URLEncoder.encode((null != args.get(oneo)) ? args.get(oneo) : "", "UTF-8"));
        }
        HttpMethod method = new GetMethod(sb.toString());
        /*method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new HttpRetryHandler(retryCount));*/
        try {
            int status = hc.executeMethod(method);
            if (status == 200) {
                content = extractContent(method);
            }
        } catch (HttpException e) {
            log.error("_get  网络请求错误", e);
        } catch (IOException e) {
            log.error("_get io读写错误", e);
        }
        method.releaseConnection();
        return content;
    }

    private static String extractContent(HttpMethod method) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    method.getResponseBodyAsStream(), "utf-8"));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            System.out.println("解析网络请求错误");
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 模拟表单上传
     *
     * @param url   上传地址
     * @param _file file表单名称
     * @param files file内容，key为file名称，内容为file
     * @param is    file内容，key为file名称，内容为文件流
     * @param texts 文本
     * @return
     */
    public static String _postForm(String url, String _file,
                                   Map<String, File> files, Map<String, InputStream> is,
                                   Map<String, String> texts) {
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
            log.error("发送http请求异常",e);
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