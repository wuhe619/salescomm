package com.bdaim.common.controller;


import com.alibaba.fastjson.JSON;
import com.bdaim.callcenter.dto.XzPullPhoneDTO;
import com.bdaim.common.service.PhoneService;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import com.bdaim.util.wechat.WeChatUtil;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.indices.DeleteIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/phone")
public class PhoneAction extends BasicAction {

    public static final Logger LOG = LoggerFactory.getLogger(PhoneAction.class);

    @Resource
    private PhoneService phoneService;


    @RequestMapping(value = "/xzGetTaskPhone", method = {RequestMethod.GET, RequestMethod.POST})
    public void xzGetTaskPhone(String taskid, String name, String count, String id, String type, HttpServletResponse response) {
        Map<Object, Object> param = new HashMap<>();
        param.put("taskid", taskid);
        param.put("name", name);
        param.put("count", count);
        param.put("id", id);
        Map<String, Object> resp = new HashMap<>(), data;
        List<Map<String, Object>> list;
        int code = 200;
        String reason = "ok";
        if (StringUtil.isEmpty(type)) {
            code = 500;
            reason = "type不能为空:" + type;
            LOG.warn("type不能为空:" + type);
        } else if (StringUtil.isEmpty(taskid)) {
            code = 500;
            reason = "taskid不能为空:" + taskid;
            LOG.warn("taskid不能为空:" + taskid);
        } else {
            taskid = taskid.trim();
            type = type.trim();
            list = new ArrayList<>();
            int pageNum = 0;
            int pageSize = 10;
            // uuid为空则起始页为0
            if (StringUtil.isNotEmpty(id)) {
                pageNum = NumberConvertUtil.parseInt(id);
            }
            if (StringUtil.isNotEmpty(count)) {
                pageSize = NumberConvertUtil.parseInt(count);
            }
            // 获取手机号和随路参数
            List<XzPullPhoneDTO> phones = phoneService.pagePhoneByTaskId(NumberConvertUtil.parseInt(type), taskid, pageNum, pageSize);
            if (phones == null || phones.size() == 0) {
                data = new HashMap<>();
                data.put("id", "");
                data.put("phone", "");
                data.put("param", "");
                list.add(data);
                resp.put("nums", list);
                resp.put("isend", "true");
            } else {
                for (int i = 0; i < phones.size(); i++) {
                    if (StringUtil.isEmpty(phones.get(i).getPhone())) {
                        continue;
                    }
                    data = new HashMap<>();
                    data.put("id", String.valueOf(pageNum + i + 1));
                    data.put("phone", phones.get(i).getPhone().split(","));
                    data.put("param", phones.get(i).getParam());
                    list.add(data);
                }
                resp.put("nums", list);
                resp.put("isend", "false");
            }
        }
        resp.put("code", code);
        resp.put("reason", reason);
        LOG.info("讯众taskId:" + taskid + ",取号接口请求参数:" + param);
        LOG.info("讯众taskId:" + taskid + ",取号接口返回结果:" + JSON.toJSONString(resp));
        response.setContentType("application/json");
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(JSON.toJSONString(resp).getBytes("UTF-8"));
        } catch (IOException e) {
            LOG.error("讯众取号接口异常,", e);
        }
    }


    @RequestMapping(value = "/xzGetTaskPhone0", method = {RequestMethod.GET, RequestMethod.POST})
    public void xzGetTaskPhone0(String taskid, String name, String count, String id, String type, String cId, HttpServletResponse response) {
        Map<Object, Object> param = new HashMap<>();
        param.put("taskid", taskid);
        param.put("cId", cId);
        param.put("name", name);
        param.put("count", count);
        param.put("id", id);
        Map<String, Object> resp = new HashMap<>(), data;
        List<Map<String, Object>> list;
        int code = 200;
        String reason = "ok";
        if (StringUtil.isEmpty(type)) {
            code = 500;
            reason = "type不能为空:" + type;
            LOG.warn("type不能为空:" + type);
        } else if (StringUtil.isEmpty(cId)) {
            code = 500;
            reason = "cId不能为空:" + cId;
            LOG.warn("cId不能为空:" + cId);
        } else {
            cId = cId.trim();
            type = type.trim();
            list = new ArrayList<>();
            int pageNum = 0;
            int pageSize = 10;
            // uuid为空则起始页为0
            if (StringUtil.isNotEmpty(id)) {
                pageNum = NumberConvertUtil.parseInt(id);
            }
            if (StringUtil.isNotEmpty(count)) {
                pageSize = NumberConvertUtil.parseInt(count);
            }
            // 获取手机号和随路参数
            List<XzPullPhoneDTO> phones = phoneService.pagePhoneById(NumberConvertUtil.parseInt(type), cId, pageNum, pageSize);
            if (phones == null || phones.size() == 0) {
                data = new HashMap<>();
                data.put("id", "");
                data.put("phone", "");
                data.put("param", "");
                list.add(data);
                resp.put("nums", list);
                resp.put("isend", "true");
            } else {
                for (int i = 0; i < phones.size(); i++) {
                    if (StringUtil.isEmpty(phones.get(i).getPhone())) {
                        continue;
                    }
                    data = new HashMap<>();
                    data.put("id", String.valueOf(pageNum + i + 1));
                    data.put("phone", phones.get(i).getPhone().split(","));
                    data.put("param", phones.get(i).getParam());
                    list.add(data);
                }
                resp.put("nums", list);
                resp.put("isend", "false");
            }
        }
        resp.put("code", code);
        resp.put("reason", reason);
        LOG.info("讯众taskId:" + taskid + ",取号接口请求参数:" + param);
        LOG.info("讯众taskId:" + taskid + ",取号接口返回结果:" + JSON.toJSONString(resp));
        response.setContentType("application/json");
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(JSON.toJSONString(resp).getBytes("UTF-8"));
        } catch (IOException e) {
            LOG.error("讯众取号接口异常,", e);
        }
    }
}
