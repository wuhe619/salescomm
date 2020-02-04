package com.bdaim.crm.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.util.StringUtil;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.kit.Kv;
import com.jfinal.upload.UploadFile;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.redis.RedisManager;
import com.bdaim.crm.erp.admin.service.AdminFileService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 系统配置
 *
 * @author hmb
 */
@RestController
@RequestMapping("/sysConfig")
public class AdminSysConfigController extends Controller {

    private static final String SYS_CONFIG_KEY = "sys_config";

    @Resource
    private AdminFileService adminFileService;

    /**
     * 设置系统配置
     *
     * @author hmb
     */
    @Permissions("manage:system")
    public void setSysConfig() {
        String prefix = BaseUtil.getDate();
        UploadFile file = getFile("file", prefix);
        Kv kv = getKv();
        if (file != null) {
            R r = adminFileService.upload(file, null, "file", "/" + prefix);
            kv.set("logo", r.get("url"));
        }
        RedisManager.getRedis().set(SYS_CONFIG_KEY, JSON.toJSONString(kv));
        renderJson(R.ok());
    }

    /**
     * 查询系统配置
     *
     * @author hmb
     */
    @Clear
    @ResponseBody
    @RequestMapping(value = "/querySysConfig", method = RequestMethod.POST)
    public ResponseInfo querySysConfig() {
        ResponseInfo resp = new ResponseInfo();
        if (StringUtil.isEmpty(RedisManager.getRedis().get(SYS_CONFIG_KEY))) {
            resp.setData(Kv.by("logo", "").set("name", ""));
            //renderJson(R.ok().put("data", Kv.by("logo","").set("name","")));
            return resp;
        }
        String data = RedisManager.getRedis().get(SYS_CONFIG_KEY);
        Map map = JSON.parseObject(data, Map.class);
        resp.setData(map);
        // renderJson(R.ok().put("data",map));
        return resp;
    }
}
