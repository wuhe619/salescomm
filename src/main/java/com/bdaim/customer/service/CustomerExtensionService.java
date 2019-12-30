package com.bdaim.customer.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.page.PageList;
import com.bdaim.common.page.Pagination;
import com.bdaim.util.DateUtil;
import com.bdaim.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomerExtensionService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String saveExtension(JSONObject info) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String sql = "insert into op_crm_clue_log(content,create_time,update_time) values ('" + info + "',now(),now())";
        jdbcTemplate.update(sql);
        return "Success";
    }

    public String updateExtension(long id, JSONObject info) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String sql = "update op_crm_clue_log set content = '" + info.toJSONString() + "' ,update_time = now()   where id = " + id;
        jdbcTemplate.update(sql);
        return "Success";
    }

    public PageList query(JSONObject info, PageParam page) {
        StringBuffer sql = new StringBuffer("select id,content,create_time from op_crm_clue_log where 1=1");
        if (StringUtil.isNotEmpty(info.getString("custName"))) {
            sql.append(" and content-> '$.custName' like '%" + info.getString("custName") + "%'");
        }
        if (StringUtil.isNotEmpty(info.getString("tel"))) {
            sql.append(" and content->'$.tel' = '" + info.getString("tel") + "'");
        }
        if (StringUtil.isNotEmpty(info.getString("create_time")) && StringUtil.isNotEmpty(info.getString("end_time"))) {
            sql.append(" and create_time between '" + info.getString("create_time") + "' and '" + info.getString("end_time") + "'");
        }
        if (StringUtil.isNotEmpty(info.getString("source"))) {
            sql.append(" and content->'$.source' ='" + info.getString("source") + "'");
        }
        if (StringUtil.isNotEmpty(info.getString("id"))) {
            sql.append(" and  id in (" + info.getString("id") + ")");
        }
        sql.append(" order by create_time desc");
        List<Map<String, Object>> ds = jdbcTemplate.queryForList(sql + " limit " + (page.getPageNum() - 1) * page.getPageSize() + ", " + page.getPageNum());
        PageList list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
        List list1 = new ArrayList();
        ds.stream().forEach(map -> {
            Object content = map.get("content");
            if (content != null) {
                JSONObject jsonObject = JSONObject.parseObject(content.toString());
                for (String key : jsonObject.keySet()) {
                    map.put(key, jsonObject.getString(key));
                }
            }
            list1.add(map);
        });
        list.setList(list1);
        return list;
    }
}
