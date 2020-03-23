package com.bdaim.customer.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.service.PhoneService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.smscenter.service.SendSmsService;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CustomerExtensionService {
    private static Logger logger = LoggerFactory.getLogger(CustomerExtensionService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private SendSmsService sendSmsService;

    @Autowired
    private PhoneService phoneService;

    public String saveExtension(JSONObject info) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if(!info.containsKey("clazz")){
            info.put("clazz","toB");
        }
        String sql = "insert into op_crm_clue_log(content,create_time,update_time) values (?, ?, ?)";
        jdbcTemplate.update(sql, info.toJSONString(), timestamp, timestamp);
        try {
            if ("toC".equals(info.getString("clazz"))) {
                sendSmsService.sendSmsVcCodeByCommChinaAPI("18888851832", 12, "admin");
                sendSmsService.sendSmsVcCodeByCommChinaAPI("13601128981", 12, "admin");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "Success";
    }

    public String updateExtension(long id, JSONObject info) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if(!info.containsKey("clazz")){
            info.put("clazz","toB");
        }
        String sql = "update op_crm_clue_log set content = ? ,update_time = ? where id = ? ";
        jdbcTemplate.update(sql, info.toJSONString(), timestamp, id);
        return "Success";
    }

    public Page query(JSONObject info, PageParam page) {
        StringBuffer sql = new StringBuffer("select id,content,create_time from op_crm_clue_log where 1=1");
        List<Object> p = new ArrayList<>();
        if(info.containsKey("clazz")) {
            p.add(info.getString("clazz").trim());
            sql.append(" and content->'$.clazz' = ?");
        }else{
            p.add("other");
            sql.append(" and content->'$.clazz' = ?");
        }
        if (StringUtil.isNotEmpty(info.getString("custName"))) {
            p.add("%" + info.getString("custName") + "%");
            sql.append(" and content-> '$.custName' like ? ");
        }
        if (StringUtil.isNotEmpty(info.getString("tel"))) {
            p.add(info.getString("tel"));
            sql.append(" and content->'$.tel' = ?");
        }
        if (StringUtil.isNotEmpty(info.getString("create_time")) && StringUtil.isNotEmpty(info.getString("end_time"))) {
            p.add(info.getString("create_time"));
            p.add(info.getString("end_time"));
            sql.append(" and create_time between ? and ? ");
        }
        if (StringUtil.isNotEmpty(info.getString("source"))) {
            p.add(info.getString("source"));
            sql.append(" and content->'$.source' =? ");
        }
        if (StringUtil.isNotEmpty(info.getString("id"))) {
//            p.add(info.getString("id"));
            sql.append(" and  id in ("+info.getString("id")+")");
        }

        sql.append(" order by create_time desc");
//        List<Map<String, Object>> ds = jdbcTemplate.queryForList(sql + " limit " + (page.getPageNum() - 1) * page.getPageSize() + ", " + page.getPageSize());
        logger.info("sql {},param{}",sql.toString(),p.toArray());
        Page list = customerDao.sqlPageQuery(sql.toString(), page.getPageNum(), page.getPageSize(), p.toArray());
        logger.info("list::{}",list);
        List list1 = new ArrayList();
        list.getData().stream().forEach(m -> {
            Map map = (Map) m;
            Object content = map.get("content");
            if (content != null) {
                JSONObject jsonObject = JSONObject.parseObject(content.toString());
                for (String key : jsonObject.keySet()) {
                    map.put(key, jsonObject.getString(key));
                }
            }
            list1.add(map);
        });
        list.setData(list1);
        return list;
    }
}
