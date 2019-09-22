package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.ResourceService;
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/***
 * 报关单.税单
 */
@Service("busi_bgd_s")
public class BgdSService implements BusiService{

	private static Logger log = LoggerFactory.getLogger(BgdSService.class);

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
	private ResourceService resourceService;

	@Override
	public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
		// TODO Auto-generated method stub
		Integer pid = info.getInteger("pid");
		String billNo = info.getString("party_no");
		if(pid==null){
			log.error("分单id不能为空");
			throw new Exception("分单id不能为空");
		}
		if(StringUtil.isEmpty(billNo)){
			log.error("分单号不能为空");
			throw new Exception("分单号不能为空");
		}
		String code_ts=info.getString("code_ts");
		if(StringUtil.isEmpty(code_ts)){
			log.error("商品编码不能为空");
			throw new Exception("商品编码不能为空");
		}
		info.put("cust_id",cust_id);
		info.put("create_id",cust_user_id);
		info.put("create_date",new Date());
		info.put("ext_3",code_ts);
		info.put("ext_4",billNo);
        info.put("opt_type","APD");
		float duty_paid_price = 0;
		float estimated_tax = 0;
		float tax_rate = 0;
		int is_low_price = 0;


		if(StringUtil.isNotEmpty(code_ts)) {
			JSONObject params = new JSONObject();
			params.put("code", code_ts);
			Page page = resourceService.query("", "duty_paid_rate",params);
			if(page!=null && page.getTotal()>0){
				List dataList = page.getData();
				Map<String ,Object> d = (Map<String, Object>) dataList.get(0);
				String content = (String) d.get("content");
				JSONObject contentObj= JSON.parseObject(content);
				duty_paid_price = contentObj.getFloatValue("duty_price");
				if(StringUtil.isNotEmpty(info.getString("decl_price"))){
					if(Float.valueOf(info.getString("decl_price")) < duty_paid_price){
						is_low_price = 1;
					}
				}
				tax_rate = contentObj.getFloatValue("tax_rate");
				estimated_tax = duty_paid_price*tax_rate;
			}
		}
		info.put("is_low_price",is_low_price);
		info.put("duty_paid_price", duty_paid_price);//完税价格
		info.put("estimated_tax", estimated_tax);//预估税金
		info.put("tax_rate", tax_rate);//税率
		info.put("total_price",0);//价格合计
		List<HBusiDataManager> goodslist = getDataList(Long.valueOf(pid));
		Integer index = 0;
		for(HBusiDataManager m:goodslist){
			String indexStr = m.getExt_5();
			if(StringUtil.isNotEmpty(indexStr)){
				if(Integer.valueOf(indexStr)>index){
					index = Integer.valueOf(indexStr);
				}
			}
		}
		index += 1;
		info.put("index",index);
		addDataToES(id.toString(),busiType,info);

		HBusiDataManager partH = getObjectByIdAndType(pid.longValue(),BusiTypeEnum.SF.getType());

		String pcontent = partH.getContent();
		JSONObject jsonObject = JSON.parseObject(pcontent);
		Float weight = jsonObject.getFloatValue("weight");
		Float pack_NO = jsonObject.getFloatValue("pack_no");
		if (weight == null) weight = 0f;
		if (info.containsKey("ggrosswt") && StringUtil.isNotEmpty(info.getString("ggrosswt"))) {
			weight += Float.valueOf(info.getString("ggrosswt"));
		}
		if (pack_NO == null) pack_NO = 0f;
		if (info.containsKey("g_qty") && StringUtil.isNotEmpty(info.getString("g_qty"))) {
			pack_NO += Float.valueOf(info.getString("g_qty"));
		}
		jsonObject.put("weight", weight);
		jsonObject.put("pack_no", pack_NO);
		Integer lowPricegoods = jsonObject.getInteger("low_price_goods");
		if(lowPricegoods==null)lowPricegoods=0;
		jsonObject.put("low_price_goods",lowPricegoods+is_low_price);
		partH.setContent(jsonObject.toJSONString());
		hBusiDataManagerDao.saveOrUpdate(partH);

		updateDataToES(BusiTypeEnum.BF.getType(),pid.toString(),jsonObject);

