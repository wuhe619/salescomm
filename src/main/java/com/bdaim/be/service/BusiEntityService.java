package com.bdaim.be.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.BusiMetaConfig;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.common.spring.SpringContextHelper;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.customs.services.SbdZService;
import com.bdaim.customs.utils.ServiceUtils;
import com.bdaim.util.StringUtil;
import net.sf.json.JSONArray;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 通用业务实体服务
 */
@Service
public class BusiEntityService {
    private static Logger logger = LoggerFactory.getLogger(BusiEntityService.class);

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private ServiceUtils serviceUtils;

    /*
     * 按ID获取记录
     */
    public JSONObject doInfo(String cust_id, String cust_group_id, Long cust_user_id, String busiType, Long id, JSONObject param) throws Exception {
        JSONObject jo = new JSONObject();
        logger.info("开始");
        long startTime = System.currentTimeMillis();
        String sql = "select content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=? and id=? ";
        if (!"all".equals(cust_id))
            sql += " and cust_id='" + cust_id + "'";


        Map data = null;
        try {
            data = jdbcTemplate.queryForMap(sql, busiType, id);
        } catch (EmptyResultDataAccessException e) {
            logger.warn("查询:{},busiType:{}失败,数据不存在", id, busiType);
            throw new TouchException("1000", "未查询到数据:[" + busiType + "]" + id);
        }
        if (data == null)
            return jo;
        String content = (String) data.get("content");
        try {
            if ("express_trajectory".equals(busiType)) {
                JSONArray jsonarray = JSONArray.fromObject(content);
                for (int i = 0; i < jsonarray.size(); i++) {
                    net.sf.json.JSONObject jsonObject = jsonarray.getJSONObject(i);
                    String upload_time = jsonObject.getString("upload_Time");
                    jo.put(upload_time, jsonarray.get(i));
                }
            } else {
                jo = JSONObject.parseObject(content);
                jo.put("id", id);
                jo.put("cust_id", data.get("cust_id"));
                jo.put("cust_group_id", data.get("cust_group_id"));
                jo.put("cust_user_id", data.get("cust_user_id"));
                jo.put("create_id", data.get("create_id"));
                jo.put("create_date", data.get("create_date"));
                jo.put("update_id", data.get("update_id"));
                jo.put("update_date", data.get("update_date"));
                if (data.get("ext_1") != null && !"".equals(data.get("ext_1")))
                    jo.put("ext_1", data.get("ext_1"));
                if (data.get("ext_2") != null && !"".equals(data.get("ext_2")))
                    jo.put("ext_2", data.get("ext_2"));
                if (data.get("ext_3") != null && !"".equals(data.get("ext_3")))
                    jo.put("ext_3", data.get("ext_3"));
                if (data.get("ext_4") != null && !"".equals(data.get("ext_4")))
                    jo.put("ext_4", data.get("ext_4"));
                if (data.get("ext_5") != null && !"".equals(data.get("ext_5")))
                    jo.put("ext_5", data.get("ext_5"));
                long endTime1 = System.currentTimeMillis();
                logger.info("查询耗时:" + (endTime1 - startTime));
                //执行自定义单数据规则
                BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_" + busiType);
                if (busiService != null)
                    busiService.doInfo(busiType, cust_id, cust_group_id, cust_user_id, id, jo, param);
                long endTime2 = System.currentTimeMillis();
                if (!"SBDCHECK".equals(param.getString("_rule_"))) {
                    //查询场站和报关单位
                    serviceUtils.getStationCustName(jo);
                    // 查询字典数据
                    serviceUtils.getHDicData(jo);
                }
            }
        } catch (TouchException e) {
            logger.error("数据格式错误！", e);
            throw new TouchException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("数据格式错误！", e);
            throw new Exception("数据格式错误！");
        } finally {
            long endTime = System.currentTimeMillis();
            logger.info("耗时:" + (endTime - startTime));
        }

        return jo;
    }

