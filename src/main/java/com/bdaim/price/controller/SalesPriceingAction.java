package com.bdaim.price.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.page.Page;
import com.bdaim.price.dto.SalePriceDTO;
import com.bdaim.price.service.SalePriceService;

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
 * @author duanliying
 * @date 2018/10/11
 * @description 销售定价
 */
@Controller
@RequestMapping("/price")
public class SalesPriceingAction extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(SalesPriceingAction.class);
    @Resource
    private SalePriceService salePriceService;

    /**
     * 销售定价查询
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getSalePriceList", method = RequestMethod.GET)
    public String searchPage(@Valid PageParam page, BindingResult error, CustomerBillQueryParam customerBillQueryParam) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        LoginUser lu = opUser();
        Page list = null;
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            list = salePriceService.getSalePriceList(page, customerBillQueryParam);
        }
        return JSON.toJSONString(list);
    }

    /**
     * 设置销售价
     */
    @ResponseBody
    @RequestMapping(value = "/updateSalePrice", method = RequestMethod.POST)
    public String updateSalePrice(@RequestBody Map<String, List<SalePriceDTO>> salePricList) {
        Map<String, Object> map = new HashMap<String, Object>();
        JSONObject json = new JSONObject();
        LoginUser lu = opUser();
        long userId = opUser().getId();
        try {
            // 1.查询销售价格表的价格
            // List<CustomerPropertyDO> oldPriceList = salePriceService.getLabelSalePriceOld(salePriceDTO);
            // 2.将销售价格表价格更新
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                salePriceService.updateSalePrice(salePricList);
            }
            // 3.将销售价格的记录增加到销售价格日志表中
            // salePriceService.addLabelSalePriceModifyLog(oldPriceList,salePriceDTO, userId);
            map.put("result", 1);
            map.put("message", "更新价格成功");

        } catch (Exception e) {
            logger.error("修改销售定价" + e);
            map.put("result", 0);
            map.put("message", "更新价格失败");
        }
        json.put("data", map);
        return json.toJSONString();

    }

    /**
     * 查询销售定价
     */
    @ResponseBody
    @RequestMapping(value = "/querySalePrice", method = RequestMethod.GET)
    public String querySalePrice(String custId) {
        return JSONObject.toJSONString(salePriceService.querySalePriceList(custId));
    }

    /**
     * @description 验证当前企业是否配置定价公共方法
     * @author:duanliying
     * @method
     * @date: 2018/11/6 11:59
     */
    @ResponseBody
    @RequestMapping(value = "/checkSalePrice", method = RequestMethod.GET)
    public String checkSalePrice(String custId, String channel) {
        return JSONObject.toJSONString(salePriceService.checkSalePrice(custId, channel));
    }
}
