package com.bdaim.api.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.bdaim.api.Dto.ApiData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.auth.service.impl.TokenServiceImpl;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;

@RestController
@RequestMapping("/api")
public class ApiController {
	@Resource
	private TokenServiceImpl tokenService;

	/** 
	 * Query Api Infos
	 **/
	@PostMapping("/infos")
    public ResponseInfo apis(@RequestBody JSONObject params) {
		ResponseInfo info = new ResponseInfo();
		
		return info;
	}
	
	/**
	 * Save Api
	 **/
    @PostMapping("/info/{apiId}")
    public ResponseInfo saveApi(@RequestBody ApiData apiData, @PathVariable(name = "apiId") String apiId) {
    	ResponseInfo info = new ResponseInfo();
    	
    	LoginUser lu = tokenService.opUser();
    	
    	return info;
    }
    
    /**
     * Get Api
     **/
    @GetMapping("/info/{apiId}")
    public ResponseInfo getApi(@PathVariable(name = "apiId") String apiId) {
    	ResponseInfo info = new ResponseInfo();
    	
    	
    	return info;
    }
    
    /**
     * Delete Api
     **/
    @DeleteMapping("/info/{apiId}")
    public ResponseInfo deleteApi(@PathVariable(name = "apiId") String apiId) {
    	ResponseInfo info = new ResponseInfo();
    	
    	return info;
    }
    
    
    /**
	 * Subscribe Api
	 **/
    @PostMapping("/subscription/{apiId}")
    public ResponseInfo subApi(@RequestBody JSONObject params, @PathVariable(name = "apiId") String apiId) {
    	ResponseInfo info = new ResponseInfo();
    	
    	LoginUser lu = tokenService.opUser();
    	
    	return info;
    }
    
    /**
	 * No Subscribe Api
	 **/
    @DeleteMapping("/subscription/{apiId}")
    public ResponseInfo subApi(@PathVariable(name = "apiId") String apiId) {
    	ResponseInfo info = new ResponseInfo();
    	
    	LoginUser lu = tokenService.opUser();
    	
    	return info;
    }
    
    /**
	 * Set Api Price
	 **/
    @PostMapping("/price/{apiId}")
    public ResponseInfo priceApi(@RequestBody JSONObject params, @PathVariable(name = "apiId") String apiId) {
    	ResponseInfo info = new ResponseInfo();
    	
    	LoginUser lu = tokenService.opUser();
    	
    	return info;
    }
}
