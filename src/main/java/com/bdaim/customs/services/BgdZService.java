package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HBusiDataManager;
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

    @Autowired
    private HBusiDataManagerDao hBusiDataManagerDao;

    @Autowired
    private ServiceUtils serviceUtils;

    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        // TODO Auto-generated method stub
        if (StringUtil.isNotEmpty(info.getString("fromSbzId"))) {
            HBusiDataManager h = serviceUtils.getObjectByIdAndType(info.getLong("fromSbzId"), BusiTypeEnum.SZ.getType());
            if (h == null) {
                throw new Exception("数据不存在");
            }
            if (!cust_id.equals(h.getCust_id().toString())) {
                throw new Exception("你无权处理");
            }

            List<HBusiDataManager> dataList = new ArrayList<>();
            if ("Y".equals(h.getExt_1())) {
                throw new Exception("已经提交过了,不能重复提交");
            }

            buildDanList(info, id, dataList, cust_id, cust_user_id, h, BusiTypeEnum.BZ.getType());
            int index = -1;
            for (int i = 0; i < dataList.size(); i++) {
                HBusiDataManager dm = dataList.get(i);
                serviceUtils.addDataToES(dm.getId().toString(), dm.getType(), JSON.parseObject(dm.getContent()));
                if (dm.getType().equals(BusiTypeEnum.BZ.getType())) {
                    index = i;
                }
            }
            if (index > -1) {
                dataList.remove(index);
            }
            if (dataList.size() > 0) {
                hBusiDataManagerDao.batchSaveOrUpdate(dataList);
            }
        }

    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        // 提交至海关平台
        if ("HAIGUAN".equals(info.getString("_rule_"))) {
            String sql = "select content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from h_data_manager where type=? and id=? ";
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

            sql = "UPDATE h_data_manager SET ext_1 = '1', ext_date1 = NOW(), content=? WHERE id = ?  AND type = ? AND ext_1 <>'1' ";
            jdbcTemplate.update(sql, jo.toJSONString(), id, BusiTypeEnum.SZ.getType());
            serviceUtils.updateDataToES(BusiTypeEnum.SZ.getType(), id.toString(), jo);

            //更新报关单分单信息
            String selectSql = "select content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from h_data_manager WHERE ( CASE WHEN JSON_VALID(content) THEN JSON_EXTRACT(content, '$.pid')=? ELSE null END  or CASE WHEN JSON_VALID(content) THEN JSON_EXTRACT(content, '$.pid')=? ELSE null END) AND type = ? AND ext_1 <>'1' ";
            List<Map<String, Object>> ds = jdbcTemplate.queryForList(selectSql, id, id, BusiTypeEnum.BF.getType());
            String updateSql = " UPDATE h_data_manager SET ext_1 = '1', ext_date1 = NOW(), content=? WHERE JSON_EXTRACT(content, '$.pid')=? AND type = ? AND ext_1 <>'1' ";
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
                jdbcTemplate.update(updateSql, jo.toJSONString(), id, BusiTypeEnum.BF.getType());
                serviceUtils.updateDataToES(BusiTypeEnum.SF.getType(), String.valueOf(m.get("id")), jo);
            }
        } else {
            HBusiDataManager dbManager = serviceUtils.getObjectByIdAndType(id, busiType);
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
    public void getInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) {
        if (StringUtil.isNotEmpty(param.getString("_rule_")) && param.getString("_rule_").startsWith("_export")) {
            info.put("export_type", 2);
            switch (param.getString("_rule_")) {
                case "_export_bgd_z_main_data":
                    List singles = queryChildData(BusiTypeEnum.BF.getType(), cust_id, cust_group_id, cust_user_id, id, info, param);
                    /*if (singles != null) {
                        List products = new ArrayList();
                        List tmp;
                        JSONObject js;
                        // 查询分单下的低价商品
                        for (int i = 0; i < singles.size(); i++) {
                            js = (JSONObject) singles.get(i);
                            tmp = queryChildData(BusiTypeEnum.BS.getType(), cust_id, cust_group_id, cust_user_id, js.getLong("id"), info, param);
                            if (tmp != null && tmp.size() > 0) {
                                products.addAll(tmp);
                            }
                        }
                        info.put("singles", products);
                    }*/

                    if (singles != null) {
                        info.put("singles", singles);
                        List products;
                        JSONObject js;
                        for (int i = 0; i < singles.size(); i++) {
                            js = (JSONObject) singles.get(i);
                            products = queryChildData(BusiTypeEnum.BS.getType(), cust_id, cust_group_id, cust_user_id, js.getLong("id"), info, param);
                            js.put("products", products);
                        }
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


    public void buildDanList(JSONObject info, Long id, List<HBusiDataManager> dataList, String custId, Long userId, HBusiDataManager h, String type) throws Exception {
        HBusiDataManager CZ = new HBusiDataManager();
        CZ.setType(BusiTypeEnum.BZ.getType());
        CZ.setId(id.intValue());
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
        dataList.add(h);

        CZ.setContent(info.toJSONString());
        dataList.add(CZ);
        List<HBusiDataManager> parties = serviceUtils.getDataList(BusiTypeEnum.SF.getType(), info.getLong("fromSbzId"));
        for (HBusiDataManager hp : parties) {
            HBusiDataManager hm = new HBusiDataManager();
            hm.setType(BusiTypeEnum.BF.getType());
            hm.setCreateDate(new Date());
            Long fid = sequenceService.getSeq(BusiTypeEnum.BF.getType());
            hm.setId(fid.intValue());
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
                good.setId(gid.intValue());
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

    private List queryChildData(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long pid, JSONObject info, JSONObject param) {
        List sqlParams = new ArrayList();
        StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from h_data_manager where type=?");
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
        sqlstr.append(" and ( CASE WHEN JSON_VALID(content) THEN JSON_EXTRACT(content, '$.pid')=? ELSE null END  or CASE WHEN JSON_VALID(content) THEN JSON_EXTRACT(content, '$.pid')=? ELSE null END)");
        sqlParams.add(pid);
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
