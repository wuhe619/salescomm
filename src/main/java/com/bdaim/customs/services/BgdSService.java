package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.ResourceService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.customs.utils.ServiceUtils;
import com.bdaim.util.BigDecimalUtil;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

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
//
//	@Autowired
//	private HBusiDataManagerDao hBusiDataManagerDao;

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private ServiceUtils serviceUtils;

	@Override
	public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
		Integer pid = info.getInteger("pid");
		String billNo = info.getString("bill_no");
		if(pid==null){
			log.error("分单id不能为空");
			throw new TouchException("分单id不能为空");
		}
		if(StringUtil.isEmpty(billNo)){
			log.error("分单号不能为空");
			throw new TouchException("分单号不能为空");
		}
		String code_ts=info.getString("code_ts");
		if(StringUtil.isEmpty(code_ts)){
			log.error("商品编码不能为空");
			throw new TouchException("商品编码不能为空");
		}
		info.put("cust_id",cust_id);
		info.put("create_id",cust_user_id);
		info.put("create_date",new Date());
		info.put("ext_3",code_ts);
		info.put("ext_4",billNo);
        info.put("opt_type","APD");
		BigDecimal duty_paid_price = BigDecimal.ZERO;
		BigDecimal estimated_tax = BigDecimal.ZERO;
		BigDecimal tax_rate = BigDecimal.ZERO;
		int is_low_price = 0;


		if(StringUtil.isNotEmpty(code_ts)) {
			JSONObject params = new JSONObject();
			params.put("code", code_ts);
			Page page = resourceService.query("", "duty_paid_rate",params);
			if(page!=null && page.getTotal()>0){
				List dataList = page.getData();
				Map<String ,Object> d = (Map<String, Object>) dataList.get(0);
				JSONObject contentObj = JSON.parseObject(JSON.toJSONString(d));
				duty_paid_price = BigDecimal.valueOf(contentObj.getFloatValue("duty_price"));
				if(StringUtil.isNotEmpty(info.getString("decl_price"))){
					if(BigDecimal.valueOf(Float.valueOf(info.getString("decl_price"))) .compareTo(duty_paid_price)<0){
						is_low_price = 1;
					}
				}
				tax_rate = BigDecimal.valueOf(contentObj.getFloatValue("tax_rate"));
				estimated_tax = duty_paid_price.multiply(tax_rate);
			}
		}
		info.put("is_low_price",is_low_price);
		info.put("duty_paid_price", duty_paid_price.doubleValue());//完税价格
		info.put("estimated_tax", estimated_tax.doubleValue());//预估税金
		info.put("tax_rate", tax_rate.doubleValue());//税率
//		Double total_price = Double.valueOf(info.getString("g_qty"))*Double.valueOf(info.getString("decl_price"));
		BigDecimal total_price = BigDecimalUtil.mul(info.getString("g_qty"), info.getString("decl_price"));
		info.put("total_price",total_price.doubleValue());//价格合计
		HBusiDataManager partH = serviceUtils.getObjectByIdAndType(cust_id,pid.longValue(),BusiTypeEnum.BF.getType());
		if(partH==null){
			throw new TouchException("无权操作");
		}
		List<HBusiDataManager> goodslist = serviceUtils.listSdByBillNo(cust_id,BusiTypeEnum.BS.getType(),partH.getExt_4(),billNo);

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
		serviceUtils.addDataToES(id.toString(),busiType,info);

		String pcontent = partH.getContent();
		JSONObject jsonObject = JSON.parseObject(pcontent);
		Float weight = jsonObject.getFloatValue("weight");
		Float pack_NO = jsonObject.getFloatValue("pack_no");
		if (weight == null) weight = 0f;
		if (info.containsKey("ggrosswt") && StringUtil.isNotEmpty(info.getString("ggrosswt"))) {
			weight += Float.valueOf(info.getString("ggrosswt"));
		}
		//if (pack_NO == null) pack_NO = 0f;
//		if (info.containsKey("g_qty") && StringUtil.isNotEmpty(info.getString("g_qty"))) {
//			pack_NO += Float.valueOf(info.getString("g_qty"));
//		}
		jsonObject.put("weight", weight);
