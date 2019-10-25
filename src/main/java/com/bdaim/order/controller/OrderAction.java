package com.bdaim.order.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.exception.TouchException;
import com.bdaim.customgroup.dto.CustomerGrpOrdParam;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.order.dto.MarketRsOrderQueryParam;
import com.bdaim.order.service.OrderService;
import com.bdaim.util.StringUtil;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping(value = "/order")
/**
 * 订单
 */
public class OrderAction extends BasicAction {
    private static Logger logger = Logger.getLogger(OrderAction.class);
    @Resource
    OrderService orderService;
    @Resource
    CustomGroupService customGroupService;

    @RequestMapping(value = "/customerGroupOrder/query")
    @ResponseBody
    public Object customerGroupOrdQuery(CustomerGrpOrdParam param) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        LoginUser lu = opUser();
        if (StringUtil.isEmpty(lu.getCustId())) {
            throw new TouchException("20010", "系统异常:用户信息不存在");
        }
        resultMap.put("data", JSONObject.toJSON(orderService.listCustGroupOrders(lu.getId(), lu.getCustId(), lu.getUserType(), param)));
        return JSONObject.toJSON(resultMap);
    }

    @RequestMapping(value = "/customerGroupOrder0/query")
    @ResponseBody
    public Object customerGroupOrdQuery0(CustomerGrpOrdParam param) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        if (StringUtil.isEmpty(opUser().getCustId())) {
            throw new TouchException("20010", "系统异常:用户信息不存在");
        }
        resultMap.put("data", JSONObject.toJSON(orderService.listCustGroupOrders0(opUser(),opUser().getCustId(), param)));
        return JSONObject.toJSON(resultMap);
    }

    @RequestMapping(value = "/marketRsOrder/query")
    @ResponseBody
    public Object marketRsOrdQuery(MarketRsOrderQueryParam param) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        if (StringUtil.isEmpty(opUser().getCustId())) {
            throw new TouchException("20010", "系统异常:用户信息不存在");
        }
        resultMap.put("data", JSONObject.toJSON(orderService.listMarketRsOrders(opUser().getCustId(), param)));
        return JSONObject.toJSON(resultMap);
    }

    @RequestMapping(value = "/detail/{orderType}/{orderId}")
    @ResponseBody
    public Object queryOrderDetail(@PathVariable int orderType, @PathVariable String orderId) throws Exception {
    	LoginUser lu = opUser();
        String custId = lu.getCustId();
        if (!"admin".equals(lu.getRole()) && !"".equals(lu.getRole()) && StringUtil.isEmpty(custId)) {
            throw new TouchException("20010", "系统异常:用户信息不存在");
        }
        Map<String, Object> resultMap = new HashMap<>();
        //客户群类订单详情
        if (1 == orderType) {
            resultMap.put("data", JSONObject.toJSON(orderService.queryCustomerOrdDetail(custId, orderId)));
        }
        //资源类订单详情
        if (2 == orderType) {
            resultMap.put("data", JSONObject.toJSON(orderService.queryMarketRsOrdDetail(custId, orderId)));
        }
        return JSONObject.toJSON(resultMap);

    }
}
