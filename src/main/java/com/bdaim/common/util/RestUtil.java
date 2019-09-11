package com.bdaim.common.util;

import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RestUtil {
	private static final Log log = LogFactory.getLog("REST");
	
	public static String getResponseString(InputStream is) throws Exception{
		byte[] data = read(is);
		String result = new String(data);
		return result;
	}
	
	public static byte[] read(InputStream inStream) throws Exception {
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        byte[] buffer = new byte[1024];
	        int len = 0;
	        while ((len = inStream.read(buffer)) != -1) {
	            outputStream.write(buffer);
	        }
	        inStream.close();
	        return outputStream.toByteArray();
	    }
	

	public static String  getData(String authPath){
		String result="";
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(authPath).openConnection();
			String line="";
			InputStream is = conn.getInputStream();
			result = getResponseString(is);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("获取rest服务所提供端口的信息:"+e.getMessage());
		}
		return result;
	}
	
	public static String postDataWithParms(JSONObject jsonObj, String url){
		String result="";
		log.info(url+" "+jsonObj);
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers=new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<JSONObject> request=new HttpEntity<JSONObject>(jsonObj,headers);
			result=restTemplate.postForObject(url, request, String.class);
			log.info(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("rest error:"+e.getMessage());
		}
		return result;
	}
	

}
