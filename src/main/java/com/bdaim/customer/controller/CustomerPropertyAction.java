package com.bdaim.customer.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.PageList;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerPropertyParam;
import com.bdaim.customer.service.CustomerPropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/7
 * @description
 */
@Controller
@RequestMapping("/customer/property")
public class CustomerPropertyAction extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(CustomerPropertyAction.class);
    @Resource
    private CustomerPropertyService customerPropertyService;

    @RequestMapping(value = "/add.do", method = RequestMethod.POST)
    @ResponseBody
    public String addCustomerProperty(@RequestBody CustomerPropertyParam customerPropertyParam) {
        customerPropertyParam.setCustomerId(StringUtil.isNotEmpty(opUser().getCustId()) ? opUser().getCustId() : "0");
        customerPropertyParam.setUserId(String.valueOf(opUser().getId()));
        int returnCode = customerPropertyService.save(customerPropertyParam);
        if (returnCode == 1) {
            return returnSuccess();
        } else {
            return returnError();
        }
    }

    @RequestMapping(value = "/modify.do", method = RequestMethod.POST)
    @ResponseBody
    public String modifyCustomerProperty(@RequestBody CustomerPropertyParam customerPropertyParam) {
        customerPropertyParam.setCustomerId(StringUtil.isNotEmpty(opUser().getCustId()) ? opUser().getCustId() : "0");
        customerPropertyParam.setUserId(String.valueOf(opUser().getId()));
        int returnCode = customerPropertyService.update(customerPropertyParam);
        if (returnCode == 1) {
            return returnSuccess();
        } else {
            return returnError();
        }
    }

    @RequestMapping(value = "/list.do", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String listPage(@Valid PageParam page, BindingResult error, CustomerPropertyParam customerPropertyParam) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        if ("admin".equals(opUser().getRole()) || "ROLE_USER".equals(opUser().getRole())) {
            customerPropertyParam.setCustomerId(null);
        } else {
            customerPropertyParam.setCustomerId(opUser().getCustId());
        }
        PageList list = customerPropertyService.pageList(page, customerPropertyParam);
        return JSON.toJSONString(list);

    }

    @ResponseBody
    @RequestMapping(value = "/getSelLabel.do", method = RequestMethod.GET)
    @CacheAnnotation
    public String getSelLabel(String superId, String customerGroupId) {
        List<Map<String, Object>> list = customerPropertyService.getSelectedLabelsBySuperId(superId, customerGroupId);
        return JSON.toJSONString(list);
    }


    //企业密码增加+修改

    @RequestMapping(value = "/enterpriseadd.do", method = RequestMethod.POST)
    @ResponseBody
    public Object enterpriseadd(@RequestBody CustomerProperty customerProperty) {
        LoginUser lu = opUser();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            try {
                customerPropertyService.addenterprise(customerProperty);
                resultMap.put("result", "1");
                resultMap.put("_message", "添加成功！");
            } catch (Exception e) {
                e.printStackTrace();
                resultMap.put("result", "0");
                resultMap.put("_message", "添加失败！");
                logger.error("企业密码修改" + e);
            }
        }
        return JSONObject.toJSON(resultMap);
    }


    //企业密码查询
    @RequestMapping(value = "/enterpriselist.do", method = RequestMethod.GET)
    @ResponseBody
    public Object enterprise(String custId, String channel) {
        LoginUser lu = opUser();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            String enterprisePassword= customerPropertyService.getListenterprise(custId, channel);
                resultMap.put("enterprisePassword", enterprisePassword);
            }
            return JSONObject.toJSON(resultMap);
    }

    /*
     *查询企业渠道信息
     */
    @RequestMapping(value = "/querySupplier", method = RequestMethod.GET)
    @ResponseBody
    public Object queryCustSupplier(String custId) {
        List<Map<String, Object>> resultMap = customerPropertyService.listCustSupplier(custId);
        return JSON.toJSONString(resultMap);
    }
}
