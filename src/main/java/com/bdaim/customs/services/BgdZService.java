package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.Constants;
import com.bdaim.customs.entity.HBusiDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
		if("HAIGUAN".equals(info.getString("rule.to"))) {
			HBusiDataManager h = hBusiDataManagerDao.get(Long.valueOf(id));
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
	public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) {
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
		json.put("commit_baodan_status", "Y");

		JSONObject jon = JSON.parseObject(h.getContent());
		jon.put("commit_baodan_status", "Y");
		h.setExt_1("Y");
		h.setContent(jon.toJSONString());
		dataList.add(h);

		String content = json.toJSONString();
		CZ.setContent(content);
		dataList.add(CZ);
		List<HBusiDataManager> parties = getHbusiDataByBillNo(CZ.getExt_3(), BusiTypeEnum.SF.getType());
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
			List<HBusiDataManager> goods = getHbusiDataByBillNo(hp.getExt_3(), BusiTypeEnum.SS.getType());
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

	/**
	 * 根据主单获取分单
	 *
	 * @param billNo
	 * @return
	 */
	private List<HBusiDataManager> getHbusiDataByBillNo(String billNo, String type) {
		String hql = " from HBusiDataManager a where a.ext_4='" + billNo + "' and type='" + type + "'";
		List<HBusiDataManager> list = hBusiDataManagerDao.find(hql);
		return list;
	}

}
