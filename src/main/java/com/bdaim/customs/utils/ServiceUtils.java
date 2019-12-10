package com.bdaim.customs.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.common.BusiMetaConfig;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customs.entity.*;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.supplier.dto.SupplierEnum;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.SqlAppendUtil;
import com.bdaim.util.StringUtil;
import io.searchbox.core.SearchResult;
import net.sf.json.xml.XMLSerializer;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServiceUtils {

    private static Logger log = LoggerFactory.getLogger(ServiceUtils.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private SequenceService sequenceService;
    @Autowired
    private SourceDao sourceDao;
    @Autowired
    private HDicUtil dicUtil;
    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private MarketResourceService marketResourceService;

    @Autowired
    private MarketResourceDao marketResourceDao;

    public void addDataToES(String id, String type, JSONObject content) {
        if (type.equals(BusiTypeEnum.SZ.getType())) {
            elasticSearchService.addDocumentToType(Constants.SZ_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.CZ.getType())) {
            elasticSearchService.addDocumentToType(Constants.CZ_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.BZ.getType())) {
            elasticSearchService.addDocumentToType(Constants.BZ_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.SF.getType())) {
            elasticSearchService.addDocumentToType(Constants.SF_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.CF.getType())) {
            elasticSearchService.addDocumentToType(Constants.CF_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.BF.getType())) {
            elasticSearchService.addDocumentToType(Constants.BF_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.SS.getType())) {
            elasticSearchService.addDocumentToType(Constants.SS_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.CS.getType())) {
            elasticSearchService.addDocumentToType(Constants.CS_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.BS.getType())) {
            elasticSearchService.addDocumentToType(Constants.BS_INFO_INDEX, "haiguan", id, content);
        }
    }

    /**
     * 更新es
     *
     * @param type
     * @param id
     * @param content
     */
    public void updateDataToES(String type, String id, JSONObject content) {
        if (type.equals(BusiTypeEnum.SZ.getType())) {
            elasticSearchService.updateDocumentToType(Constants.SZ_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.CZ.getType())) {
            elasticSearchService.updateDocumentToType(Constants.CZ_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.BZ.getType())) {
            elasticSearchService.updateDocumentToType(Constants.BZ_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.SF.getType())) {
            elasticSearchService.updateDocumentToType(Constants.SF_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.CF.getType())) {
            elasticSearchService.updateDocumentToType(Constants.CF_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.BF.getType())) {
            elasticSearchService.updateDocumentToType(Constants.BF_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.SS.getType())) {
            elasticSearchService.updateDocumentToType(Constants.SS_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.CS.getType())) {
            elasticSearchService.updateDocumentToType(Constants.CS_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.BS.getType())) {
            elasticSearchService.updateDocumentToType(Constants.BS_INFO_INDEX, "haiguan", id, content);
        }

    }

    /**
     * 从es删除文档
     *
     * @param type
     * @param id
     */
    public void deleteDatafromES(String type, String id) {
        if (type.equals(BusiTypeEnum.SZ.getType())) {
            elasticSearchService.deleteDocumentFromType(Constants.SZ_INFO_INDEX, "haiguan", id);
        } else if (type.equals(BusiTypeEnum.CZ.getType())) {
            elasticSearchService.deleteDocumentFromType(Constants.CZ_INFO_INDEX, "haiguan", id);
        } else if (type.equals(BusiTypeEnum.BZ.getType())) {
            elasticSearchService.deleteDocumentFromType(Constants.BZ_INFO_INDEX, "haiguan", id);
        } else if (type.equals(BusiTypeEnum.SF.getType())) {
            elasticSearchService.deleteDocumentFromType(Constants.SF_INFO_INDEX, "haiguan", id);
        } else if (type.equals(BusiTypeEnum.CF.getType())) {
            elasticSearchService.deleteDocumentFromType(Constants.CF_INFO_INDEX, "haiguan", id);
        } else if (type.equals(BusiTypeEnum.BF.getType())) {
            elasticSearchService.deleteDocumentFromType(Constants.BF_INFO_INDEX, "haiguan", id);
        } else if (type.equals(BusiTypeEnum.SS.getType())) {
            elasticSearchService.deleteDocumentFromType(Constants.SS_INFO_INDEX, "haiguan", id);
        } else if (type.equals(BusiTypeEnum.CS.getType())) {
            elasticSearchService.deleteDocumentFromType(Constants.CS_INFO_INDEX, "haiguan", id);
        } else if (type.equals(BusiTypeEnum.BS.getType())) {
            elasticSearchService.deleteDocumentFromType(Constants.BS_INFO_INDEX, "haiguan", id);
        }
    }


    public HBusiDataManager getObjectByIdAndType(String cust_id, Long id, String type) {
        String sql = "select * from " + HMetaDataDef.getTable(type, "") + " where id=" + id + " and type='" + type + "'";
        if (StringUtil.isNotEmpty(cust_id)) {
            sql += " and cust_id='" + cust_id + "'";
        }
        RowMapper<HBusiDataManager> managerRowMapper = new BeanPropertyRowMapper<>(HBusiDataManager.class);
        List<HBusiDataManager> list = jdbcTemplate.query(sql, managerRowMapper);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public void delDataListByPid(String type, Long pid) {
        //String sql = "delete from " + HMetaDataDef.getTable(type, "") + " where CASE WHEN JSON_VALID(content) THEN  JSON_EXTRACT(content, '$.pid')=" + pid + " ELSE null END or CASE WHEN JSON_VALID(content) THEN  JSON_EXTRACT(content, '$.pid')='" + pid + "' ELSE null END";
        String sql = "delete from " + HMetaDataDef.getTable(type, "") + " where " + BusiMetaConfig.getFieldIndex(type, "pid") + "=(SELECT ext_3 FROM " + HMetaDataDef.getTable(BusiTypeEnum.getParentType(type), "") + " WHERE id = ?) ";
        jdbcTemplate.update(sql, pid);
    }

    /**
     * 删除税单
     *
     * @param type
     * @param mainBillNo
     * @param billNo
     * @param custId
     */
    public void deleteSListByBillNo(String custId, String type, String mainBillNo, String billNo) {
        String sql = "delete from " + HMetaDataDef.getTable(type, "") + " where cust_id = ?  AND ext_2 = ? AND ext_4 = ? ";
        jdbcTemplate.update(sql, custId, mainBillNo, billNo);
    }

    /**
     * 根据多个ID删除税单
     *
     * @param custId
     * @param type
     * @param ids
     */
    public void deleteByIds(String custId, String type, List ids) {
        if (ids == null || ids.size() == 0) {
            log.warn("批量根据多个ID删除ids为空");
            return;
        }
        String sql = "delete from " + HMetaDataDef.getTable(type, "") + " where cust_id = ?  AND id IN(" + SqlAppendUtil.sqlAppendWhereIn(ids) + ") ";
        jdbcTemplate.update(sql, custId);
    }

    public List<HBusiDataManager> getDataList(String type, Long pid) {
        String sql2 = "select id, type, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(type, "") + " where  type='" + type + "' and " + BusiMetaConfig.getFieldIndex(type, "pid") + " = (SELECT ext_3 FROM " + HMetaDataDef.getTable(BusiTypeEnum.getParentType(type), "") + " WHERE id = ?) ";
        log.info("sql2=" + sql2);
       /* RowMapper<HBusiDataManager> managerRowMapper=new BeanPropertyRowMapper<>(HBusiDataManager.class);
        List<HBusiDataManager> list = jdbcTemplate.query(sql2,managerRowMapper);*/

        List<Map<String, Object>> list2 = jdbcTemplate.queryForList(sql2, pid);
        log.info("list==" + list2);
        List<HBusiDataManager> list = JSON.parseArray(JSON.toJSONString(list2), HBusiDataManager.class);
        return list;
    }

    public List<HBusiDataManager> listDataByPid(String custId, String type, long pid, String pBusiType) {
        StringBuffer sql = new StringBuffer();
        sql.append("select id, type, content, cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(type, "") + " where cust_id = ? AND type=? AND ext_4 = (SELECT ext_3 FROM " + HMetaDataDef.getTable(pBusiType, "") + " WHERE id = ?) ");
        log.info("查询分单sql:{}", sql);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), custId, type, pid);
        List<HBusiDataManager> result = JSON.parseArray(JSON.toJSONString(list), HBusiDataManager.class);
        return result;
    }

    /**
     * 查询税单列表
     *
     * @param custId
     * @param type
     * @param mainBillNo
     * @param billNo
     * @return
     */
    public List<HBusiDataManager> listSdByBillNo(String custId, String type, String mainBillNo, String billNo) {
        StringBuffer sql = new StringBuffer();
        sql.append("select id, type, content, cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(type, "") + " where cust_id = ? AND type=? AND ext_2 = ?  AND ext_4 = ?");
        log.info("查询税单sql:{}", sql);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), custId, type, mainBillNo, billNo);
        List<HBusiDataManager> result = JSON.parseArray(JSON.toJSONString(list), HBusiDataManager.class);
        return result;
    }

    /**
     * 查询所有分单
     * @param type
     * @param mainBillNo
     * @return
     */
    public List<HBusiDataManager> listFdByBillNo(String type, String mainBillNo) {
        StringBuffer sql = new StringBuffer();
        sql.append("select id, type, content, cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(type, "") + " where type=?  AND ext_4 = ?");
        log.info("查询税单sql:{}", sql);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), type, mainBillNo);
        List<HBusiDataManager> result = JSON.parseArray(JSON.toJSONString(list), HBusiDataManager.class);
        return result;
    }

    /**
     * 根据主单号、分单号，查询分单
     *
     * @param custId
     * @param type
     * @param mainBillNo
     * @param billNo
     * @return
     */
    public HBusiDataManager findFendanByBillNo(String custId, String type, String mainBillNo, String billNo) {
        StringBuffer sql = new StringBuffer();
        sql.append("select id, type, content, cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5,ext_6 from " + HMetaDataDef.getTable(type, "") + " where cust_id = ? AND type=? AND ext_4 = ?  AND ext_3 = ?");
        log.info("查询分单sql:{}", sql);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), custId, type, mainBillNo, billNo);
        List<HBusiDataManager> result = JSON.parseArray(JSON.toJSONString(list), HBusiDataManager.class);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    /**
     * 根据主单号查询主单
     *
     * @param custId
     * @param type
     * @param billNo
     * @return
     */
    public HBusiDataManager findZhudanByBillNo(String custId, String type, String billNo) {
        StringBuffer sql = new StringBuffer();
        sql.append("select id, type, content, cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(type, "") + " where cust_id = ? AND type=? AND ext_3 = ?");
        log.info("查询主单sql:{}", sql);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), custId, type, billNo);
        List<HBusiDataManager> result = JSON.parseArray(JSON.toJSONString(list), HBusiDataManager.class);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    /**
     * 根据主单号和查询税单列表
     *
     * @param custId
     * @param type
     * @param mainBillNo
     * @param partyNos
     * @return
     */
    public List<JSONObject> listSdByBillNos1(String custId, String type,String type1, String mainBillNo, List<String> partyNos, JSONObject param) {
        if (partyNos == null || partyNos.size() == 0) {
            return new ArrayList<>();
        }
        List sqlParams = new ArrayList();
        StringBuffer sql = new StringBuffer();
        sql.append("select s.id, s.type, s.content, s.cust_id, s.create_id, s.create_date,s.ext_1, s.ext_2, s.ext_3, s.ext_4, s.ext_5 ,f.content->'$.receive_tel',f.content->'$.id_type',f.content->'id_no',f.content->'$.receive_name',f.content->'$.receive_address' from " + HMetaDataDef.getTable(type, "") +
                " s left join "+ HMetaDataDef.getTable(type, "")+" f on s.ext_4= f.ext_3" +
                " where s.type=? AND s." + BusiMetaConfig.getFieldIndex(type, "main_bill_no") + " = ?  AND s." + BusiMetaConfig.getFieldIndex(type, "pid") + " IN (" + SqlAppendUtil.sqlAppendWhereIn(partyNos) + ")");
        if (!"all".equals(custId))
            sql.append(" and s.cust_id='").append(custId).append("'");
        sqlParams.add(type);
        sqlParams.add(mainBillNo);

        Iterator keys = param.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if ("".equals(String.valueOf(param.get(key)))) continue;
            if ("pageNum".equals(key) || "pageSize".equals(key) || "stationId".equals(key) || "cust_id".equals(key) || "_rule_".equals(key)) {
                continue;
            } else if (key.startsWith("_g_")) {
                sql.append(" and s." + BusiMetaConfig.getFieldIndex(type, key) + " > ?");
            } else if (key.startsWith("_ge_")) {
                sql.append(" and s." + BusiMetaConfig.getFieldIndex(type, key) + " >= ?");
            } else if (key.startsWith("_l_")) {
                sql.append(" and s." + BusiMetaConfig.getFieldIndex(type, key) + " < ?");
            } else if (key.startsWith("_le_")) {
                sql.append(" and s." + BusiMetaConfig.getFieldIndex(type, key) + " <= ?");
            } else if (key.startsWith("_eq_")) {
                sql.append(" and s." + BusiMetaConfig.getFieldIndex(type, key) + " = ?");
            } else {
                sql.append(" and s." + BusiMetaConfig.getFieldIndex(type, key) + "=?");
            }
            sqlParams.add(param.get(key));
        }
        log.info("查询税单sql:{}", sql);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), sqlParams.toArray());
        List<Map<String, Object>> collect = list.parallelStream().map(map -> {
            if (map.containsKey("content")) {
                JSONObject content = JSON.parseObject(map.get("content").toString());
                content.put("receive_tel", map.containsKey("receive_tel") ? map.get("receive_tel") : "");
                content.put("receive_address", map.containsKey("receive_address") ? map.get("receive_address") : "");
                content.put("receive_name", map.containsKey("receive_name") ? map.get("receive_name") : "");
                content.put("id_type", map.containsKey("id_type") ? map.get("id_type") : "");
                content.put("id_no", map.containsKey("id_no") ? map.get("id_no") : "");
                map.put("content", content);
            }

            return map;
        }).collect(Collectors.toList());
        //List<JSONObject> result = JSON.parseArray(JSON.toJSONString(list), JSONObject.class);
        return JSON.parseArray(JSON.toJSONString(collect), JSONObject.class);
    }
    /**
     * 根据主单号和查询税单列表
     *
     * @param custId
     * @param type
     * @param mainBillNo
     * @param partyNos
     * @return
     */
    public List<JSONObject> listSdByBillNos(String custId, String type, String mainBillNo, List<String> partyNos, JSONObject param) {
        if (partyNos == null || partyNos.size() == 0) {
            return new ArrayList<>();
        }
        List sqlParams = new ArrayList();
        StringBuffer sql = new StringBuffer();
        sql.append("select id, type, content, cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(type, "") + " where type=? AND " + BusiMetaConfig.getFieldIndex(type, "main_bill_no") + " = ?  AND " + BusiMetaConfig.getFieldIndex(type, "pid") + " IN (" + SqlAppendUtil.sqlAppendWhereIn(partyNos) + ")");
        if (!"all".equals(custId))
            sql.append(" and cust_id='").append(custId).append("'");
        sqlParams.add(type);
        sqlParams.add(mainBillNo);

        Iterator keys = param.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if ("".equals(String.valueOf(param.get(key)))) continue;
            if ("pageNum".equals(key) || "pageSize".equals(key) || "stationId".equals(key) || "cust_id".equals(key) || "_rule_".equals(key)) {
                continue;
            } else if (key.startsWith("_g_")) {
                sql.append(" and " + BusiMetaConfig.getFieldIndex(type, key) + " > ?");
            } else if (key.startsWith("_ge_")) {
                sql.append(" and " + BusiMetaConfig.getFieldIndex(type, key) + " >= ?");
            } else if (key.startsWith("_l_")) {
                sql.append(" and " + BusiMetaConfig.getFieldIndex(type, key) + " < ?");
            } else if (key.startsWith("_le_")) {
                sql.append(" and " + BusiMetaConfig.getFieldIndex(type, key) + " <= ?");
            } else if (key.startsWith("_eq_")) {
                sql.append(" and " + BusiMetaConfig.getFieldIndex(type, key) + " = ?");
            } else {
                sql.append(" and " + BusiMetaConfig.getFieldIndex(type, key) + "=?");
            }
            sqlParams.add(param.get(key));
        }
        log.info("查询税单sql:{}", sql);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), sqlParams.toArray());
        //List<JSONObject> result = JSON.parseArray(JSON.toJSONString(list), JSONObject.class);
        return JSON.parseArray(JSON.toJSONString(list), JSONObject.class);
    }

    public List<HBusiDataManager> listSdByBillNo(String custId, String type, String mainBillNo, List<String> partyNos, JSONObject param) {
        if (partyNos == null || partyNos.size() == 0) {
            return new ArrayList<>();
        }
        List sqlParams = new ArrayList();
        StringBuffer sql = new StringBuffer();
        sql.append("select id, type, content, cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(type, "") + " where type=? AND " + BusiMetaConfig.getFieldIndex(type, "main_bill_no") + " = ?  AND " + BusiMetaConfig.getFieldIndex(type, "pid") + " IN (" + SqlAppendUtil.sqlAppendWhereIn(partyNos) + ")");
        if (!"all".equals(custId))
            sql.append(" and cust_id='").append(custId).append("'");
        sqlParams.add(type);
        sqlParams.add(mainBillNo);

        Iterator keys = param.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if ("".equals(String.valueOf(param.get(key)))) continue;
            if ("pageNum".equals(key) || "pageSize".equals(key) || "stationId".equals(key) || "cust_id".equals(key) || "_rule_".equals(key)) {
                continue;
            } else if (key.startsWith("_g_")) {
                sql.append(" and " + BusiMetaConfig.getFieldIndex(type, key) + " > ?");
            } else if (key.startsWith("_ge_")) {
                sql.append(" and " + BusiMetaConfig.getFieldIndex(type, key) + " >= ?");
            } else if (key.startsWith("_l_")) {
                sql.append(" and " + BusiMetaConfig.getFieldIndex(type, key) + " < ?");
            } else if (key.startsWith("_le_")) {
                sql.append(" and " + BusiMetaConfig.getFieldIndex(type, key) + " <= ?");
            } else if (key.startsWith("_eq_")) {
                sql.append(" and " + BusiMetaConfig.getFieldIndex(type, key) + " = ?");
            } else {
                sql.append(" and " + BusiMetaConfig.getFieldIndex(type, key) + "=?");
            }
            sqlParams.add(param.get(key));
        }
        log.info("查询税单sql:{}", sql);
        Object [] s = sqlParams.toArray();

        RowMapper<HBusiDataManager> managerRowMapper=new BeanPropertyRowMapper<>(HBusiDataManager.class);
        List<HBusiDataManager> list = jdbcTemplate.query(sql.toString(),s,managerRowMapper);
//        List<Map<String, Object>> list = jdbcTemplate.query(sql.toString(), sqlParams.toArray(),);
        return list;
    }

    /**
     * 根据父级ID查询子单列表
     *
     * @param busiType
     * @param cust_id
     * @param pid
     * @return
     */
    public List queryChildData(String busiType, String cust_id, String cust_group_id, long cust_user_id, Long pid, JSONObject param) {
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
                sqlstr.append(" and " + BusiMetaConfig.getFieldIndex(busiType, key) + " > ?");
            } else if (key.startsWith("_ge_")) {
                sqlstr.append(" and " + BusiMetaConfig.getFieldIndex(busiType, key) + " >= ?");
            } else if (key.startsWith("_l_")) {
                sqlstr.append(" and " + BusiMetaConfig.getFieldIndex(busiType, key) + " < ?");
            } else if (key.startsWith("_le_")) {
                sqlstr.append(" and " + BusiMetaConfig.getFieldIndex(busiType, key) + " <= ?");
            } else if (key.startsWith("_eq_")) {
                sqlstr.append(" and " + BusiMetaConfig.getFieldIndex(busiType, key) + " = ?");
            } else {
                sqlstr.append(" and " + BusiMetaConfig.getFieldIndex(busiType, key) + "=?");
            }
            sqlParams.add(param.get(key));
        }
        sqlstr.append(" AND " + BusiMetaConfig.getFieldIndex(busiType, "pid") + "=(SELECT ext_3 FROM " + HMetaDataDef.getTable(BusiTypeEnum.getParentType(busiType), "") + " WHERE id = ?)");
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

    /**
     * 根据父级单号查询子单列表
     *
     * @param custId
     * @param type
     * @param pBillNo
     * @return
     */
    public List<HBusiDataManager> listDataByParentBillNo(String custId, String type, String pBillNo) {
        StringBuffer sql = new StringBuffer();
        sql.append("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(type, "") + " where cust_id = ? AND type=? AND ext_4 = ?");
        log.info("查询分单sql:{}", sql);
        /*List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), custId, type, pBillNo);
        List<HBusiDataManager> result = JSON.parseArray(JSON.toJSONString(list), HBusiDataManager.class);
*/
        RowMapper<HBusiDataManager> managerRowMapper=new BeanPropertyRowMapper<>(HBusiDataManager.class);
        Object[] args=new Object[]{custId,type,pBillNo};
        List<HBusiDataManager> list = jdbcTemplate.query(sql.toString(),args,managerRowMapper);

        return list;
    }

    public List<HBusiDataManager> listDataByParentBillNos(String custId, String type, List<String> pBillNos) {
        StringBuffer sql = new StringBuffer();
        sql.append("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(type, "") + " where cust_id = ? AND type=? AND ext_4 IN (" + SqlAppendUtil.sqlAppendWhereIn(pBillNos) + ")");
        log.info("查询分单sql:{}", sql);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), custId, type);
        List<HBusiDataManager> result = JSON.parseArray(JSON.toJSONString(list), HBusiDataManager.class);
        return result;
    }

    /**
     * 根据pid从ES查询子列表
     *
     * @param type
     * @param pid
     * @return
     */
    public List<HBusiDataManager> getDataListByES(String type, Long pid) {
        JSONObject params = new JSONObject();
        params.put("pid", pid);
        List result = listQueryByEs("all", 0, 0L, type, params);
        List<HBusiDataManager> list = JSON.parseArray(JSON.toJSONString(result), HBusiDataManager.class);
        return list;
    }

    public void delDataListByIdAndType(Long id, String type) {
        String sql = "delete from " + HMetaDataDef.getTable(type, "") + " where type='" + type + "' and id=" + id;
        jdbcTemplate.execute(sql);
    }

    /**
     * 查询主单下的分单
     *
     * @param pid
     * @param type
     * @param idCardPhotoStatus 1-有身份证图片 2-无
     * @param idCardCheckStatus 1-身份证校验通过 2-无
     * @return
     */
    public List<HBusiDataManager> listFDIdCard(int pid, String type, String pBusiType, int idCardPhotoStatus, int idCardCheckStatus) {
        //StringBuilder hql = new StringBuilder("select * from " + HMetaDataDef.getTable(type, "") + " WHERE type = ? AND JSON_EXTRACT(content, '$.pid')=?  ");
        StringBuilder sql = new StringBuilder("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(type, "") + " WHERE type = ? AND ext_4 = (SELECT ext_3 FROM " + HMetaDataDef.getTable(pBusiType, "") + " WHERE id = ?)  ");
        // 有身份照片 AND ext_4 = (SELECT ext_3 FROM " + HMetaDataDef.getTable(pBusiType, "") + " WHERE id = ?)
        if (1 == idCardPhotoStatus) {
            //hql.append(" AND ext_6 IS NOT NULL AND ext_6 <>'' ");
            sql.append(" AND ").append(BusiMetaConfig.getFieldIndex(type, "idcard_pic_flag")).append(" ='1' ");
        } else if (2 == idCardPhotoStatus) {
            //hql.append(" AND (ext_6 IS NULL OR ext_6 ='') ");
            sql.append(" AND ").append(BusiMetaConfig.getFieldIndex(type, "idcard_pic_flag")).append(" ='0' ");
        }
        //身份核验结果通过
        if (1 == idCardCheckStatus) {
            //sql.append(" AND ext_7 = 1 ");
            sql.append(" AND ").append(BusiMetaConfig.getFieldIndex(type, "check_status")).append(" ='1' ");
        } else if (2 == idCardCheckStatus) {
            // 身份核验未通过
            //sql.append(" AND (ext_7 IS NULL OR ext_7 ='' OR ext_7 =2) ");
            sql.append(" AND ").append(BusiMetaConfig.getFieldIndex(type, "check_status")).append(" ='2' ");
        }
        List<Map<String, Object>> list2 = jdbcTemplate.queryForList(sql.toString(), type, pid);
        List<HBusiDataManager> list = JSON.parseArray(JSON.toJSONString(list2), HBusiDataManager.class);
        return list;
    }


    private static Integer BATCH_SIZE = 1000;

    public void batchInsert(String busiType, List<HBusiDataManager> hBusiDataManagerList) {
        String sql = "insert into " + HMetaDataDef.getTable(busiType, "") + "(id, type, content, cust_id, cust_group_id, cust_user_id, create_id, create_date, ext_1, ext_2, ext_3, ext_4, ext_5 ) value(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        List<Object[]> args = transformFlowCarReportDayBoToObjects(hBusiDataManagerList);
        int fromIndex = 0;
        int toIndex = BATCH_SIZE;
        while (fromIndex != args.size()) {
            if (toIndex > args.size()) {
                toIndex = args.size();
            }
            jdbcTemplate.batchUpdate(sql, args.subList(fromIndex, toIndex));
            fromIndex = toIndex;
            toIndex += BATCH_SIZE;
            if (toIndex > args.size())
                toIndex = args.size();
        }

    }

    private List<Object[]> transformFlowCarReportDayBoToObjects(List<HBusiDataManager> hBusiDataManagerList) {
        List<Object[]> list = new ArrayList<>();
        Object[] object = null;
        for (HBusiDataManager hBusiDataManager : hBusiDataManagerList) {
            object = new Object[]{
                    hBusiDataManager.getId(),
                    hBusiDataManager.getType(),
                    hBusiDataManager.getContent(),
                    hBusiDataManager.getCust_id(),
                    hBusiDataManager.getCust_group_id(),
                    hBusiDataManager.getCreateId(),
                    hBusiDataManager.getCreateId(),
                    hBusiDataManager.getCreateDate(),
                    hBusiDataManager.getExt_1(),
                    hBusiDataManager.getExt_2(),
                    hBusiDataManager.getExt_3(),
                    hBusiDataManager.getExt_4(),
                    hBusiDataManager.getExt_5()
            };
            list.add(object);
        }

        return list;
    }

    /**
     * 保存身份核验信息到身份核验队列表
     *
     * @param content
     * @param billId
     * @param userId
     * @param custId
     */
    public void insertSFVerifyQueue(String content, long billId, long userId, String custId, String batchId) {
        TResourceLog queue = new TResourceLog();
        queue.setCustId(custId);
        queue.setContent(content);
        queue.setBusiId(String.valueOf(billId));
        queue.setCustUserId(userId);
        queue.setBatchId(batchId);
        queue.setCreateTime(new Timestamp(System.currentTimeMillis()));
        queue.setBusiType(1);
        queue.setSupplierId(SupplierEnum.ZAX.getSupplierId());
        MarketResourceEntity resourceId = sourceDao.getResourceId(SupplierEnum.ZAX.getSupplierId(), ResourceEnum.CHECK_IDCARD.getType());
        if (resourceId != null) {
            queue.setResourceId(resourceId.getResourceId());
        }
        customerDao.saveOrUpdate(queue);
    }

    public List<Map<String, Object>> listObjectByParam(String busiType, String cust_id, JSONObject params) {
        List sqlParams = new ArrayList();
        StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=?");
        if (!"all".equals(cust_id))
            sqlstr.append(" and cust_id='").append(cust_id).append("'");

        sqlParams.add(busiType);

        Iterator keys = params.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if ("".equals(String.valueOf(params.get(key)))) continue;
            if ("pageNum".equals(key) || "pageSize".equals(key)) continue;
            if ("cust_id".equals(key)) {
                sqlstr.append(" and cust_id=?");
            } else if (key.startsWith("_c_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(3) + "') like concat('%',?,'%')");
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
            } else if (key.startsWith("_range_")) {
                if ("0".equals(String.valueOf(params.get(key)))) {
                    sqlstr.append(" and ( JSON_EXTRACT(content, '$." + key.substring(7) + "') <= ?")
                            .append(" OR JSON_EXTRACT(content, '$." + key.substring(7) + "') = '' ")
                            .append(" OR JSON_EXTRACT(content, '$." + key.substring(7) + "') IS NULL ) ");
                } else {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(7) + "') >= ?");
                }
            } else {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key + "')=?");
            }

            sqlParams.add(params.get(key));
        }
        List<Map<String, Object>> ds = jdbcTemplate.queryForList(sqlstr.toString(), sqlParams.toArray());
        return ds;
    }

    /**
     * 通过数据查询场站和报关单位名称
     *
     * @param jo
     */
    public void getStationCustName(JSONObject jo) {
        //查询场站和报关单位
        String custId = jo.getString("cust_id");
        jo.put("cust_name", "");
        jo.put("station_name", "");
        jo.put("station_id", "");
        Customer customer = customerDao.get(custId);
        if (customer != null) {
            jo.put("cust_name", customer.getEnterpriseName());
            CustomerProperty cp = customerDao.getProperty(custId, "station_id");
            if (cp != null) {
                jo.put("station_id", cp.getPropertyValue());
                String stationSql = "select content, create_id, create_date, update_id, update_date from h_resource where type=? and id=? ";
                try {
                    Map station = jdbcTemplate.queryForMap(stationSql, "station", cp.getPropertyValue());
                    if (station != null) {
                        jo.put("station_name", JSONObject.parseObject(String.valueOf(station.get("content"))).getString("name"));
                    }
                } catch (DataAccessException e) {
                    log.error("查询场站信息异常", e);
                }
            }
        }
    }

    /**
     * 查询字典数据
     *
     * @param jo
     */
    public void getHDicData(JSONObject jo) {
        Iterator keys = jo.keySet().iterator();
        HDic dic;
        JSONObject tmp = new JSONObject();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (StringUtil.isEmpty(jo.getString(key))) continue;
            dic = dicUtil.getCache(key.toUpperCase(), jo.getString(key));
            if (dic == null) {
                continue;
            }
            //log.info("查询缓存字典数据type:[{}],code:[{}],字典数据:[{}]", key.toUpperCase(), jo.getString(key), dic);
            tmp.put(key + "_name", dic.getName_zh());
            tmp.put(key + "_name_en", dic.getName_en());
        }
        if (tmp.size() > 0) {
            Iterator tmpKeys = tmp.keySet().iterator();
            while (tmpKeys.hasNext()) {
                String key = (String) tmpKeys.next();
                jo.put(key, tmp.get(key));
            }
        }
    }

    /**
     * 获取资源
     *
     * @param resourceType
     * @param code
     * @return
     */
    public Map<String, Object> getHResourceData(String resourceType, String code) {
        StringBuffer sql = new StringBuffer("select id, content, create_id, create_date from h_resource where type=? and JSON_EXTRACT(content, '$.code')=?");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), resourceType, code);
        if (list != null) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 根据类型获取资源缓存
     *
     * @param resourceType
     * @return
     */
    public Map<String, JSONObject> getHResourceCacheData(String resourceType) {
        Map<String, JSONObject> cache = new HashMap<>();
        StringBuffer sql = new StringBuffer("select id, content from h_resource where type=? ");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), resourceType);
        if (list != null) {
            JSONObject jsonObject;
            for (Map<String, Object> m : list) {
                jsonObject = JSON.parseObject(String.valueOf(m.get("content")));
                cache.put(jsonObject.getString("code"), jsonObject);
            }
        }
        return cache;
    }

    /**
     * es检索数据
     *
     * @param cust_id
     * @param cust_group_id
     * @param cust_user_id
     * @param busiType
     * @param params
     * @return
     */
    public Page queryByEs(String cust_id, String cust_group_id, Long cust_user_id, String busiType, JSONObject params) {
        Page page = new Page();
        if (!"all".equals(cust_id)) {
            params.put("cust_id", cust_id);
        }
        //params.put("cust_group_id", cust_group_id);
        //params.put("cust_user_id", cust_user_id);
        SearchResult result = elasticSearchService.search(elasticSearchService.queryConditionToDSL(params).toString(), BusiTypeEnum.getEsIndex(busiType), Constants.INDEX_TYPE);
        if (result != null) {
            List list = new ArrayList<>();
            JSONObject t;
            for (SearchResult.Hit<JSONObject, Void> hit : result.getHits(JSONObject.class)) {
                t = hit.source;
                t.put("id", NumberConvertUtil.parseLong(hit.id));
                list.add(t);
            }
            page.setData(list);
            page.setTotal(NumberConvertUtil.parseInt(result.getTotal()));
        }
        return page;
    }

    public List listQueryByEs(String cust_id, int cust_group_id, Long cust_user_id, String busiType, JSONObject params) {
        if (!"all".equals(cust_id)) {
            params.put("cust_id", cust_id);
        }
        //params.put("cust_group_id", cust_group_id);
        //params.put("cust_user_id", cust_user_id);
        List list = new ArrayList<>();
        SearchResult result = elasticSearchService.search(elasticSearchService.queryConditionToDSL(params).toString(), BusiTypeEnum.getEsIndex(busiType), Constants.INDEX_TYPE);
        if (result != null) {
            JSONObject t;
            for (SearchResult.Hit<JSONObject, Void> hit : result.getHits(JSONObject.class)) {
                t = hit.source;
                t.put("id", NumberConvertUtil.parseLong(hit.id));
                list.add(t);
            }
        }
        return list;
    }

    public void esTestData() {
        //测试主单数据
        JSONObject json = new JSONObject();
        json.put("id", 0);
        json.put("type", BusiTypeEnum.SZ.getType());
        addDataToES("0", BusiTypeEnum.SZ.getType(), json);

        json = new JSONObject();
        json.put("id", -1);
        json.put("type", BusiTypeEnum.SF.getType());
        addDataToES("-1", BusiTypeEnum.SF.getType(), json);
    }

    /**
     * XML格式转为map格式
     *
     * @param xmlString
     * @return
     */
    public static Map<String, String> xmlToMap(String xmlString) {

        Map<String, String> map = new HashMap<String, String>();
        try {
            InputStream inputStream = null;
            ByteArrayInputStream byteArrayInputStream2 = new ByteArrayInputStream(xmlString.getBytes(Charset.forName("UTF-8")));

            inputStream = byteArrayInputStream2;
            SAXReader reader = new SAXReader();
            Document doc = reader.read(inputStream);
            Element rootElement = doc.getRootElement();
            List<Element> elements = rootElement.elements();
            for (Element el : elements) {
                map.put(el.getName(), el.getText());
            }
            inputStream.close();
            System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void xml2Json(String xml) {
        XMLSerializer xmlSerializer = new XMLSerializer();
        String resutStr = xmlSerializer.read(xml).toString();
        net.sf.json.JSONObject result = net.sf.json.JSONObject.fromObject(resutStr);

        System.out.println(result);
    }

    /**
     * 根据税单列表计算主要货物
     * 规则：
     * 1、1个商品默认为主要货物
     * 2、多个商品价格最高的为主要货物
     * 3、价格都为0或者空第一个商品为主要货物
     *
     * @param list
     * @return
     */
    public Map<String, String> generateFDMainGName(List<Product> list) {
        String spilt = "|";
        Map<String, String> resultmap = new HashMap<>();
        if (list == null || list.size() == 0) {
            return resultmap;
        } else if (list.size() == 1) {
            // 1个商品默认为主要货物名称
            StringBuffer name = new StringBuffer();
            StringBuffer name_en = new StringBuffer();
            name.append(list.get(0).getG_name());
            name_en.append(list.get(0).getG_name_en());
            //.append(spilt)
            //.append(list.get(0).getG_name_en());
                   /* .append(spilt)
                    .append(list.get(0).getG_model());*/
            resultmap.put("name", name.toString());
            resultmap.put("name_en", name_en.toString());
            return resultmap;
        } else {
            Optional<Product> result = list.stream().filter(Objects::nonNull).filter(s -> StringUtil.isNotEmpty(s.getG_qty()) && StringUtil.isNotEmpty(s.getDecl_price()))
                    .max(Comparator.comparingDouble(s -> NumberConvertUtil.parseDouble(s.getG_qty()) * NumberConvertUtil.parseDouble(s.getDecl_price())));
            Product data = result.orElse(new Product());
            StringBuffer name = new StringBuffer();
            StringBuffer name_en = new StringBuffer();
            name.append(data.getG_name()).append(spilt);
            //.append(data.getG_name_en());
                   /* .append(spilt)
                    .append(data.getG_model());*/
            name_en.append(data.getG_name_en()).append(spilt);
            String _name = name.toString();
            if(_name.endsWith(spilt)){
                _name=_name.replace(spilt,"");
            }
            String _name_en = name_en.toString();
            if(_name_en.endsWith(spilt)){
                _name_en=_name_en.replace(spilt,"");
            }
            resultmap.put("name", _name);
            resultmap.put("name_en", _name_en);
            return resultmap;
        }
    }

    /**
     * 核验身份证信息金额判断
     *
     * @param custId
     * @param quantity
     * @return
     */
    public boolean checkBatchIdCardAmount(String custId, int quantity) throws TouchException {
        MarketResourceEntity mr = sourceDao.getResourceId(SupplierEnum.ZAX.getSupplierId(), ResourceEnum.CHECK_IDCARD.getType());
        String resourceId = null;
        if (mr != null) {
            resourceId = String.valueOf(mr.getResourceId());
        }
        //判断余额是否充足
        boolean custBalance = marketResourceService.judRemainAmount0(custId);
        if (!custBalance) {
            log.warn("企业id:{}余额不足无法核验", custId);
            return false;
        }
        int custCheckPrice = -1;
        //根据企业id查询销售定价
        CustomerProperty custConfigPrice = customerDao.getProperty(custId, resourceId + "_config");
        if (custConfigPrice != null && StringUtil.isNotEmpty(custConfigPrice.getPropertyValue())) {
            //将销售定价元转为厘
            custCheckPrice = NumberConvertUtil.changeY2L(custConfigPrice.getPropertyValue());
            log.info("企业id:{}未配置销售定价,使用的是资源成本价:{}厘", custId, custCheckPrice);
        } else {
            log.warn("企业id:{}未配置销售定价", custId);
            throw new TouchException("1001", "未配置销售定价");
            /*//查询供应商成本价
            ResourcePropertyEntity resourceProperty = marketResourceDao.getProperty(resourceId, "price_config");
            if (resourceProperty != null && StringUtil.isNotEmpty(resourceProperty.getPropertyValue())) {
                custCheckPrice = NumberConvertUtil.changeY2L(resourceProperty.getPropertyValue());
                log.warn("企业id:{}未配置销售定价,使用的是资源成本价:{}厘", custId, custCheckPrice);
            } else {
                log.warn("企业id:{}未配置销售定价", custId);
                return false;
            }*/
        }

        //计算批量核验身份需要的金额
        BigDecimal price = new BigDecimal(String.valueOf(custCheckPrice));
        price = price.multiply(new BigDecimal(quantity));
        // 账户余额
        CustomerProperty remainAmount = customerDao.getProperty(custId, "remain_amount");
        BigDecimal amount = new BigDecimal(remainAmount.getPropertyValue());
        if (amount.multiply(new BigDecimal(1000)).subtract(price).compareTo(new BigDecimal(0)) < 0) {
            return false;
        }
        return true;
    }


}
