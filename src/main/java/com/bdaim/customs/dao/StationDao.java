package com.bdaim.customs.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customs.entity.Station;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @description 场站信息Dao
 * @author:duanliying
 * @method
 * @date: 2019/9/16 11:38
 */
@Component
public class StationDao extends SimpleHibernateDao<Station, String> {
    private static Logger logger = LoggerFactory.getLogger(StationDao.class);
    @Autowired
    JdbcTemplate jdbcTemplate;

    public Station getStationById(int id) {
        Station cp = null;
        String sql = "SELECT type, content, create_id, create_date,update_id, update_date FROM h_resource m where m.id=? AND m.type ='station' ";
        Map data = null;
        try {
            data = jdbcTemplate.queryForMap(sql, id);
        } catch (EmptyResultDataAccessException e) {
            logger.warn("查询主单:{}失败", id);
            data = null;
        }
        if (data == null)
            return new Station();
        String content = (String) data.get("content");
        JSONObject jo = JSONObject.parseObject(content);
        jo.put("id", id);
        jo.put("cust_id", data.get("cust_id"));
        jo.put("cust_group_id", data.get("cust_group_id"));
        jo.put("cust_user_id", data.get("cust_user_id"));
        jo.put("create_id", data.get("create_id"));
        jo.put("create_date", data.get("create_date"));
        jo.put("update_id", data.get("update_id"));
        jo.put("update_date", data.get("update_date"));
        cp = JSON.parseObject(jo.toJSONString(), Station.class);
        return cp;
    }
}
