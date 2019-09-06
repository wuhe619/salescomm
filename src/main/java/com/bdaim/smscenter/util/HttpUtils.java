package com.bdaim.smscenter.util;

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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpUtils {
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

	// public static String httpsPost(String strUrl, String strReq) throws
	// Exception {
	// TrustManager[] tm = { new TrustAnyTrustManager() };
	// SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
	// sslContext.init(null, tm, new java.security.SecureRandom());
	// SSLSocketFactory ssf = sslContext.getSocketFactory();
	// URL url = new URL(strUrl);
	// HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	// conn.setSSLSocketFactory(ssf);
	// conn.setRequestMethod("POST");
	// conn.setRequestProperty("Content-Type", "application/json");
	// conn.setDoOutput(true);
	// conn.setDoInput(true);
	// conn.setUseCaches(false);
	// OutputStream out = conn.getOutputStream();
	// out.write(strReq.getBytes("UTF-8"));
	// out.flush();
	// out.close();
	// InputStream in = conn.getInputStream();
	// ByteArrayOutputStream res = new ByteArrayOutputStream();
	// int i = -1;
	// while ((i = in.read()) != -1) {
	// res.write(i);
	// }
	// res.flush();
	// res.close();
	// conn.disconnect();
	// return new String(new String(res.toByteArray(), "UTF-8"));
	// }

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

	public static String httpGet(String strUrl, Map<String, Object> params, Map<String, Object> headers) throws Exception {
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
			for (Map.Entry<String, Object> header : headers.entrySet()) {
				httpGet.setHeader(header.getKey(), String.valueOf(header.getValue()));
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
}