package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/***
 * 报关单.主单
 */
@Service("busi_bgd_z")
public class BgdZService implements BusiService{

	private static Logger log = LoggerFactory.getLogger(BusiService.class);

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


	@Override
	public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
		// TODO Auto-generated method stub
		if(StringUtil.isNotEmpty(info.getString("fromSbzId"))) {
			HBusiDataManager h = getObjectByIdAndType(info.getLong("fromSbzId"),BusiTypeEnum.SZ.getType());
			if (h == null) {
				throw new Exception("数据不存在");
			}
			if (!cust_id.equals(h.getCust_id().toString())) {
				throw new Exception("你无权处理");
			}

			List<HBusiDataManager> dataList = new ArrayList<>();
			if ("Y".equals(h.getExt_1())) {
				throw new Exception("已经提交过了,不能重复提交");
			}

			buildDanList(info,id,dataList, cust_id,cust_user_id, h, BusiTypeEnum.BZ.getType());

			for (HBusiDataManager dm : dataList) {
				if(!dm.getType().equals(BusiTypeEnum.BZ.getType())) {
					hBusiDataManagerDao.saveOrUpdate(dm);
				}
				addDataToES(dm);
			}
		}

	}

	@Override
	public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
		// 提交至海关平台
		if ("HAIGUAN".equals(info.getString("_rule_"))) {
			String sql = "select content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from h_data_manager where type=? and id=? ";
			Map m = jdbcTemplate.queryForMap(sql, busiType, id);
			if ("1".equals(String.valueOf(m.get("ext_1")))) {
				log.warn("报关单:[" + id + "]已提交至海关");
				throw new Exception("报关单:[" + id + "]已提交至海关");
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
			updateDataToES(BusiTypeEnum.SZ.getType(), id.toString(), jo);

			//更新报关单分单信息
			String selectSql = "select content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from h_data_manager JSON_EXTRACT(content, '$.pid')=? AND ext_1 <>'1' ";
			List<Map<String, Object>> ds = jdbcTemplate.queryForList(selectSql, id);
			String updateSql = " UPDATE h_data_manager SET ext_1 = '1', ext_date1 = NOW(), content=? WHERE JSON_EXTRACT(content, '$.pid')=? AND ext_1 <>'1' ";
			for (int i = 0; i < ds.size(); i++) {
				m = ds.get(i);
				content = (String) m.get("content");
				jo = JSONObject.parseObject(content);
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
				jdbcTemplate.update(updateSql, jo.toJSONString(), id);
				updateDataToES(BusiTypeEnum.SF.getType(), String.valueOf(m.get("id")), jo);

			}

		}


	}

	/**
	 * 更新es
	 *
	 * @param type
	 * @param id
	 * @param content
	 */
	private void updateDataToES(String type, String id, JSONObject content) {
		if (type.equals(BusiTypeEnum.SZ.getType())) {
			elasticSearchService.updateDocumentToType(Constants.SZ_INFO_INDEX, "haiguan", id, content);
		} else if (type.equals(BusiTypeEnum.CZ.getType())) {
			elasticSearchService.updateDocumentToType(Constants.CZ_INFO_INDEX, "haiguan", id, content);
		} else if (type.equals(BusiTypeEnum.BZ.getType())) {
			elasticSearchService.updateDocumentToType(Constants.BZ_INFO_INDEX, "haiguan", id, content);
		} else if (type.equals(BusiTypeEnum.SF.getType())) {
			elasticSearchService.updateDocumentToType(Constants.SF_INFO_INDEX, "haiguan", id, content);
		} else if (type.equals(BusiTypeEnum.CF.getType())) {
			elasticSearchService.updateDocumentToType(Constants.CF_INFO_INDEX, "haiguan", id, content);
		} else if (type.equals(BusiTypeEnum.BF.getType())) {
			elasticSearchService.updateDocumentToType(Constants.BF_INFO_INDEX, "haiguan", id, content);
		} else if (type.equals(BusiTypeEnum.SS.getType())) {
			elasticSearchService.updateDocumentToType(Constants.SS_INFO_INDEX, "haiguan", id, content);
		} else if (type.equals(BusiTypeEnum.CS.getType())) {
			elasticSearchService.updateDocumentToType(Constants.CS_INFO_INDEX, "haiguan", id, content);
		} else if (type.equals(BusiTypeEnum.BS.getType())) {
			elasticSearchService.updateDocumentToType(Constants.BS_INFO_INDEX, "haiguan", id, content);
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


	public HBusiDataManager getObjectByIdAndType(Long id,String type){
		String sql="select * from h_data_manager where id="+id+" and type='"+type+"'";
		RowMapper<HBusiDataManager> managerRowMapper=new BeanPropertyRowMapper<>(HBusiDataManager.class);
		return jdbcTemplate.queryForObject(sql,managerRowMapper);
	}

	public void delDataListByIdAndType(Long id,String type){
		String sql="delete from h_data_manager where type='"+type+"' and id="+id;
		jdbcTemplate.execute(sql);
	}

	/**
	 * 添加数据到es
	 *
	 * @param hBusiDataManager
	 * @param
	 */
	private void addDataToES(HBusiDataManager hBusiDataManager) {
		String type = hBusiDataManager.getType();
		String id=hBusiDataManager.getId().toString();
		if (type.equals(BusiTypeEnum.SZ.getType())) {
			elasticSearchService.addDocumentToType(Constants.SZ_INFO_INDEX, "haiguan", id, JSON.parseObject(hBusiDataManager.getContent()));
		}else if(type.equals(BusiTypeEnum.CZ.getType())){
			elasticSearchService.addDocumentToType(Constants.CZ_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
		}else if(type.equals(BusiTypeEnum.BZ.getType())){
			elasticSearchService.addDocumentToType(Constants.BZ_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
		} else if (type.equals(BusiTypeEnum.SF.getType())) {
			elasticSearchService.addDocumentToType(Constants.SF_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
		}else if( type.equals(BusiTypeEnum.CF.getType())){
			elasticSearchService.addDocumentToType(Constants.CF_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
		}else if(type.equals(BusiTypeEnum.BF.getType())){
			elasticSearchService.addDocumentToType(Constants.BF_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
		}else if (type.equals(BusiTypeEnum.SS.getType())) {
			elasticSearchService.addDocumentToType(Constants.SS_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
		}else if(type.equals(BusiTypeEnum.CS.getType())){
			elasticSearchService.addDocumentToType(Constants.CS_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
		}else if(type.equals(BusiTypeEnum.BS.getType())){
			elasticSearchService.addDocumentToType(Constants.BS_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
		}
	}

	public void buildDanList(JSONObject info,Long id,List<HBusiDataManager> dataList, String custId,Long userId, HBusiDataManager h, String type) throws Exception {
		HBusiDataManager CZ = new HBusiDataManager();
		CZ.setType(BusiTypeEnum.BZ.getType());
		CZ.setId(id.intValue());
		CZ.setCreateDate(new Date());
		CZ.setCust_id(Long.valueOf(custId));
		CZ.setCreateId(Long.valueOf(userId));
		CZ.setExt_3(h.getExt_3());
		CZ.setExt_1("0");//未发送 1，已发送


		JSONObject json = JSON.parseObject(h.getContent());
		json.put("create_id", userId);
		json.put("cust_id", custId);
		json.put("type", CZ.getType());
		json.put("create_date", CZ.getCreateDate());
		json.put("send_status", CZ.getExt_1());
		json.put("commit_baodan_status", "Y");

		Iterator keys = json.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			info.put(key, json.get(key));
		}
		info.put("ext_3",h.getExt_3());
		info.put("ext_1","0");

		JSONObject jon = JSON.parseObject(h.getContent());
		jon.put("commit_baodan_status", "Y");
		h.setExt_1("Y");
		h.setContent(jon.toJSONString());
		dataList.add(h);

		CZ.setContent(info.toJSONString());
		dataList.add(CZ);
		List<HBusiDataManager> parties = getDataList(info.getLong("fromSbzId"));
		for (HBusiDataManager hp : parties) {
			HBusiDataManager hm = new HBusiDataManager();
			hm.setType(BusiTypeEnum.BF.getType());
			hm.setCreateDate(new Date());
			Long fid = sequenceService.getSeq(BusiTypeEnum.BF.getType());
			hm.setId(fid.intValue());
			hm.setExt_3(hp.getExt_3());
			hm.setExt_4(hp.getExt_4());
			hm.setCreateId(hp.getCreateId());
			hm.setCust_id(hp.getCust_id());
			JSONObject _content = JSON.parseObject(hp.getContent());
			_content.put("pid",id);
			hm.setContent(_content.toJSONString());
			dataList.add(hm);
			List<HBusiDataManager> goods = getDataList(hp.getId().longValue());
			for (HBusiDataManager gp : goods) {
				HBusiDataManager good = new HBusiDataManager();
				gp.setType(BusiTypeEnum.BS.getType());
				Long gid = sequenceService.getSeq(BusiTypeEnum.BS.getType());
				good.setId(gid.intValue());
				good.setCreateDate(new Date());
				JSONObject __content = JSON.parseObject(gp.getContent());
				__content.put("pid",fid);
				good.setContent(__content.toJSONString());
				good.setType(BusiTypeEnum.BS.getType());
				good.setCreateId(gp.getCreateId());
				good.setCust_id(gp.getCust_id());
				good.setExt_3(gp.getExt_3());
				good.setExt_4(gp.getExt_4());
				dataList.add(good);
			}
		}
	}


	public List<HBusiDataManager> getDataList(Long pid){
		String sql2 = "select * from h_data_manager where  JSON_EXTRACT(content, '$.pid')="+pid +" or JSON_EXTRACT(content, '$.pid')='"+pid+"'";
		RowMapper<HBusiDataManager>managerRowMapper = new BeanPropertyRowMapper<>(HBusiDataManager.class);
		return jdbcTemplate.query(sql2,managerRowMapper);
	}
}
