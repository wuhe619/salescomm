package com.bdaim.customs.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;

/***
 * 舱单.主单
 */
@Service("busi_cd_z")
public class CdZService implements BusiService{

	@Override
	public void insertInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id, JSONObject info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id, JSONObject info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id, JSONObject info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String formatQuery(String busiType, String cust_id, String cust_group_id, String cust_user_id, JSONObject params, List sqlParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void formatInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, JSONObject info) {
		// TODO Auto-generated method stub
		
	}

}
