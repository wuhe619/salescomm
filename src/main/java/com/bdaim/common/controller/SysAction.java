package com.bdaim.common.controller;


import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.auth.service.impl.TokenServiceImpl;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.SystemConfigService;
import com.bdaim.util.StringUtil;
import com.bdaim.util.http.HttpUtil;
import org.apache.commons.httpclient.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/sys")
public class SysAction extends BasicAction {

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * token列表
     */
    @ResponseBody
    @RequestMapping("/tokens")
    public List listTokens() throws Exception {
        LoginUser lu = super.opUser();
        if (!"admin".equals(lu.getName())) {
            throw new TouchException("401", "auth is error");
        }

        List data = new ArrayList();
        Map tokens = TokenServiceImpl.listTokens();
        Iterator keys = tokens.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            LoginUser u = (LoginUser) tokens.get(key);
            Map d = new HashMap();
            d.put("tokenid", lu.getTokenid());
            d.put("tokentime", DateUtil.formatDate(new Date(lu.getTokentime())));
            d.put("name", lu.getName());
        }
        return data;
    }

    /**
     * 获取讯众呼叫中心agent配置
     *
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/xzagentconfig")
    public String xzAgentConfig(@RequestParam Map map) {
        int port = 8200;
        String http = "http";
        if ("wss".equals(String.valueOf(map.get("wstype")))) {
            port = 8201;
            http = "https";
        }
        String url = "{http}://api.salescomm.net:{port}/Handler/agent.ashx?action={action}&compid={compid}&agentid={agentid}&serverid={serverid}&wstype={wstype}&_=1585645347517";
        String jsonp = HttpUtil.httpGet(url.replace("{action}", String.valueOf(map.get("action")))
                .replace("{compid}", String.valueOf(map.get("compid")))
                .replace("{agentid}", String.valueOf(map.get("agentid")))
                .replace("{wstype}", String.valueOf(map.get("wstype")))
                .replace("{serverid}", String.valueOf(map.get("serverid")))
                .replace("{port}", String.valueOf(port))
                .replace("{http}", http), null, null);
        if (StringUtil.isNotEmpty(jsonp)) {
            return StringUtil.parseJSONP(jsonp).toJSONString();
        } else {
            return new JSONObject().toJSONString();
        }
    }

    @ResponseBody
    @RequestMapping("/updateConfig")
    public String updateConfig(String key, String value) {
        int code = systemConfigService.saveSystemConfig(key, value);
        JSONObject json = new JSONObject();
        json.put("code", code);
        return json.toJSONString();
    }
}
