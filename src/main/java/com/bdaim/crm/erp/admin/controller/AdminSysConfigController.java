package com.bdaim.crm.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.redis.RedisManager;
import com.bdaim.crm.erp.admin.service.AdminFileService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.StringUtil;
import com.jfinal.aop.Clear;
import com.jfinal.kit.Kv;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.Map;

/**
 * 系统配置
 *
 * @author hmb
 */
@RestController
@RequestMapping("/sysConfig")
public class AdminSysConfigController extends BasicAction {

    private static final String SYS_CONFIG_KEY = "sys_config";

    @Resource
    private AdminFileService adminFileService;

    /**
     * 设置系统配置
     *
     * @author hmb
     */
    @Permissions("manage:system")
    @RequestMapping(value = "/setSysConfig", method = RequestMethod.POST)
    public R setSysConfig() {
        String prefix = BaseUtil.getDate();
        //UploadFile file = getFile("file", prefix);
        Map kv = getKv();
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                BaseUtil.getRequest().getSession().getServletContext());
        if (multipartResolver.isMultipart(BaseUtil.getRequest())) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) BaseUtil.getRequest();
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                R r = adminFileService.upload0(BaseUtil.getRequest(), null, "file", "/" + prefix);
                kv.put("logo", r.get("url"));
                break;
            }
        }
       /* if (BaseUtil.getRequest().getParameter("file") != null) {
            R r = adminFileService.upload0(BaseUtil.getRequest(), null, "file", "/" + prefix);
            kv.put("logo", r.get("url"));
        }*/
        RedisManager.getRedis().set(SYS_CONFIG_KEY + ":" + BaseUtil.getUser().getCustId(), JSON.toJSONString(kv));
        return (R.ok());
    }

    /**
     * 查询系统配置
     *
     * @author hmb
     */
    @Clear
    @ResponseBody
    @RequestMapping(value = "/querySysConfig", method = RequestMethod.POST)
    public R querySysConfig() {
        if (StringUtil.isEmpty(RedisManager.getRedis().get(SYS_CONFIG_KEY + ":" + BaseUtil.getUser().getCustId()))) {
            //renderJson(R.ok().put("data", Kv.by("logo","").set("name","")));
            return R.ok().put("data", Kv.by("logo", "").set("name", ""));
        }
        String data = RedisManager.getRedis().get(SYS_CONFIG_KEY + ":" + BaseUtil.getUser().getCustId());
        Map map = JSON.parseObject(data, Map.class);
        return (R.ok().put("data", map));
    }
}
