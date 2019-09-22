package com.bdaim.customs.services;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.Constants;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.utils.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;

/***
 * 舱单.分单
 */
@Service("busi_cd_f")
public class CdFService implements BusiService{

	private static Logger log = LoggerFactory.getLogger(CdFService.class);

	@Autowired
	private ElasticSearchService elasticSearchService;

	@Autowired
	private CustomerDao customerDao;

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private SequenceService sequenceService;

	@Autowired
	private HBusiDataManagerDao hBusiDataManagerDao;

	@Autowired
    private ServiceUtils serviceUtils;


	@Override
	public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
		// TODO Auto-generated method stub
		HBusiDataManager fM = serviceUtils.getObjectByIdAndType(id,busiType);
		if(fM == null){
			throw new Exception("仓单分单["+id+"] 不存在");
		}
		JSONObject json = JSONObject.parseObject(fM.getContent());

		Iterator keys = json.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			json.put(key, info.get(key));
		}
        serviceUtils.updateDataToES(busiType,id.toString(),json);

		List<HBusiDataManager> list = serviceUtils.getDataList(json.getLong("pid"));
		int packNo=0;
		float weight=0;
		for(HBusiDataManager hBusiDataManager:list){
			JSONObject json2 = JSONObject.parseObject(fM.getContent());
			if(hBusiDataManager.getId()==id.intValue()){
				if(info.containsKey("pack_no") && StringUtil.isNotEmpty(info.getString("pack_no"))){
					packNo += info.getInteger("pack_no");
				}
				if(info.containsKey("pack_no") && StringUtil.isNotEmpty(info.getString("pack_no"))){
					weight += info.getFloatValue("weight");
				}
			}else{
				if(json2.containsKey("pack_no")&& StringUtil.isNotEmpty("pack_no")){
					packNo += json2.getInteger("pack_no");
				}
				if(json2.containsKey("pack_no") && StringUtil.isNotEmpty(json2.getString("pack_no"))){
					weight += json2.getInteger("weight_total");
				}
			}
		}
		HBusiDataManager cangdanz = serviceUtils.getObjectByIdAndType(json.getLong("pid"),BusiTypeEnum.CZ.getType());
		JSONObject superObj = JSONObject.parseObject(cangdanz.getContent());
		superObj.put("weight",weight);
		superObj.put("pack_no",packNo);
		cangdanz.setContent(superObj.toJSONString());
		hBusiDataManagerDao.saveOrUpdate(cangdanz);
        serviceUtils.updateDataToES(BusiTypeEnum.CZ.getType(),cangdanz.getId().toString(),superObj);
	}

	@Override
	public void getInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
		// TODO Auto-generated method stub
		
	}


}
