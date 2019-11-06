package com.bdaim.express.controller;

import com.bdaim.auth.LoginUser;
import com.bdaim.auth.service.impl.TokenServiceImpl;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.express.dto.ExpressOrderData;
import com.bdaim.express.service.ExpressService;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/express")
public class ExpressController extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(ExpressController.class);
    @Autowired
    private ExpressService expressService;
    @Resource
    private TokenServiceImpl tokenService;

    /**
     * 快递面单下单
     *
     * @param orderData
     * @return
     */
    @PostMapping("/order")
    public ResponseInfo expressOrder(@Valid @RequestBody ExpressOrderData orderData) {
        logger.info("订单详情:渠道类型:" + orderData.getTradeNoType());
        ResponseInfo resp = new ResponseInfo();
        LoginUser lu = tokenService.opUser();
        Object o = expressService.expressOeder(orderData,lu);

        if (o == null) {
            return new ResponseInfoAssemble().failure(-1, "快递订单创建失败");
        }
        resp.setData(o);
        return resp;
    }

//    public ResponseInfo queryExpressOrderList(){
//
//        ResponseInfo resp = new ResponseInfo();
//        LoginUser lu = tokenService.opUser();
//
//
//        return null;
//    }

    /**
     * 获取快递订单信息（打印面单）
     *
     * @return
     */
    @PostMapping("/order/data")
    public ResponseInfo getExpressOrderData(@RequestBody ExpressOrderData orderData) {
        ResponseInfo resp = new ResponseInfo();
        if (StringUtil.isNotEmpty(orderData.getTxLogisticID())) {
            return new ResponseInfoAssemble().failure(-1, "快递订单号不能为空");
        }
        if (StringUtil.isNotEmpty(orderData.getMailNo())) {
            return new ResponseInfoAssemble().failure(-1, "快递面单号不能为空");
        }
        logger.info("获取面单信息,面单号:" + orderData.getMailNo() + ",订单号:" + orderData.getTxLogisticID() + ",渠道tradeNoType:" + orderData.getTradeNoType());
        resp.setData(expressService.queryExpressOrderData(orderData));
        return resp;
    }

    /**
     * 快递轨迹查询
     *
     * @param number
     * @param tradeNoType
     * @return
     */
    @GetMapping("/trajectory/{tradeNoType}")
    public ResponseInfo queryTrajectory(String number, @PathVariable int tradeNoType) {
        if (StringUtil.isEmpty(number)) {
            return new ResponseInfoAssemble().failure(-1, "运单号不能为空");
        }
        logger.info("运单号:" + number);
        ResponseInfo resp = new ResponseInfo();
        Object o = expressService.queryTrajectory(number, tradeNoType);
        if (o == null) {
            return new ResponseInfoAssemble().failure(-1, "快递轨迹查询失败");
        }
        resp.setData(o);
        return resp;
    }

}
