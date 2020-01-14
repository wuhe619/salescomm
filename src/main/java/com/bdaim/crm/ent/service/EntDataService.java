package com.bdaim.crm.ent.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.crm.EntInfo;
import com.bdaim.customs.entity.Constants;
import com.bdaim.util.ExcelUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
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

    public int importByExcel(String path, int titleRows, int headerRows) {
        List<EntInfo> personList = ExcelUtil.importExcel(path, titleRows, headerRows, EntInfo.class);
        LOG.info("导入数据{}行,开始导入数据库", personList.size());
        String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String sql = "INSERT INTO enterprise_info_" + yearMonth + " (content, create_time) VALUES (?,?);";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    if (StringUtil.isNotEmpty(personList.get(i).getPhoneNumbers())) {
                        personList.get(i).setPhoneNumbers(JSON.toJSONString(personList.get(i).getPhoneNumbers().split(",")));
                    } else {
                        personList.get(i).setPhoneNumbers(JSON.toJSONString(new ArrayList<>()));
                    }
                    preparedStatement.setString(1, JSON.toJSONString(personList.get(i)));
                    preparedStatement.setTimestamp(2, now);
                }

                @Override
                public int getBatchSize() {
                    return personList.size();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return personList.size();
    }

    public void nowDayDataToES(){
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
