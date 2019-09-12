package com.bdaim.open.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.account.service.OpenService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author 
 *
 */
@Controller
@RequestMapping("/api/open")
public class OpenAction0 {
	private Logger log = Logger.getLogger(OpenAction0.class);

	@Resource
	private OpenService openService;

	/**
	 * 标签覆盖用户数量（标签体系）
	 * 
	 * @param
	 * @return
	 */
	@RequestMapping(value = "ids", method = RequestMethod.GET, consumes = "application/json")
	@ResponseBody
	public String listIds(@RequestBody String orderId) {
		JSONObject result = new JSONObject();
		try {
			result.put("isSuccess", 1);
			result.put("_message", "标签用户数量接口调用成功！");
		} catch (Exception e) {
			result.put("success", 0);
			result.put("_message", "标签用户数量接口调用失败！"+e);
			log.error("标签用户数量接口调用失败！",e);
		}
		return result.toJSONString();
	}


	/**
	 * 获取token
	 * @param param
	 * @return
	 */
	@RequestMapping(value = "/getToken", method = RequestMethod.POST)
	@ResponseBody
	public String tokenInfoGet(@RequestBody JSONObject param) {
		Map<String, Object> resultMap = new HashMap<>();
		String username = param.getString("username");
		String password = param.getString("password");
		resultMap = openService.getTokenInfo(username, password);
		return JSONObject.toJSONString(resultMap);
	}

	/**
	 * 刷新token
	 * @param param
	 * @return
	 */
	@RequestMapping(value = "/refreshToken", method = RequestMethod.POST)
	@ResponseBody
	public Object refreshToken(@RequestBody JSONObject param) {
		String oldtoken = param.getString("oldtoken");
		String username = param.getString("username");
		Map<String, Object> resultMap = openService.refreshToken0(oldtoken, username);
		return JSONObject.toJSONString(resultMap);
	}
}
