package com.bdaim.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.callcenter.dto.UnicomSendSmsParam;
import com.bdaim.customgroup.dto.UnicomCustomGroupDataDTO;
import com.bdaim.util.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author duanliying
 * @date 2019/10/23
 * @description 联通相关接口工具类 V1
 */
public class UnicomUtil {
    private static Logger LOG = LoggerFactory.getLogger(UnicomUtil.class);

    private final static String UNICOM_BASE_URL_V1 = "http://120.52.23.243:10080/sdyxinterface/20190426/";
    //private final static String UNICOM_BASE_URL_V1 = "";

    public static void main(String[] args) throws Exception {
        //JSONObject jsonObject = registerUserExtension("Bq2.g_mp4", "D1HMFW", "UkzesbWEmdTIgywsacIIboam", "18630016545");
        //LOG.warn("添加联通主叫号码返回:{}", jsonObject);
        //UnicomUtil.getEntActivityAll("Bq2.g_mp4", "D1HMFW", "UkzesbWEmdTIgywsacIIboam");
        //String fileUrl = getActivityFileUrlByName("Bq2.g_mp4", "D1HMFW", "UkzesbWEmdTIgywsacIIboam", "品创邦1次外呼1105");
        //LOG.warn("获取活动文件url:{}", fileUrl);
        //UnicomUtil.getEntActivityResult("SJYXDD191021002472", "Bq2.g_mp4", "D1HMFW", "UkzesbWEmdTIgywsacIIboam");
        //unicomSeatMakeCall("D1HMFW", "C201911051642007894_12985", "Bq2.g_mp4", "18630016545", "", "UkzesbWEmdTIgywsacIIboam");
        //handleActivityFile("http://120.52.23.243:10080/voice/group1/M00/41/15/Cr8TfV3BNe7RQEIAAAAUN6qGHL8263.txt", "", "");
    }

    /**
     * 通过名称获取文件地址
     * @param pwd
     * @param entId
     * @param key
     * @param activityName
     * @return
     * @throws Exception
     */
    public static String getActivityFileUrlByName(String pwd, String entId, String key, String activityName) throws Exception {
        if (StringUtil.isEmpty(activityName)) {
            LOG.warn("获取活动文件url,活动名称不能为空");
            return "";
        }
        JSONObject result = getEntActivityAll(pwd, entId, key);
        if (result != null && "03000".equals(result.getString("code"))) {
            JSONArray jsonArray = result.getJSONObject("data").getJSONArray("resultList");
            for (int i = 0; i < jsonArray.size(); i++) {
                if (jsonArray.getJSONObject(i).getString("ACTIVITY_NAME").equals(activityName)) {
                    LOG.info("活动名称:{},活动文件地址:{}", activityName, jsonArray.getJSONObject(i).getString("FASTDFS_URL"));
                    return jsonArray.getJSONObject(i).getString("FASTDFS_URL");
                }
            }
        }
        return "";
    }

    /**
     * 通过活动文件地址获取联通活动文件内容,转为导入客群的数据格式
     *
     * @param url
     * @param custId
     * @param customGroupId
     * @return
     */
    public static List<UnicomCustomGroupDataDTO> handleActivityFile(String url, String custId, String customGroupId) {
        List<UnicomCustomGroupDataDTO> data = new ArrayList();
        String content = HttpUtil.httpGet(url, null, null);
        LOG.info("获取活动文件url:{},custId:{},customGroupId:{},返回数据:{}", url, custId, customGroupId, content);
        if (StringUtil.isEmpty(content)) {
            return new ArrayList<>();
        }
        String[] c = content.split("\r\n");
        if (c.length == 0) {
            return new ArrayList<>();
        }
        int length = c.length;
        String[] line;
        String activityId, dataId, activityName = c[0].split("\t")[0];
        for (int i = 1; i < length; i++) {
            line = c[i].split("\t");
            activityId = line[0];
            dataId = line[1];
            data.add(new UnicomCustomGroupDataDTO(activityId, activityName, customGroupId, dataId));
        }
        return data;
    }

