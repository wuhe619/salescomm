package com.bdaim.crm.ent.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.crm.*;
import com.bdaim.util.ExcelUtil;
import com.bdaim.util.MD5Util;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                    JSONObject jsonObject = JSON.parseObject(String.valueOf(m.get("content")));
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
                    JSONObject jsonObject = JSON.parseObject(String.valueOf(m.get("content")));
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
}
