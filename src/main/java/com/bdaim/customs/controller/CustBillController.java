package com.bdaim.customs.controller;

import com.bdaim.auth.LoginUser;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.Page;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customs.services.HBillService;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author duanliying
 * @date 2019/9/22
 * @description 海关企业账单
 */
@Controller
@RequestMapping(value = "/bill")
public class CustBillController extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(CustBillController.class);
    @Resource
    HBillService hbillService;

    /*
     *
     * 企业账单展示（根据主单id进行展示）
     * */
    @RequestMapping(value = "/custBill", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo getListCustomerBill(@RequestBody CustomerBillQueryParam param) {
        LoginUser lu = opUser();
        Map<String, Object> data = null;
        try {
            if (StringUtil.isEmpty(param.getBillDate())) {
                return new ResponseInfoAssemble().failure(-1, "缺少必要参数");
            }
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                data = hbillService.getCustomerBill(param);
            } else {
                String custId = opUser().getCustId();
                param.setCustomerId(custId);
                data = hbillService.getCustomerBill(param);
            }
        } catch (Exception e) {
            logger.error("查询账单异常", e);
            return new ResponseInfoAssemble().failure(-1, "查询账单失败");
        }
        return new ResponseInfoAssemble().success(data);
    }


    /*
     *
     * 企业账单详情页
     * */
    @RequestMapping(value = "/billDetail", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo getBillDetailList(@RequestBody CustomerBillQueryParam param) {
        Page page = null;
        try {
            if (StringUtil.isEmpty(param.getMainId())) {
                return new ResponseInfoAssemble().failure(-1, "缺少必要参数");
            }
            page = hbillService.getBillDetail(param);
        } catch (Exception e) {
            logger.error("查询账单详情异常", e);
            return new ResponseInfoAssemble().failure(-1, "查询账单详情失败");
        }
        return new ResponseInfoAssemble().success(page);
    }


    /*
     *
     * 账单导出
     * */
    @RequestMapping(value = "/billExport", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo customerBillExport(CustomerBillQueryParam param, String exportType, HttpServletResponse response) {
        try {
            hbillService.customerBillExport(param, exportType, response);
        } catch (Exception e) {
            logger.error("导出企业账单异常", e);
            return new ResponseInfoAssemble().failure(-1, "导出企业账单失败");
        }
        return new ResponseInfoAssemble().success(null);
    }

}
