package com.bdaim.crm.erp.crm.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.exception.TouchException;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.customer.service.B2BTcbService;
import com.bdaim.order.dao.OrderDao;
import com.bdaim.order.entity.OrderDO;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.dao.ResourcePropertyDao;
import com.bdaim.resource.entity.ResourcePropertyDOPK;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.util.ConfigUtil;
import com.bdaim.util.IDHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class CrmPackagesService {
    private static final Logger logger = LoggerFactory.getLogger(CrmPackagesService.class);

    @Autowired
    private MarketResourceDao resourceDao;
    @Autowired
    private ResourcePropertyDao resourceProDao;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private B2BTcbService b2BTcbService;

    /**
     * 购买套餐
     *
     * @author Chacker
     * @date 2020/3/24
     */
    public void payForAPackage(Integer resourceId, HttpServletResponse response) throws IOException {
        //1. 创建订单
        OrderDO order = new OrderDO();
        //订单ID
        String orderId = String.valueOf(IDHelper.getTransactionId());
        order.setOrderId(orderId);
        //企业ID
        String custId = BaseUtil.getCustId();
        order.setCustId(custId);
        //订单类型 【3】CRM企业套餐
        order.setOrderType(3);
        //订单状态 初始化为未付款
        order.setOrderState(1);
        order.setCreateTime(new Date());

        // 根据套餐ID查询套餐信息
        ResourcePropertyDOPK pk = new ResourcePropertyDOPK();
        pk.setResourceId(resourceId);
        pk.setPropertyName("price_config");
        ResourcePropertyEntity resourceProperty = resourceProDao.get(pk);
        String propertyValue = resourceProperty.getPropertyValue();
        JSONObject jsonObject = JSON.parseObject(propertyValue);

        //保存订单信息
        order.setProductName(jsonObject.getString("name"));
        order.setRemarks(resourceId.toString());
        BigDecimal price = new BigDecimal(jsonObject.getString("price"));
        order.setCostPrice(price.multiply(new BigDecimal("100")).intValue());
        orderDao.save(order);

        //支付宝支付接口调用
        aliPayPc(order, jsonObject, response);

    }

    private void aliPayPc(OrderDO order, JSONObject jsonObject, HttpServletResponse response) throws IOException {
        ConfigUtil config = ConfigUtil.getInstance();
        //初始化请求客户端
        AlipayClient alipayClient = new DefaultAlipayClient(config.get("alipay_server_url_pro"),
                config.get("alipay_app_id_pro"), config.get("alipay_app_private_key_pro"), "json",
                "utf-8", config.get("alipay_public_key_pro"), "RSA2");
        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
//        alipayRequest.setReturnUrl(AlipayConstants.RETURN_URL_PRO);
        alipayRequest.setNotifyUrl(config.get("alipay_notify_url_pro") + "/packages/getAliPayResult");

        //订单号
        String out_trade_no = order.getOrderId();
        //付款金额
        String total_amount = jsonObject.getString("price");
        //订单名称 (套餐名称)
        String subject = order.getProductName();
        //商品描述
        String body = "联客CRM套餐[" + subject + "]";
        Map<String, Object> bizContentMap = new HashMap<>(16);
        bizContentMap.put("out_trade_no", out_trade_no);
        bizContentMap.put("total_amount", total_amount);
        bizContentMap.put("subject", subject);
        bizContentMap.put("body", body);
        //4表示精简模式，只返回一个二维码，嵌入到CRM网页中
        bizContentMap.put("qr_pay_mode", "4");
        bizContentMap.put("width", "160");
        //PC快捷支付方式
        bizContentMap.put("product_code", "FAST_INSTANT_TRADE_PAY");
        alipayRequest.setBizContent(JSON.toJSONString(bizContentMap));

        String form = "";
        try {
            //调用SDK生成表单
            form = alipayClient.pageExecute(alipayRequest, "GET").getBody();
            logger.info("调用支付宝返回>>> " + form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=" + "utf-8");
        //直接将完整的表单html输出到页面
        form = out_trade_no + "{{split}}" + form;
        response.getWriter().write(form);
        response.getWriter().flush();
        response.getWriter().close();
    }

    public void updateOrderStatus(String orderId, String totalAmount) {
        OrderDO orderDO = orderDao.get(orderId);
        //修改订单状态为已支付
        orderDO.setOrderState(2);
        //支付时间
        orderDO.setPayTime(new Date());
        //支付金额
        BigDecimal payAmount = new BigDecimal(totalAmount);
        payAmount = payAmount.multiply(new BigDecimal("100"));
        orderDO.setPayAmount(payAmount.intValue());
        orderDao.update(orderDO);
        // 开通套餐包
        JSONObject info = new JSONObject();
        info.put("resource_id", orderDO.getRemarks());
        info.put("type", 4);
        info.put("status", 1);
        String custId = orderDO.getCustId();
        LoginUser user = BaseUtil.getUser();
        try {
            logger.info("通过crm官网购买套餐包开通custId:{},userId:{},resource_id:{}", custId, user.getUserId(), orderDO.getRemarks());
            b2BTcbService.saveTcbData(custId, user.getUserId(), LocalDateTime.now(), info);
        } catch (Exception e) {
            logger.error("通过crm官网购买套餐包开通失败:custId:{},userId:{}", custId, user.getUserId(), e);
        }
    }

    public boolean getOrderState(String orderId) throws TouchException {
        OrderDO orderDO = orderDao.get(orderId);
        if (orderDO == null) {
            throw new TouchException("订单信息不存在");
        }
        int orderState = orderDO.getOrderState();
        return orderState == 1 ? false : true;
    }
}
