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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/***
 * 舱单.主单
 */
@Service("busi_cd_z")
public class CdZService implements BusiService{

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
			if ("Y".equals(h.getExt_2())) {
				throw new Exception("已经提交过了,不能重复提交");
			}

			buildDanList(info,id,dataList, cust_id,cust_user_id, h);

			for (HBusiDataManager dm : dataList) {
				if(!dm.getType().equals(BusiTypeEnum.CZ.getType())) {
					hBusiDataManagerDao.saveOrUpdate(dm);
				}
				addDataToES(dm);
			}
		}



	}

	@Override
	public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) {
		// TODO Auto-generated method stub
		
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

	public void buildDanList(JSONObject info,Long id,List<HBusiDataManager> dataList, String custId,Long userId, HBusiDataManager h) throws Exception {
		HBusiDataManager CZ = new HBusiDataManager();
		CZ.setType(BusiTypeEnum.CZ.getType());

		CZ.setCreateDate(new Date());
		CZ.setCust_id(Long.valueOf(custId));
		CZ.setCreateId(Long.valueOf(userId));
		CZ.setExt_3(h.getExt_3());
		CZ.setExt_1("0");//未发送 1，已发送

		info.put("ext_3",h.getExt_3());
		info.put("ext_1","0");

		JSONObject json = JSON.parseObject(h.getContent());
		json.put("create_id", userId);
		json.put("cust_id", custId);
		json.put("type", CZ.getType());
		json.put("create_date", CZ.getCreateDate());
		json.put("send_status", CZ.getExt_1());
		json.put("commit_cangdan_status", "Y");

		JSONObject jon = JSON.parseObject(h.getContent());
		jon.put("commit_cangdan_status", "Y");
		h.setExt_2("Y");
		h.setContent(jon.toJSONString());
		dataList.add(h);

		String content = json.toJSONString();
		CZ.setContent(content);
		dataList.add(CZ);
		List<HBusiDataManager> parties = getDataList(info.getLong("fromSbzId"));
		for (HBusiDataManager hp : parties) {
			HBusiDataManager hm = new HBusiDataManager();
			hm.setType(BusiTypeEnum.CF.getType());
			hm.setCreateDate(new Date());
			Long fid = sequenceService.getSeq(BusiTypeEnum.CF.getType());
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
				gp.setType(BusiTypeEnum.CS.getType());
				Long gid = sequenceService.getSeq(BusiTypeEnum.CS.getType());
				good.setId(gid.intValue());
				good.setCreateDate(new Date());
				JSONObject __content = JSON.parseObject(gp.getContent());
				__content.put("pid",fid);
				good.setContent(__content.toJSONString());
				good.setType(BusiTypeEnum.CS.getType());
				good.setCreateId(gp.getCreateId());
				good.setCust_id(gp.getCust_id());
				good.setExt_3(gp.getExt_3());
				good.setExt_4(gp.getExt_4());
				dataList.add(good);
			}
		}
	}



	public List<HBusiDataManager> getDataList(Long pid){
		String sql2 = "select type,id,content from h_data_manager where  JSON_EXTRACT(content, '$.pid')="+pid;
		RowMapper<HBusiDataManager>managerRowMapper=new BeanPropertyRowMapper<>(HBusiDataManager.class);
		return jdbcTemplate.query(sql2,managerRowMapper);
	}
}
