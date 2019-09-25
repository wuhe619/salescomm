package com.bdaim.customs.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.common.util.CipherUtil;
import com.bdaim.common.util.IDHelper;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.entity.*;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.supplier.dto.SupplierEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class ServiceUtils {

    private static Logger log = LoggerFactory.getLogger(ServiceUtils.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private HBusiDataManagerDao hBusiDataManagerDao;
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


    public HBusiDataManager getObjectByIdAndType(Long id, String type) {
        String sql = "select * from h_data_manager where id=" + id + " and type='" + type + "'";
        RowMapper<HBusiDataManager> managerRowMapper = new BeanPropertyRowMapper<>(HBusiDataManager.class);
        List<HBusiDataManager> list = jdbcTemplate.query(sql, managerRowMapper);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public void delDataListByPid(Long pid) {
        String sql = "delete from h_data_manager where CASE WHEN JSON_VALID(content) THEN  JSON_EXTRACT(content, '$.pid')=" + pid + " ELSE null END or CASE WHEN JSON_VALID(content) THEN  JSON_EXTRACT(content, '$.pid')='" + pid + "' ELSE null END";
        jdbcTemplate.execute(sql);
    }

    public List<HBusiDataManager> getDataList(String type, Long pid) {
        String sql2 = "select * from h_data_manager where  type='" + type + "' and ( CASE WHEN JSON_VALID(content) THEN JSON_EXTRACT(content, '$.pid')=" + pid + " ELSE null END  or CASE WHEN JSON_VALID(content) THEN JSON_EXTRACT(content, '$.pid')='" + pid + "' ELSE null END)";
        log.info("sql2=" + sql2);
       /* RowMapper<HBusiDataManager> managerRowMapper=new BeanPropertyRowMapper<>(HBusiDataManager.class);
        List<HBusiDataManager> list = jdbcTemplate.query(sql2,managerRowMapper);*/

        List<Map<String, Object>> list2 = jdbcTemplate.queryForList(sql2);
        log.info("list==" + list2);
        List<HBusiDataManager> list = JSON.parseArray(JSON.toJSONString(list2), HBusiDataManager.class);
        return list;
    }

    public void delDataListByIdAndType(Long id, String type) {
        String sql = "delete from h_data_manager where type='" + type + "' and id=" + id;
        jdbcTemplate.execute(sql);
    }

    public void insertSFVerifyQueue(String content, long billId, long userId, String custId) {
        TResourceLog queue = new TResourceLog();
        queue.setCustId(custId);
        queue.setContent(content);
        queue.setBusiId(String.valueOf(billId));
        queue.setCustUserId(userId);
        queue.setBatchId(CipherUtil.encodeByMD5(IDHelper.getID().toString()));
        queue.setCreateTime(new Timestamp(System.currentTimeMillis()));
        queue.setBusiType(1);
        queue.setSupplierId(SupplierEnum.ZAX.getSupplierId());
        MarketResourceEntity resourceId = sourceDao.getResourceId(SupplierEnum.ZAX.getSupplierId(), ResourceEnum.CHECK_IDCARD.getType());
        if (resourceId != null) {
            queue.setResourceId(resourceId.getResourceId());
        }
        hBusiDataManagerDao.saveOrUpdate(queue);
    }

    public List<Map<String, Object>> listObjectByParam(String busiType, String cust_id, JSONObject params) {
        List sqlParams = new ArrayList();
        StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from h_data_manager where type=?");
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
            log.info("查询缓存字典数据type:[{}],code:[{}],字典数据:[{}]", key.toUpperCase(), jo.getString(key), dic);
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
}
