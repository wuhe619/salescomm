package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.ResourceService;
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
import java.util.*;

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

    @Autowired
    private ResourceService resourceService;


	@Override
	public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
		CustomerProperty station_idProperty = customerDao.getProperty(cust_id, "station_id");
		if (station_idProperty == null || StringUtil.isEmpty(station_idProperty.getPropertyValue())) {
			log.error("未配置场站信息");
			throw new Exception("未配置场站信息");
		}
		String billno = info.getString("bill_no");
		String sql="select id from h_data_manager where  type='"+busiType+ " and JSON_EXTRACT(content, '$.ext_3')='"+billno+"'";
		List<Map<String,Object>>countList = jdbcTemplate.queryForList(sql);
		if(countList!=null && countList.size()>0){
			throw new Exception("此主单已经申报");
		}

		List<HBusiDataManager> list = new ArrayList<>();
		MainDan mainDan = JSON.parseObject(info.toJSONString(),MainDan.class);
		try {
			buildMain(info,list,mainDan,cust_user_id,cust_id,station_idProperty.getPropertyValue(),id);
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
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) {
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
        }else {
			updateDataToES(busiType,id.toString(),info);
		}
    }

	@Override
	public void getInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws Exception {
		String sql="select id,type,content,ext_1,ext_2,ext_3,ext_4 from h_data_manager where id="+id +" and type='"+busiType+"'";
		HBusiDataManager manager = jdbcTemplate.queryForObject(sql,HBusiDataManager.class);

		if ("Y".equals(manager.getExt_1()) || "Y".equals(manager.getExt_2())) {
			throw new Exception("已经被提交，无法删除");
		}

		List<HBusiDataManager> list = getDataList(id);
		for(HBusiDataManager hBusiDataManager:list){
			List<HBusiDataManager> slist = getDataList(hBusiDataManager.getId().longValue());//所有税单
			for(HBusiDataManager shBusiDataManager:slist) {
				deleteDatafromES(BusiTypeEnum.CS.getType(),shBusiDataManager.getId().toString());
			}
			deleteDatafromES(BusiTypeEnum.CS.getType(),hBusiDataManager.getId().toString());
			delDataListByPid(hBusiDataManager.getId().longValue());
		}
		delDataListByPid(id);

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

	public void delDataListByPid(Long pid){
		String sql="delete from h_data_manager where JSON_EXTRACT(content, '$.pid')="+pid;
		jdbcTemplate.execute(sql);
	}

	public List<HBusiDataManager> getDataList(Long pid){
		String sql2 = "select type,id,content from h_data_manager where  JSON_EXTRACT(content, '$.pid')="+pid;
		return jdbcTemplate.queryForList(sql2,HBusiDataManager.class);
	}

	/**
	 * 从es删除文档
	 *
	 * @param type
	 * @param id
	 */
	private void deleteDatafromES(String type, String id) {
		if (type.equals(BusiTypeEnum.SZ.getType())) {
			elasticSearchService.deleteDocumentFromType(Constants.SZ_INFO_INDEX, "haiguan", id);
		}else if(type.equals(BusiTypeEnum.CZ.getType())){
			elasticSearchService.deleteDocumentFromType(Constants.CZ_INFO_INDEX, "haiguan", id);
		}else if(type.equals(BusiTypeEnum.BZ.getType())){
			elasticSearchService.deleteDocumentFromType(Constants.BZ_INFO_INDEX, "haiguan", id);
		} else if (type.equals(BusiTypeEnum.SF.getType())) {
			elasticSearchService.deleteDocumentFromType(Constants.SF_INFO_INDEX, "haiguan", id);
		}else if( type.equals(BusiTypeEnum.CF.getType())){
			elasticSearchService.deleteDocumentFromType(Constants.CF_INFO_INDEX, "haiguan", id);
		}else if(type.equals(BusiTypeEnum.BF.getType())){
			elasticSearchService.deleteDocumentFromType(Constants.BF_INFO_INDEX, "haiguan", id);
		}else if (type.equals(BusiTypeEnum.SS.getType())) {
			elasticSearchService.deleteDocumentFromType(Constants.SS_INFO_INDEX, "haiguan", id);
		}else if(type.equals(BusiTypeEnum.CS.getType())){
			elasticSearchService.deleteDocumentFromType(Constants.CS_INFO_INDEX, "haiguan", id);
		}else if(type.equals(BusiTypeEnum.BS.getType())){
			elasticSearchService.deleteDocumentFromType(Constants.BS_INFO_INDEX, "haiguan", id);
		}
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

	public void buildMain(JSONObject info,List<HBusiDataManager> list, MainDan mainDan, Long userId,String custId, String station_id,Long mainid) throws Exception {
		HBusiDataManager dataManager = new HBusiDataManager();
		dataManager.setCreateId(userId);
		dataManager.setId(mainid.intValue());
		dataManager.setCreateDate(new Date());
		dataManager.setType(BusiTypeEnum.SZ.getType());

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
        buildPartyDan(list, mainDan, userId,custId,mainid,info);
        buildMainContent(mainDan,info);
		dataManager.setContent(info.toJSONString());

		list.add(dataManager);

	}

	/**
	 * 组装分单
	 *
	 * @param list
	 * @param mainDan
	 * @param
	 */
	public void buildPartyDan(List<HBusiDataManager> list, MainDan mainDan, Long userId,String custId,Long mainid,JSONObject info) throws Exception {
		List<PartyDan> partList = mainDan.getSingles();
		if (partList != null && partList.size() > 0) {
			for (PartyDan dan : partList) {
			    if(StringUtil.isEmpty(dan.getMain_bill_no())){
			        dan.setMain_bill_no(mainDan.getBill_no());
                }
				buildSenbaodanFendan(dan, list,  userId, custId, mainDan.getBill_no(),mainid,info);
			}
		}
	}

	public void buildSenbaodanFendan(PartyDan dan, List<HBusiDataManager> list,Long userId,String custId, String mainBillNo,Long mainid,JSONObject info) throws Exception {
		List<Product> pList = dan.getProducts();
		Long id = sequenceService.getSeq(BusiTypeEnum.SF.getType());
        JSONObject arrt=new JSONObject();
		buildGoods(list, pList,  userId, custId,id.toString(),arrt);
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
        JSONArray jsonArray = arrt.getJSONArray("mainGoodsName");
        String mainGoodsName="";
        if(jsonArray!=null && jsonArray.size()>0){
            for(int i=0;i<jsonArray.size();i++){
                JSONObject obj=jsonArray.getJSONObject(i);
                mainGoodsName += obj.getString("name")+"|"+obj.getString("name_en")+"|"+obj.getString("g_model");
            }
        }
        json.put("main_gname",mainGoodsName);
        json.put("low_price_goods",arrt.getString("low_price_goods"));
        if(info.containsKey("low_price_goods") && info.getInteger("low_price_goods")!=null){
            int low_price_goods = info.getInteger("low_price_goods");
            info.put("low_price_goods",low_price_goods+arrt.getInteger("low_price_goods"));
        }else{
            info.put("low_price_goods",arrt.getString("low_price_goods"));
        }
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
	public void buildGoods(List<HBusiDataManager> list, List<Product> pList, Long userId,String custId,String pid,JSONObject arrt) throws Exception {
		if (pList != null && pList.size() > 0) {
            List<Map<String,String>> mainGoodsName = new ArrayList<>();
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

                Float duty_paid_price=0f;
                int is_low_price=0;
                float tax_rate=0;
                float estimated_tax=0;
                if(StringUtil.isNotEmpty(product.getCode_ts())) {
                    JSONObject params = new JSONObject();
                    params.put("code", product.getCode_ts());
                    Page page = resourceService.query("", "duty_paid_rate",params);
                    if(page!=null && page.getTotal()>0){
                        List dataList = page.getData();
                        Map<String ,Object> d = (Map<String, Object>) dataList.get(0);
                        String content = (String) d.get("content");
                        JSONObject contentObj=JSON.parseObject(content);
                        duty_paid_price = contentObj.getFloatValue("duty_price");
                        if(StringUtil.isNotEmpty(product.getDecl_price())){
                            if(Float.valueOf(product.getDecl_price())<duty_paid_price){
                                is_low_price = 1;
                            }
                        }
                        tax_rate = contentObj.getFloatValue("tax_rate");
                        estimated_tax = duty_paid_price*tax_rate;
                    }
                }
                if(mainGoodsName.size()<3){
                    Map<String,String> smap = new HashMap<>();
                    smap.put("name",product.getG_name()==null?"":product.getG_name());
                    smap.put("name_en",product.getG_name_en()==null?"":product.getG_name_en());
                    smap.put("g_model",product.getG_model()==null?"":product.getG_model());
                    smap.put("price",product.getDecl_price()==null?"0":product.getDecl_price());
                    mainGoodsName.add(smap);
                   /* Collections.sort(mainGoodsName, new Comparator<Map<String, String>>() {
                        @Override
                        public int compare(Map<String, String> o1, Map<String, String> o2) {
                            if(Float.valueOf(o1.get("price"))>Float.valueOf(o2.get("price"))){
                                return 1;
                            }
                            return 0;
                        }
                    });*/
                }else{
                    /*Map<String,String> m = mainGoodsName.get(mainGoodsName.size()-1);
                    if(Float.valueOf(m.get("price"))<Float.valueOf(product.getDecl_price())){
                        mainGoodsName.remove(mainGoodsName.size()-1);
                        Map<String,String>smap=new HashMap<>();
                        smap.put("name",product.getG_name()==null?"":product.getG_name());
                        smap.put("name_en",product.getG_name_en()==null?"":product.getG_name_en());
                        smap.put("g_model",product.getG_model()==null?"":product.getG_model());
                        smap.put("price",product.getDecl_price()==null?"0":product.getDecl_price());
                        mainGoodsName.add(smap);
                        Collections.sort(mainGoodsName, new Comparator<Map<String, String>>() {
                            @Override
                            public int compare(Map<String, String> o1, Map<String, String> o2) {
                                if(Float.valueOf(o1.get("price"))>Float.valueOf(o2.get("price"))){
                                    return 1;
                                }
                                return 0;
                            }
                        });
                    }*/
                }
                if(is_low_price==1){
                    if(arrt.containsKey("low_price_goods")){
                        arrt.put("low_price_goods",arrt.getInteger("low_price_goods")+1);
                    }else{
                        arrt.put("low_price_goods",1);
                    }
                    arrt.put("main_goods_name",mainGoodsName);
                }
                json.put("is_low_price",is_low_price);
                float total_price = Float.valueOf(product.getDecl_total()==null||"".equals(product.getDecl_total())?"0":product.getDecl_total());
                json.put("duty_paid_price", duty_paid_price);//完税价格
                json.put("estimated_tax", estimated_tax);//预估税金
                json.put("tax_rate", tax_rate);//税率
                json.put("total_price",total_price);//价格合计

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
            info.put("over_warp", "溢装");//溢装
		} else if (Integer.valueOf(partynum) > list.size()) {
            info.put("over_warp", "短装");//短装
		} else {
            info.put("over_warp", "正常");//正常
		}

		//todo:低价商品暂时不处理
		//System.out.println(jsonObject);

//		return jsonObject;

	}
}
