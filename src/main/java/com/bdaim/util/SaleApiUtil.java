package com.bdaim.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.callcenter.dto.SignAuthorizationResult;
import com.bdaim.util.http.HttpUtil;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author duanliying
 * @date 2019/1/14
 * @description
 */
public class SaleApiUtil {
    private static Logger LOG = LoggerFactory.getLogger(SaleApiUtil.class);
    public final static int ENV = 2;
    /**
     * 正式环境请求地址
     */
    public final static String PRD_BASE_URL = "http://api.ytx.net";
    /**
     * 沙箱环境请求地址
     */
    public final static String SANDBOX_BASE_URL = "http://sandbox.ytx.net";

    /**
     * 云通信平台用户账户Id：对应管理控制台中的 ACCOUNT SID
     */
    public static String accountSid = "e7459a8d5f4844939bb139e5da36e268";
    /**
     * 云通信平台用户账户授权令牌：对应管理控制台中的 AUTH TOKEN
     */
    public static String authToken = "1ed582d1035844f1b8231db10c6d61e4";

    /**
     * 云通信平台营销短信用户账户Id：对应管理控制台中的 ACCOUNT SID
     */
    public static String market_accountSid = "498edd8b309d48078575cd46dc409ecd";
    /**
     * 云通信平台营销短信用户账户授权令牌：对应管理控制台中的 AUTH TOKEN
     */
    public static String market_authToken = "dbcf67b401f24d6db97653bf09c1722b";

    public static final String MARKET_SMS_APP_ID = "767ad08b784548e4825fe75b6be03f9c";

    public final static String MARKET_SP_UID = "555";

    public final static String MARKET_SP_PWD = "630294";

    public static final String API_VERSION = "201512";

    public static final String TEMPLATE_SMS_ACTION = "templateSms";

    public static final String DAIL_BACK_CALL_ACTION = "callDailBack";

    /**
     * 增量方式话单——getCdrByResId
     */
    public static final String GET_CALL_BACK_RECORD_BY_RES_ID = "getCdrByResId";

    /**
     * 某小时话单——getCdrByTime
     */
    public static final String GET_CALL_BACK_RECORD_BY_TIME = "getCdrByTime";


    /**
     * 短信appId
     * APPID---短信AppId
     */
    public static final String SMS_APP_ID = "c217790f22634c288dd6a917dc809722";

    /**
     * 闪信appId
     */
    public static final String FLASH_APP_ID = "c217790f22634c288dd6a917dc809722";

    /**
     * 失联修复双向回呼appId
     */
    public static final String NOLOSE_CALL_BACK_APP_ID = "0b9612e9f8f24b6e81c3527ce6318eaf";

    /**
     * 精准营销双向回呼appId
     */
    public static final String ONLINE_CALL_BACK_APP_ID = "c217790f22634c288dd6a917dc809722";

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");


    /**
     * 短信
     */
    public final static int SMS_TYPE = 1;

    /**
     * 闪信
     */
    public final static int FLASH_TYPE = 2;

    /**
     *短信appId
     */

    /**
     * 接口请求包头模板地址<br/>
     * <pre>
     * {0}-version 云通信API接口版本 目前可选版本：201512
     * {1}-accountSID 云通信平台用户账户Id：对应管理控制台中的 ACCOUNT SID
     * {2}-func 功能所属分类call【语音类】/sms【消息类】/traffic【流量类】/account【账户类】 当前功能属：sms
     * {3}-funcURL 业务功能的各类具体操作分支 当前功能属：TemplateSMS.wx
     * {4}-Sign 云通信平台API接口，API 验证参数 ：MD5加密（账户Id + 账户授权令牌 +时间戳)   *URL后必须带有Sign参数，例如：Sign=AAABBBCCCDDDEEEFFFGGG   *时间戳需与Authorization中时间戳相同  注:MD5加密32位,无论大小写
     * </pre>
     */
    public final static String REQUEST_TEMP_URL = "/{0}/sid/{1}/{2}/{3}?Sign={4}";

    /**
     * 呼叫中心api地址
     */
    public static final String CALL_CENTER_API = "http://api.salescomm.net:8017";

    /**
     * 讯众外呼pdf版地址
     */
    public static final String CALL_CENTER_API_PDF = "http://api.salescomm.net:8012";


    public enum FunctionType {
        CALL("call"),
        CALL_RECORD("call"),
        SMS("sms"),
        TRAFFIC("traffic"),
        ACCOUNT("account");

