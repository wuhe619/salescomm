package com.bdaim.common.controller;


import com.alibaba.fastjson.JSON;
import com.bdaim.common.entity.Config;
import com.bdaim.common.service.ConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/config")
public class ConfigAction extends BasicAction {
    @Resource
    private ConfigService configService;

    /**
     * 根据id获取配置信息
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/getConfigById")
    public String getConfigById(Integer id) {
        Config config = configService.getConfigById(id);
        return JSON.toJSONString(config);
    }
}
