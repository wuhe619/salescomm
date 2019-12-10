package com.bdaim.customer.controller;


import com.bdaim.common.controller.BasicAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customer.service.CustMsgService;

/**
 * 企业客户消息
 *
 */
@RestController
@RequestMapping("/customer/msg")
public class CustMsgController extends BasicAction {
	private static Logger logger = LoggerFactory.getLogger(CustMsgController.class);
			
	@Autowired
	private CustMsgService custMsgService;
	
	
	@PostMapping(value = "/all")
	public ResponseInfo query(@RequestBody(required=false) String body) {
		ResponseInfo resp = new ResponseInfo();
    	
		JSONObject params = null;
    	try {
    		if(body==null || "".equals(body))
    			body="{}";
    		params = JSONObject.parseObject(body);
    	}catch(Exception e) {
    		return new ResponseInfoAssemble().failure(-1, "查询条件解析异常");
    	}
    	
    	try {
        	LoginUser lu = opUser();
        	String cust_id = lu.getCustId();
        	String cust_group_id = lu.getUserGroupId();
        	String cust_user_id = lu.getUser_id();
        	if(cust_id==null || "".equals(cust_id))
        		cust_id="-1";
        	if(lu.getRole().contains("admin") || lu.getRole().contains("ROLE_USER"))
        		cust_id="all";
        	
        	resp.setData(custMsgService.query(cust_id, cust_user_id, params));
        } catch (Exception e) {
            logger.error("查询消息异常:"+e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "查询消息异常");
        }
    	
		return resp;
	}
	
	
	/**
     * 根据id唯一标识获取记录
     *
     */
    @ResponseBody
    @GetMapping(value = "/{id}")
    public ResponseInfo getInfo(@PathVariable(name = "id") Long id) {
    	ResponseInfo resp = new ResponseInfo();
    	
        try {
        	LoginUser lu = opUser();
        	String cust_id = lu.getCustId();
        	String cust_group_id = lu.getUserGroupId();
        	String cust_user_id = lu.getUser_id();
        	
        	JSONObject jo = custMsgService.getInfo(cust_id, cust_user_id, id);
        	resp.setData(jo);
        } catch (Exception e) {
            logger.error("获取消息异常:"+id+" "+e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "查询消息异常");
        }
        return resp;
    }
	
}
