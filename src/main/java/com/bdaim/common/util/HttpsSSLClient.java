package com.bdaim.common.util;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.SecureRandom;

public class HttpsSSLClient {
	/**
	 * 获取Https 请求客户端
	 * 
	 * @return
	 */
	public static CloseableHttpClient createSSLInsecureClient() throws Exception {
		SSLContext sslcontext = createSSLContext();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);
		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		return httpclient;
	}

	private static SSLContext createSSLContext() throws Exception {
		SSLContext sslcontext = null;
		sslcontext = SSLContext.getInstance("SSL", "SunJSSE");
		sslcontext.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new SecureRandom());
		return sslcontext;
	}
}
