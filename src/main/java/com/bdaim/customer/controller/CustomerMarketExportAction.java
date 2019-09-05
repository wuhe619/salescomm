package com.bdaim.customer.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.customer.dto.CustomerMarketExport;
import com.bdaim.customer.service.CustomerMarketExportService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/4
 * @description
 */
@Controller
@RequestMapping("customerMarketExport")
public class CustomerMarketExportAction extends BasicAction {

    @Resource
    private CustomerMarketExportService customerMarketExportService;

    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    @ResponseBody
    public Object apply(CustomerMarketExport customerMarketExport) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        int applyCount = customerMarketExportService.countCustomerMarketExport(opUser().getCustId(), null, null);
        if (applyCount >= 5) {
            map.put("code", "2");
            map.put("message", "导出已达到上限");
        } else {
        	LoginUser u = opUser();
            customerMarketExport.setCustomerId(u.getCustId());
            customerMarketExport.setEnterpriseName(u.getEnterpriseName());
            customerMarketExport.setOperator(u.getName());
            int returnCode = customerMarketExportService.customerMarketExportApply(customerMarketExport);

            if (returnCode == 1) {
                map.put("code", "0");
                map.put("message", "成功");
            } else {
                map.put("code", "1");
                map.put("message", "失败");
            }
        }
        resultMap.put("data", map);
        return JSONObject.toJSON(resultMap);
    }

    @RequestMapping(value = "/approval", method = RequestMethod.POST)
    @ResponseBody
    public Object approval(CustomerMarketExport customerMarketExport) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        int returnCode = customerMarketExportService.customerMarketExportApproval(customerMarketExport);
        if (returnCode == 1) {
            map.put("code", "0");
            map.put("message", "成功");
        } else {
            map.put("code", "1");
            map.put("message", "失败");
        }
        resultMap.put("data", map);
        return JSONObject.toJSON(resultMap);
    }

    @RequestMapping(value = "/listPage", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String list(String status, Integer pageNum, Integer pageSize, String customerName, String createTimeStart,
                       String createTimeEnd) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        if (pageNum == null || pageSize == null) {
            map.put("code", -1);
            map.put("message", "分页参数必填");
            json.put("data", map);
            return json.toJSONString();
        }
        LoginUser u = opUser();
        List<Map<String, Object>> page = customerMarketExportService.listPage(u.getCustId(), status,
                pageNum, pageSize, customerName, createTimeStart, createTimeEnd);
        List<Map<String, Object>> list = customerMarketExportService.list(u.getCustId(), status,
                customerName, createTimeStart, createTimeEnd);
        map.put("data", page);
        map.put("total", list.get(0).get("count"));
        json.put("data", map);
        return json.toJSONString();

    }

    @RequestMapping(value = "/detail/listPage", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String detailListPage(String superId, String customerMarketExportId, Integer pageNum, Integer pageSize) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        if (pageNum == null || pageSize == null) {
            map.put("code", -1);
            map.put("message", "分页参数必填");
            json.put("data", map);
            return json.toJSONString();
        }
        List<Map<String, Object>> page = customerMarketExportService.listPageIntentionCustomer(customerMarketExportId, pageNum, pageSize, superId, "1", null, "是");
        List<Map<String, Object>> list = customerMarketExportService.listPageIntentionCustomer(customerMarketExportId, null, null, superId, "1", null, "是");
        map.put("data", page);
        map.put("total", list.size() > 0 ? list.get(0).get("count") : 0);
        json.put("data", map);
        return json.toJSONString();

    }
}
