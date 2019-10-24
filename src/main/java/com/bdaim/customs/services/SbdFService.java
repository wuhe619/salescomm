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
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        String sql = "update " + HMetaDataDef.getTable(sbdzd.getType(), "") + " set content='" + jsonObject.toJSONString() + "'" +
                " where id=" + sbdzd.getId() + " and type='" + sbdzd.getType() + "'";
        jdbcTemplate.execute(sql);
        serviceUtils.updateDataToES(BusiTypeEnum.SZ.getType(), sbdzd.getId().toString(), jsonObject);
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws TouchException {
        // 身份核验
        if ("verification".equals(info.getString("_rule_"))) {
            serviceUtils.esTestData();
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
                if (data != null && !"0".equals(data.getString("check_status"))) {
                    log.warn("申报单分单已经核验[" + busiType + "]" + id);
                    throw new TouchException("1000", "申报单分单已经核验");
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
                            " content='" + jsonObject.toJSONString() + "'" +
                            " ,ext_6='' " +
                            " where id=" + d.getId() + " and type='" + d.getType() + "'";
                    jdbcTemplate.execute(sql);
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
            totalPartDanToMainDan(json.getLongValue("pid"), BusiTypeEnum.SZ.getType(), id, cust_id, "update");
        }
    }


    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long
            id, JSONObject info, JSONObject param) {
        // TODO Auto-generated method stub

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
        totalPartDanToMainDan(json.getLongValue("pid"), BusiTypeEnum.SZ.getType(), id, cust_id, "del");
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
            params, List sqlParams) {
        String sql = null;
        //查询主列表
        if ("main".equals(params.getString("_rule_"))) {
            sqlParams.clear();
            StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=?");
            if (!"all".equals(cust_id))
                sqlstr.append(" and cust_id='").append(cust_id).append("'");

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
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(0, key.length() - 2) + "') like '%?%'");
                } else if (key.endsWith(".start")) {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(0, key.length() - 6) + "') >= ?");
                } else if (key.endsWith(".end")) {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(0, key.length() - 6) + "') <= ?");
                } else {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key + "')=?");
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
    public void totalPartDanToMainDan(long zid, String type, Long id, String custId, String optype) {

        List<HBusiDataManager> data = serviceUtils.listDataByPid(custId, BusiTypeEnum.SF.getType(), zid, BusiTypeEnum.SZ.getType());
        Float weightTotal = 0f;
        Integer low_price_goods = 0;
        for (HBusiDataManager d : data) {
            if (d.getId() == id.intValue()) continue;

            String content = d.getContent();
            JSONObject json = JSONObject.parseObject(content);
            Integer s = json.getInteger("low_price_goods");
            if (s == null) s = 0;
            low_price_goods += s;
            String WEIGHT = json.getString("weight");
            if (StringUtil.isEmpty(WEIGHT)) {
                WEIGHT = "0";
            }
            weightTotal += Float.valueOf(WEIGHT);
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
        jsonObject.put("weight_total", weightTotal);//总重量
        if ("del".equals(optype)) {
            jsonObject.put("party_total", data.size() - 1 < 0 ? 0 : data.size() - 1);//分单总数
        }
        Integer s = jsonObject.getInteger("low_price_goods");
        if (s == null) {
            s = 0;
        }
        jsonObject.put("low_price_goods", s + low_price_goods);
        manager.setContent(jsonObject.toJSONString());
        sql = " update " + HMetaDataDef.getTable(BusiTypeEnum.SZ.getType(), "") + " set content='" + jsonObject.toJSONString() + "' where id=" + zid + " and type='" + type + "'";
        jdbcTemplate.update(sql);
        serviceUtils.updateDataToES(BusiTypeEnum.SZ.getType(), String.valueOf(zid), jsonObject);

    }


}