		HBusiDataManager zh = getObjectByIdAndType(jsonObject.getLong("pid"), BusiTypeEnum.SZ.getKey());
		String zcontent = zh.getContent();
		JSONObject jsonz = JSON.parseObject(zcontent);
		Float weight_total = jsonz.getFloatValue("weight_total");
		Integer lowPricegoodsz = jsonObject.getInteger("low_price_goods");
		if(lowPricegoodsz == null)lowPricegoodsz = 0;
		jsonz.put("low_price_goods",lowPricegoodsz + is_low_price);

		if (weight_total == null) weight_total = 0f;
		if(StringUtil.isNotEmpty(info.getString("ggrosswt"))){
			weight_total+=Float.valueOf(info.getString("ggrosswt"));
		}
		jsonz.put("weight_total", weight_total);

		zh.setContent(jsonz.toJSONString());
		hBusiDataManagerDao.saveOrUpdate(zh);
		updateDataToES(BusiTypeEnum.BZ.getType(),zh.getId().toString(),jsonz);

	}

	@Override
	public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
		HBusiDataManager dbManager = getObjectByIdAndType(id,busiType);
		String content = dbManager.getContent();
		JSONObject json = JSONObject.parseObject(content);
		Iterator keys = info.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			json.put(key, info.get(key));
		}
		updateDataToES(busiType,id.toString(),json);

		HBusiDataManager fmanager = getObjectByIdAndType(id,busiType);
		String fcontent = fmanager.getContent();

		JSONObject fjson = JSONObject.parseObject(fcontent);

		List<HBusiDataManager> goodsList = getDataList(fjson.getLong("pid"));
		float weight = 0;  //重量
		float pack_NO = 0; //数量
		int lowPricegoods = 0; //低价商品数
		int is_low_price = 0;
		float festimated_tax = 0;//预估税金
		for(HBusiDataManager m:goodsList){
			JSONObject params = new JSONObject();
			params.put("code", m.getExt_1());
			float tax_rate=0;
			float estimated_tax=0;
			float duty_paid_price=0;
			Page page = resourceService.query("", "duty_paid_rate",params);
			if(page!=null && page.getTotal()>0){
				List dataList = page.getData();
				Map<String ,Object> d = (Map<String, Object>) dataList.get(0);
				String _content = (String) d.get("content");
				JSONObject contentObj = JSON.parseObject(_content);
				duty_paid_price = contentObj.getFloatValue("duty_price");

				tax_rate = contentObj.getFloatValue("tax_rate");
				estimated_tax = duty_paid_price*tax_rate;
				festimated_tax += estimated_tax;
			}

			JSONObject goods = JSONObject.parseObject(m.getContent());
			if(m.getId()==id.intValue()){
				if(StringUtil.isNotEmpty(info.getString("decl_price"))){
					if(Float.valueOf(info.getString("decl_price")) < duty_paid_price){
						is_low_price = 1;
					}
				}
				info.put("is_low_price",is_low_price);
				info.put("duty_paid_price", duty_paid_price);//完税价格
				info.put("estimated_tax", estimated_tax);//预估税金
				info.put("tax_rate", tax_rate);//税率
				info.put("total_price",0);//价格合计
			}else{
				if(goods.containsKey("ggrosswt") && StringUtil.isNotEmpty(goods.getString("ggrosswt"))){
					weight += goods.getFloatValue("ggrosswt");
				}
				if(goods.containsKey("g_qty") && StringUtil.isNotEmpty(goods.getString("g_qty"))){
					pack_NO += goods.getFloatValue("g_qty");
				}
				if(StringUtil.isNotEmpty(goods.getString("decl_price"))){
					if(Float.valueOf(goods.getString("decl_price")) < duty_paid_price){
						is_low_price = 1;
					}
				}
			}
			if(is_low_price==1){
				lowPricegoods++;
			}
		}
		fjson.put("weight_total",weight);
		fjson.put("lowPricegoods",lowPricegoods);
		fjson.put("pack_no",pack_NO);
		fjson.put("estimated_tax",festimated_tax);
		updateDataToES(BusiTypeEnum.BF.getType(),fmanager.getId().toString(),fjson);
		updateDataToES(BusiTypeEnum.BS.getType(),id.toString(),info);

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

	public HBusiDataManager getObjectByIdAndType(Long id, String type){
		String sql="select * from h_data_manager where id="+id+" and type='"+type+"'";
		RowMapper<HBusiDataManager> managerRowMapper=new BeanPropertyRowMapper<>(HBusiDataManager.class);
		return jdbcTemplate.queryForObject(sql,managerRowMapper);
	}


	public List<HBusiDataManager> getDataList(Long pid){
		String sql2 = "select * from h_data_manager where  JSON_EXTRACT(content, '$.pid')="+pid +" or JSON_EXTRACT(content, '$.pid')='"+pid+"'";
		RowMapper<HBusiDataManager>managerRowMapper = new BeanPropertyRowMapper<>(HBusiDataManager.class);
		return jdbcTemplate.query(sql2,managerRowMapper);
	}

	/**
	 * 更新es
	 * @param type
	 * @param id
	 * @param content
	 */
	private void updateDataToES(String type,String id,JSONObject content) {
		if (type.equals(BusiTypeEnum.SZ.getType())) {
			elasticSearchService.updateDocumentToType(Constants.SZ_INFO_INDEX, "haiguan", id, content);
		}else if(type.equals(BusiTypeEnum.CZ.getType())){
			elasticSearchService.updateDocumentToType(Constants.CZ_INFO_INDEX, "haiguan", id, content);
		}else if(type.equals(BusiTypeEnum.BZ.getType())){
			elasticSearchService.updateDocumentToType(Constants.BZ_INFO_INDEX, "haiguan", id, content);
		} else if (type.equals(BusiTypeEnum.SF.getType())) {
			elasticSearchService.updateDocumentToType(Constants.SF_INFO_INDEX, "haiguan", id, content);
		}else if( type.equals(BusiTypeEnum.CF.getType())){
			elasticSearchService.updateDocumentToType(Constants.CF_INFO_INDEX, "haiguan", id, content);
		}else if(type.equals(BusiTypeEnum.BF.getType())){
			elasticSearchService.updateDocumentToType(Constants.BF_INFO_INDEX, "haiguan", id, content);
		}else if (type.equals(BusiTypeEnum.SS.getType())) {
			elasticSearchService.updateDocumentToType(Constants.SS_INFO_INDEX, "haiguan", id, content);
		}else if(type.equals(BusiTypeEnum.CS.getType())){
			elasticSearchService.updateDocumentToType(Constants.CS_INFO_INDEX, "haiguan", id, content);
		}else if(type.equals(BusiTypeEnum.BS.getType())){
			elasticSearchService.updateDocumentToType(Constants.BS_INFO_INDEX, "haiguan", id, content);
		}

	}

	private void addDataToES(String id,String type,JSONObject content) {
		if (type.equals(BusiTypeEnum.SZ.getType())) {
			elasticSearchService.addDocumentToType(Constants.SZ_INFO_INDEX, "haiguan", id, content);
		}else if(type.equals(BusiTypeEnum.CZ.getType())){
			elasticSearchService.addDocumentToType(Constants.CZ_INFO_INDEX, "haiguan", id, content);
		}else if(type.equals(BusiTypeEnum.BZ.getType())){
			elasticSearchService.addDocumentToType(Constants.BZ_INFO_INDEX, "haiguan", id, content);
		} else if (type.equals(BusiTypeEnum.SF.getType())) {
			elasticSearchService.addDocumentToType(Constants.SF_INFO_INDEX, "haiguan", id, content);
		}else if( type.equals(BusiTypeEnum.CF.getType())){
			elasticSearchService.addDocumentToType(Constants.CF_INFO_INDEX, "haiguan", id, content);
		}else if(type.equals(BusiTypeEnum.BF.getType())){
			elasticSearchService.addDocumentToType(Constants.BF_INFO_INDEX, "haiguan", id,content);
		}else if (type.equals(BusiTypeEnum.SS.getType())) {
			elasticSearchService.addDocumentToType(Constants.SS_INFO_INDEX, "haiguan", id, content);
		}else if(type.equals(BusiTypeEnum.CS.getType())){
			elasticSearchService.addDocumentToType(Constants.CS_INFO_INDEX, "haiguan", id, content);
		}else if(type.equals(BusiTypeEnum.BS.getType())){
			elasticSearchService.addDocumentToType(Constants.BS_INFO_INDEX, "haiguan", id, content);
		}
	}


}
