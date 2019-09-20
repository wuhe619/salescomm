package com.bdaim.customer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CustMsgService {

	public static final Logger logger = LoggerFactory.getLogger(CustMsgService.class);
	
	@Resource
	private JdbcTemplate jdbcTemplate;
	
	
	public void createMsg(String cust_id, String cust_user_id, String msg_type, String content, int level) throws Exception{
		if(level<0)
			level = 0;
		else if(level>5)
			level = 5;
		
		Object to_cust_id = "";
		List<Object> to_cust_user_ids = new ArrayList();
		
		if(content==null || "".equals(content.trim()))
			throw new Exception("创建消息异常:空内容");
		if((cust_id==null || "".equals(cust_id)) && (cust_user_id==null || "".equals(cust_user_id)))
			throw new Exception("创建消息异常:无消息接收人");
		
		try {
			if(cust_user_id!=null && !"".equals(cust_user_id)) {
				List<Map<String,Object>> cts =jdbcTemplate.queryForList("select cust_id from t_customer_user where id=?", cust_user_id);
				if(cts.size()<=0)
					throw new Exception("创建消息异常:错误的消息接收人");
				to_cust_id = cts.get(0).get("cust_id");
				to_cust_user_ids.add(cust_user_id);
			}else {
				List<Map<String,Object>> cus = jdbcTemplate.queryForList("select id from t_customer_user where cust_id=?", cust_id);
				if(cus.size()<=0)
					throw new Exception("创建消息异常:错误的消息接收客户");
				for(Map cu : cus)
				   to_cust_user_ids.add(cu.get("id"));
				to_cust_id=cust_id;
			}
		}catch(Exception e) {
			logger.error(e.getMessage());
			throw new Exception("解析消息接收人异常");
		}
		
		String sql = "insert into h_customer_msg(cust_id, cust_user_id, msg_type, content, create_time, status, level) value(?,?,?,?, now(), 0, ?)";
		try {
			List params = new ArrayList();
			
			for(Object to_cust_user_id : to_cust_user_ids) {
				Object[] p1 = new Object[] {to_cust_id, to_cust_user_id, msg_type, content, level};
				params.add(p1);
			}
			
			jdbcTemplate.batchUpdate(sql, params);
		}catch(Exception e) {
			logger.error(e.getMessage());
			throw new Exception("创建消息异常:"+cust_id+"."+cust_user_id);
		}
	}
	
}
