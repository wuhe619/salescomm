package com.bdaim.customs.services;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;
import org.springframework.stereotype.Service;

import java.util.List;

/***
 * 报关单.主单
 */
@Service("busi_bgd_z")
public class BgdZService implements BusiService{

	@Override
	public void insertInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id, JSONObject info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id, JSONObject info) {
		/*HBusiDataManager manager = hBusiDataManagerDao.get(id);
		if (manager == null) {
			throw new Exception("修改的数据不存在");
		}
		String content = manager.getContent();
		MainDan dbjson = JSON.parseObject(content, MainDan.class);
		BeanUtils.copyProperties(mainDan, dbjson);
		manager.setContent(JSON.toJSONString(dbjson));
		hBusiDataManagerDao.save(manager);
		updateDataToES(manager, Integer.valueOf(id));*/
		
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
