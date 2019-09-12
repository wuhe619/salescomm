package com.bdaim.industry.controller;


import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.industry.service.IndustryInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/industryInfo")

/**
 * 行业
 */
public class IndustryInfoAction extends BasicAction {
    @Resource
    IndustryInfoService industryInfoService;

    /**
     * 查询行业/信息
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/listIndustryInfo")
    @CacheAnnotation
    public String getIndustryInfoList(Integer pageNum, Integer pageSize, String industryInfoId) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        List data = industryInfoService.getIndustryInfoList(pageNum, pageSize, industryInfoId);
        List total = industryInfoService.getIndustryInfoListTotal(industryInfoId);
        map.put("data", data);
        map.put("total", total);
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 新增行业信息
     *
     * @param industryName 行业名称
     * @param description  行业描述
     * @param status       状态
     * @param price        价格
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/addIndustryInfo", method = RequestMethod.POST)
    @CacheAnnotation
    public String addIndustryInfo(String industryName, String description, Integer status, Double price) {
        return industryInfoService.addIndustryInfo(industryName, description, status, price);
    }

    /**
     * 修改行业信息
     *
     * @param industryInfoId
     * @param industryName
     * @param description
     * @param status
     * @param price
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateIndustryInfo", method = RequestMethod.POST)
    @CacheAnnotation
        public String updateIndustryInfo(String industryInfoId, String industryName, String description, Integer status, Double price) {
        return industryInfoService.updateIndustryInfo(industryInfoId, industryName, description, status, price);
    }

    @ResponseBody
    @RequestMapping("/listIndustry")
    @CacheAnnotation
    public String listIndustryInfo() {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        List data = industryInfoService.listIndustryInfo();
        map.put("data", data);
        json.put("data", map);
        return json.toJSONString();
    }

}
