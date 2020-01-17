package com.bdaim.crm.ent.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.crm.*;
import com.bdaim.util.ExcelUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
        PhoneEntity p;
        List<EmailEntity> emails;
        EmailEntity e;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        for (ImportQCCEnt q : list) {
            data = new EntDataEntity();
            BeanUtils.copyProperties(q, data);
            // 处理手机号
            if (StringUtil.isNotEmpty(q.getPhoneNumbers())) {
                phones = new ArrayList<>();
                for (String phone : q.getPhoneNumbers().split(",")) {
                    p = new PhoneEntity(phone, now.getTime(), source, sourceWeb);
                    phones.add(p);
                }
                data.setPhoneNumbers(phones);
            }
            // 处理邮箱
            if (StringUtil.isNotEmpty(q.getEmail())) {
                emails = new ArrayList<>();
                for (String email : q.getEmail().split(",")) {
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
        String sql = "INSERT INTO enterprise_info_" + yearMonth + " (content, create_time) VALUES (?,?);";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String selectSql = "SELECT property_value FROM enterprise_info_property WHERE id = ? AND property_name=?";
        String updateSql = "UPDATE enterprise_info_property SET property_value = ?,update_time=? WHERE id = ? AND property_name=?";
        String insertSql = "INSERT INTO `enterprise_info_property` (`id`, `property_name`, `property_value`, `create_time`) VALUES (?,?,?,?);";
        List<EntDataPropertyEntity> phoneList = new ArrayList<>();
        int[] ints = new int[0];
        try {
            ints = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    Long ent_id = 0L;
                    try {
                        ent_id = sequenceService.getSeq("ent_id");
                    } catch (Exception e) {
                        try {
                            ent_id = sequenceService.getSeq("ent_id");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    personList.get(i).setId(ent_id);
                    preparedStatement.setString(1, JSON.toJSONString(personList.get(i)));
                    preparedStatement.setTimestamp(2, now);
                    // 处理手机号来源
                    if (personList.get(i).getPhoneNumbers().size() > 0) {
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
                        PhoneEntity p;
                        for (EmailEntity email : emails) {
                            p = new PhoneEntity(email.getEmail(), now.getTime(), email.getSource(), email.getSourceWeb());
                            jsonArray.add(p);
                        }
                        if (update) {
                            // 更新
                            jdbcTemplate.update(updateSql, jsonArray.toJSONString(), now, ent_id, propertyName);
                        } else {
                            EntDataPropertyEntity property = new EntDataPropertyEntity(ent_id, propertyName, jsonArray.toJSONString());
                            phoneList.add(property);
                        }
                    }
                }

                @Override
                public int getBatchSize() {
                    return personList.size();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    preparedStatement.setLong(1, phoneList.get(i).getId());
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
        }
        return ints.length;
    }


    public void importDayDataToES(LocalDateTime localTime) {
        String yearMonth = localTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<Map<String, Object>> count = jdbcTemplate.queryForList("select count(0) count from enterprise_info_" + yearMonth);
        long total = 0L;
        if (count.size() > 0) {
            total = NumberConvertUtil.parseLong(count.get(0).get("count"));
        }
        int limit = 20000;
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
                    data.add(jsonObject);
                }
                elasticSearchService.bulkInsertDocument("ent_data_index", "business", data);
            }
        }
    }
}