    /*
     * 查询记录
     */
    public Page query(String cust_id, String cust_group_id, Long cust_user_id, String busiType, JSONObject params) throws Exception {
        org.apache.commons.lang.time.StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        logger.info("开始计时：");
        Page p = new Page();
        String stationId = params.getString("stationId");
        String _orderby_ = params.getString("_orderby_");
        String _sort_ = params.getString("_sort_");
        List sqlParams = new ArrayList();

        BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_" + busiType);
        String sql = null;
        try {
            //执行自定义查询sql
            sql = busiService.formatQuery(busiType, cust_id, cust_group_id, cust_user_id, params, sqlParams);
            if (StringUtil.isNotEmpty(sql) && !sql.startsWith("select")) {
                Page page = new Page();
                List list = new ArrayList();
                list.add(sql);
                page.setData(list);
                return page;
            }
            if ("1".equals(sql)) return null;
        } catch (Exception e) {
            logger.error("查询条件自定义解析异常:[" + busiType + "]", e);
            throw new Exception("查询条件自定义解析异常:[" + busiType + "]");
        }
        if (sql == null || "".equals(sql)) {
            sqlParams.clear();
            StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5,update_date from " + HMetaDataDef.getTable(busiType, "") + " where type=?");
            if (!"all".equals(cust_id))
                sqlstr.append(" and cust_id='").append(cust_id).append("'");
            sqlParams.add(busiType);
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
                if ("pageNum".equals(key) || "pageSize".equals(key) || "stationId".equals(key)
                        || "cust_id".equals(key) || "_sort_".equals(key) || "_orderby_".equals(key)
                        || "_rule_".equals(key))
                    continue;
                if ("cust_id".equals(key)) {
                    sqlstr.append(" and cust_id=?");
                } else if ("pid".equals(key)) {
                    sqlstr.append(" AND " + BusiMetaConfig.getFieldIndex(busiType, key) + "=(SELECT ext_3 FROM " + HMetaDataDef.getTable(BusiTypeEnum.getParentType(busiType), "") + " WHERE id = ?)");
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
                } else if (key.startsWith("_eq_")) {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(4) + "') = ?");
                } else if (key.startsWith("_range_")) {
                    if ("0".equals(String.valueOf(params.get(key)))) {
                        sqlstr.append(" and ( JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(7) + "') <= ?")
                                .append(" OR JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(7) + "') = '' ")
                                .append(" OR JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(7) + "') IS NULL ) ");
                    } else {
                        sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(7) + "') >= ?");
                    }
                } else {
                    sqlstr.append(" and " + BusiMetaConfig.getFieldIndex(busiType, key) + "=?");
                }
                sqlParams.add(params.get(key));
            }

            sql = sqlstr.toString();
        }
        if (StringUtil.isNotEmpty(_orderby_) && StringUtil.isNotEmpty(_sort_)) {
            sql += " order by  "+ _orderby_+" "+_sort_+" ";
            //sqlParams.add(_orderby_);
            //sqlParams.add(_sort_);
        }
        int pageNum = 1;
        int pageSize = 10;
        try {
            pageNum = params.getIntValue("pageNum");
        } catch (Exception e) {
        }
        try {
            pageSize = params.getIntValue("pageSize");
        } catch (Exception e) {
        }
        if (pageNum <= 0)
            pageNum = 1;
        if (pageSize <= 0)
            pageSize = 10;
        if (pageSize > 10000)
            pageSize = 10000;

        stopwatch.split();
        logger.info("buildSql," + stopwatch.getSplitTime() + "," + stopwatch.toSplitString());
        sql += " limit ? , ? ";
        sqlParams.add((pageNum - 1) * pageSize);
        sqlParams.add(pageSize);
        try {
            logger.info("querySql:{}", sql);
            logger.info("queryParam:{}", JSON.toJSONString(sqlParams));
            logger.info("开始查询...");
            List<Map<String, Object>> ds = jdbcTemplate.queryForList(sql, sqlParams.toArray());
            stopwatch.split();
            logger.info("查询耗时:" + stopwatch.getSplitTime() + "," + stopwatch.toSplitString());
            String totalSql = "select count(0) from ( " + sql + " ) t ";
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
                    logger.error(e.getMessage());
                }
                // 只获取ID
                if ("get_id".equals(String.valueOf(params.get("_rule_")))) {
                    jo = null;
                }
                if (jo == null) { //jo异常导致为空时，只填充id
                    jo = new JSONObject();
                    jo.put("id", m.get("id"));
                } else {
                    //查询场站和报关单位
                    serviceUtils.getStationCustName(jo);
                    // 查询字典数据
                    serviceUtils.getHDicData(jo);
                }
                stopwatch.split();
                logger.info("查询耗时2:" + stopwatch.getSplitTime() + "," + stopwatch.toSplitString());
                try {
                    //执行自定义查询结果格式化
                    busiService.formatInfo(busiType, cust_id, cust_group_id, cust_user_id, jo);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }

                data.add(jo);
            }
            stopwatch.split();
            logger.info("处理数据:" + stopwatch.getSplitTime() + "," + stopwatch.toSplitString());
            p.setData(data);
            int total = jdbcTemplate.queryForObject(totalSql, sqlParams.toArray(), Integer.class);
            p.setTotal(total);
            p.setPerPageCount(pageSize);
            p.setStart((pageNum - 1) * pageSize + 1);
            stopwatch.split();
            logger.info("处理数据2:" + stopwatch.getSplitTime() + "," + stopwatch.toSplitString());
        } catch (Exception e) {
            logger.error("查询异常:[" + busiType + "]", e);
            throw new Exception("查询异常:[" + busiType + "]");
        }
        stopwatch.split();
        logger.info("处理数据end:" + stopwatch.getSplitTime() + "," + stopwatch.toSplitString());
        stopwatch.stop();
        return p;
    }

    /*
     * 保存记录
     */
    @Transactional
    public Long saveInfo(String cust_id, String cust_group_id, Long cust_user_id, String busiType, Long id, JSONObject info) throws Exception {
        String[] extKeys = new String[]{"ext_1", "ext_2", "ext_3", "ext_4", "ext_5", "cust_user_id"};
        String[] sysKeys = new String[]{"id", "cust_id", "create_id", "create_date"}; //系统数据字段名
        for (String sysKey : sysKeys) {
            if (info.containsKey(sysKey))
                info.remove(sysKey);
        }
        String extData = info.toJSONString();
        for (String extKey : extKeys) {
            if (info.containsKey(extKey))
                info.remove(extKey);
        }

        JSONObject jo = null;

        if (id == null || id == 0) {
            //insert
            id = sequenceService.getSeq(busiType);
            jo = info;
            String sql1 = "insert into " + HMetaDataDef.getTable(busiType, "") + "(id, type, content, cust_id, cust_group_id, cust_user_id, create_id, create_date, ext_1, ext_2, ext_3, ext_4, ext_5 ) value(?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?, ?)";
            try {
                //执行自定义新增规则
                BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_" + busiType);
                busiService.insertInfo(busiType, cust_id, cust_group_id, cust_user_id, id, jo);

                if (jo.containsKey("_rule_"))
                    jo.remove("_rule_");
                //JSONObject jsonObject = JSON.parseObject(extData);
                jdbcTemplate.update(sql1, id, busiType, info.toJSONString(), cust_id, cust_group_id, cust_user_id, cust_user_id
                        , jo.containsKey("ext_1") ? jo.getString("ext_1") : ""
                        , jo.containsKey("ext_2") ? jo.getString("ext_2") : ""
                        , jo.containsKey("ext_3") ? jo.getString("ext_3") : ""
                        , jo.containsKey("ext_4") ? jo.getString("ext_4") : ""
                        , jo.containsKey("ext_5") ? jo.getString("ext_5") : "");
            } catch (TouchException e) {
                logger.warn("添加记录异常:[" + busiType + "]" + id, e);
                throw e;
            } catch (Exception e) {
                logger.error("添加新记录异常:[" + busiType + "]", e);
                throw new Exception("添加新记录异常:[" + busiType + "]", e);
            }
        } else {
            // update
            Map data = null;
            try {
                data = jdbcTemplate.queryForMap("select content from " + HMetaDataDef.getTable(busiType, "") + " where type=? and cust_id=? and id=?", busiType, cust_id, id);
            } catch (DataAccessException e) {
                logger.error("未查询到数据:[" + busiType + "]" + id, e);
                throw new TouchException("1000", "未查询到数据:[" + busiType + "]" + id);
            } catch (Exception e) {
                logger.error("读取数据异常:[" + busiType + "]" + id, e);
                throw new Exception("读取数据异常:[" + busiType + "]" + id, e);
            }
            if (data == null) {
                logger.warn("数据不存在:[" + busiType + "]" + id);
                throw new Exception("数据不存在:[" + busiType + "]" + id);
            }

            String content = (String) data.get("content");
            if (content == null || "".equals(content))
                content = "{}";

            try {
                jo = JSONObject.parseObject(content);
                JSONObject jsonObject = JSON.parseObject(extData);
                if (jsonObject.containsKey("ext_1")) {
                    jo.put("ext_1", jsonObject.get("ext_1"));
                }
                Iterator keys = info.keySet().iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    jo.put(key, info.get(key));
                }
            } catch (Exception e) {
                logger.error("解析数据异常:[" + busiType + "]" + id, e);
                throw new Exception("解析数据异常:[" + busiType + "]" + id);
            }

            try {
                //执行自定义更新规则
                BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_" + busiType);
                busiService.updateInfo(busiType, cust_id, cust_group_id, cust_user_id, id, jo);

                StringBuffer sql2 = new StringBuffer("update " + HMetaDataDef.getTable(busiType, "") + " set update_id=?,update_date=now() ");
                List sqlParams = new ArrayList();
                sqlParams.add(cust_user_id);

                if (jo.containsKey("_rule_"))
                    jo.remove("_rule_");
                for (String extKey : extKeys) {
                    if (jo.containsKey(extKey)) {
                        sql2.append(",").append(extKey).append("=?");
                        sqlParams.add(jo.getString(extKey));
                        jo.remove(extKey);
                    }
                }
                sql2.append(",content=?  where type=? and cust_id=? and id=?");
                sqlParams.add(jo.toJSONString());
                sqlParams.add(busiType);
                sqlParams.add(cust_id);
                sqlParams.add(id);

                jdbcTemplate.update(sql2.toString(), sqlParams.toArray());
            } catch (TouchException e) {
                logger.warn("更新记录异常:[" + busiType + "]" + id, e);
                throw e;
            } catch (EmptyResultDataAccessException e) {
                logger.warn("更新记录异常:[" + busiType + "]" + id, e);
                throw new TouchException("1000", "未查询到数据:[" + busiType + "]" + id);
            } catch (Exception e) {
                logger.error("更新记录异常:[" + busiType + "]" + id, e);
                throw new Exception("更新记录异常:[" + busiType + "]" + id);
            }
        }

        return id;
    }

    /**
     * 删除记录
     */
    @Transactional
    public void deleteInfo(String cust_id, String cust_group_id, Long cust_user_id, String busiType, Long id) throws Exception {
        String sql = "delete from " + HMetaDataDef.getTable(busiType, "") + " where type=? and cust_id=? and id=?";
        try {
            //执行自定义删除规则
            BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_" + busiType);
            busiService.deleteInfo(busiType, cust_id, cust_group_id, cust_user_id, id);

            jdbcTemplate.update(sql, busiType, cust_id, id);

        } catch (TouchException e) {
            logger.warn("删除记录异常:[" + busiType + "]" + id, e);
            throw e;
        } catch (Exception e) {
            logger.error("删除记录异常:[" + busiType + "]" + id, e);
            throw new Exception("删除记录异常:[" + busiType + "]" + id);
        }
    }


}
