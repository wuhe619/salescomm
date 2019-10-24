package com.bdaim.marketproject.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.exception.ParamException;
import com.bdaim.customer.dto.CustomerUserGroupDTO;
import com.bdaim.customeruser.service.CustomerUserGroupService;
import com.bdaim.marketproject.entity.MarketProjectProperty;
import com.bdaim.marketproject.service.MarketProjectService;
import com.bdaim.template.entity.MarketTemplate;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author chengning@salescomm.net
 * @date 2019/6/20
 * @description
 */
@RestController
@RequestMapping("/marketProject")
public class MarketProjectAction extends BasicAction {

    @Resource
    private MarketProjectService projectService;

    @Resource
    private CustomerUserGroupService customerUserGroupService;

    /**
     * 获取项目属性
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/getProperty", method = RequestMethod.POST)
    public String getProperty(@RequestBody JSONObject jsonObject) {
        ResponseJson responseJson = new ResponseJson();
        LoginUser lu = opUser();
        String custId = lu.getCustId();
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("客户ID为空");
        }
        MarketProjectProperty data = projectService.getProperty(custId, jsonObject.getString("marketProjectId"),
                jsonObject.getString("propertyName"));
        responseJson.setData("");
        if (data != null) {
            responseJson.setData(data.getPropertyValue());
        }
        return JSON.toJSONString(responseJson);
    }

    @RequestMapping(value = "/getExecutionGroup", method = RequestMethod.POST)
    public String getExecutionGroup(@RequestBody JSONObject jsonObject) {
        ResponseJson responseJson = new ResponseJson();
        LoginUser lu = opUser();
        String custId = lu.getCustId();
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("客户ID为空");
        }
        MarketProjectProperty data = projectService.getProperty(custId, jsonObject.getString("marketProjectId"), "executionGroup");
        responseJson.setData("");
        if (data != null && StringUtil.isNotEmpty(data.getPropertyValue())) {
            List<String> groupIds = Arrays.asList(data.getPropertyValue().split(","));
            List<CustomerUserGroupDTO> list = new ArrayList<>();
            CustomerUserGroupDTO dto;
            for (String id : groupIds) {
                dto = customerUserGroupService.selectCustomerUserGroup(id);
                if (dto != null) {
                    list.add(dto);
                }
            }
            responseJson.setData(list);
        }
        return JSON.toJSONString(responseJson);
    }

    /**
     * 保存项目属性
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/saveProperty", method = RequestMethod.POST)
    public String saveProperty(@RequestBody JSONObject jsonObject) {
        ResponseJson responseJson = new ResponseJson();
        LoginUser lu = opUser();
        String custId = lu.getCustId();
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("客户ID为空");
        }
        int data = projectService.saveProperty(custId, jsonObject.getString("marketProjectId"),
                jsonObject.getString("propertyName"), jsonObject.getString("propertyValue"));
        responseJson.setData(data);
        return JSON.toJSONString(responseJson);
    }

    /**
     * 项目列表
     *
     * @param
     * @param projectName
     * @param id
     * @param status
     * @param createTime
     * @param endTime
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/project/query", method = RequestMethod.GET)
    public String getProjectList(String projectName, String id,
                                 Integer status, String createTime, String endTime,
                                 Integer pageNum, Integer pageSize, String projectUser) {
        ResponseJson responseJson = new ResponseJson();
        LoginUser lu = opUser();
        String custId = lu.getCustId();
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("客户ID为空");
        }
        if (pageNum == null || pageSize == null) {
            throw new ParamException("参数不正确");
        }
        // 项目管理员
        if ("3".equals(opUser().getUserType())) {
            projectUser = opUser().getUsername();
        }
        JSONObject data = projectService.getMarketProjectList(lu, custId, projectName, id, status, createTime, endTime, pageNum, pageSize, projectUser);
        responseJson.setData(data);
        return JSON.toJSONString(responseJson);
    }

    /**
     * 项目话术配置接口
     *
     * @param content
     * @param marketProjectId
     * @param status
     * @return
     */
    @RequestMapping(value = "/saveTelephoneTech", method = RequestMethod.POST)
    @ResponseBody
    public String saveTelephoneTech(String content, Integer marketProjectId, Integer status) {
        JSONObject json = new JSONObject();
        int map = projectService.saveTelephoneTech(content, opUser().getCustId(), marketProjectId, status);
        json.put("data", map);
        return json.toJSONString();
    }

    @RequestMapping(value = "/selectTelephoneTech", method = RequestMethod.POST)
    @ResponseBody
    public String selectTelephoneTech(String marketProjectId, String marketTaskId, String seaId, String customerGroupId) {
        JSONObject json = new JSONObject();
        int marketPId = 0, cgId = 0;
        long sId = 0L;
        if (StringUtil.isNotEmpty(marketProjectId)) {
            marketPId = NumberConvertUtil.parseInt(marketProjectId);
        }
        if (StringUtil.isNotEmpty(seaId)) {
            sId = NumberConvertUtil.parseLong(seaId);
        }
        if (StringUtil.isNotEmpty(customerGroupId)) {
            cgId = NumberConvertUtil.parseInt(customerGroupId);
        }
        MarketTemplate data = projectService.selectTelephoneTech(marketPId, marketTaskId, sId, cgId);
        Map<String, Object> map = new HashMap<>();
        if (data != null) {
            map.put("content", data.getMouldContent());
            map.put("marketProjectId", data.getMarketProjectId());
            map.put("status", data.getStatus());
        }
        json.put("data", map);
        return json.toJSONString();
    }

}
