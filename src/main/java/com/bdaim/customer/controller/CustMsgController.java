package com.bdaim.customer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customer.service.CustMsgService;

/**
 * 企业客户消息
 *
 */
@RestController
@RequestMapping("/customer/msg")
public class CustMsgController {

	@Autowired
	private CustMsgService custMsgService;
	
	
	@PostMapping(value = "/all")
	public ResponseInfo query(String body) {
		ResponseInfo resp = new ResponseInfo();
    	
    	
		return resp;
	}
	
}
