package com.bdaim.crm.ent.service;

import cn.hutool.core.date.DateException;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.AppConfig;
import com.bdaim.common.dto.Page;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.PhoneService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.crm.ent.entity.*;
import com.bdaim.customer.service.B2BTcbLogService;
import com.bdaim.util.ExcelUtil;
import com.bdaim.util.MD5Util;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import io.searchbox.core.CountResult;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class EntDataService {

    public static final Logger LOG = LoggerFactory.getLogger(EntDataService.class);

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private ElasticSearchService elasticSearchService;
    @Resource
    private SequenceService sequenceService;
    @Autowired
    private B2BTcbLogService b2BTcbLogService;
    @Autowired
    private PhoneService phoneService;

    /**
     * 导入企查查数据
     *
     * @param path
     * @param titleRows
     * @param headerRows
     * @param source
     * @param sourceWeb
     * @return
     */
    public int importQCCByExcel(String path, int titleRows, int headerRows, String source, String sourceWeb) {
        List<ImportQCCEnt> list = ExcelUtil.importExcel(path, titleRows, headerRows, ImportQCCEnt.class);
        if (list == null || list.size() == 0) {
            LOG.info("文件:{},导入数据:0行,返回", path);
        }

        LOG.info("文件:{},导入数据:{}行,开始导入数据库", path, list.size());
        List<EntDataEntity> entList = new ArrayList<>();
        EntDataEntity data;
        List<PhoneEntity> phones;
        List<TelPhoneEntity> telPhones;
        PhoneEntity p;
        TelPhoneEntity tel;
        List<EmailEntity> emails;
        EmailEntity e;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        for (ImportQCCEnt q : list) {
            data = new EntDataEntity();
            BeanUtils.copyProperties(q, data);
            // 处理手机号
            phones = new ArrayList<>();
            telPhones = new ArrayList<>();
            if (StringUtil.isNotEmpty(q.getPhoneNumbers())) {
                for (String phone : q.getPhoneNumbers().split(",")) {
                    if ("-".equals(phone)) {
                        continue;
                    }
                    if (StringUtil.isEmpty(phone)) {
                        continue;
                    }
                    if (phone.startsWith("0") || phone.indexOf("-") > 1) {
                        tel = new TelPhoneEntity(phone, now.getTime(), source, sourceWeb);
                        telPhones.add(tel);
                    } else {
                        p = new PhoneEntity(phone, now.getTime(), source, sourceWeb);
                        phones.add(p);
                    }
                }
            }
            // 更多电话
            if (StringUtil.isNotEmpty(q.getPhoneNumbers_1())) {
                for (String phone : q.getPhoneNumbers_1().split(",")) {
                    if ("-".equals(phone)) {
                        continue;
                    }
                    if (StringUtil.isEmpty(phone)) {
                        continue;
                    }
                    if (phone.startsWith("0") || phone.indexOf("-") > 1) {
                        tel = new TelPhoneEntity(phone, now.getTime(), source, sourceWeb);
                        telPhones.add(tel);
                    } else {
                        p = new PhoneEntity(phone, now.getTime(), source, sourceWeb);
                        phones.add(p);
                    }
                }
            }
            data.setPhoneNumbers(phones);
            data.setTelPhoneNumbers(telPhones);
            // 处理邮箱
            if (StringUtil.isNotEmpty(q.getEmail())) {
                emails = new ArrayList<>();
                for (String email : q.getEmail().split(",")) {
                    if ("-".equals(email)) {
                        continue;
                    }
                    e = new EmailEntity(email, now.getTime(), source, sourceWeb);
                    emails.add(e);
                }
                data.setEmail(emails);
            }
            entList.add(data);
        }
        return batchSaveEntData(entList, source, sourceWeb);
    }

    private int batchSaveEntData(List<EntDataEntity> personList, String source, String sourceWeb) {
        String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 创建天数据表
        jdbcTemplate.execute("create table IF NOT EXISTS enterprise_info_" + yearMonth + " LIKE enterprise_info");

        String sql = "INSERT INTO enterprise_info_" + yearMonth + " (content, create_time) VALUES (?,?);";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        /*String selectSql = "SELECT property_value FROM enterprise_info_property WHERE id = ? AND property_name=?";
        String updateSql = "UPDATE enterprise_info_property SET property_value = ?,update_time=? WHERE id = ? AND property_name=?";
        String insertSql = "INSERT INTO `enterprise_info_property` (`id`, `property_name`, `property_value`, `create_time`) VALUES (?,?,?,?);";
        List<EntDataPropertyEntity> phoneList = new ArrayList<>();*/
        int[] ints = new int[0];
        try {
            ints = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    /*Long ent_id = 0L;
                    try {
                        ent_id = sequenceService.getSeq("ent_id");
                    } catch (Exception e) {
                        try {
                            ent_id = sequenceService.getSeq("ent_id");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }*/
                    String ent_id = MD5Util.encode32Bit(personList.get(i).getEntEnName() + "lianke" + personList.get(i).getCreditCode());
                    personList.get(i).setId(ent_id.toString());
                    personList.get(i).setS_tag("1");
                    preparedStatement.setString(1, JSON.toJSONString(personList.get(i)));
                    preparedStatement.setTimestamp(2, now);
                    // 处理手机号来源
                    /*if (personList.get(i).getPhoneNumbers().size() > 0) {
                        boolean update = false;
                        JSONArray jsonArray = new JSONArray();
                        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, ent_id, "phone_source");
                        if (list.size() > 0) {
                            update = true;
                            jsonArray.addAll(JSON.parseArray(list.get(0).get("property_value").toString()));
                        }
                        List<PhoneEntity> phones = personList.get(i).getPhoneNumbers();
                        PhoneEntity p;
                        for (PhoneEntity phone : phones) {
                            p = new PhoneEntity(phone.getPhone(), now.getTime(), phone.getSource(), phone.getSourceWeb());
                            jsonArray.add(p);
                        }
                        if (update) {
                            // 更新
                            jdbcTemplate.update(updateSql, jsonArray.toJSONString(), now, ent_id, "phone_source");
                        } else {
                            EntDataPropertyEntity property = new EntDataPropertyEntity(ent_id, "phone_source", jsonArray.toJSONString());
                            phoneList.add(property);
                        }
                    }
                    // 处理邮箱来源
                    if (personList.get(i).getEmail().size() > 0) {
                        String propertyName = "email_source";
                        boolean update = false;
                        JSONArray jsonArray = new JSONArray();
                        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, ent_id, propertyName);
                        if (list.size() > 0) {
                            update = true;
                            jsonArray.addAll(JSON.parseArray(list.get(0).get("property_value").toString()));
                        }
                        List<EmailEntity> emails = personList.get(i).getEmail();
                        EmailEntity p;
                        for (EmailEntity email : emails) {
                            p = new EmailEntity(email.getEmail(), now.getTime(), email.getSource(), email.getSourceWeb());
                            jsonArray.add(p);
                        }
                        if (update) {
                            // 更新
                            jdbcTemplate.update(updateSql, jsonArray.toJSONString(), now, ent_id, propertyName);
                        } else {
                            EntDataPropertyEntity property = new EntDataPropertyEntity(ent_id, propertyName, jsonArray.toJSONString());
                            phoneList.add(property);
                        }
                    }*/
                }

                @Override
                public int getBatchSize() {
                    return personList.size();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

       /* try {
            jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    preparedStatement.setString(1, phoneList.get(i).getId());
                    preparedStatement.setString(2, phoneList.get(i).getPropertyName());
                    preparedStatement.setString(3, phoneList.get(i).getPropertyValue());
                    preparedStatement.setTimestamp(4, now);
                }

                @Override
                public int getBatchSize() {
                    return phoneList.size();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        System.out.println("enterprise_info_" + yearMonth + "导入数据:[" + ints.length + "]条");
        return ints.length;
    }


    public void importDayDataToES(LocalDateTime localTime, String sTag) {
        String yearMonth = localTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<Map<String, Object>> count = null;
        try {
            count = jdbcTemplate.queryForList("select count(0) count from enterprise_info_" + yearMonth);
        } catch (DataAccessException e) {
            LOG.error("查询数据异常,", e);
            return;
        }
        long total = 0L;
        if (count.size() > 0) {
            total = NumberConvertUtil.parseLong(count.get(0).get("count"));
        }
        int limit = 50000;
        for (int i = 0; i <= total; i += limit) {
            List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from enterprise_info_" + yearMonth + " where create_time between ? and ? LIMIT ?,?",
                    localTime.withHour(0).withMinute(0).withSecond(0), localTime.withHour(23).withMinute(59).withSecond(59), i, limit);
            if (list.size() > 0) {
                List<JSONObject> data = new ArrayList<>();
                for (Map<String, Object> m : list) {
                    JSONObject jsonObject = JSON.parseObject(String.valueOf(m.get("content")));
                    jsonObject.put("createTime", m.get("create_time"));
                    jsonObject.put("updateTime", m.get("update_time"));
                    if (jsonObject.getTimestamp("createTime") != null) {
                        jsonObject.put("createTime", jsonObject.getTimestamp("createTime").getTime());
                    }
                    if (jsonObject.getTimestamp("updateTime") != null) {
                        jsonObject.put("updateTime", jsonObject.getTimestamp("updateTime").getTime());
                    }
                    for (Map.Entry<String, Object> k : jsonObject.entrySet()) {
                        if ("-".equals(String.valueOf(k.getValue()))) {
                            k.setValue("");
                        }
                        boolean s = k.getKey().indexOf("Date") > 0 || k.getKey().indexOf("date") > 0 ||
                                k.getKey().indexOf("Time") > 0 || k.getKey().indexOf("time") > 0;
                        if (s && StringUtil.isNotEmpty(String.valueOf(k.getValue()))
                                && String.valueOf(k.getValue()).indexOf("-") > 1) {
                            DateTime parse = DateUtil.parse(String.valueOf(k.getValue()), "yyyy-MM-dd");
                            k.setValue(parse.getTime());
                        }
                    }

                    jsonObject.put("s_tag", sTag);
                    jsonObject.put("id", MD5Util.encode32Bit(jsonObject.getString("entName") + "lianke" + jsonObject.getString("creditCode")));
                    //elasticSearchService.addDocumentToType("test", "business",jsonObject.getString("id"),jsonObject);
                    data.add(jsonObject);
                }
                elasticSearchService.bulkInsertDocument0("ent_data", "business", data);
            }
        }
    }

    public void importHY88DataToES(String tableName, String sTag, String industryEn, String index, String type) {
        List<Map<String, Object>> count = null;
        try {
            count = jdbcTemplate.queryForList("select count(0) count from " + tableName);
        } catch (DataAccessException e) {
            LOG.error("查询数据异常,", e);
            return;
        }
        long total = 0L;
        if (count.size() > 0) {
            total = NumberConvertUtil.parseLong(count.get(0).get("count"));
        }
        System.out.println(tableName + "-" + total);
        int limit = 10000;
        for (int i = 0; i <= total; i += limit) {
            List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from " + tableName + " LIMIT ?,?", i, limit);
            if (list.size() > 0) {
                List<JSONObject> data = new ArrayList<>();
                for (Map<String, Object> m : list) {
                    JSONObject jsonObject = JSON.parseObject(String.valueOf(m.get("content")));
                    jsonObject.put("createTime", m.get("create_time"));
                    jsonObject.put("updateTime", m.get("update_time"));
                    if (jsonObject.getTimestamp("createTime") != null) {
                        jsonObject.put("createTime", jsonObject.getTimestamp("createTime").getTime());
                    }
                    if (jsonObject.getTimestamp("updateTime") != null) {
                        jsonObject.put("updateTime", jsonObject.getTimestamp("updateTime").getTime());
                    }
                    for (Map.Entry<String, Object> k : jsonObject.entrySet()) {
                        if ("-".equals(String.valueOf(k.getValue()))) {
                            k.setValue("");
                        }
                        boolean s = k.getKey().indexOf("Date") > 0 || k.getKey().indexOf("date") > 0 ||
                                k.getKey().indexOf("Time") > 0 || k.getKey().indexOf("time") > 0;

                        if (s && StringUtil.isNotEmpty(String.valueOf(k.getValue()))) {
                            if (String.valueOf(k.getValue()).indexOf("2") < 0) {
                                k.setValue(null);
                                continue;
                            }
                            k.setValue(String.valueOf(k.getValue()).replaceAll(" ", "")
                                    .replaceAll("年", "-").replaceAll("月", "-")
                                    .replaceAll("日", "-"));
                        }

                        if (s && StringUtil.isNotEmpty(String.valueOf(k.getValue()))
                                && String.valueOf(k.getValue()).indexOf("-") > 1) {
                            k.setValue(String.valueOf(k.getValue()).replaceAll(" ", ""));
                            if (String.valueOf(k.getValue()).indexOf("至") > 1) {
                                System.out.println("至:" + k.getValue());
                                if (k.getKey().equals("fromTime")) {
                                    k.setValue(String.valueOf(k.getValue()).split("至")[0].replaceAll(" ", ""));
                                } else if (k.getKey().equals("toTime") && String.valueOf(k.getValue()).split("至").length > 1) {
                                    k.setValue(String.valueOf(k.getValue()).split("至")[1].replaceAll(" ", ""));
                                }
                            }
                            DateTime parse = DateUtil.parse(String.valueOf(k.getValue()).replaceAll("至", "")
                                    .replaceAll("年", "-").replaceAll("月", "-")
                                    .replaceAll("日", "-"), "yyyy-MM-dd");
                            k.setValue(parse.getTime());
                        }
                    }
                    jsonObject.put("industryEn", industryEn);
                    jsonObject.put("s_tag", sTag);
                    jsonObject.put("id", MD5Util.encode32Bit(jsonObject.getString("entName") + "lianke" + jsonObject.getString("creditCode")));
                    data.add(jsonObject);
                }
                elasticSearchService.bulkInsertDocument0(index, type, data);
            }
        }
    }

    public void importMKDataToES(String tableName, String sTag, String industryEn, String index, String type) {
        List<Map<String, Object>> count = null;
        try {
            count = jdbcTemplate.queryForList("select count(0) count from " + tableName);
        } catch (DataAccessException e) {
            LOG.error("查询数据异常,", e);
            return;
        }
        long total = 0L;
        if (count.size() > 0) {
            total = NumberConvertUtil.parseLong(count.get(0).get("count"));
        }
        System.out.println(tableName + "-" + total);
        int limit = 10000;
        for (int i = 0; i <= total; i += limit) {
            List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from " + tableName + " LIMIT ?,?", i, limit);
            if (list.size() > 0) {
                List<JSONObject> data = new ArrayList<>();
                for (Map<String, Object> m : list) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = JSON.parseObject(String.valueOf(m.get("content")));
                    } catch (Exception e) {
                        LOG.error("解析content异常,content:{}", m.get("content"), e);
                        continue;
                    }
                    jsonObject.put("createTime", m.get("create_time"));
                    jsonObject.put("updateTime", m.get("update_time"));
                    jsonObject.put("industry", m.get("industry"));
                    jsonObject.put("province", m.get("province"));
                    jsonObject.put("indexUrl", m.get("index_url"));
                    if (jsonObject.getTimestamp("createTime") != null) {
                        jsonObject.put("createTime", jsonObject.getTimestamp("createTime").getTime());
                    }
                    if (jsonObject.getTimestamp("updateTime") != null) {
                        jsonObject.put("updateTime", jsonObject.getTimestamp("updateTime").getTime());
                    }
                    for (Map.Entry<String, Object> k : jsonObject.entrySet()) {
                        if ("-".equals(String.valueOf(k.getValue()))) {
                            k.setValue("");
                        }
                        boolean s = k.getKey().indexOf("Date") > 0 || k.getKey().indexOf("date") > 0 ||
                                k.getKey().indexOf("Time") > 0 || k.getKey().indexOf("time") > 0;

                        if (s && StringUtil.isNotEmpty(String.valueOf(k.getValue()))) {
                            if (String.valueOf(k.getValue()).indexOf("2") < 0) {
                                k.setValue(null);
                                continue;
                            }
                            k.setValue(String.valueOf(k.getValue()).replaceAll(" ", "")
                                    .replaceAll("年", "-").replaceAll("月", "-")
                                    .replaceAll("日", "-"));
                        }

                        if (s && StringUtil.isNotEmpty(String.valueOf(k.getValue()))
                                && String.valueOf(k.getValue()).indexOf("-") > 1) {
                            k.setValue(String.valueOf(k.getValue()).replaceAll(" ", ""));
                            if (String.valueOf(k.getValue()).indexOf("至") > 1) {
                                System.out.println("至:" + k.getValue());
                                if (k.getKey().equals("fromTime")) {
                                    k.setValue(String.valueOf(k.getValue()).split("至")[0].replaceAll(" ", ""));
                                } else if (k.getKey().equals("toTime") && String.valueOf(k.getValue()).split("至").length > 1) {
                                    k.setValue(String.valueOf(k.getValue()).split("至")[1].replaceAll(" ", ""));
                                }
                            }
                            DateTime parse = null;
                            try {
                                parse = DateUtil.parse(String.valueOf(k.getValue()).replaceAll("至", "")
                                        .replaceAll("年", "-").replaceAll("月", "-")
                                        .replaceAll("日", "-"), "yyyy-MM-dd");
                            } catch (DateException e) {
                                LOG.error("企业名称:{},[{}]转换异常,数据:{}", jsonObject.getString("entName"), k.getKey(), jsonObject);
                                k.setValue(null);
                                continue;
                            }
                            k.setValue(parse.getTime());
                        }
                    }
                    jsonObject.put("industryEn", industryEn);
                    jsonObject.put("s_tag", sTag);
                    jsonObject.put("id", MD5Util.encode32Bit(jsonObject.getString("entName") + "lianke" + jsonObject.getString("creditCode")));
                    data.add(jsonObject);
                }
                elasticSearchService.bulkInsertDocument0(index, type, data);
            }
        }
    }

    public void importWGDataToES(String tableName, String sTag, String industryEn, String index, String type) {
        List<Map<String, Object>> count = null;
        try {
            count = jdbcTemplate.queryForList("select count(0) count from " + tableName);
        } catch (DataAccessException e) {
            LOG.error("查询数据异常,", e);
            return;
        }
        long total = 0L;
        if (count.size() > 0) {
            total = NumberConvertUtil.parseLong(count.get(0).get("count"));
        }
        System.out.println(tableName + "-" + total);
        int limit = 10000;
        for (int i = 0; i <= total; i += limit) {
            List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from " + tableName + " LIMIT ?,?", i, limit);
            if (list.size() > 0) {
                List<JSONObject> data = new ArrayList<>();
                for (Map<String, Object> m : list) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = JSON.parseObject(String.valueOf(m.get("content")));
                    } catch (Exception e) {
                        LOG.error("解析content异常,content:{}", m.get("content"), e);
                        continue;
                    }
                    jsonObject.put("createTime", m.get("create_time"));
                    jsonObject.put("updateTime", m.get("update_time"));
                    jsonObject.put("industry", m.get("industry"));
                    jsonObject.put("province", m.get("province"));
                    jsonObject.put("indexUrl", m.get("index_url"));
                    jsonObject.put("page", m.get("page"));
                    if (jsonObject.getTimestamp("createTime") != null) {
                        jsonObject.put("createTime", jsonObject.getTimestamp("createTime").getTime());
                    }
                    if (jsonObject.getTimestamp("updateTime") != null) {
                        jsonObject.put("updateTime", jsonObject.getTimestamp("updateTime").getTime());
                    }
                    for (Map.Entry<String, Object> k : jsonObject.entrySet()) {
                        if ("-".equals(String.valueOf(k.getValue()))) {
                            k.setValue("");
                        }
                        boolean s = k.getKey().indexOf("Date") > 0 || k.getKey().indexOf("date") > 0 ||
                                k.getKey().indexOf("Time") > 0 || k.getKey().indexOf("time") > 0;

                        if (s && StringUtil.isNotEmpty(String.valueOf(k.getValue()))) {
                            if (String.valueOf(k.getValue()).indexOf("2") < 0) {
                                k.setValue(null);
                                continue;
                            }
                            k.setValue(String.valueOf(k.getValue()).replaceAll(" ", "")
                                    .replaceAll("年", "-").replaceAll("月", "-")
                                    .replaceAll("日", "-"));
                        }

                        if (s && StringUtil.isNotEmpty(String.valueOf(k.getValue()))
                                && String.valueOf(k.getValue()).indexOf("-") > 1) {
                            k.setValue(String.valueOf(k.getValue()).replaceAll(" ", ""));
                            if (String.valueOf(k.getValue()).indexOf("至") > 1) {
                                System.out.println("至:" + k.getValue());
                                if (k.getKey().equals("fromTime")) {
                                    k.setValue(String.valueOf(k.getValue()).split("至")[0].replaceAll(" ", ""));
                                } else if (k.getKey().equals("toTime") && String.valueOf(k.getValue()).split("至").length > 1) {
                                    k.setValue(String.valueOf(k.getValue()).split("至")[1].replaceAll(" ", ""));
                                }
                            }
                            DateTime parse = null;
                            try {
                                parse = DateUtil.parse(String.valueOf(k.getValue()).replaceAll("至", "")
                                        .replaceAll("年", "-").replaceAll("月", "-")
                                        .replaceAll("日", "-"), "yyyy-MM-dd");
                            } catch (DateException e) {
                                LOG.error("企业名称:{},[{}]转换异常,数据:{}", jsonObject.getString("entName"), k.getKey(), jsonObject);
                                k.setValue(null);
                                continue;
                            }
                            k.setValue(parse.getTime());
                        }
                    }
                    jsonObject.put("industryEn", industryEn);
                    jsonObject.put("s_tag", sTag);
                    jsonObject.put("id", MD5Util.encode32Bit(jsonObject.getString("entName") + "lianke" + jsonObject.getString("creditCode")));
                    data.add(jsonObject);
                }
                elasticSearchService.bulkInsertDocument0(index, type, data);
            }
        }
    }

    public void importQY9DataToES(String tableName, String sTag, String industryEn, String index, String type) {
        List<Map<String, Object>> count = null;
        try {
            count = jdbcTemplate.queryForList("select count(0) count from " + tableName);
        } catch (DataAccessException e) {
            LOG.error("查询数据异常,", e);
            return;
        }
        long total = 0L;
        if (count.size() > 0) {
            total = NumberConvertUtil.parseLong(count.get(0).get("count"));
        }
        System.out.println(tableName + "-" + total);
        int limit = 10000;
        for (int i = 0; i <= total; i += limit) {
            List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from " + tableName + " LIMIT ?,?", i, limit);
            if (list.size() > 0) {
                List<JSONObject> data = new ArrayList<>();
                for (Map<String, Object> m : list) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = JSON.parseObject(String.valueOf(m.get("content")));
                    } catch (Exception e) {
                        LOG.error("解析content异常,content:{}", m.get("content"), e);
                        continue;
                    }
                    jsonObject.put("createTime", m.get("create_time"));
                    jsonObject.put("updateTime", m.get("update_time"));
                    jsonObject.put("province", m.get("province"));
                    if (jsonObject.getTimestamp("createTime") != null) {
                        jsonObject.put("createTime", jsonObject.getTimestamp("createTime").getTime());
                    }
                    if (jsonObject.getTimestamp("updateTime") != null) {
                        jsonObject.put("updateTime", jsonObject.getTimestamp("updateTime").getTime());
                    }
                    for (Map.Entry<String, Object> k : jsonObject.entrySet()) {
                        if ("-".equals(String.valueOf(k.getValue()))) {
                            k.setValue("");
                        }
                        boolean s = k.getKey().indexOf("Date") > 0 || k.getKey().indexOf("date") > 0 ||
                                k.getKey().indexOf("Time") > 0 || k.getKey().indexOf("time") > 0;

                        if (s && StringUtil.isNotEmpty(String.valueOf(k.getValue()))) {
                            if (String.valueOf(k.getValue()).indexOf("2") < 0) {
                                k.setValue(null);
                                continue;
                            }
                            k.setValue(String.valueOf(k.getValue()).replaceAll(" ", "")
                                    .replaceAll("年", "-").replaceAll("月", "-")
                                    .replaceAll("日", "-"));
                        }

                        if (s && StringUtil.isNotEmpty(String.valueOf(k.getValue()))
                                && String.valueOf(k.getValue()).indexOf("-") > 1) {
                            k.setValue(String.valueOf(k.getValue()).replaceAll(" ", ""));
                            if (String.valueOf(k.getValue()).indexOf("至") > 1) {
                                System.out.println("至:" + k.getValue());
                                if (k.getKey().equals("fromTime")) {
                                    k.setValue(String.valueOf(k.getValue()).split("至")[0].replaceAll(" ", ""));
                                } else if (k.getKey().equals("toTime") && String.valueOf(k.getValue()).split("至").length > 1) {
                                    k.setValue(String.valueOf(k.getValue()).split("至")[1].replaceAll(" ", ""));
                                }
                            }
                            DateTime parse = null;
                            try {
                                parse = DateUtil.parse(String.valueOf(k.getValue()).replaceAll("至", "")
                                        .replaceAll("年", "-").replaceAll("月", "-")
                                        .replaceAll("日", "-"), "yyyy-MM-dd");
                            } catch (DateException e) {
                                LOG.error("企业名称:{},[{}]转换异常,数据:{}", jsonObject.getString("entName"), k.getKey(), jsonObject);
                                k.setValue(null);
                                continue;
                            }
                            k.setValue(parse.getTime());
                        }
                    }
                    jsonObject.put("industryEn", industryEn);
                    jsonObject.put("s_tag", sTag);
                    jsonObject.put("id", MD5Util.encode32Bit(jsonObject.getString("entName") + "lianke" + jsonObject.getString("creditCode")));
                    data.add(jsonObject);
                }
                elasticSearchService.bulkInsertDocument0(index, type, data);
            }
        }
    }

    public SearchSourceBuilder queryCondition(JSONObject param) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (param.getInteger("pageNum") != null && param.getInteger("pageSize") != null) {
            int pageNum = param.getIntValue("pageNum");
            int pageSize = param.getIntValue("pageSize");
            if (pageNum < 0) {
                pageNum = 1;
            }
            if (pageSize < 0 || pageSize > 100) {
                pageSize = 100;
            }
            pageNum = (pageNum - 1) * pageSize;
            searchSourceBuilder.from(pageNum).size(pageSize);
        }
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        if (StringUtil.isNotEmpty(param.getString("id"))) {
            qb.must(QueryBuilders.idsQuery().addIds(param.getString("id")));
        }
        // 企业子类型
        if (StringUtil.isNotEmpty(param.getString("entType1"))) {
            BoolQueryBuilder temp = QueryBuilders.boolQuery();
            for (int i = 0; i < param.getJSONArray("entType1").size(); i++) {
                if ("外商".equals(param.getJSONArray("entType1").getString(i))) {
                    temp.should(QueryBuilders.wildcardQuery("entType", "*外商*"));
                    temp.should(QueryBuilders.wildcardQuery("entType", "*外国*"));
                    temp.should(QueryBuilders.wildcardQuery("entType", "*中外*"));
                } else {
                    temp.should(QueryBuilders.wildcardQuery("entType", "*" + param.getJSONArray("entType1").getString(i) + "*"));
                }
            }
            qb.must(temp);
        }
        // 投资控股
        if (StringUtil.isNotEmpty(param.getString("Investment"))) {
            qb.must(QueryBuilders.wildcardQuery("entType", "*" + param.getString("Investment") + "*"));
        }
        // 支持多个行业
        if (param.getJSONArray("industry") != null && param.getJSONArray("industry").size() > 0) {
            BoolQueryBuilder temp = QueryBuilders.boolQuery();
            JSONArray jsonArray = param.getJSONArray("industry");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray texts = jsonArray.getJSONObject(i).getJSONArray("value");
                for (int j = 0; j < texts.size(); j++) {
                    temp.should(QueryBuilders
                            .wildcardQuery("industry", "*" + texts.getString(j) + "*"));
                }

            }
            qb.must(temp);
        }
        // 企业名称 支持多个
        if (param.getJSONArray("entName") != null && param.getJSONArray("entName").size() > 0) {
            JSONArray jsonArray = param.getJSONArray("entName");
            BoolQueryBuilder temp = QueryBuilders.boolQuery();
            for (int i = 0; i < jsonArray.size(); i++) {
                // 1-包含任一词 2-包含全部词 3-排除任一词 4-排除全部词
                int typeName = jsonArray.getJSONObject(i).getInteger("typeName");
                JSONArray texts = jsonArray.getJSONObject(i).getJSONArray("value");
                if (typeName == 1) {
                    for (int j = 0; j < texts.size(); j++) {
                       /* MatchQueryBuilder mpq = QueryBuilders
                                .matchQuery("entName", text);
                        temp.should(mpq);*/
                        MatchPhraseQueryBuilder mpq = QueryBuilders
                                .matchPhraseQuery("entName", texts.getString(j));
                        mpq.slop(1);
                        temp.should(mpq);
                    }
                    qb.must(temp);
                } else if (typeName == 2) {
                    for (int j = 0; j < texts.size(); j++) {
                       /* TermQueryBuilder mpq = QueryBuilders
                                .termQuery("entName", texts.getString(j));*/
                        MatchPhraseQueryBuilder mpq = QueryBuilders
                                .matchPhraseQuery("entName", texts.getString(j));
                        mpq.slop(1);
                        temp.must(mpq);
                    }
                    qb.must(temp);
                } else if (typeName == 3) {
                    for (int j = 0; j < texts.size(); j++) {
                       /* MatchQueryBuilder mpq = QueryBuilders
                                .matchQuery("entName", text);
                        temp.should(mpq);*/
                        /*WildcardQueryBuilder mpq = QueryBuilders
                                .wildcardQuery("entName", "*" + texts.getString(j) + "*");*/
                        MatchPhraseQueryBuilder mpq = QueryBuilders
                                .matchPhraseQuery("entName", texts.getString(j));
                        mpq.slop(1);
                        temp.must(mpq);
                    }
                    qb.mustNot(temp);
                } else if (typeName == 4) {
                    for (int j = 0; j < texts.size(); j++) {
                        /*TermQueryBuilder mpq = QueryBuilders
                                .termQuery("entName", texts.getString(j));*/
                        MatchPhraseQueryBuilder mpq = QueryBuilders
                                .matchPhraseQuery("entName", texts.getString(j));
                        mpq.slop(1);
                        temp.must(mpq);
                    }
                    qb.mustNot(temp);
                }
            }

        }

        // 经营范围 支持多个
        if (param.getJSONArray("compayScope") != null && param.getJSONArray("compayScope").size() > 0) {
            JSONArray jsonArray = param.getJSONArray("compayScope");
            BoolQueryBuilder temp = QueryBuilders.boolQuery();
            for (int i = 0; i < jsonArray.size(); i++) {
                // 1-包含任一词 2-包含全部词 3-排除任一词 4-排除全部词
                int typeScope = jsonArray.getJSONObject(i).getInteger("typeScope");
                JSONArray texts = jsonArray.getJSONObject(i).getJSONArray("value");
                if (typeScope == 1) {
                    for (int j = 0; j < texts.size(); j++) {
                       /* WildcardQueryBuilder mpq = QueryBuilders
                                .wildcardQuery("opScope", "*" + texts.getString(j) + "*");*/

                        MatchPhraseQueryBuilder mpq = QueryBuilders
                                .matchPhraseQuery("opScope", texts.getString(j));
                        mpq.slop(1);
                        temp.should(mpq);
                    }
                    qb.must(temp);
                } else if (typeScope == 2) {
                    for (int j = 0; j < texts.size(); j++) {
                        /*TermQueryBuilder mpq = QueryBuilders
                                .termQuery("opScope", texts.getString(j));*/

                        MatchPhraseQueryBuilder mpq = QueryBuilders
                                .matchPhraseQuery("opScope", texts.getString(j));
                        mpq.slop(1);
                        temp.must(mpq);
                    }
                    qb.must(temp);
                } else if (typeScope == 3) {
                    for (int j = 0; j < texts.size(); j++) {
                        /*WildcardQueryBuilder mpq = QueryBuilders
                                .wildcardQuery("opScope", "*" + texts.getString(j) + "*");*/
                        MatchPhraseQueryBuilder mpq = QueryBuilders
                                .matchPhraseQuery("opScope", texts.getString(j));
                        mpq.slop(1);
                        temp.should(mpq);
                    }
                    qb.mustNot(temp);
                } else if (typeScope == 4) {
                    for (int j = 0; j < texts.size(); j++) {
                        /*TermQueryBuilder mpq = QueryBuilders
                                .termQuery("opScope", texts.getString(j));*/
                        MatchPhraseQueryBuilder mpq = QueryBuilders
                                .matchPhraseQuery("opScope", texts.getString(j));
                        mpq.slop(1);
                        temp.must(mpq);
                    }
                    qb.mustNot(temp);
                }
            }

        }
        // 经营状态
        if (param.getJSONArray("regStatus") != null && param.getJSONArray("regStatus").size() > 0) {
            JSONArray jsonArray = param.getJSONArray("regStatus");
            BoolQueryBuilder temp = QueryBuilders.boolQuery();
            for (int i = 0; i < jsonArray.size(); i++) {
                /*WildcardQueryBuilder mpq = QueryBuilders
                        .wildcardQuery("entStatus", "*" + jsonArray.getJSONObject(i).getString("value") + "*");*/
                WildcardQueryBuilder mpq = QueryBuilders
                        .wildcardQuery("entStatus", "*" + jsonArray.getJSONObject(i).getString("value") + "*");
                temp.should(mpq);
            }
            qb.must(temp);
        }

        // 注册地址
        if (param.getJSONArray("regLocation") != null && param.getJSONArray("regLocation").size() > 0) {
            JSONArray jsonArray = param.getJSONArray("regLocation");
            JSONObject regLocation;
            BoolQueryBuilder temp = QueryBuilders.boolQuery();
            for (int i = 0; i < jsonArray.size(); i++) {
                BoolQueryBuilder address = QueryBuilders.boolQuery();
                regLocation = jsonArray.getJSONObject(i);
                if (StringUtil.isNotEmpty(regLocation.getString("regarea"))) {
                    address.must(QueryBuilders.termQuery("regarea", regLocation.getString("regarea")));
                }
                if (StringUtil.isNotEmpty(regLocation.getString("regcity"))) {
                    address.must(QueryBuilders.termQuery("regcity", regLocation.getString("regcity")));
                }
                if (StringUtil.isNotEmpty(regLocation.getString("address"))) {
                    address.must(QueryBuilders.wildcardQuery("address", "*" + regLocation.getString("address") + "*"));
                }
                temp.should(address);
            }
            qb.must(temp);
        }
        // 注册时间
        if (StringUtil.isNotEmpty(param.getString("startTime")) && StringUtil.isNotEmpty(param.getString("endTime"))) {
            qb.filter(QueryBuilders.rangeQuery("estabTime").from(param.getDate("startTime").getTime()).to(param.getDate("endTime").getTime()));
        } else if (StringUtil.isNotEmpty(param.getString("startTime"))) {
            qb.filter(QueryBuilders.rangeQuery("estabTime").from(param.getDate("startTime").getTime()));
        } else if (StringUtil.isNotEmpty(param.getString("endTime"))) {
            qb.filter(QueryBuilders.rangeQuery("estabTime").to(param.getDate("endTime").getTime()));
        }

        // 注册资金
        if (StringUtil.isNotEmpty(param.getString("regCapital")) && StringUtil.isNotEmpty(param.getString("regCapital2"))) {
            qb.filter(QueryBuilders.rangeQuery("regCapNum").gt(param.getIntValue("regCapital")).lt(param.getIntValue("regCapital2")));
        } else if (StringUtil.isNotEmpty(param.getString("regCapital"))) {
            qb.filter(QueryBuilders.rangeQuery("regCapNum").from(param.getIntValue("regCapital")));
        } else if (StringUtil.isNotEmpty(param.getString("regCapital2"))) {
            qb.filter(QueryBuilders.rangeQuery("regCapNum").to(param.getIntValue("regCapital2")));
        }

        //来源
        if (StringUtil.isNotEmpty(param.getString("src"))) {
            qb.filter(QueryBuilders.termQuery("src", param.getString("src")));
        }
        // 联系电话
        if (StringUtil.isNotEmpty(param.getString("phoneStatus"))) {
            JSONArray jsonArray = param.getJSONArray("phoneStatus");
            BoolQueryBuilder temp = QueryBuilders.boolQuery();
            for (int i = 0; i < jsonArray.size(); i++) {
                String phoneStatus = jsonArray.getJSONObject(i).getString("value");
                // 有固话
                if ("1".equals(phoneStatus)) {
                    temp.should(QueryBuilders.regexpQuery("phone1", "[0-9].+"));
                } else if ("2".equals(phoneStatus)) {
                    // 有手机
                    temp.should(QueryBuilders.regexpQuery("phone", "1[3|4|5|7|8].*"));
                } else if ("3".equals(phoneStatus)) {
                    // 无联系方式
                    temp.mustNot(QueryBuilders.regexpQuery("phone1", "[0-9].+"));
                    temp.mustNot(QueryBuilders.regexpQuery("phone", "1[3|4|5|7|8].*"));
                }
            }
            qb.filter(temp);
        }
        // 联系电话
        /*if (StringUtil.isNotEmpty(param.getString("phoneStatus"))) {
            String phoneStatus = param.getString("phoneStatus");
            // 有联系电话
            if ("1".equals(phoneStatus)) {
                qb.must(QueryBuilders.regexpQuery("phone", "[0-9].+"));
                //qb.mustNot(QueryBuilders.matchQuery("phone", ","));
            }*//* else if ("2".equals(phoneStatus)) {
                // 有手机
                qb.must(QueryBuilders.regexpQuery("phone", "1[3|4|5|7|8].*"));
            } *//* else if ("2".equals(phoneStatus)) {
                // 无联系方式
                qb.mustNot(QueryBuilders.regexpQuery("phone", "[0-9].+"));
            }
        }*/
        // 邮箱
        if (StringUtil.isNotEmpty(param.getString("emailStatus"))) {
            // 有邮箱
            if ("1".equals(param.getString("emailStatus"))) {
                qb.filter(QueryBuilders.regexpQuery("email", "[0-9|a-z|A-Z]@.+"));
            } else if ("2".equals(param.getString("emailStatus"))) {
                // 无邮箱
                qb.mustNot(QueryBuilders.regexpQuery("email", "[0-9|a-z|A-Z]@.+"));
            }
        }
        // 其他标签
        if (StringUtil.isNotEmpty(param.getString("tag"))) {
            if ("tag1".equals(param.getString("tag"))) {
                qb.must(QueryBuilders.wildcardQuery("tag", "*高新企业*"));
            } else {
                qb.must(QueryBuilders.wildcardQuery("tag", "*" + param.getString("tag") + "*"));
            }

        }
        searchSourceBuilder.query(qb);
        return searchSourceBuilder;
    }


    public Page pageSearch(String custId, String custGroupId, Long custUserId, String busiType, JSONObject params) throws Exception {
        Page page = new Page();
        LOG.info("企业列表查询参数:{}", params);
        // 构造DSL语句
        SearchSourceBuilder searchSourceBuilder = queryCondition(params);
        String _orderby_ = params.getString("_orderby_");
        String _sort_ = params.getString("_sort_");
        if (StringUtil.isNotEmpty(_orderby_) && StringUtil.isNotEmpty(_sort_)) {
            if ("asc".equals(_sort_) || "desc".equals(_sort_)) {
                searchSourceBuilder.sort(_orderby_, SortOrder.valueOf(_sort_.toUpperCase()));
            }
        }
        SearchResult result = elasticSearchService.search(searchSourceBuilder.toString(), AppConfig.getEnt_data_index(), AppConfig.getEnt_data_type());
        if (result != null && result.isSucceeded() && result.getHits(JSONObject.class) != null) {
            List list = new ArrayList<>();
            JSONObject t;
            int sum;
            for (SearchResult.Hit<JSONObject, Void> hit : result.getHits(JSONObject.class)) {
                t = hit.source;
                t.put("id", hit.id);
                // 处理企业领取标志
                t.put("_receivingStatus", b2BTcbLogService.checkClueGetStatus(custId, t.getString("id")));
                sum = 0;
                if (StringUtil.isNotEmpty(t.getString("phone"))) {
                    for (String p : t.getString("phone").split(",")) {
                        if (StringUtil.isNotEmpty(p.trim().replaceAll(" ", ""))
                                && !"-".equals(p)) {
                            sum++;
                        }
                    }
                }
                if (StringUtil.isNotEmpty(t.getString("phone1"))) {
                    for (String p : t.getString("phone1").split(",")) {
                        if (StringUtil.isNotEmpty(p.trim().replaceAll(" ", ""))
                                && !"-".equals(p)) {
                            sum++;
                        }
                    }
                }
                t.put("sum", sum);
                list.add(t);
            }
            page.setData(list);
            int maxTotal = 100000;
            int total = NumberConvertUtil.parseInt(result.getTotal());
            page.setTotal(total);
            if (total > maxTotal) {
                page.setTotal(maxTotal);
            }
        }
        return page;
    }

    /**
     * 查询指定条件下的数据总量
     *
     * @param custId
     * @param custGroupId
     * @param custUserId
     * @param busiType
     * @param params
     * @return
     */
    public Map count(String custId, String custGroupId, Long custUserId, String busiType, JSONObject params) {
        Map data = new HashMap();
        LOG.info("企业检索总数查询参数:{}", params);
        // 构造DSL语句
        params.remove("pageNum");
        params.remove("pageSize");
        SearchSourceBuilder searchSourceBuilder = queryCondition(params);
        CountResult result = elasticSearchService.count(searchSourceBuilder.toString(), AppConfig.getEnt_data_index(), AppConfig.getEnt_data_type());
        data.put("total", JSON.parseObject(result.getJsonString()).get("count"));
        return data;
    }
    /*public Page pageSearch0(String custId, String custGroupId, Long custUserId, String busiType, JSONObject params)  {
        Page page = new Page();
        LOG.info("企业列表查询参数:{}", params);
        // 构造DSL语句
        SearchSourceBuilder searchSourceBuilder = queryCondition(params);
        System.out.println(searchSourceBuilder.toString());
        SearchResponse result = elasticSearchService.searchByClient(searchSourceBuilder.toString(), AppConfig.getEnt_data_index(), AppConfig.getEnt_data_type());
        if (result != null  && result.getHits() != null) {
            List list = new ArrayList<>();
            JSONObject t;
            int sum;
            for (SearchHit hit : result.getHits()) {
                t = JSON.parseObject(hit.getSourceAsString());
                // 处理企业领取标志
                t.put("_receivingStatus", b2BTcbLogService.checkClueGetStatus(custId, t.getString("id")));
                sum = 0;
                if (StringUtil.isNotEmpty(t.getString("phone"))) {
                    for (String p : t.getString("phone").split(",")) {
                        if (StringUtil.isNotEmpty(p.trim().replaceAll(" ", ""))
                                && !"-".equals(p)) {
                            sum++;
                        }
                    }
                }
                t.put("sum", sum);
                list.add(t);
            }
            page.setData(list);
            page.setTotal(JSON.parseObject(result.toString()).getJSONObject("hits").getIntValue("total"));
        }
        return page;
    }*/

    public JSONObject getCompanyDetail(String companyId, JSONObject param, String busiType, long seaId) {
        JSONObject baseResult = elasticSearchService.getDocumentById0(AppConfig.getEnt_data_index(), AppConfig.getEnt_data_type(), companyId);
        if (baseResult != null) {
            Set phones = new HashSet();
            if (baseResult.containsKey("phone") && StringUtil.isNotEmpty(baseResult.getString("phone"))) {
                // 手机号
                for (String p : baseResult.getString("phone").split(",")) {
                    if (StringUtil.isEmpty(p) || "-".equals(p)) {
                        continue;
                    }
                    phones.add(p);
                }
            }
            if (baseResult.containsKey("phone1") && StringUtil.isNotEmpty(baseResult.getString("phone1"))) {
                // 固话
                for (String p : baseResult.getString("phone1").split(",")) {
                    if (StringUtil.isEmpty(p) || "-".equals(p)) {
                        continue;
                    }
                    phones.add(p);
                }
            }
            baseResult.put("phones", phones);
            if (seaId > 0) {
                //处理公司联系方式是否有意向
                handleClueFollowStatus(seaId, baseResult);
            }
        }
        return baseResult;
    }

    /**
     * 根据企业名称查询企业详情
     *
     * @param companyName
     * @return
     */
    public JSONObject getCompanyByName(String companyName) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        qb.filter(QueryBuilders.termQuery("entName", companyName));
        searchSourceBuilder.query(qb);
        SearchResult result = elasticSearchService.search(searchSourceBuilder.toString(), AppConfig.getEnt_data_index(), AppConfig.getEnt_data_type());
        JSONObject t = null;
        if (result != null && result.isSucceeded() && result.getHits(JSONObject.class) != null) {
            int sum;
            for (SearchResult.Hit<JSONObject, Void> hit : result.getHits(JSONObject.class)) {
                t = hit.source;
                t.put("id", hit.id);
                sum = 0;
                if (StringUtil.isNotEmpty(t.getString("phone"))) {
                    for (String p : t.getString("phone").split(",")) {
                        if (StringUtil.isNotEmpty(p.trim().replaceAll(" ", ""))
                                && !"-".equals(p)) {
                            sum++;
                        }
                    }
                }
                if (StringUtil.isNotEmpty(t.getString("phone1"))) {
                    for (String p : t.getString("phone1").split(",")) {
                        if (StringUtil.isNotEmpty(p.trim().replaceAll(" ", ""))
                                && !"-".equals(p)) {
                            sum++;
                        }
                    }
                }
                t.put("sum", sum);
                break;
            }
        }
        return t;
    }

    /**
     * 处理公司联系方式是否有意向
     *
     * @param seaId
     * @param data
     */
    private void handleClueFollowStatus(long seaId, JSONObject data) {
        if (data.containsKey("phone") && StringUtil.isNotEmpty(data.getString("phone"))) {
            String sql = "SELECT id from t_customer_sea_list_" + seaId + " WHERE JSON_EXTRACT(super_data, '$.SYS007') = ? AND id = ?";
            JSONArray clueFollowStatus = new JSONArray();
            JSONObject jsonObject = null;
            String uid = "";
            List<Map<String, Object>> id = null;
            for (String p : data.getString("phone").split(",")) {
                if (StringUtil.isEmpty(p) || "-".equals(p)) {
                    continue;
                }
                jsonObject = new JSONObject();
                uid = phoneService.savePhoneToAPI(p);
                id = jdbcTemplate.queryForList(sql, "意向线索", uid);
                jsonObject.put("phone", p);
                jsonObject.put("status", id == null || id.size() == 0 ? false : true);
                clueFollowStatus.add(jsonObject);
            }
            data.put("clueFollowStatus", clueFollowStatus);
        }
    }

    public int addCrmClueQueue(String custId, long userId, long num, JSONObject condition, int type) {
        int update = jdbcTemplate.update("INSERT INTO `lkcrm_clue_queue` (`cust_id`, `user_id`, `num`, `condition`, `type`, `create_time`,status) VALUES (?, ?, ?, ?, ?, ?, ?);",
                custId, userId, num, condition.toJSONString(), type, DateUtil.date().toTimestamp(), 1);
        return update;
    }

}
