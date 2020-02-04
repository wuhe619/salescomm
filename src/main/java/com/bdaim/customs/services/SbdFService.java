package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.Constants;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.customs.utils.ServiceUtils;
import com.bdaim.util.BigDecimalUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/***
 * 申报单.分单
 */
@Service("busi_sbd_f")
@Transactional
public class SbdFService implements BusiService {

    private static Logger log = LoggerFactory.getLogger(SbdFService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    //    @Autowired
//    private HBusiDataManagerDao hBusiDataManagerDao;
    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private CustomsService customsService;


    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        Integer pid = info.getInteger("pid");
        String billNo = info.getString("bill_no");
        if (pid == null) {
            log.error("主单id不能为空");
            throw new TouchException("主单id不能为空");
        }
        if (StringUtil.isEmpty(billNo)) {
            log.error("分单号不能为空");
            throw new TouchException("分单号不能为空");
        }
        HBusiDataManager sbdzd = serviceUtils.getObjectByIdAndType(cust_id, pid.longValue(), BusiTypeEnum.SZ.getType());
        List<HBusiDataManager> list = serviceUtils.listDataByPid(cust_id, BusiTypeEnum.SF.getType(), pid.longValue(), BusiTypeEnum.SZ.getType());
        if (list != null && list.size() > 0) {
            for (HBusiDataManager hBusiDataManager : list) {
                if (billNo.equals(hBusiDataManager.getExt_3())) {
                    log.error("分单号【" + billNo + "】在主单【" + sbdzd.getExt_3() + "】中已经存在");
                    throw new TouchException("分单号【" + billNo + "】在主单【" + sbdzd.getExt_3() + "】中已经存在");
                }
            }
        }
        info.put("type", BusiTypeEnum.SF.getType());
        info.put("check_status", "0");
        info.put("idcard_pic_flag", "0");
        info.put("main_gname", "");
        info.put("low_price_goods", 0);
        info.put("id", id);
        info.put("pid", pid);
        info.put("ext_3", billNo);
        info.put("ext_4", sbdzd.getExt_3());
        info.put("mail_bill_no", sbdzd.getExt_3());

        serviceUtils.addDataToES(id.toString(), busiType, info);
        JSONObject jsonObject = JSONObject.parseObject(sbdzd.getContent());
        if (info.containsKey("weight") && info.getString("weight") != null) {
            if (jsonObject.containsKey("weight_total")) {
                String weight_total = jsonObject.getString("weight_total");
                if (StringUtil.isNotEmpty(weight_total)) {
                    weight_total = String.valueOf(Float.valueOf(weight_total) + Float.valueOf(info.getString("weight")));
                    jsonObject.put("weight_total", weight_total);//总重量
                }
            }
        }
        int value = 1;
        if (jsonObject.containsKey("party_total")) {
            value = jsonObject.getInteger("party_total") + value;
        }
        jsonObject.put("party_total", value);//分单总数
        if (jsonObject.containsKey("single_batch_num")) {
            value = jsonObject.getInteger("single_batch_num") + 1;
        }
        jsonObject.put("single_batch_num", value);//分单总数
        //sbdzd.setContent(jsonObject.toJSONString());
        //hBusiDataManagerDao.saveOrUpdate(sbdzd);
        String sql = "update " + HMetaDataDef.getTable(sbdzd.getType(), "") + " set content=?" +
                " where id=" + sbdzd.getId() + " and type='" + sbdzd.getType() + "'";
        jdbcTemplate.update(sql, jsonObject.toJSONString());
        serviceUtils.updateDataToES(BusiTypeEnum.SZ.getType(), sbdzd.getId().toString(), jsonObject);
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws TouchException {
        // 身份核验
        if ("verification".equals(info.getString("_rule_"))) {
            StringBuffer sql = new StringBuffer("select id, content from " + HMetaDataDef.getTable(busiType, "") + " where type=?")
                    .append(" and cust_id='").append(cust_id).append("'")
                    //.append(" and id =? AND (ext_7 IS NULL OR ext_7 = '' OR ext_7 = 2 )  ");
                    .append(" and id =?  ");
            List sqlParams = new ArrayList();
            sqlParams.add(busiType);
            sqlParams.add(id);

            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), sqlParams.toArray());
            if (list.size() == 0) {
                log.warn("申报单分单数据不存在[" + busiType + "]" + id);
                throw new TouchException("1000", "申报单分单数据不存在");
            }
            Map map = list.get(0);
            if (map != null && map.size() > 0) {
                String updateSql = "UPDATE " + HMetaDataDef.getTable(busiType, "") + " SET ext_7 = 3, content = ? WHERE id =? AND type =? ";
                // 身份核验待核验入队列
                JSONObject input = new JSONObject();
                JSONObject data = JSON.parseObject(String.valueOf(map.getOrDefault("content", "")));
                if (data != null) {
                    if ("1".equals(data.getString("check_status"))) {
                        log.warn("申报单分单已经核验通过[" + busiType + "]" + id);
                        throw new TouchException("1000", "申报单分单已经核验通过");
                    } else if ("3".equals(data.getString("check_status"))) {
                        log.warn("申报单分单正在核验中[" + busiType + "]" + id);
                        throw new TouchException("1000", "申报单分单正在核验中");
                    }
                }
                // 判断余额
                boolean amountStatus = serviceUtils.checkBatchIdCardAmount(cust_id, 1);
                if (!amountStatus) {
                    log.warn("申报单核验余额不足[" + busiType + "]" + id);
                    throw new TouchException("1001", "资金不足无法核验,请充值");
                }
                // 判断身份证是否合法
                if ("1".equals(data.getString("id_type"))) {
                    if (StringUtil.isEmpty(data.getString("id_no")) || data.getString("id_no").length() != 18) {
                        log.warn("申报单分单身份证号不合法[" + busiType + "]" + id);
                        throw new TouchException("1000", "申报单分单身份证号不合法");
                    }
                }

                input.put("name", data.getString("receive_name"));
                input.put("idCard", data.getString("id_no"));
                JSONObject content = new JSONObject();
                content.put("main_id", data.getLongValue("pid"));
                // 主单号
                content.put("main_bill_no", data.getString("main_bill_no"));
                content.put("status", 0);
                content.put("input", input);
                serviceUtils.insertSFVerifyQueue(content.toJSONString(), NumberConvertUtil.parseLong(map.get("id")), cust_user_id, cust_id, content.getString("main_bill_no"));
                if (data != null) {
                    data.put("check_status", "3");
                    info.put("check_status", "3");
                    jdbcTemplate.update(updateSql, data.toJSONString(), map.get("id"), busiType);
                }
            }

        } else if ("clear_verify".equals(info.getString("_rule_"))) {
            // 清空身份证件图片
            //List ids = info.getJSONArray("ids");
            HBusiDataManager d = serviceUtils.getObjectByIdAndType(cust_id, id, BusiTypeEnum.SF.getType());
            if (d != null) {
                JSONObject jsonObject;
                String picKey = "id_no_pic";
                //for (HBusiDataManager d : hBusiDataManagers) {
                d.setExt_6("");
                jsonObject = JSON.parseObject(d.getContent());
                if (jsonObject != null) {
                    // 身份证照片存储对象ID
                    jsonObject.put(picKey, "");
                    jsonObject.put("idcard_pic_flag", "0");
                    d.setContent(jsonObject.toJSONString());
                    info.put(picKey, "");
                    info.put("idcard_pic_flag", "0");
//                    hBusiDataManagerDao.saveOrUpdate(d);
                    String sql = "update " + HMetaDataDef.getTable(d.getType(), "") + " set " +
                            " content=? " +
                            " ,ext_6='' " +
                            " where id=" + d.getId() + " and type='" + d.getType() + "'";
                    jdbcTemplate.update(sql, jsonObject.toJSONString());
                    elasticSearchService.update(d, d.getId());
                    customsService.updateMainDanIdCardNumber(jsonObject.getIntValue("pid"), cust_id);
                }
            }

        } else {
            HBusiDataManager dbManager = serviceUtils.getObjectByIdAndType(cust_id, id, busiType);
            String content = dbManager.getContent();
            JSONObject json = JSONObject.parseObject(content);
            Iterator keys = info.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                json.put(key, info.get(key));
            }
            //dbManager.setContent(json.toJSONString());
            serviceUtils.updateDataToES(busiType, id.toString(), json);
            totalPartDanToMainDan(json.getLongValue("pid"), BusiTypeEnum.SZ.getType(), id, cust_id, "update", info);
        }
    }


    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long
            id, JSONObject info, JSONObject param) throws TouchException {
        // TODO Auto-generated method stub
        if ("SBDCHECK".equals(param.getString("_rule_"))) {
            sbdfCheck(id, cust_id, param);
        }

    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws
            Exception {
        log.info("申报单分单id:{}开始删除,type:{}", id, busiType);
//        String sql = "select id,type,content,ext_1,ext_2,ext_3,ext_4 from "+HMetaDataDef.getTable()+" where id=" + id + " and type='" + busiType + "'";
        HBusiDataManager manager = serviceUtils.getObjectByIdAndType(cust_id, id, busiType);//jdbcTemplate.queryForObject(sql, HBusiDataManager.class);
        if (manager.getCust_id() == null || (!cust_id.equals(manager.getCust_id().toString()))) {
            throw new TouchException("无权删除");
        }

        JSONObject json = JSONObject.parseObject(manager.getContent());
        //List<HBusiDataManager> list = serviceUtils.getDataList(BusiTypeEnum.SS.getType(), id);
        List<HBusiDataManager> list = serviceUtils.listSdByBillNo(cust_id, BusiTypeEnum.SS.getType(), json.getString("main_bill_no"), json.getString("bill_no"));
        List<String> sdIds = new ArrayList<>();
        for (HBusiDataManager manager2 : list) {
            sdIds.add(String.valueOf(manager2.getId()));
            //serviceUtils.deleteDatafromES(manager2.getType(), manager2.getId().toString());
        }
        /*serviceUtils.delDataListByPid(BusiTypeEnum.SS.getType(), id);
        serviceUtils.deleteDatafromES(manager.getType(), manager.getId().toString());*/
        // 批量删除数据库税单
        serviceUtils.deleteByIds(cust_id, BusiTypeEnum.SS.getType(), sdIds);
        // 批量删除es税单
        elasticSearchService.bulkDeleteDocument(BusiTypeEnum.getEsIndex(BusiTypeEnum.SS.getType()), Constants.INDEX_TYPE, sdIds);
        Integer zid = json.getInteger("pid");
        totalPartDanToMainDan(json.getLongValue("pid"), BusiTypeEnum.SZ.getType(), id, cust_id, "del", null);
        // 更新主单身份证照片数量
        json.put("id_no_pic", "");
        json.put("idcard_pic_flag", "0");
        manager.setExt_6("");
        StringBuffer sql2 = new StringBuffer("update " + HMetaDataDef.getTable(busiType, "") + " set update_id=?,update_date=now(), content=?  where type=? and cust_id=? and id=? ");
        jdbcTemplate.update(sql2.toString(), cust_user_id, json.toJSONString(), busiType, cust_id, id);
        customsService.updateMainDanIdCardNumber(zid, cust_id);
    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject
            params, List sqlParams) throws Exception {
        String sql = null;
        //查询主列表
        if ("main".equals(params.getString("_rule_"))) {
            sqlParams.clear();
            StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=?");
            if (!"all".equals(cust_id)){
                sqlParams.add(cust_id);
                sqlstr.append(" and cust_id=? ");
            }
            sqlParams.add(busiType);

            Iterator keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (StringUtil.isNotEmpty(String.valueOf(params.get(key)))) continue;
                if ("pageNum".equals(key) || "pageSize".equals(key) || "pid1".equals(key) || "pid2".equals(key))
                    continue;
                if ("cust_id".equals(key)) {
                    sqlstr.append(" and cust_id=?");
                } else if (key.endsWith(".c")) {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(0, key.length() - 2) + "') like '%?%'");
                } else if (key.endsWith(".start")) {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(0, key.length() - 6) + "') >= ?");
                } else if (key.endsWith(".end")) {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(0, key.length() - 6) + "') <= ?");
                } else {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key + "')=?");
                }

                sqlParams.add(params.get(key));
            }
            String verify_status = params.getString("verify_status");
            String verify_photo = params.getString("verify_photo");
            // 身份校验状态
            if (StringUtil.isNotEmpty(verify_status)) {
                if ("3".equals(verify_status)) {
                    sqlstr.append(" and ( ext_7 IS NULL OR ext_7='' OR ext_7 =3 ");
                }
            }
            //身份图片状态
            if (StringUtil.isNotEmpty(verify_photo)) {
                if ("1".equals(verify_photo)) {
                    sqlstr.append(" and ext_6 IS NOT NULL ");
                } else if ("2".equals(verify_photo)) {
                    sqlstr.append(" and (ext_6 IS NULL OR ext_6='') ");
                }
            }
            return sqlstr.toString();
        }
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject
            info) {
        // TODO Auto-generated method stub

    }


    /**
     * 重新统计主单content
     *
     * @param zid
     * @param type
     * @param id
     */
    public void totalPartDanToMainDan(long zid, String type, Long id, String custId, String optype, JSONObject info) {

        List<HBusiDataManager> data = serviceUtils.listDataByPid(custId, BusiTypeEnum.SF.getType(), zid, BusiTypeEnum.SZ.getType());
        BigDecimal weightTotal = new BigDecimal("0.0");
        Integer low_price_goods = 0;
        for (HBusiDataManager d : data) {
            if ("del".equals(optype)) {
                if (d.getId() == id.intValue()) continue;
            }
            String content = d.getContent();
            JSONObject json = JSONObject.parseObject(content);
            Integer s = json.getInteger("low_price_goods");
            if (s == null) s = 0;
            low_price_goods += s;
            String WEIGHT = json.getString("weight");
            if (StringUtil.isEmpty(WEIGHT)) {
                WEIGHT = "0";
            }
            if ("update".equals(optype)) {
                if (d.getId() == id.intValue()) {
                    if (StringUtil.isEmpty(WEIGHT)) {
                        WEIGHT = "0";
                    } else {
                        WEIGHT = info.getString("weight");
                    }
                }
            }
            weightTotal = weightTotal.add(new BigDecimal(WEIGHT));
        }

        String sql = "";//"select id,type,content,ext_1,ext_2,ext_3,ext_4 from "+HMetaDataDef.getTable()+" where id=" + zid + " and type='" + type + "'";
        HBusiDataManager manager = null;
        try {
            manager = serviceUtils.getObjectByIdAndType(custId, NumberConvertUtil.parseLong(zid), type);
        } catch (EmptyResultDataAccessException e) {
            log.warn("查询主单:{},type:{}失败", zid, type);
        }

        String hcontent = manager.getContent();
        JSONObject jsonObject = JSONObject.parseObject(hcontent);
        weightTotal = BigDecimalUtil.roundingValue(weightTotal, BigDecimal.ROUND_DOWN, 5);
        jsonObject.put("weight_total", weightTotal.doubleValue());//总重量
        if ("del".equals(optype)) {
            jsonObject.put("party_total", data.size() - 1 < 0 ? 0 : data.size() - 1);//分单总数
        }
        Integer s = jsonObject.getInteger("low_price_goods");
        if (s == null) {
            s = 0;
        }
        jsonObject.put("low_price_goods", s + low_price_goods);
        manager.setContent(jsonObject.toJSONString());
        sql = " update " + HMetaDataDef.getTable(BusiTypeEnum.SZ.getType(), "") + " set content=? where id=" + zid + " and type='" + type + "'";
        jdbcTemplate.update(sql, jsonObject.toJSONString());
        serviceUtils.updateDataToES(BusiTypeEnum.SZ.getType(), String.valueOf(zid), jsonObject);

    }

    /*
    校验
     */
    public int sbdfCheck(long id, String cust_id, JSONObject param) throws TouchException {
        long startTime = System.currentTimeMillis();

        String sql1 = "select ext_3,ext_4,content from h_data_manager_sbd_f where id = ?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql1, id);
        if (list.size() == 0) {
            throw new TouchException("2000", "分单:[" + id + "],不存在");
        }
        Object ext_3 = list.get(0).get("ext_3");
        Object ext_4 = list.get(0).get("ext_4");
        Object content = list.get(0).get("content");
        JSONObject jsonObject = JSON.parseObject(content.toString());
        double weight = jsonObject.getDoubleValue("weight");//毛重
        double net_weight = jsonObject.getDoubleValue("net_weight");//净重
        log.info("净重:" + net_weight);
        if (net_weight > weight) {
            throw new TouchException("2000", "分单:[" + ext_3 + "],净重大于毛重");
        }
        log.info("主单号:{" + ext_4 + "}");
        long startTime1 = System.currentTimeMillis();
        String sql = "select content from h_data_manager_sbd_s where ext_4 = ? " +
                "and ext_2 = ? and type='sbd_s' and cust_id=? ";
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql,ext_3,ext_4,cust_id);
        long startTime2 = System.currentTimeMillis();
        log.info("查询税单耗时：" + (startTime2 - startTime1));
        DoubleStream ggrosswt = maps.parallelStream().map(m -> {
            JSONObject contentJson = JSONObject.parseObject(m.get("content").toString());
            return contentJson.getDouble("ggrosswt");
        }).mapToDouble(value -> value);
        long startTime3 = System.currentTimeMillis();
        log.info("获取税单总重量耗时：" + (startTime3 - startTime2));
        Double d = 0.0;

        if (ggrosswt != null) {
            d = ggrosswt.sum();
        }
//            String sql2 = "select content->'$.ggrosswt' from h_data_manager_sbd_s where ext_4 = '" + ext_3 + "'";
//            List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql2);
//            long endTime3 = System.currentTimeMillis();
//            log.info("校验耗时2：" + (endTime3 - endTime2));
        log.info("商品重量:" + d);
        log.info("毛重:" + weight);
        if (weight >= d + 1) {
            throw new TouchException("2000", "分单:[" + ext_3 + "],毛重大于商品重量之和一公斤");
        }
        if (d > weight) {
            throw new TouchException("2000", "分单:[" + ext_3 + "],商品重量之和大于分单的毛重");
        }

        long endTime = System.currentTimeMillis();
        log.info("校验耗时：" + (endTime - startTime));

        return 1;
    }

}
