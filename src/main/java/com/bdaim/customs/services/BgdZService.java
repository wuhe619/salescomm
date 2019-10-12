package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.Constants;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.customs.utils.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/***
 * 报关单.主单
 */
@Service("busi_bgd_z")
public class BgdZService implements BusiService {

    private static Logger log = LoggerFactory.getLogger(BgdZService.class);

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private CustomerDao customerDao;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SequenceService sequenceService;

//    @Autowired
//    private HBusiDataManagerDao hBusiDataManagerDao;

    @Autowired
    private ServiceUtils serviceUtils;

    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        // TODO Auto-generated method stub
        if (StringUtil.isNotEmpty(info.getString("fromSbzId"))) {
            HBusiDataManager h = serviceUtils.getObjectByIdAndType(cust_id, info.getLong("fromSbzId"), BusiTypeEnum.SZ.getType());
            if (h == null) {
                throw new TouchException("数据不存在");
            }
            if (!cust_id.equals(h.getCust_id().toString())) {
                throw new TouchException("无权处理");
            }

            List<HBusiDataManager> dataList = new ArrayList<>();
            if ("Y".equals(h.getExt_1())) {
                throw new TouchException("已经提交过了,不能重复提交");
            }

            buildDanList0(info, id, dataList, cust_id, cust_user_id, h, BusiTypeEnum.BZ.getType());
            int index = -1;
            List<JSONObject> fdData = new ArrayList();
            List<JSONObject> sData = new ArrayList();
            JSONObject content, json;
            HBusiDataManager dm;
            for (int i = 0; i < dataList.size(); i++) {
                dm = dataList.get(i);
                json = JSON.parseObject(JSON.toJSONString(dm));
                content = JSON.parseObject(dm.getContent());
                content.remove("products");
                //serviceUtils.addDataToES(dm.getId().toString(), dm.getType(), JSON.parseObject(dm.getContent()));
                if (dm.getType().equals(BusiTypeEnum.BZ.getType())) {
                    index = i;
                    serviceUtils.addDataToES(dm.getId().toString(), dm.getType(), JSON.parseObject(dm.getContent()));
                } else if (dm.getType().equals(BusiTypeEnum.BF.getType())) {
                    json.putAll(content);
                    fdData.add(json);
                    //fdData.add(JSON.parseObject(dm.getContent()));
                } else if (dm.getType().equals(BusiTypeEnum.BS.getType())) {
                    json.putAll(content);
                    sData.add(json);
                }
            }
            if (fdData.size() > 0) {
                elasticSearchService.bulkInsertDocument(BusiTypeEnum.getEsIndex(BusiTypeEnum.BF.getType()), Constants.INDEX_TYPE, fdData);
            }
            if (sData.size() > 0) {
                elasticSearchService.bulkInsertDocument(BusiTypeEnum.getEsIndex(BusiTypeEnum.BS.getType()), Constants.INDEX_TYPE, sData);
            }
            if (index > -1) {
                dataList.remove(index);
            }
            if (dataList.size() > 0) {
                //hBusiDataManagerDao.batchSaveOrUpdate(dataList);

                Map<String, List<HBusiDataManager>> datamap = new TreeMap<>();
                for (HBusiDataManager manager : dataList) {
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
                    serviceUtils.batchInsert(key, d);
                }
            }
        }

    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        // 提交至海关平台
        if ("HAIGUAN".equals(info.getString("_rule_"))) {
            String sql = "select content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=? and id=? ";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, busiType, id);
            if (list.size() == 0) {
                log.warn("报关单主单数据不存在[" + busiType + "]" + id);
                throw new TouchException("1000", "报关单主单数据不存在");
            }
            Map m = list.get(0);
            //Map m = jdbcTemplate.queryForMap(sql, busiType, id);
            String cdContent = String.valueOf(m.get("content"));
            if ("1".equals(String.valueOf(m.get("ext_1"))) && StringUtil.isNotEmpty(cdContent)
                    && "1.".equals(JSON.parseObject(cdContent).getString("send_status"))) {
                log.warn("报关单主单:[" + id + "]已提交至海关");
                throw new TouchException("报关单主单:[" + id + "]已提交至海关");
            }
            // 更新报关单主单信息
            String content = (String) m.get("content");
            JSONObject jo = JSONObject.parseObject(content);
            jo.put("ext_1", "1");
            jo.put("send_status", "1");
            info.put("ext_1", "1");
            info.put("send_status", "1");
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

