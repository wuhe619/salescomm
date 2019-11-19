package com.bdaim.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.callcenter.dto.UnicomSendSmsParam;
import com.bdaim.util.http.HttpUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author duanliying
 * @date 2019/10/23
 * @description 联通相关接口工具类 V1
 */
public class UnicomUtil {
    private static Logger LOG = LoggerFactory.getLogger(UnicomUtil.class);
    private final static String UNICOM_BASE_URL_V1 = "http://120.52.23.243:10080/sdyxinterface/20190426/";

    /**
     * 获取企业所有活动接口
     * @param pwd
     * @param entId
     * @param key
     * @return
     * @throws Exception
     */
    public static JSONObject getEntActivityAll(String pwd, String entId, String key) throws Exception {
        //获取token,加密获取sign
        String sign = getSign(pwd, entId, key);
        LOG.info("sign:" + sign);
        Map<String, Object> headers = new HashMap<>(16);
        headers.put("Sig", sign);
        headers.put("Content-Type", "application/json;charset=utf-8");
        Map<String, String> params = new HashMap<>();
        params.put("entPassword", pwd);
        LOG.info("联通接口请求地址是：" + UNICOM_BASE_URL_V1 + "activity/getEntActivityAll/" + entId + ",参数:" + params.toString());
        String result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "activity/getEntActivityAll/" + entId, params, headers);
        LOG.info("联通结果返回:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 获取失联修复匹配结果接口
     * @param activityId
     * @param entId
     * @param key
     * @return
     * @throws Exception
     */
    public static JSONObject getEntActivityResult(String activityId, String entId, String key) throws Exception {
        //获取token,加密获取sign
        String sign = getSign(activityId, entId, key);
        LOG.info("sign:" + sign);
        Map<String, Object> headers = new HashMap<>(16);
        headers.put("Sig", sign);
        headers.put("Content-Type", "application/json;charset=utf-8");
        Map<String, String> params = new HashMap<>();
        params.put("activity_id", activityId);
        LOG.info("联通接口请求地址是：" + UNICOM_BASE_URL_V1 + "shortUrl/bindShortLink/" + entId + ",参数:" + params.toString());
        String result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "shortUrl/bindShortLink/" + entId, params, headers);
        LOG.info("联通结果返回:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 联通坐席外呼接口v1
     * dataId 联通返回唯一id    callerNumber 主叫号码   showNumber   外显号（不传默认从企业外显号码池随机一个外显号码 key加密私钥
     */
    public static Map<String, Object> unicomSeatMakeCall(String entId, String dataId, String entPassWord, String callerNumber, String showNumber, String key) {
        LOG.info("坐席外呼接收參數:entId是" + entId + "数据id是：" + dataId + "企业密码：" + entPassWord + "主叫号：" + callerNumber + "外显号码是： " + showNumber + "密钥：" + key);
        Map<String, String> params = new HashMap<>();
        params.put("dataId", dataId);
        params.put("callerNumber", callerNumber);
        params.put("showNumber", showNumber);
        String result;
        try {
            Map<String, Object> headers = new HashMap<>(16);
            //获取请求token
            if (StringUtil.isEmpty(entPassWord)) {
                entPassWord = "111111";
            }
            String token = unicomGetToken(entPassWord, entId);
            //String token = "1111";
            //签名处理
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String sysTime = df.format(new Date());
            String parmToken = token + sysTime;
            LOG.info("加密数据是:" + parmToken);
            String sgin = encryptThreeDESECB(URLEncoder.encode(parmToken, "UTF-8"), key);
            LOG.info("加密结果是:" + sgin);
            headers.put("Sig", sgin);
            headers.put("Content-Type", "application/json;charset=utf-8");
            LOG.info("联通外呼接口请求地址是：" + UNICOM_BASE_URL_V1 + "call/makeCall/" + entId + " 联通坐席外呼参数:" + params.toString());
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "call/makeCall/" + entId, params, headers);
            //result="{\"msg\":\"success\",\"code\":\"01000\",\"data\":{\"callId\":\"1539516627199289711\",\"msg\":\"呼叫成功\",\"code\":\"000\",\"uuid\":\"01539516627199289733\",\"callNum\":\"1/600\",\"todayCallNum\":\"1/30\",\"monthCallNum\":\"1/90\"}}";
            LOG.info("联通坐席外呼返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通坐席外呼失败:", e);
            throw new RuntimeException("联通坐席外呼失败", e);
        }
        return null;
    }

    /**
     * 联通坐席短信接口v1
     *
     * @desc:联通短信触达 wordId 话术码  variableOne  变量标识1  variableTwo 变量标识2 variableThree 变量标识3 variableFour 变量标识4 dataList 发送短信加密手机号集合 key加密私钥
     */
    public static Map<String, Object> unicomSeatMakeSms(UnicomSendSmsParam unicomSendSmsParam) {
        LOG.info("联通坐席短信接口 请求参数是" + unicomSendSmsParam.toString());
        String entId = unicomSendSmsParam.getEntId();
        String key = unicomSendSmsParam.getKey();
        LOG.info("联通短信触达企业id是:" + entId + "密钥是：" + key);
        Map<String, String> params = new HashMap<>();
        params.put("wordId", unicomSendSmsParam.getWordId());
        params.put("dataId", unicomSendSmsParam.getDataId());
        params.put("variableOne", unicomSendSmsParam.getVariableOne());
        params.put("variableTwo", unicomSendSmsParam.getVariableThree());
        params.put("variableThree", unicomSendSmsParam.getVariableThree());
        params.put("variableFour", unicomSendSmsParam.getVariableFour());
        params.put("variableFive", unicomSendSmsParam.getVariableFive());
        String result;
        try {
            Map<String, Object> headers = new HashMap<>(16);
            //获取请求token
            String token = unicomGetToken(unicomSendSmsParam.getEntPassWord(), entId);
            //签名处理
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String sysTime = df.format(new Date());
            String parmToken = token + sysTime;
            LOG.info("加密数据是:" + parmToken);
            String sgin = encryptThreeDESECB(URLEncoder.encode(parmToken, "UTF-8"), key);
            LOG.info("加密结果是:" + sgin);
            headers.put("Sig", sgin);
            headers.put("Content-Type", "application/json;charset=utf-8");
            LOG.info("联通短信触达接口请求地址是：" + UNICOM_BASE_URL_V1 + "msg/sendMsg/" + entId + " 联通短信触达参数:" + params.toString());
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "msg/sendMsg/" + entId, params, headers);
            //result = "{\"msg\":\"success\",\"code\":\"02000\",\"data\":{\"msgNum\":\"1/10\",\"todayMsgNum\":\"1/3\",\"contactId\":\"09092049461013989641\",\"monthMsgNum\":\"1/5\"}}";
            LOG.info("联通短信触达结果返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通短信触达失败:", e);
            throw new RuntimeException("联通短信触达异常", e);
        }
        return null;
    }


