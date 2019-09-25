package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.Constants;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.utils.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/***
 * 申报单.分单
 */
@Service("busi_sbd_f")
public class SbdFService implements BusiService {

    private static Logger log = LoggerFactory.getLogger(SbdFService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private HBusiDataManagerDao hBusiDataManagerDao;
    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private ServiceUtils serviceUtils;


    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        Integer pid = info.getInteger("pid");
        String billNo = info.getString("bill_no");
        if (pid == null) {
            log.error("主单id不能为空");
            throw new Exception("主单id不能为空");
        }
        if (StringUtil.isEmpty(billNo)) {
            log.error("分单号不能为空");
            throw new Exception("分单号不能为空");
        }
        HBusiDataManager sbdzd = serviceUtils.getObjectByIdAndType(pid.longValue(), BusiTypeEnum.SZ.getType());
        List<HBusiDataManager> list = serviceUtils.getDataList(BusiTypeEnum.SF.getType(), pid.longValue());
        if (list != null && list.size() > 0) {
            for (HBusiDataManager hBusiDataManager : list) {
                JSONObject jsonObject = JSONObject.parseObject(hBusiDataManager.getContent());
                if (billNo.equals(jsonObject.getString("bill_no"))) {
                    log.error("分单号【" + billNo + "】在主单【" + pid + "】中已经存在");
                    throw new Exception("分单号【" + billNo + "】在主单【" + pid + "】中已经存在");
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

        sbdzd.setContent(jsonObject.toJSONString());
        hBusiDataManagerDao.saveOrUpdate(sbdzd);
        serviceUtils.updateDataToES(BusiTypeEnum.SZ.getType(), sbdzd.getId().toString(), jsonObject);
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) {
        // 身份核验
        if ("verification".equals(info.getString("_rule_"))) {
            serviceUtils.esTestData();
            StringBuffer sql = new StringBuffer("select id,content from h_data_manager where type=?")
                    .append(" and cust_id='").append(cust_id).append("'")
                    .append(" and id =? AND (ext_7 IS NULL OR ext_7 = '' OR ext_7 = 2 )  ");
            List sqlParams = new ArrayList();
            sqlParams.add(busiType);
            sqlParams.add(id);

            Map map = jdbcTemplate.queryForMap(sql.toString(), sqlParams.toArray());
            if (map != null && map.size() > 0) {
                String updateSql = "UPDATE h_data_manager SET ext_7 = 0, content = ? WHERE id =? AND type =? ";
                // 身份核验待核验入队列
                JSONObject input = new JSONObject();
                JSONObject data = JSON.parseObject(String.valueOf(map.getOrDefault("content", "")));
                input.put("name", data.getString("receive_name"));
                input.put("idCard", data.getString("id_no"));
                JSONObject content = new JSONObject();
                content.put("main_id", data.getLongValue("pid"));
                content.put("status", 0);
                content.put("input", input);
                serviceUtils.insertSFVerifyQueue(content.toJSONString(), NumberConvertUtil.parseLong(map.get("id")), cust_user_id, cust_id);
                if (data != null) {
                    data.put("check_status", "0");
                    info.put("check_status", "0");
                    jdbcTemplate.update(updateSql, data.toJSONString(), map.get("id"), busiType);
                }
            }

        } else if ("clear_verify".equals(info.getString("_rule_"))) {
            // 清空身份证件图片
            //List ids = info.getJSONArray("ids");
            HBusiDataManager d = serviceUtils.getObjectByIdAndType(id, BusiTypeEnum.SF.getType());
            if (d != null) {
                JSONObject jsonObject;
                String picKey = "id_no_pic";
                HBusiDataManager mainD = null;
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
                }
                hBusiDataManagerDao.saveOrUpdate(d);
                elasticSearchService.update(d, d.getId());
                mainD = hBusiDataManagerDao.getHBusiDataManager("ext_3", d.getExt_4());
                //}
                if (mainD != null) {
                    updateMainDanIdCardNumber(mainD.getId());
                }
                //}
            }

        } else {
            HBusiDataManager dbManager = serviceUtils.getObjectByIdAndType(id, busiType);
            String content = dbManager.getContent();
            JSONObject json = JSONObject.parseObject(content);
            //BeanUtils.copyProperties(info,json);
            Iterator keys = info.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                json.put(key, info.get(key));
            }
            //dbManager.setContent(json.toJSONString());
            serviceUtils.updateDataToES(busiType, id.toString(), json);
            totalPartDanToMainDan(json.getInteger("pid"), BusiTypeEnum.SZ.getType(), id);
        }
    }

    /**
     * 更新申报单主单身份证照片数量
     *
     * @param mainId
     * @return
     */
    private int updateMainDanIdCardNumber(int mainId) {
        int idCardNumber = hBusiDataManagerDao.countMainDIdCardNum(mainId, BusiTypeEnum.SF.getType());
        log.info("开始更新主单:{}的身份证照片数量:{}", mainId, idCardNumber);
        int code = 0;

        JSONObject mainDetail = elasticSearchService.getDocumentById(Constants.SF_INFO_INDEX, "haiguan", String.valueOf(mainId));
        if (mainDetail == null) {
            HBusiDataManager param = new HBusiDataManager();
            param.setId(NumberConvertUtil.parseInt(mainId));
            param.setType(BusiTypeEnum.SZ.getType());
            HBusiDataManager h = hBusiDataManagerDao.get(param);
            if (h != null && h.getContent() != null) {
                mainDetail = JSON.parseObject(h.getContent());
            }
        }
        if (mainDetail != null) {
            mainDetail.put("id", mainId);
        }
        if (mainDetail != null && mainDetail.containsKey("id")) {
            mainDetail.put("idCardNumber", idCardNumber);
            HBusiDataManager param = new HBusiDataManager();
            param.setId(mainId);
            param.setType(BusiTypeEnum.SZ.getType());
            HBusiDataManager mainD = hBusiDataManagerDao.get(param);
            if (mainD != null) {
                mainD.setContent(mainDetail.toJSONString());
                hBusiDataManagerDao.update(mainD);
                elasticSearchService.update(mainD, mainId);
            }
            code = 1;
        }
        return code;
    }

    @Override
    public void getInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long
            id, JSONObject info, JSONObject param) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws
            Exception {
        log.info("申报单分单id:{}开始删除,type:{}", id, busiType);
//        String sql = "select id,type,content,ext_1,ext_2,ext_3,ext_4 from h_data_manager where id=" + id + " and type='" + busiType + "'";
        HBusiDataManager manager = serviceUtils.getObjectByIdAndType(id, busiType);//jdbcTemplate.queryForObject(sql, HBusiDataManager.class);
        if (manager.getCust_id() == null || (!cust_id.equals(manager.getCust_id().toString()))) {
            throw new Exception("无权删除");
        }
        List<HBusiDataManager> list = serviceUtils.getDataList(BusiTypeEnum.SS.getType(), id);
        for (HBusiDataManager manager2 : list) {
            serviceUtils.deleteDatafromES(manager2.getType(), manager2.getId().toString());
        }
        serviceUtils.delDataListByPid(id);
        serviceUtils.deleteDatafromES(manager.getType(), manager.getId().toString());
        JSONObject json = JSONObject.parseObject(manager.getContent());
        Integer zid = json.getInteger("pid");
        totalPartDanToMainDan(zid, BusiTypeEnum.SZ.getType(), id);

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject
            params, List sqlParams) {
        String sql = null;
        //查询主列表
        if ("main".equals(params.getString("_rule_"))) {
            sqlParams.clear();
            StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from h_data_manager where type=?");
            if (!"all".equals(cust_id))
                sqlstr.append(" and cust_id='").append(cust_id).append("'");

            sqlParams.add(busiType);

            Iterator keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
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
    public void totalPartDanToMainDan(Integer zid, String type, Long id) {

        List<HBusiDataManager> data = serviceUtils.getDataList(BusiTypeEnum.SF.getType(), zid.longValue());
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

        String sql = "select id,type,content,ext_1,ext_2,ext_3,ext_4 from h_data_manager where id=" + zid + " and type='" + type + "'";
        HBusiDataManager manager = null;
        try {
            manager = serviceUtils.getObjectByIdAndType(NumberConvertUtil.parseLong(zid), type);
        } catch (EmptyResultDataAccessException e) {
            log.warn("查询主单:{},type:{}失败", zid, type);
        }

        String hcontent = manager.getContent();
        JSONObject jsonObject = JSONObject.parseObject(hcontent);
        jsonObject.put("weight_total", weightTotal);//总重量
        jsonObject.put("party_total", data.size() - 1 < 0 ? 0 : data.size() - 1);//分单总数
        Integer s = jsonObject.getInteger("low_price_goods");
        if (s == null) {
            s = 0;
        }
        jsonObject.put("low_price_goods", s + low_price_goods);
        manager.setContent(jsonObject.toJSONString());
        sql = " update h_data_manager set content='" + jsonObject.toJSONString() + "' where id=" + zid + " and type='" + type + "'";
        jdbcTemplate.update(sql);
        serviceUtils.updateDataToES(BusiTypeEnum.SZ.getType(), zid.toString(), jsonObject);

    }


}
