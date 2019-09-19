package com.bdaim.customs.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;

/***
 * 申报单.主单
 */
@Service("busi_sbd_z")
public class SbdZService implements BusiService{

	@Override
	public void insertInfo(String busiType, String cust_id, String user_id, String id, JSONObject info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateInfo(String busiType, String cust_id, String user_id, String id, JSONObject info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getInfo(String busiType, String cust_id, String user_id, String id, JSONObject info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteInfo(String busiType, String cust_id, String user_id, String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String formatQuery(String busiType, String cust_id, String user_id, JSONObject params, List sqlParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void formatInfo(String busiType, String cust_id, String user_id, JSONObject info) {
		// TODO Auto-generated method stub
		
	}

}
