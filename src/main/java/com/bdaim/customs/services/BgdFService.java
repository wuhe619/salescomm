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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/***
 * 报关单.分单
 */
@Service("busi_bgd_f")
public class BgdFService implements BusiService{

	private static Logger log = LoggerFactory.getLogger(BgdFService.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ElasticSearchService elasticSearchService;

	@Autowired
	private CustomerDao customerDao;

	@Autowired
	private SequenceService sequenceService;

	@Autowired
	private HBusiDataManagerDao hBusiDataManagerDao;

	@Autowired
	private ServiceUtils serviceUtils;


	@Override
	public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
		// TODO Auto-generated method stub
		Integer pid = info.getInteger("pid");
		String billNo = info.getString("bill_no");
		if(pid==null){
			log.error("主单id不能为空");
			throw new Exception("主单id不能为空");
		}
		if(StringUtil.isEmpty(billNo)){
			log.error("分单号不能为空");
			throw new Exception("分单号不能为空");
		}
		HBusiDataManager sbdzd = serviceUtils.getObjectByIdAndType(pid.longValue(),BusiTypeEnum.SZ.getType());
		List<HBusiDataManager> list = serviceUtils.getDataList(pid.longValue());
		if(list!=null && list.size()>0){
			for(HBusiDataManager hBusiDataManager:list){
				JSONObject jsonObject=JSONObject.parseObject(hBusiDataManager.getContent());
				if(billNo.equals(jsonObject.getString("bill_no"))){
					log.error("分单号【"+billNo+"】在主单【"+pid+"】中已经存在");
					throw new Exception("分单号【"+billNo+"】在主单【"+pid+"】中已经存在");
				}
			}
		}
		info.put("type", BusiTypeEnum.BF.getType());
		info.put("check_status", "0");
		info.put("idcard_pic_flag", "0");
		info.put("main_gname","");
		info.put("low_price_goods",0);
		info.put("id",id);
		info.put("pid",pid);
		info.put("opt_type","APD");
		serviceUtils.addDataToES(id.toString(),busiType,info);
		JSONObject jsonObject = JSONObject.parseObject(sbdzd.getContent());
		if(info.containsKey("weight") && info.getString("weight")!=null){
			if(jsonObject.containsKey("weight_total")) {
				String  weight_total = jsonObject.getString("weight_total");
				if(StringUtil.isNotEmpty(weight_total)){
					weight_total=String.valueOf(Float.valueOf(weight_total)+Float.valueOf(info.getString("weight")));
					jsonObject.put("weight_total", weight_total);//总重量
				}
			}
		}
		int value = 1;
		if(jsonObject.containsKey("party_total")){
			value = jsonObject.getInteger("party_total")+value;
		}
		jsonObject.put("party_total", value);//分单总数

		sbdzd.setContent(jsonObject.toJSONString());
		hBusiDataManagerDao.saveOrUpdate(sbdzd);
		serviceUtils.updateDataToES(BusiTypeEnum.BZ.getType(),sbdzd.getId().toString(),jsonObject);
		
	}

	@Override
	public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
		// 提交至海关平台
		if ("HAIGUAN".equals(info.getString("_rule_"))) {
			String sql = "select content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from h_data_manager where type=? and id=? ";
			Map m = jdbcTemplate.queryForMap(sql, busiType, id);
			if ("1".equals(String.valueOf(m.get("ext_1")))) {
				log.warn("报关单分单:[" + id + "]已提交至海关");
				throw new Exception("报关单分单:[" + id + "]已提交至海关");
			}
			// 更新报关单主单信息
			String content = (String) m.get("content");
			JSONObject jo = JSONObject.parseObject(content);
			jo.put("ext_1", "1");
			jo.put("send_status", "1");
			jo.put("id", m.get("id"));
			jo.put("cust_id", m.get("cust_id"));
			jo.put("cust_group_id", m.get("cust_group_id"));
			jo.put("cust_user_id", m.get("cust_user_id"));
			jo.put("create_id", m.get("create_id"));
			jo.put("create_date", m.get("create_date"));
			jo.put("update_id", m.get("update_id"));
			jo.put("update_date", m.get("update_date"));
			if (m.get("ext_1") != null && !"".equals(m.get("ext_1")))
				jo.put("ext_1", m.get("ext_1"));
			if (m.get("ext_2") != null && !"".equals(m.get("ext_2")))
				jo.put("ext_2", m.get("ext_2"));
			if (m.get("ext_3") != null && !"".equals(m.get("ext_3")))
				jo.put("ext_3", m.get("ext_3"));
			if (m.get("ext_4") != null && !"".equals(m.get("ext_4")))
				jo.put("ext_4", m.get("ext_4"));
			if (m.get("ext_5") != null && !"".equals(m.get("ext_5")))
				jo.put("ext_5", m.get("ext_5"));

			sql = "UPDATE h_data_manager SET ext_1 = '1', ext_date1 = NOW(), content=? WHERE id = ? AND ext_1 <>'1' ";
			jdbcTemplate.update(sql, jo.toJSONString(), id);
			serviceUtils.updateDataToES(BusiTypeEnum.SZ.getType(), id.toString(), jo);
		}else{
			HBusiDataManager dbManager = serviceUtils.getObjectByIdAndType(id,busiType);
			String content = dbManager.getContent();
			JSONObject json = JSONObject.parseObject(content);
			Iterator keys = info.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				json.put(key, info.get(key));
			}
			serviceUtils.updateDataToES(busiType,id.toString(),json);
		}
		
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
