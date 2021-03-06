package com.bdaim.crm.erp.crm.controller;

import com.bdaim.common.exception.TouchException;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.crm.erp.crm.service.CrmPackagesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 套餐购买
 *
 * @author Chacker
 * @date 2020/3/24
 */
@RequestMapping(value = "/packages")
@RestController
public class CrmPackagesController {
    private static final Logger logger = LoggerFactory.getLogger(CrmPackagesController.class);

    @Autowired
    private CrmPackagesService packagesService;

    /**
     * 购买套餐 (支付宝电脑网站支付)
     *
     * @param resourceId 套餐ID
     * @author Chacker
     * @date 2020/3/24
     */
    @RequestMapping(value = "/orderPay", method = RequestMethod.POST)
    public ResponseInfo payForAPackage(@RequestParam Integer resourceId, HttpServletResponse response) throws IOException {
        if (resourceId == null || resourceId.equals(0)) {
            return new ResponseInfoAssemble().failure(401, "请选择套餐后进行购买[resourceId]");
        }

        packagesService.payForAPackage(resourceId, response);
        return new ResponseInfoAssemble().success(resourceId);
    }

    /**
     * 支付宝异步通知的支付结果
     *
     * @author Chacker
     * @date 2020/3/24
     */
    @RequestMapping(value = "/getAliPayResult")
    public String getAliPayResult(@RequestParam Map<String, Object> map) {
        logger.info("这里是支付宝返回的支付结果>>>>>>>>>");
        logger.info(map.toString());
        String tradeStatus = String.valueOf(map.get("trade_status"));
        if (map != null && "TRADE_SUCCESS".equals(tradeStatus)) {
            //修改订单状态
            String orderId = String.valueOf(map.get("out_trade_no"));
            String totalAmount = String.valueOf(map.get("total_amount"));
            packagesService.updateOrderStatus(orderId,totalAmount);
        }
        return "success";
    }

    /**
     * 根据订单ID查询订单状态
     *
     * @param orderId
     * @return
     */
    @RequestMapping(value = "/getOrderState")
    public ResponseInfo getOrderState(String orderId) throws TouchException {
        boolean state = packagesService.getOrderState(orderId);
        return new ResponseInfoAssemble().success(state);
    }

}
