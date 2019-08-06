package com.bdaim.resource.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.resource.service.impl.MarketResourceServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2019/8/6
 * @description
 */
@Controller
@RequestMapping("/resource")
public class ResourceAction extends BasicAction {
    @Resource
    MarketResourceServiceImpl marketResourceService;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ResourceAction.class);

    /**
     * @Title:
     * @Description: 根据资源类型查询供应商信息和资源信息
     */
    @RequestMapping(value = "/getResource", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo getResourceInfoByType(String type) {
        try {
            List<Map<String, Object>> list = marketResourceService.getResourceInfoByType(type);
            return new ResponseInfoAssemble().success(list);
        } catch (Exception e) {
            logger.error("查询资源信息异常", e);
            return new ResponseInfoAssemble().failure(-1, "查询资源信息失败");
        }
    }
}
