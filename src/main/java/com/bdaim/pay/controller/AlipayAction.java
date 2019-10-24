package com.bdaim.pay.controller;

import com.alibaba.fastjson.JSON;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.pay.config.AlipayConfig;
import com.bdaim.pay.service.AlipayService;
import com.bdaim.pay.util.AlipaySubmit;
import com.bdaim.util.IDHelper;

import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/aliPay")
public class AlipayAction extends BasicAction {
	/** 日志对象 */
	private static Log logger = LogFactory.getLog(AlipayAction.class);
	
	@Resource
	private AlipayService alipayService;
	
	/**
	 * @param orderName 订单名称
	 * @param total_fee 订单金额
	 * @param body 商品描述信息可以为空
	 * @param extra_common_param 备用信息可以用来传递一些自己需要返回时记录的参数
	 * @param outTradeNo 订单号
	 * @param type 订单来源
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "alipayapi",method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
	public String alipayapi(HttpServletRequest request, String orderName, String total_fee, String body, String extra_common_param, String outTradeNo, String type) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//String userId = securityContext.getUserPrincipal().getName();
//			if(StringUtils.isEmpty(userId)){
//				map.put("msg","请用户登录");
//				map.put("flag","false");
//				logger.error("用户未登录");
//				resourceModule.setResourceMap(map);
//				return resourceModule;
//			}
	        //订单名称，必填
			if(orderName==null||"".equals(orderName)){
				returnMap.put("msg","订单名称为空");
				returnMap.put("flag","false");
				logger.error("订单名称为空");
		        return JSON.toJSONString(returnMap);
			}


	        //付款金额，必填
	        if(total_fee==null||"".equals(total_fee)){
	        	returnMap.put("msg","金额必输项为空");
	        	returnMap.put("flag","false");
				logger.error("金额必输项为空");
		        return JSON.toJSONString(returnMap);
			}
	   
	        //商品描述，可空
	        if(body==null){
	        	returnMap.put("msg","商品描述可以为空但是不能为null");
	        	returnMap.put("flag","false");
				logger.error("商品描述可以为空但是不能为null");
		        return JSON.toJSONString(returnMap);
			}
	
	        if("zfbRecharge".equals(type)){
	        	outTradeNo=IDHelper.getTransactionId()+"";
	        }
	        //订单号
	        if(outTradeNo==null||"".equals(outTradeNo)){
	        	returnMap.put("msg","订单号不能为空");
	        	returnMap.put("flag","false");
				logger.error("订单号为空不能通过");
				return JSON.toJSONString(returnMap);
			}
//	        Map<String,String>map=new HashMap<String,String>();
//	        map.put("cust_id", user.get().getCust_Id());
//	        map.put("type", type);
//	        JSONObject jsonObject = JSONObject.fromObject(map);
	        
	        extra_common_param=opUser().getCustId()+"_"+type;
			//////////////////////////////////////////////////////////////////////////////////
			
			//把请求参数打包成数组
			Map<String, String> sParaTemp = new HashMap<String, String>();
			sParaTemp.put("service", AlipayConfig.service);
	        sParaTemp.put("partner", AlipayConfig.partner);
	        sParaTemp.put("seller_id", AlipayConfig.seller_id);
	        sParaTemp.put("_input_charset", AlipayConfig.input_charset);
			sParaTemp.put("payment_type", AlipayConfig.payment_type);
			sParaTemp.put("notify_url", AlipayConfig.notify_url);
			sParaTemp.put("return_url", AlipayConfig.return_url);
			sParaTemp.put("anti_phishing_key", AlipayConfig.anti_phishing_key);
			sParaTemp.put("exter_invoke_ip", AlipayConfig.exter_invoke_ip);
			sParaTemp.put("out_trade_no", outTradeNo);
			sParaTemp.put("subject", orderName);
			sParaTemp.put("total_fee", total_fee);
			sParaTemp.put("body", body);
			sParaTemp.put("extra_common_param", extra_common_param);//如果用户请求时传递了该参数，则返回给商户时会回传该参数。String(100)
			//其他业务参数根据在线开发文档，添加参数.文档地址:https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.O9yorI&treeId=62&articleId=103740&docType=1
	        //如sParaTemp.put("参数名","参数值");
			
			//建立请求
			String sHtmlText = AlipaySubmit.buildRequest(sParaTemp,"get","确认");
			returnMap.put("msg",sHtmlText);
			returnMap.put("flag","true");
		}catch (Exception e) {
			logger.error(e.getMessage());			
			returnMap.put("msg","输出失败");
			returnMap.put("flag","false");
		
		}
	
	//	PrintWriter out = null;
//		String jsonText = "[{\"result\":\"添加工作计划成功！\",\"id\":\""+planItem.getFiwpitemId()+
//		            "\",\"planDate\":\""+planDate+"\"," +
//		            "\"content\":\""+content+"\",\"responseMan\":\""+uMap.get(responseMan)+"\"}]";
//		PrintWriter out = null;
//		String jsonText = "[{\"result\":\"添加工作计划成功！\",\"id\":\""+planItem.getFiwpitemId()+
//		            "\",\"planDate\":\""+planDate+"\"," +
//		 


	    //xxx逻辑处理 
		//订单号没有稳定生成订单进入订单处理逻辑
		//直接进入订单处理逻辑
		alipayService.createOrder(request,orderName, total_fee, body, extra_common_param, outTradeNo, type,returnMap);
		
		
		return JSON.toJSONString(returnMap);
	    
	}
	public static void main(String[] args) {
		 Map<String,String>map=new HashMap<String,String>();
	        map.put("user_id", "1");
	        map.put("type", "2");
	        JSONObject jsonObject = JSONObject.fromObject(map);
	        
	        String extra_common_param=jsonObject.toString();
	        System.out.println(extra_common_param);
	        
	        JSONObject jsonObjecta = JSONObject.fromObject(extra_common_param);
	        System.out.println(jsonObjecta.get("user_id"));
	}
}
