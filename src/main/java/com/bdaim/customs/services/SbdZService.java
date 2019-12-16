package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.ResourceService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customs.entity.*;
import com.bdaim.customs.utils.ServiceUtils;
import com.bdaim.util.BigDecimalUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;

import io.datakernel.serializer.StringFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/***
 * 申报单.主单
 */
@Service("busi_sbd_z")
@Transactional
public class SbdZService implements BusiService {
    private static Logger log = LoggerFactory.getLogger(SbdZService.class);

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

    @Autowired
    ElasticSearchService elasticSearchService;

    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        CustomerProperty station_idProperty = customerDao.getProperty(cust_id, "station_id");
        if (station_idProperty == null || StringUtil.isEmpty(station_idProperty.getPropertyValue())) {
            log.warn("custId:{}未配置场站信息", cust_id);
            throw new TouchException("未配置场站信息");
        }
        String billno = info.getString("bill_no");
        String sql = "select id from " + HMetaDataDef.getTable(busiType, "") + " where type='" + busiType + "' and ext_3 = '" + billno + "'";
        List<Map<String, Object>> countList = jdbcTemplate.queryForList(sql);
        if (countList != null && countList.size() > 0) {
            log.warn("主单:{}已经申报", billno);
            throw new TouchException("此主单已经申报");
        }
        log.info("申报单主单号:{}开始插入:{}", billno, System.currentTimeMillis());
        List<HBusiDataManager> list = new ArrayList<>();
        MainDan mainDan = JSON.parseObject(info.toJSONString(), MainDan.class);
        try {
            // 构造主单 分单 税单数据
            buildMain(info, list, mainDan, cust_user_id, cust_id, station_idProperty.getPropertyValue(), id);
            log.info("has " + list.size() + " data");
            if (list != null && list.size() > 0) {
                int index = -1;
                HBusiDataManager mainData = null;
                List<JSONObject> sfdData = new ArrayList();
                List<JSONObject> ssData = new ArrayList();
                JSONObject content, json;
                HBusiDataManager hBusiDataManager;
                for (int i = 0; i < list.size(); i++) {
                    hBusiDataManager = list.get(i);
                    json = JSON.parseObject(JSON.toJSONString(hBusiDataManager));
                    content = JSON.parseObject(hBusiDataManager.getContent());
                    content.remove("products");
                    if (BusiTypeEnum.SZ.getType().equals(hBusiDataManager.getType())) {
                        info.remove("singles");
                        hBusiDataManager.setContent(info.toJSONString());
                        index = i;
                        mainData = hBusiDataManager;
                    } else if (BusiTypeEnum.SF.getType().equals(hBusiDataManager.getType())) {
                        json.remove("products");
                        hBusiDataManager.setContent(content.toJSONString());
                        json.putAll(content);
                        sfdData.add(json);
                    } else if (BusiTypeEnum.SS.getType().equals(hBusiDataManager.getType())) {
                        json.putAll(content);
                        ssData.add(json);
                    }
                    //serviceUtils.addDataToES(hBusiDataManager.getId().toString(), hBusiDataManager.getType(), JSONObject.parseObject(hBusiDataManager.getContent()));
                }
                if (mainData != null) {
                    try {
                        serviceUtils.addDataToES(String.valueOf(mainData.getId()), mainData.getType(), JSON.parseObject(mainData.getContent()));
                    } catch (Exception e) {
                        log.error("主单信息保存到es失败");
                    }
                }
                if (sfdData.size() > 0) {
                    elasticSearchService.bulkInsertDocument(BusiTypeEnum.getEsIndex(BusiTypeEnum.SF.getType()), Constants.INDEX_TYPE, sfdData);
                }
                if (ssData.size() > 0) {
                    elasticSearchService.bulkInsertDocument(BusiTypeEnum.getEsIndex(BusiTypeEnum.SS.getType()), Constants.INDEX_TYPE, ssData);
                }

                if (index > -1) {
                    list.remove(index);
                }
                if (list.size() > 0) {
                    Map<String, List<HBusiDataManager>> datamap = new TreeMap<>();
                    for (HBusiDataManager manager : list) {
                        if (datamap.containsKey(manager.getType())) {
                            List<HBusiDataManager> d = datamap.get(manager.getType());
                            d.add(manager);
                        } else {
                            List<HBusiDataManager> d = new ArrayList<>();
                            d.add(manager);
                            datamap.put(manager.getType(), d);
                        }
                    }
                    Set<String> types = datamap.keySet();
                    Iterator<String> iterator = types.iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        List<HBusiDataManager> d = datamap.get(key);
                        log.info("key=" + key + ":" + d.size());
                        serviceUtils.batchInsert(key, d);
                    }
                }
            }
            log.info("申报单主单号:{}结束插入:{}", billno, System.currentTimeMillis());
            log.info(info.toJSONString());
        } catch (Exception e) {
            log.error("保存主单出错", e);
            throw new Exception("保存主单出错", e);
        }
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws TouchException {
        // 身份核验
        if ("verification".equals(info.getString("_rule_"))) {
            //serviceUtils.esTestData();
            StringBuffer sql = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(BusiTypeEnum.SF.getType(), "") + " where type=?")
                    .append(" and cust_id='").append(cust_id).append("'")
                    .append(" and (ext_7 IS NULL OR ext_7 = '' OR JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.check_status')='0') ");
            //.append(" and JSON_EXTRACT(content, '$.check_status')=1");

            sql.append(" and ext_4=(SELECT ext_3 FROM " + HMetaDataDef.getTable(BusiTypeEnum.getParentType(BusiTypeEnum.SF.getType()), "") + " WHERE id = ?)");

            List sqlParams = new ArrayList();
            sqlParams.add(BusiTypeEnum.SF.getType());
            sqlParams.add(id);
            // 根据主单查询待核验的分单列表
            List<Map<String, Object>> dfList = jdbcTemplate.queryForList(sql.toString(), sqlParams.toArray());
            int failIdCardNum = 0;
            if (dfList != null && dfList.size() > 0) {
                // 判断余额
                boolean amountStatus = serviceUtils.checkBatchIdCardAmount(cust_id, dfList.size());
                if (!amountStatus) {
                    log.warn("申报单核验余额不足[" + busiType + "]" + id);
                    throw new TouchException("1001", "资金不足无法核验,请充值");
                }
                JSONObject content = new JSONObject();
                content.put("main_id", id);
                content.put("status", 0);
                JSONObject input;
                JSONObject data;
                String updateSql = "UPDATE " + HMetaDataDef.getTable(BusiTypeEnum.SF.getType(), "") + " SET ext_7 = 3, content = ? WHERE id =? AND type =? ";
                for (Map<String, Object> m : dfList) {
                    input = new JSONObject();
                    // 身份核验待核验入队列
                    data = JSON.parseObject(String.valueOf(m.getOrDefault("content", "")));
                    // 判断身份证是否合法
                    if ("1".equals(data.getString("id_type"))) {
                        if (StringUtil.isEmpty(data.getString("id_no"))) {
                            failIdCardNum++;
                        } else if (data.getString("id_no").length() != 18) {
                            failIdCardNum++;
                        }
                    }
                    //主单号
                    content.put("main_bill_no", data.getString("main_bill_no"));
                    input.put("name", data.getString("receive_name"));
                    input.put("idCard", data.getString("id_no"));
                    content.put("input", input);
                    serviceUtils.insertSFVerifyQueue(content.toJSONString(), NumberConvertUtil.parseLong(m.get("id")), cust_user_id, cust_id, content.getString("main_bill_no"));
                    if (data != null) {
                        data.put("check_status", "3");
                        jdbcTemplate.update(updateSql, data.toJSONString(), m.get("id"), BusiTypeEnum.SF.getType());
                    }
                }
                if (failIdCardNum > 0) {
                    log.warn("申报单分单身份证号不合法[" + busiType + "]" + id);
                    throw new TouchException("1000", "申报总数:" + dfList.size() + ",不合法数据总数:" + failIdCardNum);
                    /*info.put("idCardNum", dfList.size());
                    info.put("failIdCardNum", failIdCardNum);*/
                }
            } else {
                log.warn("申报单没有需要核验的分单数据[" + busiType + "]" + id);
                throw new TouchException("1000", "申报单没有需要核验的分单数据");
            }
        } else {
            String billNo = info.getString("bill_no");
            String sql = "select id from " + HMetaDataDef.getTable(busiType, "") + " where type=? and ext_3 = ? AND id <>? ";
            List<Map<String, Object>> countList = jdbcTemplate.queryForList(sql, busiType, billNo, id);
            if (countList != null && countList.size() > 0) {
                log.warn("主单号:{}已经存在", billNo);
                //throw new TouchException("主单号:" + billNo + "已经存在");
            }
            serviceUtils.updateDataToES(busiType, id.toString(), info);
        }
    }

    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) {
        if (StringUtil.isNotEmpty(param.getString("_rule_")) && param.getString("_rule_").startsWith("_export")) {
            //info.put("export_type", 2);
            List singles;
            switch (param.getString("_rule_")) {
                case "_export_verification_result":
                    // 核验通过
                    param.put("check_status", "1");
                    singles = serviceUtils.queryChildData(BusiTypeEnum.SF.getType(), cust_id, cust_group_id, cust_user_id, id, param);
                    if (singles == null) {
                        singles = new ArrayList();
                    }
                    // 核验未通过
                    param.put("check_status", "2");
                    singles.addAll(serviceUtils.queryChildData(BusiTypeEnum.SF.getType(), cust_id, cust_group_id, cust_user_id, id, param));
                    info.put("singles", singles);
                    break;
                // 低价商品
                case "_export_low_product":
                    param.put("_ge_low_price_goods", 1);
                    //查询包含低价的分单列表
                    singles = serviceUtils.queryChildData(BusiTypeEnum.SF.getType(), cust_id, cust_group_id, cust_user_id, id, param);
                    if (singles != null && singles.size() > 0) {
                        param.remove("_ge_low_price_goods");
                        param.put("_eq_is_low_price", 1);
                        List partyBillNos = new ArrayList();
                        JSONObject js, product, content;
                        String main_bill_no = "";
                        // 查询分单下的低价商品
                        for (int i = 0; i < singles.size(); i++) {
                            js = (JSONObject) singles.get(i);
                            js.put("index", i + 1);
                            partyBillNos.add(js.getString("bill_no"));
                            main_bill_no = js.getString("ext_4");
                            //main_bill_no = js.getString("main_bill_no");
                        }
                        List products = serviceUtils.listSdByBillNos(cust_id, BusiTypeEnum.SS.getType(), main_bill_no, partyBillNos, param);
                        for (int j = 0; j < products.size(); j++) {
                            product = (JSONObject) products.get(j);
                            content = JSON.parseObject(product.getString("content"));
                            product.putAll(content);
                            //product.put("index", j + 1);
                            product.put("main_bill_no", main_bill_no);
                            product.put("party_bill_no", product.getString("ext_4"));
                        }
                        info.put("singles", products);
                    }
                    break;
                // 查询报检单,理货单下的分单和商品
                case "_export_declaration_form":
                case "_export_tally_form":
                    singles = serviceUtils.queryChildData(BusiTypeEnum.SF.getType(), cust_id, cust_group_id, cust_user_id, id, param);
                    if (singles != null) {
                        //List products;
                        JSONObject js, product, content;
                        String main_bill_no = "";
                        List partyBillNos = new ArrayList();
                        for (int i = 0; i < singles.size(); i++) {
                            js = (JSONObject) singles.get(i);
                            js.put("index", i + 1);
                            partyBillNos.add(js.getString("bill_no"));
                            main_bill_no = js.getString("ext_4");
                            //main_bill_no = js.getString("main_bill_no");
                        }

                        List products = serviceUtils.listSdByBillNos(cust_id, BusiTypeEnum.SS.getType(), main_bill_no, partyBillNos, param);
                        for (int j = 0; j < products.size(); j++) {
                            product = (JSONObject) products.get(j);
                            content = JSON.parseObject(product.getString("content"));
                            product.putAll(content);
                            //product.put("index", j + 1);
                            product.put("main_bill_no", main_bill_no);
                            product.put("party_bill_no", product.getString("ext_4"));
                        }
                        info.put("singles", singles);
                        info.put("products", products);
                    }
                    break;
                case "_export_estimated_tax":
                    // 预估税单
                    singles = serviceUtils.queryChildData(BusiTypeEnum.SF.getType(), cust_id, cust_group_id, cust_user_id, id, param);
                    if (singles != null) {
                        JSONObject js, product, content;
                        String main_bill_no = "", billNo;
                        List partyBillNos = new ArrayList();

                        for (int i = 0; i < singles.size(); i++) {
                            js = (JSONObject) singles.get(i);
                            //js.put("index", i + 1);
                            partyBillNos.add(js.getString("bill_no"));
                            js.put("main_bill_no", js.getString("ext_4"));
                            main_bill_no = js.getString("ext_4");
                        }
                        Map<String, List<JSONObject>> data = new HashMap<>();
                        List<JSONObject> list;
                        // 查询分单下的所有税单
                        List products = serviceUtils.listSdByBillNos(cust_id, BusiTypeEnum.SS.getType(), main_bill_no, partyBillNos, param);
                        for (int j = 0; j < products.size(); j++) {
                            product = (JSONObject) products.get(j);
                            content = JSON.parseObject(product.getString("content"));
                            product.putAll(content);

                            billNo = content.getString("bill_no");
                            if (data.get(billNo) == null) {
                                list = new ArrayList();
                            } else {
                                list = data.get(billNo);
                            }
                            list.add(content);
                            data.put(content.getString("bill_no"), list);
                        }
                        JSONObject fdData;
                        StringBuffer gName;
                        String split = "|";
                        //double estimated_tax;
                        BigDecimal estimated_tax = null;
                        for (int i = 0; i < singles.size(); i++) {
                            gName = new StringBuffer();
                            estimated_tax = new BigDecimal("0.0");
                            fdData = (JSONObject) singles.get(i);
                            // 商品名称处理
                            list = data.get(fdData.getString("bill_no"));
                            if (list != null) {
                                for (JSONObject fd : list) {
                                    gName.append(fd.getString("g_name"))
                                            .append(split)
                                            .append(fd.getString("g_model"))
                                            .append(split)
                                            .append("(").append(fd.getString("g_qty")).append("*").append(fd.getString("decl_price")).append("),");
                                    //estimated_tax += fd.getDoubleValue("estimated_tax");
                                    estimated_tax = estimated_tax.add(new BigDecimal(fd.getString("estimated_tax")));
                                }
                            }
                            fdData.put("g_name", gName.toString());
                            // 件数统计
                            fdData.put("pack_no", list != null ? list.size() : 0);
                            // 预估税金统计
                            fdData.put("estimated_tax", estimated_tax.doubleValue());
                        }
                        info.put("singles", singles);
                    }
                    break;
                default:
                    break;
            }

        }
    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws Exception {
        HBusiDataManager manager = serviceUtils.getObjectByIdAndType(cust_id, id, busiType);
        if (manager == null) {
            throw new TouchException("主单已经删除");
        }
        if ("Y".equals(manager.getExt_1()) || "Y".equals(manager.getExt_2())) {
            throw new TouchException("已经被提交，无法删除");
        }

        List<HBusiDataManager> list = serviceUtils.listDataByPid(cust_id, BusiTypeEnum.SF.getType(), id, BusiTypeEnum.SZ.getType());
        JSONObject content;
        List<String> fdIds = new ArrayList<>();
        List<String> sdIds = new ArrayList<>();
        List<HBusiDataManager> slist;
        for (HBusiDataManager hBusiDataManager : list) {
            //分单ID集合
            fdIds.add(String.valueOf(hBusiDataManager.getId()));
            content = JSON.parseObject(hBusiDataManager.getContent());
            slist = serviceUtils.listSdByBillNo(cust_id, BusiTypeEnum.SS.getType(), content.getString("main_bill_no"), content.getString("bill_no"));
            for (HBusiDataManager shBusiDataManager : slist) {
                sdIds.add(String.valueOf(shBusiDataManager.getId()));
                //serviceUtils.deleteDatafromES(BusiTypeEnum.SS.getType(), shBusiDataManager.getId().toString());
            }
            // 删除es税单
            //serviceUtils.deleteDatafromES(BusiTypeEnum.SF.getType(), hBusiDataManager.getId().toString());
            // 删除数据库税单
            //serviceUtils.deleteSListByBillNo(cust_id, BusiTypeEnum.SS.getType(), content.getString("main_bill_no"), content.getString("bill_no"));
        }
        // 批量删除数据库分单
        serviceUtils.deleteByIds(cust_id, BusiTypeEnum.SF.getType(), fdIds);
        // 批量删除es分单
        elasticSearchService.bulkDeleteDocument(BusiTypeEnum.getEsIndex(BusiTypeEnum.SF.getType()), Constants.INDEX_TYPE, fdIds);
        // 批量删除数据库税单
        serviceUtils.deleteByIds(cust_id, BusiTypeEnum.SS.getType(), sdIds);
        // 批量删除es税单
        elasticSearchService.bulkDeleteDocument(BusiTypeEnum.getEsIndex(BusiTypeEnum.SS.getType()), Constants.INDEX_TYPE, sdIds);
    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) {
//        if (StringUtil.isEmpty(params.getString("_rule_")) && !"SBDCHECK".equals(params.getString("_rule_"))) {
            StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=?");
            String _orderby_ = params.getString("_orderby_");
            String _sort_ = params.getString("_sort_");
            if (!"all".equals(cust_id))
                sqlstr.append(" and cust_id='").append(cust_id).append("'");
            sqlParams.add(busiType);
            String stationId = params.getString("stationId");
            // 处理场站检索
            if (StringUtil.isNotEmpty(stationId)) {
                String stationSql = "SELECT cust_id FROM t_customer_property WHERE property_name='station_id' AND property_value = ?";
                sqlstr.append(" and cust_id IN ( ").append(stationSql).append(" )");
                sqlParams.add(stationId);
            }

            Iterator keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (StringUtil.isEmpty(String.valueOf(params.get(key)))) continue;
                if ("pageNum".equals(key) || "pageSize".equals(key) || "stationId".equals(key) || "cust_id".equals(key)
                        || "cust_id".equals(key) || "_sort_".equals(key) || "_orderby_".equals(key))
                    continue;
                if ("cust_id".equals(key)) {
                    sqlstr.append(" and cust_id=?");
                } else if (key.equals("bill_no")) {
                    sqlstr.append(" and ext_3 = ? ");
                } else if (key.startsWith("_c_")) {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(3) + "') like concat('%',?,'%')");
                } else if (key.startsWith("_g_")) {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(3) + "') > ?");
                } else if (key.startsWith("_ge_")) {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(4) + "') >= ?");
                } else if (key.startsWith("_l_")) {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(3) + "') < ?");
                } else if (key.startsWith("_le_")) {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(4) + "') <= ?");
                } else if (key.startsWith("_range_")) {
                    if ("0".equals(String.valueOf(params.get(key)))) {
                        sqlstr.append(" and ( JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(7) + "') <= ?")
                                .append(" OR JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(7) + "') = '' ")
                                .append(" OR JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(7) + "') IS NULL ) ");
                    } else {
                        sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(7) + "') >= ?");
                    }
                } else if ("commit_status".equals(key)) {
                    // 提交记录特殊处理
                    if ("1".equals(String.valueOf(params.get(key)))) {
                        //  未提交
                        sqlstr.append(" AND ( JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.commit_cangdan_status') = 'N' OR JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.commit_cangdan_status') = '' )  ")
                                .append(" AND ( JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.commit_baodan_status') = 'N' OR JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.commit_baodan_status') = '' )  ");
                    } else if ("2".equals(String.valueOf(params.get(key)))) {
                        //  舱单已提交
                        sqlstr.append(" AND JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.commit_cangdan_status') = 'Y' ");
                    } else if ("3".equals(String.valueOf(params.get(key)))) {
                        //  报单已提交
                        sqlstr.append(" AND JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.commit_baodan_status') = 'Y' ");
                    } else if ("4".equals(String.valueOf(params.get(key)))) {
                        //  舱单 报单都提交
                        sqlstr.append(" AND ( JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.commit_cangdan_status') = 'Y' AND JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.commit_baodan_status') = 'Y' ) ");
                    }
                    continue;

                } else {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key + "')=?");
                }

                sqlParams.add(params.get(key));
            }
            //sqlstr.append(" ORDER BY create_date DESC, update_date DESC ");
            if (StringUtil.isNotEmpty(_orderby_) && StringUtil.isNotEmpty(_sort_)) {
                sqlstr.append(" ORDER BY ").append(_orderby_).append(" ").append(_sort_);
            }

            return sqlstr.toString();

    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub

    }


    public void buildMain(JSONObject info, List<HBusiDataManager> list, MainDan mainDan, Long userId, String custId, String station_id, Long mainid) throws Exception {
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
            //身份核验成功数量
            info.put("id_card_check_number", 0);
            //身份图片数量
            info.put("id_card_pic_number", 0);
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
    public void buildPartyDan(List<HBusiDataManager> list, MainDan mainDan, Long userId, String custId, Long mainid, JSONObject info) throws Exception {
        List<PartyDan> partList = mainDan.getSingles();
        if (partList != null && partList.size() > 0) {
            Map<String, JSONObject> resource = serviceUtils.getHResourceCacheData("duty_paid_rate");
            for (PartyDan dan : partList) {
                if (StringUtil.isEmpty(dan.getMain_bill_no())) {
                    dan.setMain_bill_no(mainDan.getBill_no());
                }
                if (dan.getProducts() != null) {
                    for (Product p : dan.getProducts()) {
                        p.setMain_bill_no(mainDan.getBill_no());
                        // 处理保留5位小数
                        if (StringUtil.isNotEmpty(p.getGgrosswt())) {
                            BigDecimal v = BigDecimalUtil.roundingValue(new BigDecimal(p.getGgrosswt()), BigDecimal.ROUND_DOWN, 5);
                            p.setGgrosswt(String.valueOf(v.doubleValue()));
                        }
                        if (StringUtil.isNotEmpty(p.getG_qty())) {
                            BigDecimal v = BigDecimalUtil.roundingValue(new BigDecimal(p.getG_qty()), BigDecimal.ROUND_DOWN, 5);
                            p.setG_qty(String.valueOf(v.doubleValue()));
                        }
                        if (StringUtil.isNotEmpty(p.getQty_1())) {
                            BigDecimal v = BigDecimalUtil.roundingValue(new BigDecimal(p.getQty_1()), BigDecimal.ROUND_DOWN, 5);
                            p.setQty_1(String.valueOf(v.doubleValue()));
                        }
                    }
                }
                //weight保留5位小数
                if (StringUtil.isNotEmpty(dan.getWeight())) {
                    BigDecimal v = BigDecimalUtil.roundingValue(new BigDecimal(dan.getWeight()), BigDecimal.ROUND_DOWN, 5);
                    dan.setWeight(String.valueOf(v.doubleValue()));
                }

                buildSBDFenDan(dan, list, userId, custId, mainDan.getBill_no(), mainid, info, resource);
                //size--;
            }
        }
    }


    public void buildSBDFenDan(PartyDan dan, List<HBusiDataManager> list, Long userId, String custId, String mainBillNo, Long mainid, JSONObject info, Map<String, JSONObject> resource) throws Exception {
        try {
            List<Product> pList = dan.getProducts();
            Long id = sequenceService.getSeq(BusiTypeEnum.SF.getType());
            JSONObject arrt = new JSONObject();
            log.info("申报单分单:" + dan.getBill_no());
            // 构造商品数据
            buildGoods0(list, pList, userId, custId, String.valueOf(id), arrt, resource, mainBillNo);
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
            /*JSONArray jsonArray = arrt.getJSONArray("main_goods_name");
            String mainGoodsName = "";
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    mainGoodsName += obj.getString("name") + "|" + obj.getString("name_en") + "|" + obj.getString("g_model");
                }
            }*/
            // 分单总价
            json.put("total_value", arrt.getString("total_value"));
            // 分单预估税单总计
            json.put("estimated_tax", arrt.getString("estimated_tax_total"));
            //json.put("main_gname", mainGoodsName);
            // 计算主要货物
            Map<String, String> main_map = serviceUtils.generateFDMainGName(pList);
            json.put("main_gname", main_map.get("name"));
            json.put("main_gname_en", main_map.get("name_en"));

            // 低价商品数量
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

    public void buildGoods0(List<HBusiDataManager> list, List<Product> pList, Long userId, String custId, String pid, JSONObject arrt, Map<String, JSONObject> resource, String main_bill_no) throws Exception {
        arrt.put("low_price_goods", 0);
        if (pList != null && pList.size() > 0) {
            //List<Map<String, String>> mainGoodsName = new ArrayList<>();
            HBusiDataManager dataManager;
            //分单预估税金总计
            arrt.put("estimated_tax_total", 0);
            BigDecimal totalValue = new BigDecimal("0");
            BigDecimal qty = null;
            BigDecimal multiply = null;
            //分单预估税金总计
            BigDecimal fdEstimatedTax = new BigDecimal("0");
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
                    // 主单号
                    dataManager.setExt_2(main_bill_no);
                    //分单号
                    dataManager.setExt_4(product.getBill_no());
                    //商品编号
                    dataManager.setExt_3(product.getCode_ts());
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
                            // 分单预估税金总计
                            BigDecimal taxBigDecimal = new BigDecimal(contentObj.getString("tax_rate"));
                            BigDecimal taxMultiply = taxBigDecimal.multiply(new BigDecimal(String.valueOf(duty_paid_price)));
                            //fdEstimatedTax += taxMultiply.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
                            fdEstimatedTax = fdEstimatedTax.add(taxMultiply);
                        }

                    }
                    /*if (mainGoodsName.size() < 3) {
                        Map<String, String> smap = new HashMap<>();
                        smap.put("name", product.getG_name() == null ? "" : product.getG_name());
                        smap.put("name_en", product.getG_name_en() == null ? "" : product.getG_name_en());
                        smap.put("g_model", product.getG_model() == null ? "" : product.getG_model());
                        smap.put("price", product.getDecl_price() == null ? "0" : product.getDecl_price());
                        mainGoodsName.add(smap);

                    }*/
                    if (is_low_price == 1) {
                        if (arrt.containsKey("low_price_goods")) {
                            arrt.put("low_price_goods", arrt.getInteger("low_price_goods") + 1);
                        } else {
                            arrt.put("low_price_goods", 1);
                        }
                    }
                    //arrt.put("main_goods_name", mainGoodsName);
                    json.put("is_low_price", is_low_price);
                    String G_QTY = product.getG_qty();
                    String decl_price = product.getDecl_price();
                    json.put("total_price", 0);
                    json.put("decl_price", decl_price);//申报单价
                    //价格合计
                    if (StringUtil.isNotEmpty(G_QTY) && StringUtil.isNotEmpty(decl_price)) {
                        qty = new BigDecimal(G_QTY);
                        multiply = qty.multiply(new BigDecimal(decl_price));
                        //Double total_price = multiply.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
                        //价格合计
                        json.put("total_price", multiply.doubleValue());
                        json.put("decl_total", multiply.doubleValue());
                        totalValue = totalValue.add(multiply);
                    }
//                    float total_price = Float.valueOf(product.getDecl_total() == null || "".equals(product.getDecl_total()) ? "0" : product.getDecl_total());
                    json.put("duty_paid_price", duty_paid_price);//完税价格
                    json.put("estimated_tax", estimated_tax);//预估税金
                    json.put("tax_rate", tax_rate);//税率

                    dataManager.setContent(json.toJSONString());
                    list.add(dataManager);

                } catch (Exception e) {
                    log.error("生成商品信息 " + product.getCode_ts() + " 异常", e);
                }
            }
            arrt.put("total_value", totalValue.doubleValue());
            arrt.put("estimated_tax_total", fdEstimatedTax.setScale(5, BigDecimal.ROUND_DOWN).doubleValue());
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
        BigDecimal weightTotal = new BigDecimal("0");
        for (PartyDan partyDan : list) {
            String WEIGHT = partyDan.getWeight();
            if (StringUtil.isEmpty(WEIGHT)) {
                WEIGHT = "0";
            }
            //weightTotal += Double.valueOf(WEIGHT);
            weightTotal = weightTotal.add(new BigDecimal(WEIGHT));
        }
        weightTotal = BigDecimalUtil.roundingValue(weightTotal, BigDecimal.ROUND_DOWN, 5);
        //总重量
        info.put("weight_total", weightTotal.doubleValue());
        info.put("party_total", list.size());//分单总数

        if (Integer.valueOf(partynum) < list.size()) {
            info.put("over_warp", "溢装");//溢装
        } else if (Integer.valueOf(partynum) > list.size()) {
            info.put("over_warp", "短装");//短装
        } else {
            info.put("over_warp", "正常");//正常
        }

    }


    /*
    校验
     */
    public List<Map> sbdfCheck(String id, String cust_id) {
        long startTime = System.currentTimeMillis();

//        String sql = "select ext_3 from h_data_manager_sbd_z where id = " + id;
//        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
//        Map<String, Object> stringObjectMap = list.get(0);
        String sql1 = "select content,ext_3 from h_data_manager_sbd_f where ext_4='" + id + "' and cust_id='" + cust_id + "'";
        List<Map<String, Object>> list1 = jdbcTemplate.queryForList(sql1);
        String sql2 = "select content,ext_4 from h_data_manager_sbd_s where ext_2='" + id + "' and cust_id='" + cust_id + "'";
        List<Map<String, Object>> list2 = jdbcTemplate.queryForList(sql2);

        Map<String, Double> sbdsMap = new HashMap<>();
        list2.stream().forEach(m -> {
            Map map = (Map) m;
            if (!sbdsMap.containsKey(map.get("ext_4").toString())) {
                sbdsMap.put(map.get("ext_4").toString(), 0.0);
            }
            Double sbdsDouble = sbdsMap.get(map.get("ext_4").toString());
            Object content = map.get("content");
            JSONObject jsonObject = JSON.parseObject(content.toString());
            Double ggrosswt = jsonObject.getDouble("ggrosswt");
            sbdsDouble += ggrosswt;
            sbdsMap.put(map.get("ext_4").toString(), sbdsDouble);
        });
        List<Map> dataList = list1.parallelStream().map(m -> {
            Map dataMap = new HashMap();
            dataMap.put("code", 1);
            String str;
            Map map = (Map) m;
            Object ext_3 = map.get("ext_3");
            dataMap.put("bill_no", ext_3);
            Object content = map.get("content");
            JSONObject jsonObject = JSON.parseObject(content.toString());
            double weight = jsonObject.getDoubleValue("weight");//毛重
            double net_weight = jsonObject.getDoubleValue("net_weight");//净重
            if (net_weight > weight) {
                dataMap.put("code", 2000);
                dataMap.put("message", "分单:[" + ext_3 + "],净重大于毛重");
                return dataMap;
            }
            Double d = 0.0;
            if (sbdsMap.containsKey(ext_3.toString())) d = sbdsMap.get(ext_3.toString());
            if (weight >= d + 1) {
                dataMap.put("code", 2000);
                dataMap.put("message", "分单:[" + ext_3 + "],毛重大于商品重量之和一公斤");
                return dataMap;
            }
            if (d > weight) {
                dataMap.put("code", 2000);
                dataMap.put("message", "分单:[" + ext_3 + "],商品重量之和大于分单的毛重");
                return dataMap;
            }
            return dataMap;
        }).collect(Collectors.toList());

        long endTime = System.currentTimeMillis();
        log.info("校验耗时：" + (endTime - startTime));

        return dataList;
    }

}
