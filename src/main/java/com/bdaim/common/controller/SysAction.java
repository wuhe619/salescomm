package com.bdaim.common.controller;


import com.bdaim.auth.LoginUser;
import com.bdaim.auth.service.impl.TokenServiceImpl;
import com.bdaim.common.exception.TouchException;
import org.apache.commons.httpclient.util.DateUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/sys")
public class SysAction extends BasicAction {

	/**
	 * token列表
	 */
	@ResponseBody
	@RequestMapping("/tokens")
	public List listTokens() throws Exception{
		LoginUser lu = super.opUser();
		if(!"admin".equals(lu.getName())) {
			throw new TouchException("401", "auth is error");
		}
		
		List data = new ArrayList();
		Map tokens = TokenServiceImpl.listTokens();
		Iterator keys = tokens.keySet().iterator();
		while(keys.hasNext()) {
			String key = (String)keys.next();
			LoginUser u = (LoginUser)tokens.get(key);
			Map d = new HashMap();
			d.put("tokenid", lu.getTokenid());
			d.put("tokentime", DateUtil.formatDate(new Date(lu.getTokentime())));
			d.put("name", lu.getName());
		}
		return data;
	}
}