        private String value;

        FunctionType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum FunctionServiceUrl {
        TEMPLATE_SMS("TemplateSMS.wx"),
        DAIL_BACK_CALL("DailbackCall.wx"),
        CALL_RECORD("CallCdr.wx");

        private String value;

        FunctionServiceUrl(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }


    /**
     * @param jsonBody 参数 示例:{"action":"callDailBack","src":"13911281234","dst":"13522855865","appid":"ff8080813fc70a7b013fc72312324213","credit":"3600"}
     * @param env      环境 1-沙箱环境 2-正式环境
     * @return java.lang.String
     * @author chengning@salescomm.net
     * @date 2018/7/26 16:49
     */
    public static String sendCallBack(String jsonBody, int env) throws RuntimeException {
        String url;
        // 构造authorization和sign参数
        SignAuthorizationResult signAuthorizationResult = createAuthorizationAndSign();
        if (signAuthorizationResult != null) {
            // 沙箱环境
            if (env == 1) {
                url = SANDBOX_BASE_URL + MessageFormat.format(REQUEST_TEMP_URL, API_VERSION, accountSid, FunctionType.CALL.getValue(), FunctionServiceUrl.DAIL_BACK_CALL.getValue(), signAuthorizationResult.getSign());
                //正式环境
            } else {
                url = PRD_BASE_URL + MessageFormat.format(REQUEST_TEMP_URL, API_VERSION, accountSid, FunctionType.CALL.getValue(), FunctionServiceUrl.DAIL_BACK_CALL.getValue(), signAuthorizationResult.getSign());
            }
            return send(url, jsonBody, signAuthorizationResult.getAuthorization());
        } else {
            throw new RuntimeException("构造sign和authorization异常");
        }
    }

    public static String sendCallBackRecord(String jsonBody, int env) throws RuntimeException {
        String url;
        // 构造authorization和sign参数
        SignAuthorizationResult signAuthorizationResult = createAuthorizationAndSign();
        if (signAuthorizationResult != null) {
            // 沙箱环境
            if (env == 1) {
                url = SANDBOX_BASE_URL + MessageFormat.format(REQUEST_TEMP_URL, API_VERSION, accountSid, FunctionType.CALL_RECORD.getValue(), FunctionServiceUrl.CALL_RECORD.getValue(), signAuthorizationResult.getSign());
                //正式环境
            } else {
                url = PRD_BASE_URL + MessageFormat.format(REQUEST_TEMP_URL, API_VERSION, accountSid, FunctionType.CALL_RECORD.getValue(), FunctionServiceUrl.CALL_RECORD.getValue(), signAuthorizationResult.getSign());
            }
            return send(url, jsonBody, signAuthorizationResult.getAuthorization());
        } else {
            throw new RuntimeException("构造sign和authorization异常");
        }
    }

    public static String sendSms(String jsonBody, int env) throws RuntimeException {
        String url;
        // 构造authorization和sign参数
        SignAuthorizationResult signAuthorizationResult = createAuthorizationAndSign();
        if (signAuthorizationResult != null) {
            // 沙箱环境
            if (env == 1) {
                url = SANDBOX_BASE_URL + MessageFormat.format(REQUEST_TEMP_URL, API_VERSION, accountSid, FunctionType.SMS.getValue(), FunctionServiceUrl.TEMPLATE_SMS.getValue(), signAuthorizationResult.getSign());
                //正式环境
            } else {
                url = PRD_BASE_URL + MessageFormat.format(REQUEST_TEMP_URL, API_VERSION, accountSid, FunctionType.SMS.getValue(), FunctionServiceUrl.TEMPLATE_SMS.getValue(), signAuthorizationResult.getSign());
            }
            return send(url, jsonBody, signAuthorizationResult.getAuthorization());
        } else {
            throw new RuntimeException("构造sign和authorization异常");
        }
    }

    public static String sendMarketSms(String jsonBody, int env) throws RuntimeException {
        String url;
        // 构造authorization和sign参数
        SignAuthorizationResult signAuthorizationResult = createAuthorizationAndSignByMarket();
        if (signAuthorizationResult != null) {
            // 沙箱环境
            if (env == 1) {
                url = SANDBOX_BASE_URL + MessageFormat.format(REQUEST_TEMP_URL, API_VERSION, market_accountSid, FunctionType.SMS.getValue(), FunctionServiceUrl.TEMPLATE_SMS.getValue(), signAuthorizationResult.getSign());
                //正式环境
            } else {
                url = PRD_BASE_URL + MessageFormat.format(REQUEST_TEMP_URL, API_VERSION, market_accountSid, FunctionType.SMS.getValue(), FunctionServiceUrl.TEMPLATE_SMS.getValue(), signAuthorizationResult.getSign());
            }
            return send(url, jsonBody, signAuthorizationResult.getAuthorization());
        } else {
            throw new RuntimeException("构造sign和authorization异常");
        }
    }

    private static String send(String url, String jsonBody, String authorization) throws RuntimeException {
        String result;
        try {
            Map<String, Object> headers = new HashMap<>(16);
            headers.put("Authorization", authorization);
            headers.put("Content-Type", "application/x-www-from-urlencoded");
            result = HttpUtil.httpPost(url, jsonBody, headers);
        } catch (Exception e) {
            throw new RuntimeException("发送请求失败", e);
        }
        return result;
    }

    private static String send(String url, String jsonBody, String authorization, int timeout) throws RuntimeException {
        String result;
        try {
            Map<String, Object> headers = new HashMap<>(16);
            headers.put("Authorization", authorization);
            headers.put("Content-Type", "application/x-www-from-urlencoded");
            result = HttpUtil.httpPost(url, jsonBody, headers, timeout);
        } catch (Exception e) {
            throw new RuntimeException("发送请求失败", e);
        }
        return result;
    }


    /**
     * 构造authorization和sign参数,并且赋值给smsParam对象
     *
     * @return void
     * @author chengning@salescomm.net
     * @date 2018/7/26 15:23
     */
    public static SignAuthorizationResult createAuthorizationAndSign() {
        SignAuthorizationResult signAuthorizationResult = new SignAuthorizationResult();
        String dataTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        byte[] authorization;
        try {
            authorization = Base64.encodeBase64(MessageFormat.format("{0}|{1}", accountSid, dataTime).getBytes("UTF-8"));
            // 设置authorization
            signAuthorizationResult.setAuthorization(new String(authorization, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String sign = DigestUtils.md5Hex(MessageFormat.format("{0}{1}{2}", accountSid, authToken, dataTime)).toUpperCase();
        // 设置sign
        signAuthorizationResult.setSign(sign);
        return signAuthorizationResult;
    }

    public static SignAuthorizationResult createAuthorizationAndSignByMarket() {
        SignAuthorizationResult signAuthorizationResult = new SignAuthorizationResult();
        String dataTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        byte[] authorization;
        try {
            authorization = Base64.encodeBase64(MessageFormat.format("{0}|{1}", market_accountSid, dataTime).getBytes("UTF-8"));
            // 设置authorization
            signAuthorizationResult.setAuthorization(new String(authorization, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String sign = DigestUtils.md5Hex(MessageFormat.format("{0}{1}{2}", market_accountSid, market_authToken, dataTime)).toUpperCase();
        // 设置sign
        signAuthorizationResult.setSign(sign);
        return signAuthorizationResult;
    }

    /**
     * 获取呼叫中心的API的token
     *
     * @param companyId
     * @return java.lang.String
     * @author chengning@salescomm.net
     * @date 2018/7/31 11:31
     */
    public static String getCallCenterToken(String companyId) {
        // "http://api.salescomm.net:8017/GetToken?compid={0}"
        Map<String, Object> params = new HashMap<>(16);
        params.put("compid", companyId);
        Map<String, Object> headers = new HashMap<>(16);
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        String token = HttpUtil.httpGet(CALL_CENTER_API + "/GetToken", params, headers);
        if (token != null && !"".equals(token)) {
            return JSON.parseObject(token).getString("token");
        }
        throw new NullPointerException("token is null");
    }

    /**
     * 获取body请求体的加密base64字符串
     *
     * @param map 请求参数
     * @return java.lang.String
     * @author chengning@salescomm.net
     * @date 2018/7/31 11:35
     */
    private static String getCenterBodyBase64Str(Map<String, Object> map) throws Exception {
        String data = "";
        for (Map.Entry<String, Object> param : map.entrySet()) {
            data += param.getKey() + param.getValue() + "&";
        }
        data = data.substring(0, data.length() - 1);
        //data = "compid811118&agentid1001&deviceid18811526913&type1&shownumber&callback";
        String[] params = data.split("&");
        List<String> paramList = new ArrayList<>();
        for (String param : params) {
            paramList.add(param);
        }
        params = paramList.toArray(new String[paramList.size()]);
        Arrays.sort(params);
        String text = StringUtils.join(params, "");
        String md5 = DigestUtils.md5Hex(text.getBytes("UTF-8"));
        String base64Str = Base64.encodeBase64String(md5.getBytes("UTF-8"));
        return base64Str;
    }

    /**
     * 获取请求authorization
     *
     * @param method
     * @param accept
     * @param base64Str
     * @param contentType
     * @return java.lang.String
     * @author chengning@salescomm.net
     * @date 2018/7/31 11:35
     */
    private static String getCallCenterAuthorization(String method, String accept, String base64Str, String contentType) throws Exception {
        return DigestUtils.sha1Hex((method + accept + base64Str + contentType).getBytes("UTF-8"));
    }

    /**
     * 讯众呼叫中心接口请求公共方法
     * @param apiUrl
     * @param map
     * @param token
     * @return
     * @throws Exception
     */
    public static String callAgentCallOut(String apiUrl, Map<String, Object> map, String token) throws Exception {
        String sha1 = SaleApiUtil.getCallCenterAuthorization("POST", "application/json", SaleApiUtil.getCenterBodyBase64Str(map), "application/json");
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        headers.put("Authorization", sha1);
        headers.put("Token", token);
        String result = HttpUtil.httpPost(SaleApiUtil.CALL_CENTER_API + apiUrl, JSON.toJSONString(map), headers);
        return result;
    }

    /**
     * 根据讯众接口查询号码标记
     *
     * @param phoneNumber
     * @return {"statusCode":"0","requestId":"20190536493972024149934080901","statusMsg":"提交成功","data":{"phone":"01053182579","tagType":"广告推销","tagAmount":"83","tagSource":"1"}}
     */
    public static String getPhoneNumberTagByXz(String phoneNumber) {
        String url = "http://sandbox.ytx.net:8080/201612/sid/e7459a8d5f4844939bb139e5da36e268/haoma/NumberTag.wx?Sign=";
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("action", "viewNumberTag");
        jsonBody.put("phone", phoneNumber);
        jsonBody.put("tagSource", "1");
        jsonBody.put("appid", "bef04fd6fa6e4942bc4bf716da09b5a0");

        // 构造authorization和sign参数
        SignAuthorizationResult authorizationResult = new SignAuthorizationResult();
        String dataTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        byte[] authorization;
        try {
            authorization = Base64.encodeBase64(MessageFormat.format("{0}|{1}", "e7459a8d5f4844939bb139e5da36e268", dataTime).getBytes("UTF-8"));
            // 设置authorization
            authorizationResult.setAuthorization(new String(authorization, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String sign = DigestUtils.md5Hex(MessageFormat.format("{0}{1}{2}", "e7459a8d5f4844939bb139e5da36e268", "1ed582d1035844f1b8231db10c6d61e4", dataTime)).toUpperCase();
        String result = null;
        if (authorizationResult != null) {
            // 沙箱环境
            url += sign;
            result = send(url, jsonBody.toJSONString(), authorizationResult.getAuthorization(), 500);
            LOG.info("通过讯众接口查询号码标记返回结果:" + result);
            return result;
        } else {
            LOG.warn("构造sign和authorization异常");
        }
        return result;
    }

    public static void main(String[] args) {
        List<String> phones = new ArrayList<>();
        phones.add("01053182579");
        phones.add("01053189655");
        phones.add("01053189656");
        phones.add("01053189657");
        phones.add("01053189658");
        phones.add("01053189659");
        phones.add("01053189660");
        phones.add("01053189661");
        phones.add("01053189662");
        phones.add("01053189707");
        for (String phone : phones) {
            String result = getPhoneNumberTagByXz(phone);
            JSONObject jsonObject = JSON.parseObject(result);
            JSONObject data = jsonObject.getJSONObject("data");
            System.out.println("号码:" + data.getString("phone") + ",类型:" + data.getString("tagType") + ",标记次数:" + data.getString("tagAmount"));
        }

    }


    public void testHD() throws Exception {
        String comId = "933333";
        Map<String, Object> map = new HashMap<>();
        map.put("compid",comId);
        map.put("actions","time");
        map.put("lastid",0);
        map.put("limit",5000);
        map.put("pdate","2019-06-14");
        map.put("ptime","16");
        map.put("anytype",2);

        String result = SaleApiUtil.callAgentCallOut("/api/callcenter/GetCdrAnyCallRecord", map, SaleApiUtil.getCallCenterToken(comId));
        System.out.println(result);
    }
}