//		jsonObject.put("pack_no", pack_NO);
		Integer lowPricegoods = jsonObject.getInteger("low_price_goods");
		if(lowPricegoods==null)lowPricegoods=0;
		jsonObject.put("low_price_goods",lowPricegoods+is_low_price);
		partH.setContent(jsonObject.toJSONString());

		String sql = "update "+ HMetaDataDef.getTable(partH.getType(),"")+" set content=? "+
				" where id="+partH.getId()+" and type='"+partH.getType()+"'";
		jdbcTemplate.update(sql,jsonObject.toJSONString());

		serviceUtils.updateDataToES(BusiTypeEnum.BF.getType(),pid.toString(),jsonObject);

		HBusiDataManager zh = serviceUtils.getObjectByIdAndType(cust_id,jsonObject.getLong("pid"), BusiTypeEnum.BZ.getType());

		String zcontent = zh.getContent();
		JSONObject jsonz = JSON.parseObject(zcontent);
		BigDecimal weight_total = BigDecimal.valueOf(jsonz.getFloatValue("weight_total"));
		Integer lowPricegoodsz = jsonObject.getInteger("low_price_goods");
		if(lowPricegoodsz == null)lowPricegoodsz = 0;
		jsonz.put("low_price_goods",lowPricegoodsz + is_low_price);

		if (weight_total == null) weight_total = BigDecimal.ZERO;
		if(StringUtil.isNotEmpty(info.getString("ggrosswt"))){
			weight_total = weight_total.add(BigDecimal.valueOf(info.getFloatValue("ggrosswt")));
		}
		jsonz.put("weight_total", weight_total.doubleValue());

		zh.setContent(jsonz.toJSONString());

		String sql2 = "update "+ HMetaDataDef.getTable(zh.getType(),"")+" set content=? "+
				" where id="+zh.getId()+" and type='"+zh.getType()+"'";
		jdbcTemplate.update(sql2,jsonz.toJSONString());

		serviceUtils.updateDataToES(BusiTypeEnum.BZ.getType(),zh.getId().toString(),jsonz);

	}

	@Override
	public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
		HBusiDataManager dbManager = serviceUtils.getObjectByIdAndType(cust_id,id,busiType);
		String content = dbManager.getContent();
		JSONObject json = JSONObject.parseObject(content);
		Iterator keys = info.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			json.put(key, info.get(key));
		}
		if(info.containsKey("decl_price") && info.containsKey("g_qty")){
			BigDecimal total_price = BigDecimalUtil.mul(info.getString("g_qty"), info.getString("decl_price"));
//			Double total_price = Double.valueOf(info.getString("g_qty"))*Double.valueOf(info.getString("decl_price"));
			info.put("total_price", total_price.doubleValue());//价格合计
			json.put("total_price",total_price.doubleValue());
		}
		serviceUtils.updateDataToES(busiType,id.toString(),json);

		HBusiDataManager fmanager = serviceUtils.getObjectByIdAndType(cust_id,json.getLong("pid"),BusiTypeEnum.BF.getType());
		String fcontent = fmanager.getContent();

		JSONObject fjson = JSONObject.parseObject(fcontent);

		List<HBusiDataManager> goodsList = serviceUtils.listSdByBillNo(cust_id,BusiTypeEnum.BS.getType(),fmanager.getExt_4(),dbManager.getExt_4());
		BigDecimal weight = BigDecimal.ZERO;  //重量
		//float pack_NO = 0; //数量
		int lowPricegoods = 0; //低价商品数
		int is_low_price = 0;
		BigDecimal festimated_tax = BigDecimal.ZERO;//预估税金
		for(HBusiDataManager m:goodsList){
			JSONObject params = new JSONObject();
			params.put("code", m.getExt_3());
			BigDecimal tax_rate = BigDecimal.ZERO;
			BigDecimal estimated_tax = BigDecimal.ZERO;
			BigDecimal duty_paid_price = BigDecimal.ZERO;
			Page page = resourceService.query("", "duty_paid_rate",params);
			if(page!=null && page.getTotal()>0){
				List dataList = page.getData();
				Map<String ,Object> d = (Map<String, Object>) dataList.get(0);
				JSONObject contentObj = JSON.parseObject(JSON.toJSONString(d));
				if(contentObj.containsKey("duty_price") && StringUtil.isNotEmpty(contentObj.getString("duty_price"))){
					duty_paid_price = BigDecimal.valueOf(contentObj.getFloatValue("duty_price"));
				}
				if(contentObj.containsKey("tax_rate") && StringUtil.isNotEmpty(contentObj.getString("tax_rate"))) {
					tax_rate = BigDecimal.valueOf(contentObj.getFloatValue("tax_rate"));
				}
				estimated_tax = duty_paid_price.multiply(tax_rate);
				festimated_tax = festimated_tax.add(estimated_tax);
			}

			JSONObject goods = JSONObject.parseObject(m.getContent());
			if(m.getId()==id.intValue()){
				if(StringUtil.isNotEmpty(info.getString("decl_price"))){
					if(BigDecimal.valueOf(info.getFloatValue("decl_price")).compareTo(duty_paid_price)<0){
						is_low_price = 1;
					}
				}
				info.put("is_low_price",is_low_price);
				info.put("duty_paid_price", duty_paid_price.doubleValue());//完税价格
				info.put("estimated_tax", estimated_tax.doubleValue());//预估税金
				info.put("tax_rate", tax_rate.doubleValue());//税率
//				Double total_price = Double.valueOf(info.getString("g_qty"))*Double.valueOf(info.getString("decl_price"));
				BigDecimal total_price = BigDecimalUtil.mul(info.getString("g_qty"), info.getString("decl_price"));
				info.put("total_price", total_price.doubleValue());//价格合计
			}else{
				if(goods.containsKey("ggrosswt") && StringUtil.isNotEmpty(goods.getString("ggrosswt"))){
					weight = weight.add(BigDecimal.valueOf(goods.getFloatValue("ggrosswt")));
				}
//				if(goods.containsKey("g_qty") && StringUtil.isNotEmpty(goods.getString("g_qty"))){
//					pack_NO += goods.getFloatValue("g_qty");
//				}
				if(StringUtil.isNotEmpty(goods.getString("decl_price"))){
					if(BigDecimal.valueOf(Float.valueOf(goods.getString("decl_price"))) .compareTo(duty_paid_price)<0){
						is_low_price = 1;
					}
				}
			}
			if(is_low_price==1){
				lowPricegoods++;
			}
		}
		fjson.put("weight_total",weight.doubleValue());
		fjson.put("lowPricegoods",lowPricegoods);
		//fjson.put("pack_no",pack_NO);
		fjson.put("estimated_tax",festimated_tax.doubleValue());
		serviceUtils.updateDataToES(BusiTypeEnum.BF.getType(),fmanager.getId().toString(),fjson);

		String sql2 = "update "+ HMetaDataDef.getTable(BusiTypeEnum.BF.getType(),"")+" set content=? "+
				" where id="+fmanager.getId()+" and type='"+BusiTypeEnum.BF.getType()+"'";
		jdbcTemplate.update(sql2,fjson.toJSONString());

		serviceUtils.updateDataToES(BusiTypeEnum.BS.getType(),id.toString(),info);

	}

	@Override
	public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) {
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
