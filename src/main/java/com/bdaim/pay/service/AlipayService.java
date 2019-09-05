package com.bdaim.pay.service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 支付宝支付过程逻辑service服务接口
 *
 */
public interface AlipayService {

	/**
	 * 生成订单逻辑
	 * @param orderName 订单名称
	 * @param total_fee 订单金额
	 * @param body 商品描述信息可以为空
	 * @param extra_common_param 备用信息可以用来传递一些自己需要返回时记录的参数
	 * @param outTradeNo 订单号
	 * @param type 订单来源
	 * @param returnMap 处理过程出现问题返回错误提示没错错误不做任何处理
	 */
	void createOrder(HttpServletRequest request, String orderName, String total_fee, String body, String extra_common_param, String outTradeNo,
                     String type, Map<String, Object> returnMap);

}
