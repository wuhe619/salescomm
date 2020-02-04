package com.bdaim.util.http;

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
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
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

import com.bdaim.util.HttpsSSLClient;
import com.bdaim.util.StringUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 *
 *
 * @author chengning@salescomm.net
 * @date 2019/9/4 10:27
 */
public class HttpUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    /**
     * 请求方式POST Content-Type=application/json
     * @param strUrl
     * @param strReq
     * @return
     * @throws Exception
     */
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

    /**
     * https请求方式POST Content-Type=application/json,带authorization请求头
     * @param strUrl
     * @param strReq
     * @param authorization
     * @return
     * @throws Exception
     */
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

    /**
     * Http Get请求 使用超时重试机制的请求
     *
     * @param url
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

    private static int retryCount = 3;

    public static final String readContent(String url) {

        String content = "";
        int count = 0;
        while (true) {
            count++;
            try {
                content = readContend(url, 800, 800);
            } catch (Exception e) {
                content = readContend(url, 800, 800); //
            }
            if ((StringUtils.isNotBlank(content) && !content.equals("-1"))
                    || count == 5) {
                break;
            }
        }
        return content;
    }

    public static final String readContend(String url, int contimeout,
                                           int readtimeout) {
        URL url1 = null;
        BufferedReader reader = null;
        HttpURLConnection connection = null;
        try {
            url1 = new URL(url);
            connection = (HttpURLConnection) url1.openConnection();
            connection.setConnectTimeout(contimeout);
            connection.setReadTimeout(readtimeout);
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            connection.connect();

            String contentEncoding = connection.getContentEncoding();// 编码
            InputStream stream;
            if (null != contentEncoding
                    && -1 != contentEncoding.indexOf("gzip")) {
                stream = new GZIPInputStream(connection.getInputStream());
            } else if (null != contentEncoding
                    && -1 != contentEncoding.indexOf("deflate")) {
                stream = new InflaterInputStream(connection.getInputStream());
            } else {
                stream = connection.getInputStream();
            }
            reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            connection.disconnect();
            return sb.toString();
        } catch (IOException e) {
            log.error("url: " + url + ",error:" + e.getMessage());
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    log.error("读关闭错误", e1);
                }
            }

            return "-1";
        } finally {
            url1 = null;
            if (connection != null)
                connection.disconnect();
        }
    }

    /**
     * @param url
     * @return
     */
    public static InputStream _getInputStream(String url) {
        InputStream is = null;
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                // 获取服务器返回值
                HttpEntity responseEntity = response.getEntity();
                is = responseEntity.getContent();
            }
        } catch (Exception e) {
            log.error("连接失败", e);
        }
        return is;
    }

    /**
     * @param url
     * @return
     */
    public static InputStream getInputStream(String url) {
        InputStream is = null;
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient(connectionManager);
        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
        client.getHttpConnectionManager().getParams().setSoTimeout(5000);
        try {
            GetMethod get = new GetMethod(url);
            int status = client.executeMethod(get);
            if (status == org.apache.http.HttpStatus.SC_OK) {
                // 获取服务器返回值
                is = get.getResponseBodyAsStream();
            }
        } catch (Exception e) {
            log.error("连接失败", e);
        }
        return is;
    }

    public static byte[] getByte(String url) {
        byte[] data = new byte[0];
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            int code = connection.getResponseCode();
            if (code == 200 || code == 206) {
                int contentLength = connection.getContentLength();
                InputStream is = connection.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(is);
                dataInputStream.readFully(data);
                dataInputStream.close();
                is.close();
            }
        } catch (IOException e) {
            log.error("下载流异常", e);
        }
        return data;
    }

    /**
     * Http Get请求 使用超时重试机制的请求
     *
     * @param url
     * @param timeOut
     * @return
     * @throws IOException
     */
    public static String _get(String url, Integer timeOut) {
        String content = null;
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        org.apache.commons.httpclient.HttpClient hc = new org.apache.commons.httpclient.HttpClient(connectionManager);
        hc.getHttpConnectionManager().getParams().setConnectionTimeout(timeOut);
        hc.getHttpConnectionManager().getParams().setSoTimeout(timeOut);
        HttpMethod method = new GetMethod(url);
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new HttpRetryHandler(retryCount));
        try {
            int status = hc.executeMethod(method);
            if (status == 200) {
                content = extractContent(method);
            }
        } catch (HttpException e) {
            System.out.println("_get  网络请求错误");
        } catch (IOException e) {
            System.out.println("_get io读写错误");
        }
        method.releaseConnection();
        return content;
    }

    /**
     * 使用重试超时机制的post请求返回流
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
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new HttpRetryHandler(retryCount));
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
     * 使用重试超时机制的post请求
     *
     * @param url  请求的地址
     * @param args 请求的参数
     * @return
     */
    public static String _post(String url, String[] args) {
        String content = null;
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        org.apache.commons.httpclient.HttpClient hc = new org.apache.commons.httpclient.HttpClient(connectionManager);
        hc.getHttpConnectionManager().getParams().setConnectionTimeout(500);
        hc.getHttpConnectionManager().getParams().setSoTimeout(1000);
        PostMethod method = new PostMethod(url);
        method.setRequestBody(prepareParams(url, args));
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new HttpRetryHandler(retryCount));
        try {
            int status = hc.executeMethod(method);
            if (status == 200) {
                content = extractContent(method);
            }
        } catch (HttpException e) {
            System.out.println("_post  网络请求错误");
        } catch (IOException e) {
            System.out.println("_post io读写错误");
        }

        method.releaseConnection();
        return content;
    }

    public static String _postParamBinary(String url, Map<String, Object> args) {
        String content = null;
        PostMethod method = null;
        try {
            MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
            org.apache.commons.httpclient.HttpClient hc = new org.apache.commons.httpclient.HttpClient(connectionManager);
            hc.getHttpConnectionManager().getParams().setConnectionTimeout(500);
            hc.getHttpConnectionManager().getParams().setSoTimeout(1000);
            method = new PostMethod(url);
            method.setRequestBody(prepareParams(args));
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                    new HttpRetryHandler(retryCount));
            try {
                int status = hc.executeMethod(method);
                if (status == 200) {
                    content = extractContent(method);
                }
            } catch (HttpException e) {
                System.out.println("_post  网络请求错误");
            } catch (IOException e) {
                System.out.println("_post io读写错误");
            }
            return content;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            method.releaseConnection();
        }
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
            log.error("发送HTTP请求异常", e);
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

    private static org.apache.commons.httpclient.NameValuePair[] prepareParams(String url, String[] args) {
        if (args.length % 2 != 0) {
            throw new NullPointerException("参数不匹配");
        }
        List<org.apache.commons.httpclient.NameValuePair> nvps = new ArrayList<org.apache.commons.httpclient.NameValuePair>();
        int pairCount = args.length / 2;
        for (int i = 0; i < pairCount; i++) {
            org.apache.commons.httpclient.NameValuePair nvp = new org.apache.commons.httpclient.NameValuePair(args[i * 2], args[i * 2 + 1]);
            nvps.add(nvp);
        }
        return nvps.toArray(new org.apache.commons.httpclient.NameValuePair[nvps.size()]);
    }

    private static org.apache.commons.httpclient.NameValuePair[] prepareParams(Map<String, Object> args) throws IOException {
        org.apache.commons.httpclient.NameValuePair[] p = new org.apache.commons.httpclient.NameValuePair[args.size()];
        int index = 0;
        for (String oneo : args.keySet()) {
            String onestr = StringUtil.objectToBinaryString(args.get(oneo));
            org.apache.commons.httpclient.NameValuePair pair = new org.apache.commons.httpclient.NameValuePair(oneo, onestr);
            p[index++] = pair;
        }
        return p;
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

    public static String httpGet(String strUrl, Map<String, Object> params, Map<String, Object> headers) {
        org.apache.http.client.HttpClient httpClient;
        HttpGet httpGet;
        String result;
        HttpResponse response = null;
        try {
            httpClient = HttpClientBuilder.create().build();
            if (params != null && params.size() > 0) {
                List<org.apache.http.NameValuePair> nameValuePairArrayList = new ArrayList<>();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    nameValuePairArrayList.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
                }
                //参数转换为字符串
                String paramsStr = EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairArrayList, "UTF-8"));
                httpGet = new HttpGet(strUrl + "?" + paramsStr);
            } else {
                httpGet = new HttpGet(strUrl);
            }
            //设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();
            httpGet.setConfig(requestConfig);

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
     * 请求方式POST Content-Type=application/json,可以自定义header参数
     * @param strUrl
     * @param strReq
     * @param headers
     * @return
     * @throws Exception
     */
    public static String httpPost(String strUrl, String strReq, Map<String, Object> headers) throws Exception {
        org.apache.http.client.HttpClient httpClient;
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
            //设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();
            httpPost.setConfig(requestConfig);

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

    /**
     * 请求方式POST Content-Type=application/json,可以自定义header参数
     * @param strUrl
     * @param strReq
     * @param headers
     * @param timeout 超时时间
     * @return
     * @throws Exception
     */
    public static String httpPost(String strUrl, String strReq, Map<String, Object> headers, int timeout) throws Exception {
        org.apache.http.client.HttpClient httpClient;
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
            //设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();
            httpPost.setConfig(requestConfig);

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

    /**
     * 请求方式POST 可以自定义header参数
     * @param strUrl
     * @param params
     * @param headers
     * @return
     * @throws Exception
     */
    public static String httpPost(String strUrl, Map<String, String> params, Map<String, Object> headers) throws Exception {
        org.apache.http.client.HttpClient httpClient;
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
            List<org.apache.http.NameValuePair> list = new ArrayList<>();
            Iterator iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> elem = (Map.Entry<String, String>) iterator.next();
                list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
            }
            //设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();
            httpPost.setConfig(requestConfig);

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

    public static void main(String[] args) {
        String[] params = new String[6];
        params[0] = "interfaceID";
        params[1] = "BQ0003";
        params[2] = "cycle";
        params[3] = "0";
        params[4] = "terms";
        params[5] = "[{'value':'男','type':'2','labelID':'100010000100001'}]";


        System.out.println(_post("http://10.11.9.118:8080/JavaAndES/labels/rest.do", params));
    }
}