            sql = "UPDATE " + HMetaDataDef.getTable(busiType, "") + " SET ext_1 = '1', ext_date1 = NOW(), content=? WHERE id = ?  AND type = ? AND IFNULL(ext_1,'') <>'1' ";
            jdbcTemplate.update(sql, jo.toJSONString(), id, BusiTypeEnum.BZ.getType());
            serviceUtils.updateDataToES(BusiTypeEnum.BZ.getType(), id.toString(), jo);

            //更新报关单分单信息
            //String selectSql = "select id, type, content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(BusiTypeEnum.BF.getType(), "") + " WHERE ( CASE WHEN JSON_VALID(content) THEN JSON_EXTRACT(content, '$.pid')=? ELSE null END  or CASE WHEN JSON_VALID(content) THEN JSON_EXTRACT(content, '$.pid')=? ELSE null END) AND type = ? AND IFNULL(ext_1,'') <>'1' ";
            String selectSql = "select id, type, content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(BusiTypeEnum.BF.getType(), "") + " WHERE ext_4=(SELECT ext_3 FROM " + HMetaDataDef.getTable(BusiTypeEnum.getParentType(busiType), "") + " WHERE id = ?) AND type = ? AND IFNULL(ext_1,'') <>'1' ";
            List<Map<String, Object>> ds = jdbcTemplate.queryForList(selectSql, id, BusiTypeEnum.BF.getType());
            String updateSql = " UPDATE " + HMetaDataDef.getTable(BusiTypeEnum.BF.getType(), "") + " SET ext_1 = '1', ext_date1 = NOW(), content=? WHERE id=? AND type = ? AND IFNULL(ext_1,'') <>'1' ";
            for (int i = 0; i < ds.size(); i++) {
                m = ds.get(i);
                content = (String) m.get("content");
                jo = JSONObject.parseObject(content);
                jo.put("ext_1", "1");
                jo.put("send_status", "1");
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
                jdbcTemplate.update(updateSql, jo.toJSONString(), m.get("id"), BusiTypeEnum.BF.getType());
                serviceUtils.updateDataToES(BusiTypeEnum.BF.getType(), String.valueOf(m.get("id")), jo);
            }
        } else {
            HBusiDataManager dbManager = serviceUtils.getObjectByIdAndType(cust_id, id, busiType);
            if (dbManager == null) {
                throw new TouchException("无权操作");
            }
            String content = dbManager.getContent();
            JSONObject json = JSONObject.parseObject(content);
            Iterator keys = info.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                json.put(key, info.get(key));
            }
            serviceUtils.updateDataToES(busiType, id.toString(), json);
        }

    }


    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) {
        if (StringUtil.isNotEmpty(param.getString("_rule_")) && param.getString("_rule_").startsWith("_export")) {
            info.put("export_type", 2);
            switch (param.getString("_rule_")) {
                case "_export_bgd_z_main_data":
                    List singles = queryChildData(BusiTypeEnum.BF.getType(), cust_id, cust_group_id, cust_user_id, id, info, param);

                    if (singles != null) {
                        info.put("singles", singles);
                        JSONObject js, product;
                        String main_bill_no = "";
                        List partyBillNos = new ArrayList();
                        for (int i = 0; i < singles.size(); i++) {
                            js = (JSONObject) singles.get(i);
                            js.put("index", i + 1);
                            partyBillNos.add(js.getString("bill_no"));
                            main_bill_no = js.getString("main_bill_no");
                        }
                        List products = serviceUtils.listSdByBillNos(cust_id, BusiTypeEnum.BS.getType(), main_bill_no, partyBillNos, param);
                        JSONObject content;
                        for (int j = 0; j < products.size(); j++) {
                            product = (JSONObject) products.get(j);
                            content = JSON.parseObject(product.getString("content"));
                            product.putAll(content);
                            product.put("index", j + 1);
                            product.put("bill_no", product.getString("ext_4"));
                            product.put("main_bill_no", main_bill_no);
                        }
                        info.put("products", products);
                    }
            }

        }

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

    @Deprecated
    public void buildDanList(JSONObject info, Long id, List<HBusiDataManager> dataList, String custId, Long userId, HBusiDataManager h, String type) throws Exception {
        HBusiDataManager CZ = new HBusiDataManager();
        CZ.setType(BusiTypeEnum.BZ.getType());
        CZ.setId(id);
        CZ.setCreateDate(new Date());
        CZ.setCust_id(Long.valueOf(custId));
        CZ.setCreateId(Long.valueOf(userId));
        CZ.setExt_3(h.getExt_3());
        CZ.setExt_1("0");//未发送 1，已发送


        JSONObject json = JSON.parseObject(h.getContent());
        json.put("create_id", userId);
        json.put("cust_id", custId);
        json.put("type", CZ.getType());
        json.put("create_date", CZ.getCreateDate());
        json.put("send_status", CZ.getExt_1());
        json.put("commit_baodan_status", "Y");

        Iterator keys = json.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            info.put(key, json.get(key));
        }
        info.put("ext_3", h.getExt_3());
        info.put("ext_1", "0");

        JSONObject jon = JSON.parseObject(h.getContent());
        jon.put("commit_baodan_status", "Y");
        h.setExt_1("Y");
        h.setContent(jon.toJSONString());
//        dataList.add(h);
        String sql = "update " + HMetaDataDef.getTable(h.getType(), "") + " set content='" + jon.toJSONString() + "'"
                + " ,ext_1='Y'"
                + " where id=" + h.getId() + " and type='" + h.getType() + "'";
        jdbcTemplate.update(sql);
        CZ.setContent(info.toJSONString());
        dataList.add(CZ);
        List<HBusiDataManager> parties = serviceUtils.getDataList(BusiTypeEnum.SF.getType(), info.getLong("fromSbzId"));
        for (HBusiDataManager hp : parties) {
            HBusiDataManager hm = new HBusiDataManager();
            hm.setType(BusiTypeEnum.BF.getType());
            hm.setCreateDate(new Date());
            Long fid = sequenceService.getSeq(BusiTypeEnum.BF.getType());
            hm.setId(fid);
            hm.setExt_3(hp.getExt_3());
            hm.setExt_4(hp.getExt_4());
            hm.setCreateId(hp.getCreateId());
            hm.setCust_id(hp.getCust_id());
            JSONObject _content = JSON.parseObject(hp.getContent());
            _content.put("pid", id);
            hm.setContent(_content.toJSONString());
            dataList.add(hm);
            List<HBusiDataManager> goods = serviceUtils.getDataList(BusiTypeEnum.SS.getType(), hp.getId().longValue());
            int index = 1;
            for (HBusiDataManager gp : goods) {
                HBusiDataManager good = new HBusiDataManager();
                gp.setType(BusiTypeEnum.BS.getType());
                Long gid = sequenceService.getSeq(BusiTypeEnum.BS.getType());
                good.setId(gid);
                good.setCreateDate(new Date());
                JSONObject __content = JSON.parseObject(gp.getContent());
                __content.put("pid", fid);
                _content.put("index", index);
                good.setContent(__content.toJSONString());
                good.setType(BusiTypeEnum.BS.getType());
                good.setCreateId(gp.getCreateId());
                good.setCust_id(gp.getCust_id());
                good.setExt_3(gp.getExt_3());
                good.setExt_4(gp.getExt_4());
                good.setExt_5(String.valueOf(index));//商品序号
                index++;
                dataList.add(good);
            }
        }
    }

    public void buildDanList0(JSONObject info, Long id, List<HBusiDataManager> dataList, String custId, Long userId, HBusiDataManager h, String type) throws Exception {
        HBusiDataManager bgdMain = new HBusiDataManager();
        bgdMain.setType(BusiTypeEnum.BZ.getType());
        bgdMain.setId(id);
        bgdMain.setCreateDate(new Date());
        bgdMain.setCust_id(Long.valueOf(custId));
        bgdMain.setCreateId(Long.valueOf(userId));
        bgdMain.setExt_3(h.getExt_3());
        bgdMain.setExt_1("0");//未发送 1，已发送


        JSONObject json = JSON.parseObject(h.getContent());
        json.put("create_id", userId);
        json.put("cust_id", custId);
        json.put("type", bgdMain.getType());
        json.put("create_date", bgdMain.getCreateDate());
        json.put("send_status", bgdMain.getExt_1());
        json.put("commit_baodan_status", "Y");

        Iterator keys = json.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            info.put(key, json.get(key));
        }
        info.put("ext_3", h.getExt_3());
        info.put("ext_1", "0");

        JSONObject jon = JSON.parseObject(h.getContent());
        jon.put("commit_baodan_status", "Y");
        h.setExt_1("Y");
        h.setContent(jon.toJSONString());
