package com.bdaim.common.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;

import java.util.List;

public interface BusiService {

	public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception;
	
	public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception;

	public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) throws Exception;

	public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws Exception;
	
	public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) throws Exception;
	public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) ;
}