    /**
     * 用户注册接口(增加主叫号)
     *
     * @param pwd
     * @param entId
     * @param key
     * @param extensionNumber
     * @return
     * @throws Exception
     */
    /*public static JSONObject registerUserExtension(String pwd, String entId, String key, String extensionNumber) throws Exception {
        //获取token,加密获取sign
        String sign = getSign(pwd, entId, key);
        Map<String, Object> headers = new HashMap<>(16);
        headers.put("Sig", sign);
        headers.put("Content-Type", "application/json;charset=utf-8");
        Map<String, String> params = new HashMap<>();
        params.put("extension", extensionNumber);
        LOG.info("用户注册(主叫号)接口参数:" + UNICOM_BASE_URL_V1 + "user/registerUser/" + entId + ",参数:" + params.toString());
        String result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "user/registerUser/" + entId, JSON.toJSONString(params), headers, 5000);
        LOG.info("用户注册(主叫号)接口返回:" + result);
        return JSON.parseObject(result);
    }
*/
    /**
     * 用户移除接口(删除主叫号)
     *
     * @param pwd
     * @param entId
     * @param key
     * @param extensionNumber
     * @return
     * @throws Exception
     */
   /* public static JSONObject failureUserExtension(String pwd, String entId, String key, String extensionNumber) throws Exception {
        //获取token,加密获取sign
        String sign = getSign(pwd, entId, key);
        Map<String, Object> headers = new HashMap<>(16);
        headers.put("Sig", sign);
        headers.put("Content-Type", "application/json;charset=utf-8");
        Map<String, String> params = new HashMap<>();
        params.put("extension", extensionNumber);
        LOG.info("用户失效(主叫号)接口参数:" + UNICOM_BASE_URL_V1 + "user/failureUser/" + entId + ",参数:" + params.toString());
        String result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "user/failureUser/" + entId, JSON.toJSONString(params), headers, 5000);
        LOG.info("用户失效(主叫号)接口返回:" + result);
        return JSON.parseObject(result);
    }*/

    /**
     * 获取企业所有活动接口
     *
     * @param pwd
     * @param entId
     * @param key
     * @return
     * @throws Exception
     */
    public static JSONObject getEntActivityAll(String pwd, String entId, String key) throws Exception {
        //获取token,加密获取sign
        String sign = getSign(pwd, entId, key);
        Map<String, Object> headers = new HashMap<>(16);
        headers.put("Sig", sign);
        headers.put("Content-Type", "application/json;charset=utf-8");
        Map<String, String> params = new HashMap<>();
        params.put("entPassword", pwd);
        LOG.info("获取企业所有活动接口参数:" + UNICOM_BASE_URL_V1 + "activity/getEntActivityAll/" + entId + ",参数:" + params.toString());
        String result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "activity/getEntActivityAll/" + entId, JSON.toJSONString(params), headers, 5000);
        LOG.info("获取企业所有活动接口返回:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 获取失联修复匹配结果接口
     *
     * @param dataId
     * @param pwd
     * @param entId
     * @param key
     * @return
     * @throws Exception
     */
    public static JSONObject getEntActivityResult(String dataId, String pwd, String entId, String key) throws Exception {
        //获取token,加密获取sign
        String sign = getSign(pwd, entId, key);
        LOG.info("sign:" + sign);
        Map<String, Object> headers = new HashMap<>(16);
        headers.put("Sig", sign);
        headers.put("Content-Type", "application/json;charset=utf-8");
        Map<String, String> params = new HashMap<>();
        params.put("activity_id", dataId);
        LOG.info("联通接口请求地址是：" + UNICOM_BASE_URL_V1 + "shortUrl/bindShortLink/" + entId + ",参数:" + params.toString());
        String result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "shortUrl/bindShortLink/" + entId, JSON.toJSONString(params), headers, 5000);
        LOG.info("联通结果返回:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 联通坐席外呼接口v1
     * dataId 联通返回唯一id    callerNumber 主叫号码   showNumber   外显号（不传默认从企业外显号码池随机一个外显号码 key加密私钥
     */
    public static JSONObject unicomSeatMakeCall(String entId, String dataId, String entPassWord, String callerNumber, String showNumber, String key) {
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
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "call/makeCall/" + entId, JSON.toJSONString(params), headers, 5000);
            //result="{\"msg\":\"success\",\"code\":\"01000\",\"data\":{\"callId\":\"1539516627199289711\",\"msg\":\"呼叫成功\",\"code\":\"000\",\"uuid\":\"01539516627199289733\",\"callNum\":\"1/600\",\"todayCallNum\":\"1/30\",\"monthCallNum\":\"1/90\"}}";
            LOG.info("联通坐席外呼返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.parseObject(result);
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
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "msg/sendMsg/" + entId, JSON.toJSONString(params), headers, 5000);
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
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "msg/sendTelMsg/" + entId, JSON.toJSONString(params), headers, 5000);
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
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "singleCall/ambSetNumber/" + entId, JSON.toJSONString(params), headers, 5000);
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
        String token = unicomGetToken(entPassword, entId);
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
            result = HttpUtil.httpPost(UNICOM_BASE_URL_V1 + "login/getToken/" + entId, JSON.toJSONString(params), headers, 5000);
            //result = "{ \"code\": \"000\", \"msg\": \"success\", \"data\": { \"token\": \"eyJ0eXAiOiJKV1\" } }";
            LOG.info("联通获取token联通返回结果:" + result);
            if (StringUtil.isNotEmpty(result)) {
                JSONObject jsonObject = JSON.parseObject(result);
                String code = jsonObject.getString("code");
                if ("09000".equals(code)) {
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
