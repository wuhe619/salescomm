package com.bdaim.customs.services;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/***
 * 申报单.分单
 */
@Service("busi_sbd_f")
public class SbdFService implements BusiService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id, JSONObject info) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id, JSONObject info) {
        // 身份核验
        if ("verification".equals(info.getString("rule.do"))) {
            StringBuffer sql = new StringBuffer("select id from h_data_manager where type=?")
                    .append(" and cust_id='").append(cust_id).append("'")
                    .append(" and id =? ");
            List sqlParams = new ArrayList();
            sqlParams.add(busiType);
            sqlParams.add(id);
            Map<String, Object> map = jdbcTemplate.queryForMap(sql.toString(), sqlParams.toArray());
            if (map != null && map.size() > 0) {
                String updateSql = "UPDATE h_data_manager SET ext_7 = 3 WHERE id =? ";
                jdbcTemplate.update(updateSql, map.get("id"));
            }

        }
    }

    @Override
    public void getInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id, JSONObject info) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, String cust_user_id, JSONObject params, List sqlParams) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub

    }

}
