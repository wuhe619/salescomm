package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.UploadFileService;
import com.bdaim.common.util.BusinessEnum;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.ZipUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.dao.HDicDao;
import com.bdaim.customs.dao.HMetaDataDefDao;
import com.bdaim.customs.dao.HReceiptRecordDao;
import com.bdaim.customs.dto.FileModel;
import com.bdaim.customs.dto.QueryDataParams;
import com.bdaim.customs.entity.*;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@Transactional
public class CustomsService {
    private static Logger log = LoggerFactory.getLogger(CustomsService.class);

    @Autowired
    private HBusiDataManagerDao hBusiDataManagerDao;

    @Autowired
    private HDicDao hDicDao;

    @Autowired
    private HMetaDataDefDao hMetaDataDefDao;

    @Autowired
    private HReceiptRecordDao hReceiptRecordDao;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private CustomerDao customerDao;

    /**
     * 保存信息
     *
     * @param mainDan
     * @param user
     */
    public void saveinfo(MainDan mainDan, LoginUser user) throws Exception {
        log.info("saveinfo");
        List<HBusiDataManager> list = new ArrayList<>();
        CustomerProperty station_idProperty = customerDao.getProperty(user.getCustId(), "station_id");
        if (station_idProperty == null || StringUtil.isEmpty(station_idProperty.getPropertyValue())) {
            log.error("未配置场站信息");
            throw new Exception("未配置场站信息");
        }
        try {
            buildMain(list, mainDan, user, station_idProperty.getPropertyValue());
            if (list != null && list.size() > 0) {
                for (HBusiDataManager hBusiDataManager : list) {
                    Integer id = (Integer) hBusiDataManagerDao.saveReturnPk(hBusiDataManager);
                    addDataToES(hBusiDataManager, id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("保存主单出错");
        }
    }


    /**
     * 添加数据到es
     *
     * @param hBusiDataManager
     * @param id
     */
    private void addDataToES(HBusiDataManager hBusiDataManager, Integer id) {
        String type = hBusiDataManager.getType();
        if (type.equals(BusiTypeEnum.SZ.getKey()) || type.equals(BusiTypeEnum.CZ.getKey()) || type.equals(BusiTypeEnum.BZ.getKey())) {
            elasticSearchService.addDocumentToType(Constants.SZ_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        } else if (type.equals(BusiTypeEnum.SF.getKey()) || type.equals(BusiTypeEnum.CF.getKey()) || type.equals(BusiTypeEnum.BF.getKey())) {
            elasticSearchService.addDocumentToType(Constants.SF_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        } else if (type.equals(BusiTypeEnum.SS.getKey()) || type.equals(BusiTypeEnum.CS.getKey()) || type.equals(BusiTypeEnum.BS.getKey())) {
            elasticSearchService.addDocumentToType(Constants.SS_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        }
    }

    /**
     * 查询主单详情
     *
     * @param id
     */
    public JSONObject getMainDetailById(String id, String type) {
        JSONObject json = elasticSearchService.getDocumentById(Constants.SF_INFO_INDEX, "haiguan", id);
        if (json == null) {
            HBusiDataManager h = hBusiDataManagerDao.get(NumberConvertUtil.parseInt(id));
            if (h != null && h.getContent() != null) {
                json = JSON.parseObject(h.getContent());
            }
        }
        if (json != null) {
            json.put("id", id);
        }
        return json;
    }

    /**
     * 保存申报单主单信息
     *
     * @param
     * @throws Exception
     */
    public void saveMainDetail(String id, MainDan mainDan, LoginUser user) throws Exception {
//         String id = jsonObject.getString("id");
        if (StringUtil.isEmpty(id)) {
            log.error("参数id为空");
            throw new Exception("参数错误");
        }
        HBusiDataManager manager = hBusiDataManagerDao.get(id);
        if (manager == null) {
            throw new Exception("修改的数据不存在");
        }
        String content = manager.getContent();
        MainDan dbjson = JSON.parseObject(content, MainDan.class);
        BeanUtils.copyProperties(mainDan, dbjson);
        manager.setContent(JSON.toJSONString(dbjson));
        hBusiDataManagerDao.save(manager);
        updateDataToES(manager, Integer.valueOf(id));
    }

    /**
     * 保存申报单分单
     *
     * @param partyDan
     * @param user
     */
    public void saveSbPartDetail(PartyDan partyDan, LoginUser user) throws Exception {
        List<HBusiDataManager> list = new ArrayList<>();
        if (partyDan.getId() == null) {
            HBusiDataManager manager = getObjectByBillNo(partyDan.getBill_NO(), BusiTypeEnum.SF.getKey());
            if (manager != null) {
                throw new Exception("分单号" + partyDan.getBill_NO() + " 已经存在");
            }
            buildSenbaodanFendan(partyDan, list, user, partyDan.getMain_bill_NO());
            for (HBusiDataManager hBusiDataManager : list) {
                Long id = (Long) hBusiDataManagerDao.saveReturnPk(hBusiDataManager);
                addDataToES(hBusiDataManager, id.intValue());
            }
        } else {
            HBusiDataManager dbManager = hBusiDataManagerDao.get(partyDan.getId());
            String content = dbManager.getContent();
            JSONObject json = JSONObject.parseObject(content);
            json.put("weight", partyDan.getWeight());
            json.put("id_type", partyDan.getId_type());
            json.put("id_NO", partyDan.getId_NO());
            json.put("receive_name", partyDan.getReceive_name());
            json.put("receive_pro", partyDan.getReceive_pro());
            json.put("receive_city", partyDan.getReceive_city());
            json.put("receive_tel", partyDan.getReceive_tel());
            json.put("receive_address", partyDan.getReceive_address());
            dbManager.setContent(json.toJSONString());
            hBusiDataManagerDao.saveReturnPk(dbManager);
            updateDataToES(dbManager, dbManager.getId());
        }
        totalPartDanToMainDan(partyDan.getMain_bill_NO(), BusiTypeEnum.SF.getKey());
    }


    /**
     * 重新汇总主单的重量，数量等信息,并存入主单
     *
     * @param mainBillNo
     * @param type
     */
    public void totalPartDanToMainDan(String mainBillNo, String type) {
        List<HBusiDataManager> data = getHbusiDataByBillNo(mainBillNo, type);
        Float weightTotal = 0f;
        for (HBusiDataManager d : data) {
            String content = d.getContent();
            JSONObject json = JSONObject.parseObject(content);

            String WEIGHT = json.getString("weight");
            if (StringUtil.isEmpty(WEIGHT)) {
                WEIGHT = "0";
            }
            weightTotal += Float.valueOf(WEIGHT);
        }
        HBusiDataManager hBusiDataManager = getObjectByBillNo(mainBillNo, type);
        String hcontent = hBusiDataManager.getContent();
        JSONObject jsonObject = JSONObject.parseObject(hcontent);
        jsonObject.put("weight_total", weightTotal);//总重量
        jsonObject.put("party_total", data.size());//分单总数
        hBusiDataManager.setContent(jsonObject.toJSONString());
        hBusiDataManagerDao.saveOrUpdate(hBusiDataManager);
        updateDataToES(hBusiDataManager, hBusiDataManager.getId());

    }


    public HBusiDataManager getObjectByBillNo(String billNo, String type) {
        String hql = "from where ext_3='" + billNo + "' and type='" + type + "'";
        List<HBusiDataManager> hlist = hBusiDataManagerDao.find(hql);
        return hlist.get(0);
    }


    /**
     * 申报单从主单删除分单
     *
     * @param mainId 主单id
     * @param partId 分单id
     *               1.获取税单id，从db删除，从es删除
     *               2.从es删除
     *               3.从db删除
     *               4.重新统计主单信息
     */
    public void delPartDanFromSZ(String mainId, String partId, LoginUser user) throws Exception {
        HBusiDataManager hBusiDataManager = hBusiDataManagerDao.get(partId);
        if (hBusiDataManager == null) {
            throw new Exception("数据不存在");
        }
        if (!mainId.equals(hBusiDataManager.getExt_4())) {
            throw new Exception("分单不属于此主单");
        }
        if (hBusiDataManager.getCust_id() == null || (!user.getCustId().equals(hBusiDataManager.getCust_id().toString()))) {
            throw new Exception("无权删除");
        }
        List<HBusiDataManager> data = getHbusiDataByBillNo(partId, BusiTypeEnum.SS.getKey());
        for (HBusiDataManager manager : data) {
            deleteDatafromES(manager.getType(), manager.getId().toString());
            hBusiDataManagerDao.delete(manager);
        }
        deleteDatafromES(BusiTypeEnum.SF.getKey(), partId);
        hBusiDataManagerDao.delete(partId);
        totalPartDanToMainDan(mainId, BusiTypeEnum.SZ.getKey());
    }


    /**
     * 删除申报单主单
     *
     * @param id
     * @param type
     * @param user
     * @throws Exception
     */
    public void delMainById(Long id, String type, LoginUser user) throws Exception {
        HBusiDataManager manager = hBusiDataManagerDao.get(id);
        if ("Y".equals(manager.getExt_1()) || "Y".equals(manager.getExt_2())) {
            throw new Exception("已经被提交，无法删除");
        }
        String sql = "select id,ext_3 from h_data_manager where ext_4='" + manager.getExt_3() + "'";
        List<Map<String, Object>> ids = hBusiDataManagerDao.queryListBySql(sql);
        List<Map<String, Object>> idList = new ArrayList<>();
        for (Map<String, Object> map : ids) {
            Long _id = (Long) map.get("id");
            Map<String, Object> idmap = new HashMap();
            idmap.put("id", _id);
            idmap.put("type", BusiTypeEnum.SF);
            idList.add(idmap);
            String billno = (String) map.get("ext_3");
            String _sql = "select id from h_data_manager where ext_4='" + billno + "'";
            List<Map<String, Object>> _ids = hBusiDataManagerDao.queryListBySql(_sql);
            for (Map<String, Object> m : _ids) {
                Long _gid = (Long) m.get("id");
                idmap = new HashMap();
                idmap.put("id", _gid);
                idmap.put("type", BusiTypeEnum.SS);
                idList.add(idmap);
            }
        }
        Map<String, Object> temp = new HashMap();
        temp.put("id", id);
        temp.put("type", BusiTypeEnum.SZ);
        idList.add(temp);
        for (Map<String, Object> _map : idList) {
            hBusiDataManagerDao.delete((Long) _map.get("id"));
            deleteDatafromES((String) _map.get("type"), (String) _map.get("id"));
        }
    }

    /**
     * 添加商品到申报单分单
     *
     * @param product
     * @param partId
     */
    public void addProductToSB(Product product, String partId, LoginUser user) {
        HBusiDataManager hBusiDataManager = new HBusiDataManager();
        hBusiDataManager.setCreateDate(new Date());
        hBusiDataManager.setType(BusiTypeEnum.SS.getKey());
        hBusiDataManager.setCreateId(user.getId());
        hBusiDataManager.setCust_id(Long.valueOf(user.getCustId()));
        hBusiDataManager.setExt_3(product.getCode_ts());
        hBusiDataManager.setExt_4(product.getParty_No());
        product.setPid(partId);
        JSONObject json = JSON.parseObject(JSONObject.toJSONString(product));
        //todo 待合计
        json.put("duty_paid_price", "0");//完税价格
        json.put("estimated_tax", "0");//预估税金
        json.put("tax_rate", "0");//税率
        json.put("total_price", "0");//价格合计
        hBusiDataManager.setContent(json.toJSONString());
        Long id = (Long) hBusiDataManagerDao.saveReturnPk(hBusiDataManager);
        addDataToES(hBusiDataManager, id.intValue());

        HBusiDataManager partH = hBusiDataManagerDao.get(partId);
        String pcontent = partH.getContent();
        JSONObject jsonObject = JSON.parseObject(pcontent);
        Float weight = jsonObject.getFloatValue("weight");
        Float pack_NO = jsonObject.getFloatValue("pack_NO");
        if (weight == null) weight = 0f;
        if (StringUtil.isNotEmpty(product.getGgrossWt())) {
            weight += Float.valueOf(product.getGgrossWt());
        }
        if (pack_NO == null) pack_NO = 0f;
        if (StringUtil.isNotEmpty(product.getG_qty())) {
            pack_NO += Float.valueOf(product.getG_qty());
        }
        jsonObject.put("weight", weight);
        jsonObject.put("pack_NO", pack_NO);
        partH.setContent(jsonObject.toJSONString());
        hBusiDataManagerDao.saveOrUpdate(partH);
        updateDataToES(partH, Integer.valueOf(partId));

        //todo改为通过pid获取主单
        HBusiDataManager zh = getObjectByBillNo(partH.getExt_4(), BusiTypeEnum.SZ.getKey());
        String zcontent = zh.getContent();
        JSONObject jsonz = JSON.parseObject(zcontent);
        Float weight_total = jsonz.getFloatValue("weight_total");
        if (weight_total == null) weight_total = 0f;

        weight_total += Float.valueOf(product.getGgrossWt() == null ? "0" : product.getGgrossWt());
        jsonz.put("weight_total", weight_total);
        zh.setContent(jsonz.toJSONString());
        hBusiDataManagerDao.saveOrUpdate(zh);
        updateDataToES(zh, zh.getId());
    }


    /**
     * 从分单删除商品
     *
     * @param partId
     * @param productId
     */
    public void delProductFromSB(String partId, String productId) {
        HBusiDataManager ph = hBusiDataManagerDao.get(productId);
        deleteDatafromES(BusiTypeEnum.SS.getKey(), productId);
        hBusiDataManagerDao.delete(productId);
        String pcontent = ph.getContent();
        JSONObject pjson = JSON.parseObject(pcontent);

        //获取分单信息，从分单中减去商品的重量等
        HBusiDataManager parth = hBusiDataManagerDao.get(partId);
        String partcontent = parth.getContent();
        JSONObject partcontentJson = JSON.parseObject(partcontent);

        Float weight = partcontentJson.getFloatValue("weight");
        Float pack_NO = partcontentJson.getFloatValue("pack_NO");
        if (weight == null) weight = 0f;
        if (StringUtil.isNotEmpty(pjson.getString("ggrossWt"))) {
            weight -= Float.valueOf(pjson.getString("ggrossWt"));
        }
        if (pack_NO == null) pack_NO = 0f;
        if (StringUtil.isNotEmpty(pjson.getString("g_qty"))) {
            pack_NO -= Float.valueOf(pjson.getString("g_qty"));
        }
        partcontentJson.put("weight", weight);
        partcontentJson.put("pack_NO", pack_NO);
        parth.setContent(partcontentJson.toJSONString());
        hBusiDataManagerDao.saveOrUpdate(parth);
        updateDataToES(parth, Integer.valueOf(partId));

        //处理主单
        HBusiDataManager zh = getObjectByBillNo(parth.getExt_4(), BusiTypeEnum.SZ.getKey());
        String zcontent = zh.getContent();
        JSONObject jsonz = JSON.parseObject(zcontent);
        Float weight_total = jsonz.getFloatValue("weight_total");
        if (weight_total == null) weight_total = 0f;

        weight_total -= Float.valueOf(pjson.getString("ggrossWt") == null ? "0" : pjson.getString("ggrossWt"));
        jsonz.put("weight_total", weight_total);
        zh.setContent(jsonz.toJSONString());
        hBusiDataManagerDao.saveOrUpdate(zh);
        updateDataToES(zh, zh.getId());

    }


    /**
     * 从es删除文档
     *
     * @param type
     * @param id
     */
    private void deleteDatafromES(String type, String id) {
        if (type.equals(BusiTypeEnum.SZ.getKey()) || type.equals(BusiTypeEnum.CZ.getKey()) || type.equals(BusiTypeEnum.BZ.getKey())) {
            elasticSearchService.deleteDocumentFromType(Constants.SZ_INFO_INDEX, "haiguan", id);
        } else if (type.equals(BusiTypeEnum.SF.getKey()) || type.equals(BusiTypeEnum.CF.getKey()) || type.equals(BusiTypeEnum.BF.getKey())) {
            elasticSearchService.deleteDocumentFromType(Constants.SF_INFO_INDEX, "haiguan", id);
        } else if (type.equals(BusiTypeEnum.SS.getKey()) || type.equals(BusiTypeEnum.CS.getKey()) || type.equals(BusiTypeEnum.BS.getKey())) {
            elasticSearchService.deleteDocumentFromType(Constants.SS_INFO_INDEX, "haiguan", id);
        }
    }

    /**
     * 更新索引数据
     *
     * @param hBusiDataManager
     * @param id
     */
    private void updateDataToES(HBusiDataManager hBusiDataManager, Integer id) {
        String type = hBusiDataManager.getType();
        if (type.equals(BusiTypeEnum.SZ.getKey()) || type.equals(BusiTypeEnum.CZ.getKey()) || type.equals(BusiTypeEnum.BZ.getKey())) {
            elasticSearchService.updateDocumentToType(Constants.SZ_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        } else if (type.equals(BusiTypeEnum.SF.getKey()) || type.equals(BusiTypeEnum.CF.getKey()) || type.equals(BusiTypeEnum.BF.getKey())) {
            elasticSearchService.updateDocumentToType(Constants.SF_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        } else if (type.equals(BusiTypeEnum.SS.getKey()) || type.equals(BusiTypeEnum.CS.getKey()) || type.equals(BusiTypeEnum.BS.getKey())) {
            elasticSearchService.updateDocumentToType(Constants.SS_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        }
    }

    /**
     * 组装主单数据
     *
     * @param list
     * @param mainDan
     * @param user
     */
    public void buildMain(List<HBusiDataManager> list, MainDan mainDan, LoginUser user, String station_id) {
        HBusiDataManager dataManager = new HBusiDataManager();
        dataManager.setCreateId(user.getId());
        dataManager.setCreateDate(new Date());
        dataManager.setType(BusiTypeEnum.SZ.getKey());
        JSONObject jsonObject = buildMainContent(mainDan);
        jsonObject.put("type", BusiTypeEnum.SZ.getKey());
        jsonObject.put("commitCangdanStatus", "N");
        jsonObject.put("commitBaoDanStatus", "N");
        jsonObject.put("create_date", new Date());
        jsonObject.put("create_id", user.getId() + "");
        jsonObject.put("station_id", station_id);//场站id
        jsonObject.put("cust_id", user.getCustId());
        jsonObject.put("idCardNumber", 0);
        dataManager.setContent(jsonObject.toJSONString());
        dataManager.setExt_1("N");//commit to cangdan 是否提交仓单 N:未提交，Y：已提交
        dataManager.setExt_2("N");//commit to baogaundan N:未提交，Y：已提交
        dataManager.setExt_3(mainDan.getBill_no());
        list.add(dataManager);
        buildPartyDan(list, mainDan, user);
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
    private static JSONObject buildMainContent(MainDan mainDan) {
        log.info(JSON.toJSONString(mainDan));
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(mainDan));
//        String CHARGE_WT = mainDan.getCharge_wt();
        String partynum = mainDan.getSingle_batch_num();
//        String packNo = mainDan.getPack_no();

        List<PartyDan> list = mainDan.getSingles();
        float weightTotal = 0;
        for (PartyDan partyDan : list) {
            String WEIGHT = partyDan.getWeight();
            if (StringUtil.isEmpty(WEIGHT)) {
                WEIGHT = "0";
            }
            weightTotal += Float.valueOf(WEIGHT);
        }

        jsonObject.put("weight_total", weightTotal);//总重量
        jsonObject.put("party_total", list.size());//分单总数

        if (Integer.valueOf(partynum) < list.size()) {
            jsonObject.put("overWarp", "溢装");//溢装
        } else if (Integer.valueOf(partynum) > list.size()) {
            jsonObject.put("overWarp", "短装");//短装
        } else {
            jsonObject.put("overWarp", "正常");//正常
        }

        //todo:低价商品暂时不处理
        System.out.println(jsonObject);

        return jsonObject;

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
     * 组装分单
     *
     * @param list
     * @param mainDan
     * @param user
     */
    public void buildPartyDan(List<HBusiDataManager> list, MainDan mainDan, LoginUser user) {
        List<PartyDan> partList = mainDan.getSingles();
        if (partList != null && partList.size() > 0) {
            for (PartyDan dan : partList) {
                buildSenbaodanFendan(dan, list, user, mainDan.getBill_no());
            }
        }
    }

    public void buildSenbaodanFendan(PartyDan dan, List<HBusiDataManager> list, LoginUser user, String mainBillNo) {
        List<Product> pList = dan.getProducts();
        buildGoods(list, pList, user);
        HBusiDataManager dataManager = new HBusiDataManager();
        dataManager.setType(BusiTypeEnum.SF.getKey());
        dataManager.setCreateId(user.getId());
        dataManager.setCust_id(Long.valueOf(user.getCustId()));

        dataManager.setCreateDate(new Date());
        dataManager.setExt_3(dan.getBill_NO());//分单号
        dataManager.setExt_4(dan.getMain_bill_NO());//主单号

        JSONObject json = buildPartyContent(dan);
        json.put("type", BusiTypeEnum.SF.getKey());
        json.put("mail_bill_no", mainBillNo);
        json.put("create_date", dataManager.getCreateDate());
        json.put("create_id", user.getId());
        json.put("cust_id", user.getCustId());
        json.put("check_status", "0");
        json.put("idcard_pic_flag", "0");
        dataManager.setContent(json.toJSONString());

        list.add(dataManager);
    }

    /**
     * 组装商品
     *
     * @param list
     * @param pList
     * @param user
     */
    public void buildGoods(List<HBusiDataManager> list, List<Product> pList, LoginUser user) {
        if (pList != null && pList.size() > 0) {
            for (Product product : pList) {
                HBusiDataManager dataManager = new HBusiDataManager();
                dataManager.setType(BusiTypeEnum.SS.getKey());
                dataManager.setCreateDate(new Date());
                dataManager.setCreateId(user.getId());
                dataManager.setCust_id(Long.valueOf(user.getCustId()));
                dataManager.setExt_3(product.getCode_ts());//商品编号
                dataManager.setExt_4(product.getParty_No());//分单号
                JSONObject json = buildGoodsContent(product);
                json.put("create_date", new Date());
                json.put("create_id", user.getId());
                json.put("cust_id", user.getCustId());
                json.put("type", BusiTypeEnum.SS);
                dataManager.setContent(json.toJSONString());

                list.add(dataManager);
            }
        }
    }


    public Map<String, List<Map<String, Object>>> getdicList(String type, String propertyName) {
        String hql = " from  HMetaDataDef a where filed_type='array' and type='" + type + "' ";
        if (StringUtil.isNotEmpty(propertyName)) {
            hql += "a.property_name='" + propertyName + "'";
        }
        Map<String, List<Map<String, Object>>> m = new HashMap<>();
        List<HMetaDataDef> hMetaDataDeflist = hMetaDataDefDao.find(hql);
        if (hMetaDataDeflist != null && hMetaDataDeflist.size() > 0) {
            for (int i = 0; i < hMetaDataDeflist.size(); i++) {
                String propertyCode = hMetaDataDeflist.get(i).getProperty_code();
                String property_name_en = hMetaDataDeflist.get(i).getProperty_name_en();
                String sql = "select type,code,name_zh from h_dic where type='" + propertyCode + "'";
                List<Map<String, Object>> list = hMetaDataDefDao.queryListBySql(sql);
                if (list != null && list.size() > 0) {
                    for (Map<String, Object> map : list) {
                        List<Map<String, Object>> l = null;
                        if (m.containsKey(property_name_en)) {
                            l = m.get(property_name_en);
                        }
                        if (l == null) {
                            l = new ArrayList<>();
                        }
                        l.add(map);
                        m.put(property_name_en, l);
                    }
                }
            }
        }
        return m;
    }


    public Page getdicPageList(String dicType, Integer pageSize, Integer pageNo) {
        String sql = "select * from h_dic where type='" + dicType + "'";
        Page page = hDicDao.sqlPageQuery(sql, pageNo, pageSize);
        return page;
    }


    public void saveDic(HDic hdic) {
        if (StringUtil.isEmpty(hdic.getStatus())) {
            hdic.setStatus("1");
        }
        String sql = "replace INTO `h_dic` (`type`, `code`, `name_zh`, `name_en`,  `status`,`desc`, `p_code`, `ext_1`, `ext_2`, `ext_3`) VALUES('" + hdic.getType() + "'," +
                "'" + hdic.getCode() + "','" + hdic.getName_zh() + "','" + hdic.getName_en() + "','" + hdic.getStatus() + "','" + hdic.getDesc() + "','" + hdic.getP_code() + "'," +
                "'" + hdic.getExt_1() + "','" + hdic.getExt_2() + "','" + hdic.getExt_3() + "')";
//        hDicDao.saveOrUpdate(dic);
        hDicDao.executeUpdateSQL(sql);
    }


//
//    public MainDan getDetail(){
//
//    }


    /**
     * 上传申报单分单身份证照片
     *
     * @param file
     * @param id   主/分单ID
     * @param type 1-主单 2-分单
     * @return
     * @throws TouchException
     */
    public int uploadCardIdPic(MultipartFile file, String id, int type) throws TouchException {
        int code = 0;
        // 判断文件格式
        String filename = file.getOriginalFilename();
        if (!filename.endsWith("zip") && !filename.endsWith("jpg") && !filename.endsWith("png")) {
            log.warn("传入身份证文件格式错误:" + filename);
            return -1;
        }
        List<FileModel> fileList = null;
        try (InputStream inputStream = file.getInputStream()) {
            // zip身份证文件
            if (filename.endsWith("zip")) {
                fileList = ZipUtil.unZip(file);
            } else if (filename.endsWith("jpg") || filename.endsWith("png")) {
                // 单个身份证文件
                fileList = new ArrayList<>();
                String fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));

                FileModel fileModel = new FileModel(file.getOriginalFilename(), fileType, inputStream);
                fileList.add(fileModel);

            }
            if (fileList != null && fileList.size() > 0) {
                // 根据主单ID查询分单列表
                List<HBusiDataManager> fdList = new ArrayList<>();
                if (1 == type) {
                    fdList = hBusiDataManagerDao.listHBusiDataManager(NumberConvertUtil.parseInt(id), BusiTypeEnum.SF.getKey());
                } else if (2 == type) {
                    // 根据ID查询单个申报单分单
                    HBusiDataManager data = hBusiDataManagerDao.get(NumberConvertUtil.parseInt(id));
                    if (data != null) {
                        fdList.add(data);
                    }
                }
                // 文件存储
                Map<String, String> map = new HashMap<>();
                String objectId;
                for (FileModel f : fileList) {
                    objectId = uploadFileService.uploadFile(f.getFileInputstream(), BusinessEnum.CUSTOMS, false, f.getFileName());
                    map.put(f.getFileName().substring(0, f.getFileName().indexOf(".")), objectId);
                }
                // 上传身份证照片并且更新分单数据库和ES信息
                JSONObject jsonObject;
                String picKey = "ID_NO_PIC";
                for (HBusiDataManager d : fdList) {
                    if (StringUtil.isEmpty(d.getContent())) {
                        continue;
                    }
                    jsonObject = JSON.parseObject(d.getContent());
                    if (jsonObject != null) {
                        // 身份证照片存储对象ID
                        jsonObject.put(picKey, map.get(jsonObject.getString("ID_NO")));
                        d.setContent(jsonObject.toJSONString());
                        d.setExt_6(jsonObject.getString(picKey));
                        hBusiDataManagerDao.saveOrUpdate(d);
                        updateDataToES(d, d.getId());
                    }
                }
                // 更新主单下分单的身份证照片数量
                if (1 == type) {
                    updateMainDanIdCardNumber(NumberConvertUtil.parseInt(id));
                }
                code = 1;
            } else {
                log.warn("传入身份证文件为空:" + filename);
                return -5;
            }
        } catch (IOException e) {
            log.error("读取身份证单个图片异常", e);
        }
        return code;
    }

    /**
     * 提交为报单、仓单、
     * 1.添加报单主单
     * 2.添加报单分单
     * 3.添加报单税单
     * to=HAIGUAN:提交到海关
     *
     * @param id
     * @param type
     */
    public void commit2cangdanorbaodan(String id, String type, LoginUser user, String to) throws Exception {
        HBusiDataManager h = hBusiDataManagerDao.get(Long.valueOf(id));
        if (h == null) {
            throw new Exception("数据不存在");
        }
        if (!user.getCustId().equals(h.getCust_id().toString())) {
            throw new Exception("你无权处理");
        }

        if (StringUtil.isNotEmpty(to) && "HAIGUAN".equals(to)) {//提交到海关
            if (BusiTypeEnum.BZ.getKey().equals(type)) {
                //todo修改状态，生成xml
            } else if (BusiTypeEnum.CZ.getKey().equals(type)) {
                //todo修改状态，生成xml
            }
        } else {
            //提交为舱单、报关单
            List<HBusiDataManager> dataList = new ArrayList<>();
            if (BusiTypeEnum.BZ.getKey().equals(type)) { //提交为报单
                if ("Y".equals(h.getExt_1())) {
                    throw new Exception("已经提交过了,不能重复提交");
                }
            } else if (BusiTypeEnum.CZ.getKey().equals(type)) { //提交为舱单
                if ("Y".equals(h.getExt_2())) {
                    throw new Exception("已经提交过了,不能重复提交");
                }
            }
            buildDanList(dataList, user, h, type);

            for (HBusiDataManager dm : dataList) {
                Integer hid = (Integer) hBusiDataManagerDao.saveReturnPk(dm);
                addDataToES(dm, hid);
            }
        }
    }

    public void buildDanList(List<HBusiDataManager> dataList, LoginUser user, HBusiDataManager h, String type) {
        HBusiDataManager CZ = new HBusiDataManager();
        if (BusiTypeEnum.CZ.getKey().equals(type)) {
            CZ.setType(BusiTypeEnum.CZ.getKey());
            h.setExt_2("Y");
        } else if (BusiTypeEnum.BZ.getKey().equals(type)) {
            CZ.setType(BusiTypeEnum.BZ.getKey());
            h.setExt_1("Y");
        }

        CZ.setCreateDate(new Date());
        CZ.setCust_id(Long.valueOf(user.getCustId()));
        CZ.setCreateId(user.getId());
        CZ.setExt_3(h.getExt_3());
        CZ.setExt_1("0");//未发送 1，已发送

        JSONObject json = JSON.parseObject(h.getContent());
        json.put("create_id", user.getId());
        json.put("cust_id", user.getCustId());
        json.put("type", CZ.getType());
        json.put("create_date", CZ.getCreateDate());
        json.put("send_status", CZ.getExt_1());
        JSONObject jon = JSON.parseObject(h.getContent());
        if (BusiTypeEnum.CZ.getKey().equals(type)) {
            json.put("commitCangdanStatus", "Y");
            jon.put("commitCangdanStatus", "Y");
        } else {
            json.put("commitBaoDanStatus", "Y");
            jon.put("commitBaoDanStatus", "Y");
        }
        h.setContent(jon.toJSONString());

        dataList.add(h);
        String content = json.toJSONString();
        CZ.setContent(content);
//            Long cid = (Long) hBusiDataManagerDao.saveReturnPk(CZ);
//            json.put("id",cid);
        dataList.add(CZ);
        List<HBusiDataManager> parties = getHbusiDataByBillNo(CZ.getExt_3(), BusiTypeEnum.SF.getKey());
        for (HBusiDataManager hp : parties) {
            if (BusiTypeEnum.CZ.getKey().equals(type)) {
                hp.setType(BusiTypeEnum.CF.getKey());
            } else if (BusiTypeEnum.BZ.getKey().equals(type)) {
                hp.setType(BusiTypeEnum.BF.getKey());
            }
            hp.setCreateDate(new Date());
            hp.setId(null);
            dataList.add(hp);
            List<HBusiDataManager> goods = getHbusiDataByBillNo(hp.getExt_3(), BusiTypeEnum.SS.getKey());
            for (HBusiDataManager gp : goods) {
                gp.setId(null);
                gp.setCreateDate(new Date());
                if (BusiTypeEnum.CZ.getKey().equals(type)) {
                    gp.setType(BusiTypeEnum.CS.getKey());
                } else if (BusiTypeEnum.BZ.getKey().equals(type)) {
                    gp.setType(BusiTypeEnum.BS.getKey());
                }
                dataList.add(gp);
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


    /**
     * 清空分单身份证照片
     *
     * @param id
     * @return
     * @throws TouchException
     */
    public int clearSFCardIdPic(List<Integer> id) {
        int code = 0;
        List<HBusiDataManager> hBusiDataManagers = hBusiDataManagerDao.listHBusiDataManager(id, BusiTypeEnum.SF.getKey());
        if (hBusiDataManagers != null) {
            JSONObject jsonObject;
            String picKey = "ID_NO_PIC";
            HBusiDataManager mainD = null;
            for (HBusiDataManager d : hBusiDataManagers) {
                d.setExt_6("");
                jsonObject = JSON.parseObject(d.getContent());
                if (jsonObject != null) {
                    // 身份证照片存储对象ID
                    jsonObject.put(picKey, "");
                    d.setContent(jsonObject.toJSONString());
                }
                hBusiDataManagerDao.saveOrUpdate(d);
                updateDataToES(d, d.getId());
                mainD = hBusiDataManagerDao.getHBusiDataManager("ext_3", d.getExt_4());
            }
            if (mainD != null) {
                updateMainDanIdCardNumber(mainD.getId());
            }
            code = 1;
        }
        return code;
    }

    /**
     * 更新申报单主单身份证照片数量
     *
     * @param mainId
     * @return
     */
    public int updateMainDanIdCardNumber(int mainId) {
        int idCardNumber = hBusiDataManagerDao.countMainDIdCardNum(mainId, BusiTypeEnum.SF.getKey());
        log.info("开始更新主单:{}的身份证照片数量:{}", mainId, idCardNumber);
        int code = 0;
        JSONObject mainDetail = getMainDetailById(String.valueOf(mainId), BusiTypeEnum.SZ.getKey());
        if (mainDetail != null && mainDetail.containsKey("id")) {
            mainDetail.put("idCardNumber", idCardNumber);
            HBusiDataManager mainD = hBusiDataManagerDao.get(mainId);
            if (mainD != null) {
                mainD.setContent(mainDetail.toJSONString());
                hBusiDataManagerDao.update(mainD);
                updateDataToES(mainD, mainId);
            }
            code = 1;
        }
        return code;
    }

    public static void main(String[] args) {
        QueryDataParams queryDataParams = new QueryDataParams();
        //queryDataParams.setBillNo("20001");
        queryDataParams.setPageSize(2);
        queryDataParams.setPageNum(0);
        queryDataParams.setId(1714);
    }

    public SearchSourceBuilder makeQueryStringForSearch(QueryDataParams queryDataParams) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        if (queryDataParams.getId() != null) {
            MatchPhraseQueryBuilder mpq = QueryBuilders
                    .matchPhraseQuery("_id", queryDataParams.getId());
            qb.must(mpq);
        }
        //单号
        if (queryDataParams.getBill_no() != null) {
            qb.must(QueryBuilders.matchPhraseQuery("bill_no", queryDataParams.getBill_no()));
        }
        //场站id
        if (StringUtil.isNotEmpty(queryDataParams.getStationId())) {
            qb.must(QueryBuilders.matchPhraseQuery("stationId", queryDataParams.getStationId()));
        }
        //时间
        if (queryDataParams.getCreateDate() != null && queryDataParams.getI_d_date() != null) {
            qb.must(QueryBuilders.rangeQuery("createDate").from(queryDataParams.getCreateDate()).to(queryDataParams.getI_d_date()));
        }
        searchSourceBuilder.query(qb);
        return searchSourceBuilder;
       /*  int from = (queryDataParams.getPageNum() - 1) * queryDataParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(queryDataParams.getPageSize());
        //排序
        searchSourceBuilder.sort("_id", SortOrder.ASC);

        String query = searchSourceBuilder.toString();
       SearchResult result = elasticSearchService.searchToEs(query, Constants.SZ_INFO_INDEX, "haiguan");

        List<Object> list = new ArrayList<>();
        if (result != null) {
            List<SearchResult.Hit<MainDan, Void>> hits = result.getHits(MainDan.class);
            for (SearchResult.Hit<MainDan, Void> hit : hits) {
                MainDan mainDan = hit.source;
                list.add(mainDan);
            }
            page.setData(list);
            page.setTotal(NumberConvertUtil.parseInt(result.getTotal()));
        }

        return page;
       return  query;*/
    }

    public Page queryDataPage(QueryDataParams queryDataParams) {
        String index = "";
        Class c = null;
        if ("SZ".equals(queryDataParams.getIndex())) {
            index = Constants.SZ_INFO_INDEX;
            c = MainDan.class;
        } else if ("SF".equals(queryDataParams.getIndex())) {
            index = Constants.SF_INFO_INDEX;
            c = PartyDan.class;
        } else if ("SS".equals(queryDataParams.getIndex())) {
            index = Constants.SS_INFO_INDEX;
            c = Product.class;
        }
        SearchSourceBuilder searchSourceBuilder = makeQueryStringForSearch(queryDataParams);
        int from = (queryDataParams.getPageNum() - 1) * queryDataParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(queryDataParams.getPageSize());
        //排序
        searchSourceBuilder.sort("_id", SortOrder.ASC);

        String query = searchSourceBuilder.toString();
        Page page = elasticSearchService.searchToEsPage(query, index, "haiguan", c);
        return page;
    }
}