    /**
     * 联通短信接口v1
     *
     * @desc:联通短信触达 wordId 话术码  variableOne  变量标识1  variableTwo 变量标识2 variableThree 变量标识3 variableFour 变量标识4 dataList 发送短信加密手机号集合 key加密私钥
     */
    public static Map<String, Object> unicomSendSms(UnicomSendSmsParam unicomSendSmsParam) {
        String entId = unicomSendSmsParam.getEntId();
        String key = unicomSendSmsParam.getKey();
        LOG.info("联通短信触达企业id是:" + entId + "密钥是：" + key);
        Map<String, String> params = new HashMap<>();
        params.put("wordId", unicomSendSmsParam.getWordId());
        params.put("exeNo", unicomSendSmsParam.getExeNo());
        params.put("variableOne", unicomSendSmsParam.getVariableOne());
        params.put("variableTwo", unicomSendSmsParam.getVariableThree());
        params.put("variableThree", unicomSendSmsParam.getVariableThree());
        params.put("variableFour", unicomSendSmsParam.getVariableFour());
        params.put("variableFive", unicomSendSmsParam.getVariableFive());
        params.put("dataList", unicomSendSmsParam.getDataList().toString());
        String result;
        try {
            Map<String, Object> headers = new HashMap<>(16);
            //获取请求token
            String entPassWord = "";
            if (StringUtil.isEmpty(unicomSendSmsParam.getEntPassWord())) {
                entPassWord = "111111";
            }
            String token = unicomGetToken(entPassWord, entId);
            //签名处理
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String sysTime = df.format(new Date());
            String parmToken = token + sysTime;
            LOG.info("加密数据是:" + parmToken);
            String sgin = encryptThreeDESECB(URLEncoder.encode(parmToken, "UTF-8"), key);
            LOG.info("加密结果是:" + sgin);
            headers.put("Sig", sgin);
            headers.put("Content-Type", "application/json;charset=utf-8");
            LOG.info("联通短信触达接口请求地址是：" + UNICOM_BASE_URL_V1 + "msg/sendTelMsg/" + entId + " 联通短信触达参数:" + params.toString());
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "msg/sendTelMsg/" + entId, params, headers);
            LOG.info("联通短信触达结果返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通短信触达失败:", e);
            throw new RuntimeException("联通短信触达异常", e);
        }
        return null;
    }

    /**
     * 设置中间号接口
     * dataId 数据id  callerNumber 主叫号码   showNumber   外显号（不传默认从企业外显号码池随机一个外显号码 key加密私钥
     */
    public static Map<String, Object> unicomSetMiddleNum(String entId, String customerId, String entPassWord, String callerNumber, String showNumber, String key) {
        Map<String, String> params = new HashMap<>();
        params.put("dataId", customerId);
        params.put("callerNumber", callerNumber);
        params.put("showNumber", showNumber);
        String result;
        try {
            Map<String, Object> headers = new HashMap<>(16);
            //获取请求token
            if (StringUtil.isEmpty(entPassWord)) {
                entPassWord = "111111";
            }
            String token = unicomGetToken(entPassWord, entId);
            //签名处理
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String sysTime = df.format(new Date());
            String parmToken = token + sysTime;
            LOG.info("加密数据是:" + parmToken);
            String sgin = encryptThreeDESECB(URLEncoder.encode(parmToken, "UTF-8"), key);
            LOG.info("加密结果是:" + sgin);
            headers.put("Sig", sgin);
            headers.put("Content-Type", "application/json;charset=utf-8");
            LOG.info("联通设置中间号接口请求地址是：" + UNICOM_BASE_URL_V1 + "singleCall/ambSetNumber/" + entId + " 设置中间号参数:" + params.toString());
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "singleCall/ambSetNumber/" + entId, params, headers);
            LOG.info("联通设置中间号返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result, Map.class);
            }
        } catch (Exception e) {
            LOG.error("联通设置中间号失败:", e);
            throw new RuntimeException("联通设置中间号异常", e);
        }
        return null;
    }

    private static String getSign(String entPassword, String entId, String key) {
        Map<String, String> params = new HashMap<>();
        params.put("entPassword", entPassword);
        String result;
        String token = null;
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("Content-Type", "application/json;charset=utf-8");
            LOG.info("联通获取token请求地址是：" + UNICOM_BASE_URL_V1 + "login/getToken/" + entId + "联通获取token参数:" + params.toString());
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "login/getToken/" + entId, params, headers);
            //result = "{ \"code\": \"000\", \"msg\": \"success\", \"data\": { \"token\": \"eyJ0eXAiOiJKV1\" } }";
            LOG.info("联通获取token联通返回结果:" + result);
            if (StringUtil.isNotEmpty(result)) {
                JSONObject jsonObject = JSON.parseObject(result);
                String code = jsonObject.getString("code");
                if ("000".equals(code)) {
                    token = jsonObject.getJSONObject("data").getString("token");
                    LOG.info("联通获取token:" + token);
                } else {
                    LOG.warn("联通获取token失败:");
                }
            }
        } catch (Exception e) {
            LOG.error("联通获取token参数失败:", e);
            throw new RuntimeException("联通获取token失败", e);
        }
        //签名处理
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String sysTime = df.format(new Date());
        String paramToken = token + sysTime;
        LOG.info("token:" + paramToken);
        String sign = null;
        try {
            sign = encryptThreeDESECB(URLEncoder.encode(paramToken, "UTF-8"), key);
        } catch (Exception e) {
            LOG.error("联通获取sign参数失败:", e);
        }
        LOG.info("sign:" + sign);
        return sign;
    }

    /**
     * 获取企业登录token
     *
     * @param entPassword
     * @param entId
     * @return
     */
    private static String unicomGetToken(String entPassword, String entId) {
        Map<String, String> params = new HashMap<>();
        params.put("entPassword", entPassword);
        String result;
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("Content-Type", "application/json;charset=utf-8");
            LOG.info("联通获取token请求地址是：" + UNICOM_BASE_URL_V1 + "login/getToken/" + entId + "联通获取token参数:" + params.toString());
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "login/getToken/" + entId, params, headers);
            //result = "{ \"code\": \"000\", \"msg\": \"success\", \"data\": { \"token\": \"eyJ0eXAiOiJKV1\" } }";
            LOG.info("联通获取token联通返回结果:" + result);
            if (StringUtil.isNotEmpty(result)) {
                JSONObject jsonObject = JSON.parseObject(result);
                String code = jsonObject.getString("code");
                if ("000".equals(code)) {
                    String token = jsonObject.getJSONObject("data").getString("token");
                    LOG.info("联通获取token:" + token);
                    return token;
                } else {
                    LOG.warn("联通获取token失败:");
                    return "";
                }
            }
        } catch (Exception e) {
            LOG.error("联通获取token参数失败:", e);
            throw new RuntimeException("联通获取token失败", e);
        }
        return null;
    }

    /**
     * token加密流程
     *
     * @param src
     * @param key
     * @return
     * @throws Exception
     */
    private static String encryptThreeDESECB(final String src, final String key) throws Exception {

        final DESedeKeySpec dks = new DESedeKeySpec(key.getBytes("UTF-8"));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        final SecretKey securekey = keyFactory.generateSecret(dks);

        final Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, securekey);
        final byte[] b = cipher.doFinal(src.getBytes());

        final BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(b).replaceAll("\r", "").replaceAll("\n", "");

    }

    /**
     * token解密流程
     *
     * @param
     * @param key
     * @return
     * @throws Exception
     */
    public static String decryptThreeDESECB(final String token, final String key) throws Exception {
        final BASE64Decoder decoder = new BASE64Decoder();
        final byte[] bytesrc = decoder.decodeBuffer(token);
        final DESedeKeySpec dks = new DESedeKeySpec(key.getBytes("UTF-8"));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        final SecretKey securekey = keyFactory.generateSecret(dks);
        final Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, securekey);
        final byte[] retByte = cipher.doFinal(bytesrc);
        return new String(retByte);
    }
    /*public static void main(String[] args) throws Exception {
        String key = "wuhwfoihfiuwbfqegiefuwfe";
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String sysTime =df.format(new Date());
        // 加密流程
        String token="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUz";
        String parmToken=token+sysTime;
        String sgin = encryptThreeDESECB(URLEncoder.encode(parmToken, "UTF-8"), key);
        System.out.println(sgin);

        // 解密流程
        String tele_decrypt = decryptThreeDESECB(sgin, key);
        System.out.println("模拟代码解密:" + tele_decrypt);

    }*/


}
