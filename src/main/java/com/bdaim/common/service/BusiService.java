package com.bdaim.common.service;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface BusiService {

	public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception;
	
	public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) ;
	
	public void getInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id, JSONObject info, JSONObject param) ;
	public void getInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) ;

	public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws Exception;
	
	public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams);
	public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) ;
}
