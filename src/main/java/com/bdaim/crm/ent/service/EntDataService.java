package com.bdaim.crm.ent.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.crm.EntInfo;
import com.bdaim.crm.EntInfoProperty;
import com.bdaim.crm.PhoneSource;
import com.bdaim.customs.entity.Constants;
import com.bdaim.util.ExcelUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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

    public int importByExcel(String path, int titleRows, int headerRows) {
        List<EntInfo> personList = ExcelUtil.importExcel(path, titleRows, headerRows, EntInfo.class);
        LOG.info("文件:{},导入数据:{}行,开始导入数据库", path, personList.size());
        return batchSaveEntData(personList);
    }

    public int batchSaveEntData(List<EntInfo> personList) {
        String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sql = "INSERT INTO enterprise_info_" + yearMonth + " (content, create_time) VALUES (?,?);";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String selectSql = "SELECT property_value FROM enterprise_info_property WHERE id = ? AND property_name=?";
        String updateSql = "UPDATE enterprise_info_property SET property_value = ?,update_time=? WHERE id = ? AND property_name=?";
        String insertSql = "INSERT INTO `enterprise_info_property` (`id`, `property_name`, `property_value`, `create_time`) VALUES (?,?,?,?);";
        List<EntInfoProperty> phoneList = new ArrayList<>();
        int[] ints = new int[0];
        try {
            ints = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    if (StringUtil.isNotEmpty(personList.get(i).getPhoneNumbers())
                            && !"-".equals(personList.get(i).getPhoneNumbers())) {
                        personList.get(i).setPhoneNumbers(JSON.toJSONString(personList.get(i).getPhoneNumbers().split(",")));
                    } else {
                        personList.get(i).setPhoneNumbers(JSON.toJSONString(new ArrayList<>()));
                    }
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
                    if (JSON.parseArray(personList.get(i).getPhoneNumbers()).size() > 0) {
                        boolean update = false;
                        JSONArray jsonArray = new JSONArray();
                        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, ent_id, "phone_source");
                        if (list.size() > 0) {
                            update = true;
                            jsonArray.addAll(JSON.parseArray(list.get(0).get("property_value").toString()));
                        }
                        JSONArray phones = JSON.parseArray(personList.get(i).getPhoneNumbers());
                        PhoneSource p;
                        for (int j = 0; j < phones.size(); j++) {
                            p = new PhoneSource(phones.getString(j), now.getTime(), "天眼查", "https://www.tianyancha.com");
                            jsonArray.add(p);
                        }
                        if (update) {
                            // 更新
                            jdbcTemplate.update(updateSql, jsonArray.toJSONString(), now, ent_id, "phone_source");
                        } else {
                            EntInfoProperty property = new EntInfoProperty(ent_id, "phone_source", jsonArray.toJSONString());
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
            ints = jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
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


    public void nowDayDataToES() {
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from enterprise_info_202001 where create_time between ? and ?", now.withHour(0).withMinute(0).withSecond(0), now);
        if (list.size() > 0) {
            List<JSONObject> data = new ArrayList<>();
            for (Map<String, Object> m : list) {
                JSONObject jsonObject = JSON.parseObject(String.valueOf(m.get("content")));
                jsonObject.put("createTime", m.get("create_time"));
                jsonObject.put("updateTime", m.get("update_time"));
                data.add(jsonObject);
            }

            elasticSearchService.bulkInsertDocument("ent_data_index", Constants.INDEX_TYPE, data);
        }
    }
}
