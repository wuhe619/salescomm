package com.bdaim.common.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface BusiService {

	public void insertInfo(String busiType, String cust_id, String user_id, String id, JSONObject info) ;
	
	public void updateInfo(String busiType, String cust_id, String user_id, String id, JSONObject info) ;
	
	public void getInfo(String busiType, String cust_id, String user_id, String id, JSONObject info) ;
	
	public void deleteInfo(String busiType, String cust_id, String user_id, String id) ;
	
	public String formatQuery(String busiType, String cust_id, String user_id, JSONObject params, List sqlParams);
	public void formatInfo(String busiType, String cust_id, String user_id, JSONObject info) ;
}
