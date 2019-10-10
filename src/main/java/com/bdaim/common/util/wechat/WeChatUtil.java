package com.bdaim.common.util.wechat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class WeChatUtil {

    protected final static Logger logger = LoggerFactory.getLogger(WeChatUtil.class);

    private static boolean isStart = false;

    /**
     * 微信Access_Token刷新地址
     */
    private String uri = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
    /**
     * 存放Access_Token的标识符
     */
    private String tokenKey = "WEIXIN_ACCESS_TOKEN";
    /**
     * 最近一次的Access_Token
     */
    private static AccessToken lastAccessToken;

    /**
     * 微信分配的appID
     */
    //@Value("${app.id}")
    private String appId = "wx665fb599d9d40b51";
    /**
     * 微信分配的appsecret
     */
    //@Value("${app.secrect}")
    private String appSecret = "011c20df61c85b6fcd825c8a346b76e5";

    private long lastRunTime;

    public static void main(String[] args) {
        new WeChatUtil().sendTempMsg("oFqgzwFGhEAqH-aRgI6YYJTti1oc", "BHAXK1_KvuS2tzbB5wZx_DUNuU8VJhbsP5nOrHjDtUI", "", "#FF0000", "{\"name\":{\"value\":\"开发人员\",\"color\":\"#173177\"},\"time\":{\"value\":\"2019-10-10 14:58:33\",\"color\":\"#173177\"},\"content\":{\"value\":\"test\",\"color\":\"#173177\"}}");
    }

    public boolean sendTempMsg(String toUser, String templateId, String url, String topColor, String data) {
        Map param = new HashMap();
        param.put("touser", toUser);
        param.put("template_id", templateId);
        param.put("url", url);
        param.put("topcolor", topColor);
        param.put("data", JSON.parseObject(data));
        try {
            refresh();
            Map header = new HashMap();
            header.put("Content-Type", "application/json");
            logger.info("发送微信模板消息请求参数:{},accesstoken:{}", param, lastAccessToken.getAccessToken());
            String result = HttpUtil.httpPost("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + lastAccessToken.getAccessToken(), JSON.toJSONString(param), header);
            logger.info("发送微信模板消息返回结果:", result);
            if (StringUtil.isNotEmpty(result) && JSON.parseObject(result).getIntValue("errcode") == 0) {
                return true;
            }
        } catch (Exception e) {
            logger.error("发送微信模板消息异常", e);
        }
        return false;
    }

    /**
     * 刷新Access_Token
     *
     * @return boolean 刷新是否成功
     */
    private boolean refresh() {
        boolean refresh = this.refresh(false);
        //重置最后一次定时器运行时间
        lastRunTime = System.currentTimeMillis();
        return refresh;
    }


    /**
     * 刷新Access_Token
     *
     * @param forced 是否强制
     * @return boolean 刷新是否成功
     */
    private boolean refresh(boolean forced) {
        if (!(forced || this.isNeedRefresh())) {
            return false;
        }

        AccessToken token = this.getWeixinAccessToken();
        //把token存入lastAccessToken,便于下次检验token是否有效
        lastAccessToken = token;
        return true;
    }

    /**
     * 请求微信接口获取最新的access_token信息
     *
     * @return AccessToken
     */
    private AccessToken getWeixinAccessToken() {
        String uri = this.uri.replace("APPID",
                this.getAppId()).replace("APPSECRET",
                this.getAppSecret());
        String buffer = HttpUtil.httpGet(uri, null, null);
        //返回的参数是json格式
        JSONObject jsonObject = JSON.parseObject(buffer);
        AccessToken token = new AccessToken();
        if (!jsonObject.containsKey("access_token")) {
            logger.warn(jsonObject.toJSONString());
        }
        String accessToken = jsonObject.getString("access_token");
        String expiresInStr = jsonObject.getString("expires_in");
        int expiresIn = Integer.parseInt(expiresInStr);
        token.setAccessToken(accessToken);
        token.setExpiresIn(expiresIn);
        //设置token的更新时间
        token.setUpdateTime(new Date());
        logger.info("最新微信token:" + accessToken);
        return token;
    }

    /**
     * 是否需要刷新
     *
     * @return boolean 判断是否需要刷新
     */
    private boolean isNeedRefresh() {
        AccessToken token = this.getLastAccessToken();
        if (null == token) {
            return true;
        }
        //当前系统时间
        long now = System.currentTimeMillis();
        //此次运行和上次运行的时间间隔,5min
        long interval = now - this.lastRunTime;
        //上次token的更新时间
        long lastTimeMillis = this.getLastTimeMillis();
        //此处在微信规定7200秒的基础上减去1800秒，即度过1h30m的时间，就认为token已失效，以此保证token的有效性
        int expiresIn = 5400 * 1000;
        /*如果时间间隔不足以支撑到下次运算时的超时时间，则会话会在本次被刷新*/
//        logger.info("判断是否需要刷新开始---------");
//        logger.info("                   此时此刻 now            ---------" + now);
//        logger.info("此次运行和上次运行的时间间隔 interval       ---------" + interval);
//        logger.info("        上次token的更新时间 lastTimeMillis ---------" + lastTimeMillis);
//        logger.info("               token有效期  expiresIn      ---------" + expiresIn);
//        logger.info("计算表达式(lastTimeMillis + expiresIn) <= (now + interval)---------" + ((lastTimeMillis + expiresIn) <= (now + interval)));
        if ((lastTimeMillis + expiresIn) <= (now + interval)) {
            return true;
        }
        return false;
    }

    private String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    private String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    private AccessToken getLastAccessToken() {
        if (this.lastAccessToken == null) {
            return null;
        }
        return this.lastAccessToken;
    }

    private long getLastTimeMillis() {
        AccessToken token = this.getLastAccessToken();
        Date updateTime = token.getUpdateTime();
        if (updateTime == null) {
            return 0L;
        }
        long millis = updateTime.getTime();
        return millis;
    }

    @PostConstruct
    private void init() {
        if (!isStart) {
            isStart = true;
            logger.info("启动微信token刷新服务");
            this.refresh();
        }
    }

    class AccessToken {
        Date updateTime;
        int expiresIn;
        String accessToken;

        public Date getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Date updateTime) {
            this.updateTime = updateTime;
        }

        public int getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(int expiresIn) {
            this.expiresIn = expiresIn;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
