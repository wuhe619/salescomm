package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ResourceService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customs.entity.*;
import com.bdaim.customs.utils.ServiceUtils;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/***
 * api批次
 */
@Service("busi_hy_pic_x")
@Transactional
public class HypicXService implements BusiService {
    private static Logger log = LoggerFactory.getLogger(HypicXService.class);

    @Autowired
    private CustomerDao customerDao;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ServiceUtils serviceUtils;


    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        busiType = BusiTypeEnum.HY_PIC_X.getType();
        String sql1 = "insert into " + HMetaDataDef.getTable(busiType, "") + "(id, type, content, cust_id, cust_group_id, cust_user_id, create_id, create_date, ext_1,ext_3, ext_4 ) value(?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?)";
        String batchId = info.getString("bill_no");
        String batchName = info.getString("batch_name");
        //查询批次详情数据
        String detailStr = info.getString("details");
        if (StringUtil.isNotEmpty(detailStr)) {
            String details[] = detailStr.split(",");
            if (details.length > 0) {
                for (int i = 0; i < details.length; i++) {
                    String detail = details[i];
                    id = sequenceService.getSeq(busiType);
                    jdbcTemplate.update(sql1, id, busiType, detail, cust_id, cust_group_id, cust_user_id, cust_user_id, 0, detail, batchId);
                }
            }
            //构造住批次信息
            //批次名称
            info.put("ext_5", batchName);
            //批次状态
            info.put("ext_2", 0);
            //批次id
            info.put("ext_3", batchId);
        }
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) {
        String updateSql = "UPDATE " + HMetaDataDef.getTable(busiType, "") + " SET ext_2 = ?, content = ?, ext_5= ? WHERE id =? AND type =? AND cust_id =? AND ext_2 = 0 ";
        if (info.getInteger("status") == 1) {
            int updateNum = jdbcTemplate.update(updateSql, info.getInteger("status"), info.toJSONString(), info.getInteger("scoure"), id, busiType, cust_id);
            if (updateNum > 0) {
                String updateNumSql = "UPDATE " + HMetaDataDef.getTable(BusiTypeEnum.HY_PIC_Z.getType(), "")
                        + " set content = JSON_SET(content, '$.successNum', JSON_EXTRACT(content, '$.successNum') + ?), " +
                        " content = JSON_SET ( content, '$.status', cast( JSON_EXTRACT (content, '$.number') <= JSON_EXTRACT (content, '$.successNum') AS SIGNED INTEGER ))" +
                        " where ext_3 =(SELECT ext_4 from h_data_manager_hy_pic_x WHERE id = ? AND  ext_2=1 AND type =? AND cust_id =?)";
                jdbcTemplate.update(updateNumSql, new Object[]{1, id, BusiTypeEnum.HY_PIC_X.getType(), cust_id});
            }
        }
    }

    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long
            id, JSONObject info, JSONObject param) {
        if (StringUtil.isNotEmpty(param.getString("_rule_")) && param.getString("_rule_").startsWith("_export")) {
            //info.put("export_type", 2);
            switch (param.getString("_rule_")) {
                case "_export_v_photo":
                case "_export_v_nopass":
                    //info.put("export_type", 1);
                    break;
                // 低价商品
                case "_export_low_product":
                    param.put("_ge_low_price_goods", 1);
                    //查询包含低价的分单列表
                    List singles = queryChildData(BusiTypeEnum.SF.getType(), cust_id, cust_group_id, cust_user_id, id, info, param);
                    if (singles != null) {
                        param.remove("_ge_low_price_goods");
                        param.put("_eq_is_low_price", 1);
                        List products = new ArrayList();
                        List tmp;
                        JSONObject js;
                        // 查询分单下的低价商品
                        for (int i = 0; i < singles.size(); i++) {
                            js = (JSONObject) singles.get(i);
                            tmp = queryChildData(BusiTypeEnum.SS.getType(), cust_id, cust_group_id, cust_user_id, js.getLong("id"), info, param);
                            if (tmp != null && tmp.size() > 0) {
                                products.addAll(tmp);
                            }
                        }
                        info.put("singles", products);
                    }
                    break;
                // 查询报检单,理货单下的分单和商品
                case "_export_declaration_form":
                case "_export_tally_form":
                    singles = queryChildData(BusiTypeEnum.SF.getType(), cust_id, cust_group_id, cust_user_id, id, info, param);
                    if (singles != null) {
                        info.put("singles", singles);
                        List products;
                        JSONObject js;
                        for (int i = 0; i < singles.size(); i++) {
                            js = (JSONObject) singles.get(i);
                            products = queryChildData(BusiTypeEnum.SS.getType(), cust_id, cust_group_id, cust_user_id, js.getLong("id"), info, param);
                            js.put("products", products);
                        }
                    }
                    break;
                case "_export_estimated_tax":
                    // 预估税单
                    singles = queryChildData(BusiTypeEnum.SF.getType(), cust_id, cust_group_id, cust_user_id, id, info, param);
                    if (singles != null) {
                        info.put("singles", singles);
                    }
                    break;
            }

        }
    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws
            Exception {
        HBusiDataManager manager = serviceUtils.getObjectByIdAndType(cust_id, id, busiType);
        if (manager == null) {
            throw new TouchException("无权操作");
        }
        if ("Y".equals(manager.getExt_1()) || "Y".equals(manager.getExt_2())) {
            throw new TouchException("已经被提交，无法删除");
        }

        List<HBusiDataManager> list = serviceUtils.getDataList(BusiTypeEnum.SF.getType(), id);
        for (HBusiDataManager hBusiDataManager : list) {
            List<HBusiDataManager> slist = serviceUtils.getDataList(BusiTypeEnum.SS.getType(), hBusiDataManager.getId().longValue());//所有税单
            for (HBusiDataManager shBusiDataManager : slist) {
                serviceUtils.deleteDatafromES(BusiTypeEnum.SS.getType(), shBusiDataManager.getId().toString());
            }
            serviceUtils.deleteDatafromES(BusiTypeEnum.SF.getType(), hBusiDataManager.getId().toString());
            serviceUtils.delDataListByPid(BusiTypeEnum.SS.getType(), hBusiDataManager.getId().longValue());
        }
        serviceUtils.delDataListByPid(BusiTypeEnum.SF.getType(), id);

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject
            params, List sqlParams) {
        //String _orderby_ = params.getString("_orderby_");
//        String _sort_ = params.getString("_sort_");
        StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,update_date,ext_1, ext_2 , ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=?");
        sqlParams.add(busiType);
        if (!"all".equals(cust_id)) {
            sqlstr.append(" and cust_id=? ");
            sqlParams.add(cust_id);
        }

        Iterator keys = params.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (StringUtil.isEmpty(String.valueOf(params.get(key)))) continue;
            if ("pageNum".equals(key) || "pageSize".equals(key) || "cust_id".equals(key) || "_sort_".equals(key) || "_orderby_".equals(key))
                continue;
            if ("cust_id".equals(key)) {
                sqlstr.append(" and cust_id=?");
            } else if ("batchId".equals(key)) {
                sqlstr.append(" and ext_4 =?");
            } else if ("detailId".equals(key)) {
                sqlstr.append(" and ext_3 =?");
            } else if ("status".equals(key)) {
                sqlstr.append(" and ext_2 =?");
            } else continue;
            sqlParams.add(params.get(key));
        }
        /*if (StringUtil.isNotEmpty(_orderby_) && StringUtil.isNotEmpty(_sort_)) {
            sqlstr.append(" ORDER BY ? ? ");
            sqlParams.add(_orderby_);
            sqlParams.add(_sort_);
        }*/
        return sqlstr.toString();
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject
            info) {
        // TODO Auto-generated method stub

    }


    public void buildMain(JSONObject info, List<HBusiDataManager> list, MainDan mainDan, Long userId, String
            custId, String station_id, Long mainid) throws Exception {
        try {
            HBusiDataManager dataManager = new HBusiDataManager();
            dataManager.setCreateId(userId);
            dataManager.setId(mainid);
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
            info.put("ext_1", "N");
            info.put("ext_2", "N");
            info.put("ext_3", mainDan.getBill_no());
            log.info("申报单主单:" + mainDan.getBill_no());
            // 构造分单和商品信息
            buildPartyDan(list, mainDan, userId, custId, mainid, info);
            // 构造主单信息
            buildMainContent(mainDan, info);
            dataManager.setContent(info.toJSONString());

            list.add(dataManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 组装分单
     *
     * @param list
     * @param mainDan
     * @param
     */
    public void buildPartyDan(List<HBusiDataManager> list, MainDan mainDan, Long userId, String custId, Long
            mainid, JSONObject info) throws Exception {
        List<PartyDan> partList = mainDan.getSingles();
        if (partList != null && partList.size() > 0) {
            // 预先生成分单ID
            /*long size = partList.size();
            long maxId = sequenceService.getSeq(BusiTypeEnum.SF.getType(), size);
            // 预先生成商品ID
            long sSize = 0L;
            for (PartyDan dan : partList) {
                sSize += dan.getProducts().size();
            }
            long sMaxId = sequenceService.getSeq(BusiTypeEnum.SS.getType(), sSize);*/
            Map<String, JSONObject> resource = serviceUtils.getHResourceCacheData("duty_paid_rate");
            for (PartyDan dan : partList) {
                if (StringUtil.isEmpty(dan.getMain_bill_no())) {
                    dan.setMain_bill_no(mainDan.getBill_no());
                }
                if (dan.getProducts() != null) {
                    for (Product p : dan.getProducts()) {
                        p.setMain_bill_no(mainDan.getBill_no());
                    }
                }
                /*if (dan.getProducts() != null) {
                    for (Product p : dan.getProducts()) {
                        p.setId(String.valueOf(sMaxId - sSize));
                        p.setMain_bill_no(mainDan.getBill_no());
                        sSize--;
                    }
                }*/

                buildSBDFenDan(0, dan, list, userId, custId, mainDan.getBill_no(), mainid, info, resource);
                //size--;
            }
        }
    }

    public void buildSenbaodanFendan(PartyDan dan, List<HBusiDataManager> list, Long userId, String
            custId, String mainBillNo, Long mainid, JSONObject info) throws Exception {
        try {
            List<Product> pList = dan.getProducts();
            Long id = sequenceService.getSeq(BusiTypeEnum.SF.getType());
            JSONObject arrt = new JSONObject();
            log.info("申报单分单:" + dan.getBill_no());
            buildGoods(list, pList, userId, custId, id.toString(), arrt);
            HBusiDataManager dataManager = new HBusiDataManager();
            dataManager.setType(BusiTypeEnum.SF.getType());
            dataManager.setCreateId(userId);
            dataManager.setCust_id(Long.valueOf(custId));

            dataManager.setId(id);
            dataManager.setCreateDate(new Date());
            dataManager.setExt_3(dan.getBill_no());//分单号
            dataManager.setExt_4(dan.getMain_bill_no());//主单号

            JSONObject json = buildPartyContent(dan);
            json.put("type", BusiTypeEnum.SF.getType());
            json.put("main_bill_no", mainBillNo);
            json.put("create_date", dataManager.getCreateDate());
            json.put("create_id", userId);
            json.put("cust_id", custId);
            json.put("check_status", "0");
            json.put("idcard_pic_flag", "0");
            json.put("pid", mainid);
            JSONArray jsonArray = arrt.getJSONArray("mainGoodsName");
            String mainGoodsName = "";
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    mainGoodsName += obj.getString("name") + "|" + obj.getString("name_en") + "|" + obj.getString("g_model");
                }
            }
            json.put("main_gname", mainGoodsName);
            json.put("low_price_goods", arrt.getString("low_price_goods"));
            if (info.containsKey("low_price_goods") && info.getInteger("low_price_goods") != null) {
                int low_price_goods = info.getInteger("low_price_goods");
                info.put("low_price_goods", low_price_goods + arrt.getInteger("low_price_goods"));
            } else {
                info.put("low_price_goods", arrt.getString("low_price_goods"));
            }
            dataManager.setContent(json.toJSONString());

            list.add(dataManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void buildSBDFenDan(long id1, PartyDan dan, List<HBusiDataManager> list, Long userId, String
            custId, String mainBillNo, Long mainid, JSONObject info, Map<String, JSONObject> resource) throws Exception {
        try {
            List<Product> pList = dan.getProducts();
            Long id = sequenceService.getSeq(BusiTypeEnum.SF.getType());
            JSONObject arrt = new JSONObject();
            log.info("申报单分单:" + dan.getBill_no());
            // 构造商品数据
            buildGoods0(list, pList, userId, custId, String.valueOf(id), arrt, resource);
            HBusiDataManager dataManager = new HBusiDataManager();
            dataManager.setType(BusiTypeEnum.SF.getType());
            dataManager.setCreateId(userId);
            dataManager.setCust_id(Long.valueOf(custId));

            dataManager.setId(id);
            dataManager.setCreateDate(new Date());
            //分单号
            dataManager.setExt_3(dan.getBill_no());
            //主单号
            dataManager.setExt_4(dan.getMain_bill_no());

            JSONObject json = buildPartyContent(dan);
            json.put("type", BusiTypeEnum.SF.getType());
            json.put("main_bill_no", mainBillNo);
            json.put("create_date", dataManager.getCreateDate());
            json.put("create_id", userId);
            json.put("cust_id", custId);
            json.put("check_status", "0");
            json.put("idcard_pic_flag", "0");
            json.put("pid", mainid);
            JSONArray jsonArray = arrt.getJSONArray("mainGoodsName");
            String mainGoodsName = "";
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    mainGoodsName += obj.getString("name") + "|" + obj.getString("name_en") + "|" + obj.getString("g_model");
                }
            }
            json.put("main_gname", mainGoodsName);
            json.put("low_price_goods", arrt.getString("low_price_goods"));
            if (info.containsKey("low_price_goods") && info.getInteger("low_price_goods") != null) {
                int low_price_goods = info.getInteger("low_price_goods");
                info.put("low_price_goods", low_price_goods + arrt.getInteger("low_price_goods"));
            } else {
                info.put("low_price_goods", arrt.getString("low_price_goods"));
            }
            dataManager.setContent(json.toJSONString());

            list.add(dataManager);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("build 分单ERROR：" + dan.getBill_no() + " " + e.getMessage());
        }
    }

    /**
     * 组装商品
     *
     * @param list
     * @param pList
     * @param
     */
    public void buildGoods(List<HBusiDataManager> list, List<Product> pList, Long userId, String
            custId, String pid, JSONObject arrt) throws Exception {
        if (pList != null && pList.size() > 0) {
            List<Map<String, String>> mainGoodsName = new ArrayList<>();
            for (Product product : pList) {
                log.info("goods_Code_ts:" + product.getCode_ts());
                try {
                    HBusiDataManager dataManager = new HBusiDataManager();
                    dataManager.setType(BusiTypeEnum.SS.getType());
                    dataManager.setCreateDate(new Date());
                    dataManager.setCreateId(userId);
                    Long id = sequenceService.getSeq(BusiTypeEnum.SS.getType());
                    dataManager.setId(id);
                    dataManager.setCust_id(Long.valueOf(custId));
                    dataManager.setExt_3(product.getCode_ts());//商品编号
                    dataManager.setExt_4(product.getBill_no());//分单号
                    JSONObject json = buildGoodsContent(product);
                    json.put("create_date", new Date());
                    json.put("create_id", userId);
                    json.put("cust_id", custId);
                    json.put("pid", NumberConvertUtil.parseLong(pid));
                    json.put("type", BusiTypeEnum.SS.getType());

                    Float duty_paid_price = 0f;
                    int is_low_price = 0;
                    float tax_rate = 0;
                    float estimated_tax = 0;
                    if (StringUtil.isNotEmpty(product.getCode_ts())) {
                        JSONObject params = new JSONObject();
                        params.put("code", product.getCode_ts());
                        Page page = resourceService.query("", "duty_paid_rate", params);
                        if (page != null && page.getTotal() > 0) {
                            List dataList = page.getData();
                            Map<String, Object> d = (Map<String, Object>) dataList.get(0);
                            JSONObject contentObj = JSON.parseObject(JSON.toJSONString(d));
                            if (contentObj != null && contentObj.containsKey("duty_price") && StringUtil.isNotEmpty(contentObj.getString("duty_price"))) {
                                duty_paid_price = contentObj.getFloat("duty_price");
                                if (StringUtil.isNotEmpty(product.getDecl_price())) {
                                    if (Float.valueOf(product.getDecl_price()) < duty_paid_price) {
                                        is_low_price = 1;
                                    }
                                }
                            }
                            if (contentObj != null && null != contentObj.getString("tax_rate")) {
                                tax_rate = contentObj.getFloatValue("tax_rate");
                                estimated_tax = duty_paid_price * tax_rate;
                            }
                        }
                    }
                    if (mainGoodsName.size() < 3) {
                        Map<String, String> smap = new HashMap<>();
                        smap.put("name", product.getG_name() == null ? "" : product.getG_name());
                        smap.put("name_en", product.getG_name_en() == null ? "" : product.getG_name_en());
                        smap.put("g_model", product.getG_model() == null ? "" : product.getG_model());
                        smap.put("price", product.getDecl_price() == null ? "0" : product.getDecl_price());
                        mainGoodsName.add(smap);

                    } else {

                    }
                    if (is_low_price == 1) {
                        if (arrt.containsKey("low_price_goods")) {
                            arrt.put("low_price_goods", arrt.getInteger("low_price_goods") + 1);
                        } else {
                            arrt.put("low_price_goods", 1);
                        }
                        arrt.put("main_goods_name", mainGoodsName);
                    }
                    json.put("is_low_price", is_low_price);
                    float total_price = Float.valueOf(product.getDecl_total() == null || "".equals(product.getDecl_total()) ? "0" : product.getDecl_total());
                    json.put("duty_paid_price", duty_paid_price);//完税价格
                    json.put("estimated_tax", estimated_tax);//预估税金
                    json.put("tax_rate", tax_rate);//税率
                    json.put("total_price", total_price);//价格合计

                    dataManager.setContent(json.toJSONString());

                    list.add(dataManager);

                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("生成商品信息 " + product.getCode_ts() + " 异常");
                    throw new Exception("生成商品信息异常");
                }
            }
        }
    }

    public void buildGoods0(List<HBusiDataManager> list, List<Product> pList, Long userId, String
            custId, String pid, JSONObject arrt, Map<String, JSONObject> resource) throws Exception {
        if (pList != null && pList.size() > 0) {
            List<Map<String, String>> mainGoodsName = new ArrayList<>();
            HBusiDataManager dataManager;
            for (Product product : pList) {
                log.info("goods:" + product.getCode_ts());
                try {
                    dataManager = new HBusiDataManager();
                    dataManager.setType(BusiTypeEnum.SS.getType());
                    dataManager.setCreateDate(new Date());
                    dataManager.setCreateId(userId);
                    Long id = sequenceService.getSeq(BusiTypeEnum.SS.getType());
                    //dataManager.setId(NumberConvertUtil.parseLong(product.getId()));
                    dataManager.setId(id);
                    dataManager.setCust_id(Long.valueOf(custId));
                    dataManager.setExt_3(product.getCode_ts());//商品编号
                    dataManager.setExt_4(product.getBill_no());//分单号
                    JSONObject json = buildGoodsContent(product);
                    json.put("create_date", new Date());
                    json.put("create_id", userId);
                    json.put("cust_id", custId);
                    json.put("pid", NumberConvertUtil.parseLong(pid));
                    json.put("type", BusiTypeEnum.SS.getType());

                    Float duty_paid_price = 0f;
                    int is_low_price = 0;
                    float tax_rate = 0;
                    float estimated_tax = 0;
                    if (StringUtil.isNotEmpty(product.getCode_ts())) {
                        /*Map<String, Object> duty_paid_rate;
                        if (resource.get(product.getCode_ts()) == null) {
                            duty_paid_rate = serviceUtils.getHResourceData("duty_paid_rate", product.getCode_ts());
                            resource.put(product.getCode_ts(), duty_paid_rate);
                        } else {
                            duty_paid_rate = resource.get(product.getCode_ts());
                        }*/

                        JSONObject contentObj = resource.get(product.getCode_ts());
                        if (contentObj != null && contentObj.containsKey("duty_price") && StringUtil.isNotEmpty(contentObj.getString("duty_price"))) {
                            duty_paid_price = contentObj.getFloat("duty_price");
                            if (StringUtil.isNotEmpty(product.getDecl_price())) {
                                if (Float.valueOf(product.getDecl_price()) < duty_paid_price) {
                                    is_low_price = 1;
                                }
                            }
                        }
                        if (contentObj != null && null != contentObj.getString("tax_rate")) {
                            tax_rate = contentObj.getFloatValue("tax_rate");
                            estimated_tax = duty_paid_price * tax_rate;
                        }

                    }
                    if (mainGoodsName.size() < 3) {
                        Map<String, String> smap = new HashMap<>();
                        smap.put("name", product.getG_name() == null ? "" : product.getG_name());
                        smap.put("name_en", product.getG_name_en() == null ? "" : product.getG_name_en());
                        smap.put("g_model", product.getG_model() == null ? "" : product.getG_model());
                        smap.put("price", product.getDecl_price() == null ? "0" : product.getDecl_price());
                        mainGoodsName.add(smap);

                    }
                    if (is_low_price == 1) {
                        if (arrt.containsKey("low_price_goods")) {
                            arrt.put("low_price_goods", arrt.getInteger("low_price_goods") + 1);
                        } else {
                            arrt.put("low_price_goods", 1);
                        }
                        arrt.put("main_goods_name", mainGoodsName);
                    }
                    json.put("is_low_price", is_low_price);
                    float total_price = Float.valueOf(product.getDecl_total() == null || "".equals(product.getDecl_total()) ? "0" : product.getDecl_total());
                    json.put("duty_paid_price", duty_paid_price);//完税价格
                    json.put("estimated_tax", estimated_tax);//预估税金
                    json.put("tax_rate", tax_rate);//税率
                    json.put("total_price", total_price);//价格合计

                    dataManager.setContent(json.toJSONString());

                    list.add(dataManager);

                } catch (Exception e) {
                    log.error("生成商品信息 " + product.getCode_ts() + " 异常", e);
                }
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
    private void buildMainContent(MainDan mainDan, JSONObject info) {
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

    /**
     * 查询分单和商品
     *
     * @param busiType
     * @param cust_id
     * @param cust_group_id
     * @param cust_user_id
     * @param pid
     * @param info
     * @return
     */
    private List queryChildData(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long
            pid, JSONObject info, JSONObject param) {
        List sqlParams = new ArrayList();
        StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=?");
        if (!"all".equals(cust_id))
            sqlstr.append(" and cust_id='").append(cust_id).append("'");

        sqlParams.add(busiType);
        Iterator keys = param.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if ("".equals(String.valueOf(param.get(key)))) continue;
            if ("pageNum".equals(key) || "pageSize".equals(key) || "stationId".equals(key) || "cust_id".equals(key) || "_rule_".equals(key)) {
                continue;
            } else if (key.startsWith("_g_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(3) + "') > ?");
            } else if (key.startsWith("_ge_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(4) + "') >= ?");
            } else if (key.startsWith("_l_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(3) + "') < ?");
            } else if (key.startsWith("_le_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(4) + "') <= ?");
            } else if (key.startsWith("_eq_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(4) + "') = ?");
            } else {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key + "')=?");
            }
            sqlParams.add(param.get(key));
        }
        //sqlstr.append(" and JSON_EXTRACT(content, '$.pid')=?");
        sqlstr.append(" and ext_4=(SELECT ext_3 FROM " + HMetaDataDef.getTable(BusiTypeEnum.getParentType(busiType), "") + " WHERE id = ?)");
        sqlParams.add(pid);

        List<Map<String, Object>> ds = jdbcTemplate.queryForList(sqlstr.toString(), sqlParams.toArray());
        List data = new ArrayList();
        for (int i = 0; i < ds.size(); i++) {
            Map m = (Map) ds.get(i);
            JSONObject jo = null;
            try {
                if (m.containsKey("content")) {
                    jo = JSONObject.parseObject((String) m.get("content"));
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
                } else
                    jo = JSONObject.parseObject(JSONObject.toJSONString(m));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            if (jo == null) { //jo异常导致为空时，只填充id
                jo = new JSONObject();
                jo.put("id", m.get("id"));
            }
            data.add(jo);
        }
        return data;
    }
}