//        dataList.add(h);
        String sql = "update " + HMetaDataDef.getTable(h.getType(), "") + " set content='" + jon.toJSONString() + "'"
                + " ,ext_1='Y'"
                + " where id=" + h.getId() + " and type='" + h.getType() + "'";
        jdbcTemplate.update(sql);
        bgdMain.setContent(info.toJSONString());
        dataList.add(bgdMain);

        /*List<HBusiDataManager> parties = serviceUtils.getDataList(BusiTypeEnum.SF.getType(), info.getLong("fromSbzId"));
        // 预先生成分单ID
        long size = parties.size();
        long maxId = sequenceService.getSeq(BusiTypeEnum.BF.getType(), size);

        // 预先生成商品ID
        long sSize = 0L;
        Map<Long, List> cache = new HashMap<>();
        for (HBusiDataManager hp : parties) {
            List<HBusiDataManager> goods = serviceUtils.getDataList(BusiTypeEnum.SS.getType(), hp.getId().longValue());
            cache.put(hp.getId(), goods);
            sSize += goods.size();
        }
        long sMaxId = sequenceService.getSeq(BusiTypeEnum.BS.getType(), sSize);*/

        // 根据主单号查询申报单分单列表
        List<HBusiDataManager> parties = serviceUtils.listDataByParentBillNo(custId, BusiTypeEnum.SF.getType(), h.getExt_3());
        List<String> billNos = new ArrayList<>();
        for (HBusiDataManager hp : parties) {
            billNos.add(hp.getExt_3());
        }
        // 查询所有分单下的税单
        List<JSONObject> jsonObjects = serviceUtils.listSdByBillNos(custId, BusiTypeEnum.SS.getType(), h.getExt_3(), billNos, new JSONObject());
        List<HBusiDataManager> goods = JSON.parseArray(JSON.toJSONString(jsonObjects), HBusiDataManager.class);
        Map<Long, List> cache = new HashMap<>();
        JSONObject fd = null;
        List<HBusiDataManager> tmp;
        for (HBusiDataManager p : goods) {
            fd = JSON.parseObject(p.getContent());
            if (fd != null) {
                tmp = cache.get(fd.getLong("pid"));
                if (tmp == null) {
                    tmp = new ArrayList<>();
                }
                tmp.add(p);
                cache.put(fd.getLong("pid"), tmp);
            }
        }
        List<HBusiDataManager> goodList;
        HBusiDataManager good, hm;
        for (HBusiDataManager hp : parties) {
            hm = new HBusiDataManager();
            hm.setType(BusiTypeEnum.BF.getType());
            hm.setCreateDate(new Date());
            Long fid = sequenceService.getSeq(BusiTypeEnum.BF.getType());
            hm.setId(fid);
            hm.setExt_2(hp.getExt_2());
            hm.setExt_3(hp.getExt_3());
            hm.setExt_4(hp.getExt_4());
            hm.setCreateId(hp.getCreateId());
            hm.setCust_id(hp.getCust_id());
            JSONObject _content = JSON.parseObject(hp.getContent());
            _content.put("pid", id);
            hm.setContent(_content.toJSONString());
            dataList.add(hm);
            goodList = cache.get(hp.getId());
            if (goodList != null) {
                int index = 1;
                for (HBusiDataManager gp : goodList) {
                    good = new HBusiDataManager();
                    gp.setType(BusiTypeEnum.BS.getType());
                    Long gid = sequenceService.getSeq(BusiTypeEnum.BS.getType());
                    good.setId(gid);
                    good.setCreateDate(new Date());
                    JSONObject sdContent = JSON.parseObject(gp.getContent());
                    sdContent.put("pid", hp.getId());
                    _content.put("index", index);
                    sdContent.put("opt_type", "ADD");
                    good.setContent(sdContent.toJSONString());
                    good.setType(BusiTypeEnum.BS.getType());
                    good.setCreateId(gp.getCreateId());
                    good.setCust_id(gp.getCust_id());
                    good.setExt_2(gp.getExt_2());
                    good.setExt_3(gp.getExt_3());
                    good.setExt_4(gp.getExt_4());
                    good.setExt_5(String.valueOf(index));//商品序号
                    index++;
                    dataList.add(good);
                    //sSize--;
                }
            }
            //size--;
        }
    }

    private List queryChildData(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long pid, JSONObject info, JSONObject param) {
        List sqlParams = new ArrayList();
        StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=?");
        if (!"all".equals(cust_id))
            sqlstr.append(" and cust_id='").append(cust_id).append("'");

        sqlParams.add(busiType);
        Iterator keys = param.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if ("".equals(String.valueOf(param.get(key)))) continue;
            if ("pageNum".equals(key) || "pageSize".equals(key) || "stationId".equals(key) || "cust_id".equals(key) || "_rule_".equals(key))
                continue;
            if ("cust_id".equals(key)) {
                sqlstr.append(" and cust_id=?");
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
        //sqlstr.append(" and ( CASE WHEN JSON_VALID(content) THEN JSON_EXTRACT(content, '$.pid')=? ELSE null END  or CASE WHEN JSON_VALID(content) THEN JSON_EXTRACT(content, '$.pid')=? ELSE null END)");
        sqlstr.append(" and ext_4=(SELECT ext_3 FROM " + HMetaDataDef.getTable(BusiTypeEnum.getParentType(busiType), "") + " WHERE id = ?)");
        //sqlParams.add(pid);
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
