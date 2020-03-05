//package com.bdaim.log.controller;
//
//import com.bdaim.util.HttpUtil;
//import com.bdaim.util.MD5Util;
//import com.bdaim.util.MD5Util2;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.InputStream;
//import java.util.HashMap;
//import java.util.Map;
//
//public class Transfer {
//
//    private final static String url = "http://161.189.102.150:8765/uid/api/uidconv";
//
//    private final static String account = "huoke";
//    private final static String access_key  = "Hk@202003";
//
//    private final static String key_type  = "imei14";
//    private final static String value_type  = "msisdn_md5";
//
//    public static void main(String[] args) throws FileNotFoundException {
//
//        Map<String, File> files = new HashMap<>();
//        Map<String, InputStream> streams = new HashMap<>();
//        Map<String, Object> headers = new HashMap<>();
//        Map<String, String> texts = new HashMap();
//
//        long timestamp = System.currentTimeMillis() / 1000;
//        String token = MD5Util2.MD5Encode(account + access_key + timestamp);
//        //上传文件路径
//        String path = "D:\\work\\idea\\idea_workspace\\api\\src\\main\\java\\com\\path\\b";
//        streams.put("file", new FileInputStream(path));
//        //参数
//        texts.put("account", account);
//        texts.put("token", token);
//        texts.put("req_time", String.valueOf(timestamp));
//        texts.put("key_type", key_type);
//        texts.put("value_type", value_type);
//        System.out.println(texts);
//        //请求头
//        headers.put("Content-Type", "application/json");
//        //发送请求
//        //String res = HttpUtil.multipartStream(url, streams, texts, headers);
//        String res = HttpUtil.postForm(url,"file", files, streams, texts);
//
//        System.out.println("调用接口返回结果：" + res);
//    }
//
//}
