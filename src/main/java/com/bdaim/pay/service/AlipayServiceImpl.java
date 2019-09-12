package com.bdaim.pay.service;

import com.bdaim.common.util.RegularUtil;
import com.bdaim.order.entity.OrderDO;
import com.bdaim.pay.dao.AlipayDao;
import com.bdaim.resource.entity.MarketResourceEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 支付宝支付过程逻辑sservice服务实现类
 *
 */
@Service("alipayService")
@Transactional
public class AlipayServiceImpl implements AlipayService{
	
	/**
	 * 日志
	 */
	private static Log logger = LogFactory.getLog(AlipayServiceImpl.class);
	
	@Resource
	private AlipayDao alipayDao;
	
	@Resource
	private JdbcTemplate jdbcTemplate;
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
	@SuppressWarnings("unchecked")
	@Override
	public void createOrder(HttpServletRequest request, String orderName, String total_fee, String body, String extra_common_param,
                            String outTradeNo, String type, Map<String, Object> returnMap) {
		if(type==null||"".equals(type)){
			returnMap.put("msg","请说明订单来源");
			returnMap.put("flag","false");
			logger.error("订单来源不清楚："+outTradeNo);
			return;
		}
//		String acctId = (String) request.getSession().getParameter("acctId");
//		String custId = (String) request.getSession().getParameter("custId");
		//String acctId = "18888";
		String custId = "18888";
		//测试代码购买营销资源volice
		if(type.equals("MarketResource/sms")||type.equals("MarketResource/voice")||type.equals("MarketResource/email")){
/*			//将订单写入到订单表和交易表
			//1.写入交易表t_transaction
//			transaction_id	交易id
//			acct_id	账户id
//			cust_id	客户id
//			type	交易类型（1.充值 2.消费）
//			pay_mode	支付类型（1.余额 2.第三方 3.线下）
//			third_party_num	第三方交易流水号
//			amount	金额
//			order_id	订单id
//			remark	备注
//			certificate	凭证(上传凭证的路径)
//			create_time	交易时间			
			TransactionDO transactionDO = new TransactionDO();

			transactionDO.setAcctId(acctId);
			transactionDO.setCustId(custId);
			transactionDO.setType(2);//消费
			transactionDO.setPayMode(2);
//			transactionDO.setThirdPartyNum();//回调逻辑加上
			transactionDO.setAmount(Integer.parseInt(total_fee));
			transactionDO.setOrderId(outTradeNo);
			transactionDO.setRemark(extra_common_param);
			transactionDO.setCreateTime((Timestamp)(new Date()));;
	        try {
	        	alipayDao.save(transactionDO);
	        }catch (Exception e){
	            logger.error(e.getMessage());
	            returnMap.put("msg","插入t_transaction失败订单号："+outTradeNo);
				returnMap.put("flag","false");
				return;
	        }*/
	        //插入t_order表
	        OrderDO orderDO = new OrderDO();
	        orderDO.setOrderId(outTradeNo);//订单id
	        orderDO.setCustId(custId);//客户id
	        orderDO.setSupplierId(request.getParameter("supplierId")==null?"":request.getParameter("supplierId").toString());//供应商id
	        if(request.getParameter("supplierId")==null||"".equals(request.getParameter("supplierId").toString())){
				returnMap.put("msg","供应商id不清楚");
				returnMap.put("flag","flase");
				logger.error("供应商id不清楚："+outTradeNo);
				return;
			}
	        orderDO.setSupplierName(request.getParameter("supplierName")==null?"":request.getParameter("supplierName").toString());//供应商名字
	        orderDO.setEnterpriseName(request.getParameter("enterpriseName")==null?"":request.getParameter("enterpriseName").toString());//企业名称
	        orderDO.setOrderType(2);//订单类型(1.购买客户群 2.购买营销资源)
	        orderDO.setOldOrderCode(request.getParameter("oldOrderCode")==null?"":request.getParameter("oldOrderCode").toString());//原始订单号
	        orderDO.setCancleReason(request.getParameter("cancleReason")==null?"":request.getParameter("cancleReason").toString());//撤单原因
	        orderDO.setOrderState(0);//订单状态待定0待支付
	        orderDO.setPayType(2);//支付类型（1.余额 2.第三方 3.线下）
	        orderDO.setCreateTime(new Date());//创建日期
//	        orderDO.setEffectTime(new Date());//生效日期
//	        orderDO.setExpireTime(new Date());//失效日期
	        orderDO.setRemarks(extra_common_param);//备注
	        //从t_market_resource查看销售价和成本价
	    	StringBuffer hql = new StringBuffer();
			hql.append("from MarketResourceEntity where status=1 and supplierId=? and typeCode=?");//资源类型（1.voice 2.SMS 3.email）
			List<Map<String, Object>> list=new ArrayList<Map<String, Object>>();
			if(type.equals("MarketResource/voice")){
				list = alipayDao.createQuery(hql.toString(),request.getParameter("supplierId").toString(),1).list();
			}
			if(type.equals("MarketResource/sms")){
				list =  alipayDao.createQuery(hql.toString(),request.getParameter("supplierId").toString(),2).list();
			}
			if(type.equals("MarketResource/email")){
				list = alipayDao.createQuery(hql.toString(),request.getParameter("supplierId").toString(),3).list();
			}
			if(list.size()==0){
				returnMap.put("msg","找不到供应商");
				returnMap.put("flag","false");
				logger.error("找不到供应商："+outTradeNo);
				return;
			}
			if(list.size()>1){
				returnMap.put("msg","找到多个供应商");
				returnMap.put("flag","false");
				logger.error("找到多个供应商："+outTradeNo);
				return;
			}
			if(request.getParameter("quantity")==null||RegularUtil.isNum(request.getParameter("quantity").toString())){
				returnMap.put("msg","购买数量不合法:"+request.getParameter("quantity"));
				returnMap.put("flag","false");
				logger.error("购买数量不合法:"+request.getParameter("quantity"));
				return;
			}
			Integer salePrice = ((MarketResourceEntity)list.get(0)).getSalePrice();
			Integer costPrice = ((MarketResourceEntity)list.get(0)).getCostPrice();
			
	        orderDO.setAmount(salePrice);//销售价格
	        orderDO.setProductName(request.getParameter("productName")==null?"":request.getParameter("productName").toString());//产品名称
	        orderDO.setQuantity(Integer.parseInt(request.getParameter("quantity").toString()));//购买数量
	        orderDO.setCostPrice(costPrice);//成本价
	        
//	        orderDO.setPayTime(new Date());//支付时间
	        orderDO.setPayAmount(salePrice*Integer.parseInt(request.getParameter("quantity").toString()));//支付金额和支付宝返回的金额做比较
	        
	        try {
	        	alipayDao.save(orderDO);
	        }catch (Exception e){
	            logger.error(e.getMessage());
	            returnMap.put("msg","插入t_order失败订单号："+outTradeNo);
				returnMap.put("flag","false");
				return;
	        }
	        
			return;
		}
		//以此类推继续写
//		if(type.equals("topup")){
//			return;
//		}
		if("MarketResource/customerGroup".equals(type)){
		
		return;
		}
		if("zfbRecharge".equals(type)){
		
		return;
		}
		
		returnMap.put("msg","找不到合适订单处理逻辑");
		returnMap.put("flag","false");
		logger.error("找不到合适订单处理逻辑：订单号："+outTradeNo+",订单类型："+type);
		return;
		
		
	}

	
}
