package com.bdaim.common.controller;


import com.bdaim.common.util.wechat.WeChatUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/wechat")
public class WeChatAction extends BasicAction {
    @Resource
    private WeChatUtil weChatUtil;

    /**
     * 根据id获取配置信息
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/testMsg")
    public void getConfigById(Integer id) {
        weChatUtil.sendTempMsg("oFqgzwFGhEAqH-aRgI6YYJTti1oc", "BHAXK1_KvuS2tzbB5wZx_DUNuU8VJhbsP5nOrHjDtUI", "", "#FF0000", "{\"name\":{\"value\":\"开发人员\",\"color\":\"#173177\"},\"time\":{\"value\":\"2019-10-10 14:58:33\",\"color\":\"#173177\"},\"content\":{\"value\":\"test\",\"color\":\"#173177\"}}");
    }
}
