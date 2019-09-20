package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.common.util.SqlAppendUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/***
 * 申报单.主单
 */
@Service("busi_sbd_z")
public class SbdZService implements BusiService{
	private static Logger log = LoggerFactory.getLogger(SbdZService.class);

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
	public void insertInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id, JSONObject info) throws Exception {
		// TODO Auto-generated method stub
		CustomerProperty station_idProperty = customerDao.getProperty(cust_id, "station_id");
		if (station_idProperty == null || StringUtil.isEmpty(station_idProperty.getPropertyValue())) {
			log.error("未配置场站信息");
			throw new Exception("未配置场站信息");
		}
		List<HBusiDataManager> list = new ArrayList<>();
		MainDan mainDan = JSON.parseObject(info.toJSONString(),MainDan.class);
		try {
			buildMain(info,list,mainDan,Long.parseLong(cust_user_id),Long.parseLong(cust_id),station_idProperty.getPropertyValue(),id);
			if (list != null && list.size() > 0) {
				for (HBusiDataManager hBusiDataManager : list) {
					JSONObject json=JSON.parseObject(hBusiDataManager.getContent());
					if(BusiTypeEnum.SZ.getType().equals(hBusiDataManager.getType())){
                        info.remove("singles");
						hBusiDataManager.setContent(info.toJSONString());
					}else if(BusiTypeEnum.SF.getType().equals(hBusiDataManager.getType())){
						json.remove("products");
						hBusiDataManager.setContent(json.toJSONString());
						hBusiDataManagerDao.save(hBusiDataManager);
					}else{
						hBusiDataManagerDao.save(hBusiDataManager);
					}
					addDataToES(hBusiDataManager);
				}
			}
			log.info(info.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("保存主单出错");
		}
	}

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id, JSONObject info) {
        // 身份核验
        if ("verification".equals(info.getString("rule.do"))) {
            StringBuffer sql = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from h_data_manager where type=?")
                    .append(" and cust_id='").append(cust_id).append("'")
                    .append(" and (ext_7 IS NULL OR ext_7 = '') ")
                    .append(" and JSON_EXTRACT(content, '$.'pid')=?");
            List sqlParams = new ArrayList();
            sqlParams.add(busiType);
            sqlParams.add(id);
            List<Map<String, Object>> dfList = jdbcTemplate.queryForList(sql.toString(), sqlParams.toArray());
            if (dfList != null && dfList.size() > 0) {
                List ids = new ArrayList();
                for (Map<String, Object> m : dfList) {
                    ids.add(m.get("id"));
                }
                String updateSql = "UPDATE h_data_manager SET ext_7 = 3 WHERE id IN(" + SqlAppendUtil.sqlAppendWhereIn(ids) + ")";
                jdbcTemplate.update(updateSql);
            }

        }
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

	public void buildMain(JSONObject info,List<HBusiDataManager> list, MainDan mainDan, Long userId,Long custId, String station_id,Long mainid) throws Exception {
		HBusiDataManager dataManager = new HBusiDataManager();
		dataManager.setCreateId(userId);
		dataManager.setId(mainid.intValue());
		dataManager.setCreateDate(new Date());
		dataManager.setType(BusiTypeEnum.SZ.getType());
		buildMainContent(mainDan,info);
        info.put("type", BusiTypeEnum.SZ.getType());
        info.put("commit_cangdan_status", "N");
        info.put("commit_baodan_status", "N");
        info.put("create_date", new Date());
        info.put("create_id", userId + "");
        info.put("station_id", station_id);//场站id
        info.put("cust_id", custId);
        info.put("id_card_number", 0);
        info.put("ext_1","N");
        info.put("ext_2","N");
        info.put("ext_3",mainDan.getBill_no());
		dataManager.setContent(info.toJSONString());

		list.add(dataManager);
		buildPartyDan(list, mainDan, userId,custId,mainid);
	}

	/**
	 * 组装分单
	 *
	 * @param list
	 * @param mainDan
	 * @param
	 */
	public void buildPartyDan(List<HBusiDataManager> list, MainDan mainDan, Long userId,Long custId,Long mainid) throws Exception {
		List<PartyDan> partList = mainDan.getSingles();
		if (partList != null && partList.size() > 0) {
			for (PartyDan dan : partList) {
				buildSenbaodanFendan(dan, list,  userId, custId, mainDan.getBill_no(),mainid);
			}
		}
	}

	public void buildSenbaodanFendan(PartyDan dan, List<HBusiDataManager> list,Long userId,Long custId, String mainBillNo,Long mainid) throws Exception {
		List<Product> pList = dan.getProducts();
		Long id = sequenceService.getSeq(BusiTypeEnum.SF.getType());
		buildGoods(list, pList,  userId, custId,id.toString());
		HBusiDataManager dataManager = new HBusiDataManager();
		dataManager.setType(BusiTypeEnum.SF.getType());
		dataManager.setCreateId(userId);
		dataManager.setCust_id(Long.valueOf(custId));

		dataManager.setId(id.intValue());
		dataManager.setCreateDate(new Date());
		dataManager.setExt_3(dan.getBill_no());//分单号
		dataManager.setExt_4(dan.getMain_bill_no());//主单号

		JSONObject json = buildPartyContent(dan);
		json.put("type", BusiTypeEnum.SF.getType());
		json.put("mail_bill_no", mainBillNo);
		json.put("create_date", dataManager.getCreateDate());
		json.put("create_id", userId);
		json.put("cust_id", custId);
		json.put("check_status", "0");
		json.put("idcard_pic_flag", "0");
		json.put("pid",mainid);
		dataManager.setContent(json.toJSONString());

		list.add(dataManager);
	}

	/**
	 * 组装商品
	 *
	 * @param list
	 * @param pList
	 * @param
	 */
	public void buildGoods(List<HBusiDataManager> list, List<Product> pList, Long userId,Long custId,String pid) throws Exception {
		if (pList != null && pList.size() > 0) {
			for (Product product : pList) {
				HBusiDataManager dataManager = new HBusiDataManager();
				dataManager.setType(BusiTypeEnum.SS.getType());
				dataManager.setCreateDate(new Date());
				dataManager.setCreateId(userId);
				Long id = sequenceService.getSeq(BusiTypeEnum.SS.getType());
				dataManager.setId(id.intValue());
				dataManager.setCust_id(Long.valueOf(custId));
				dataManager.setExt_3(product.getCode_ts());//商品编号
				dataManager.setExt_4(product.getParty_no());//分单号
				JSONObject json = buildGoodsContent(product);
				json.put("create_date", new Date());
				json.put("create_id", userId);
				json.put("cust_id", custId);
				json.put("pid",pid);
				json.put("type", BusiTypeEnum.SS.getType());
				dataManager.setContent(json.toJSONString());

				list.add(dataManager);
			}
		}
	}

	private JSONObject buildPartyContent(PartyDan partyDan) {
		JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(partyDan));
		return jsonObject;
	}



	private JSONObject buildGoodsContent(Product product) {
		JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(product));
		return jsonObject;
	}

	/**
	 * 1.统计重量
	 * 2.统计分单数量
	 * 3.是否有低价商品
	 * 4.是否短装、溢装
	 * 件数  申报分单数  分单总计  申报重量  重量总计
	 * 低价商品判断逻辑： 跟当前企业用户历史舱单/报关单商品数据进行比较，
	 * 取近3个月的商品均值进行比较。若低于均值，则判断为低价商品
	 * 冷启动阶段：商品完税价格
	 */
	private void buildMainContent(MainDan mainDan,JSONObject info) {
		log.info(JSON.toJSONString(mainDan));
		//JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(mainDan));
		String partynum = mainDan.getSingle_batch_num();

		List<PartyDan> list = mainDan.getSingles();
		float weightTotal = 0;
		for (PartyDan partyDan : list) {
			String WEIGHT = partyDan.getWeight();
			if (StringUtil.isEmpty(WEIGHT)) {
				WEIGHT = "0";
			}
			weightTotal += Float.valueOf(WEIGHT);
		}

        info.put("weight_total", weightTotal);//总重量
        info.put("party_total", list.size());//分单总数

		if (Integer.valueOf(partynum) < list.size()) {
            info.put("overWarp", "溢装");//溢装
		} else if (Integer.valueOf(partynum) > list.size()) {
            info.put("overWarp", "短装");//短装
		} else {
            info.put("overWarp", "正常");//正常
		}

		//todo:低价商品暂时不处理
		//System.out.println(jsonObject);

//		return jsonObject;

	}
}
