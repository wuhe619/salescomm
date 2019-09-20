package com.bdaim.customs.services;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
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
        String sql = null;
        //查询主列表
        if ("main".equals(params.getString("rule.do"))) {
            sqlParams.clear();
            StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from h_data_manager where type=?");
            if (!"all".equals(cust_id))
                sqlstr.append(" and cust_id='").append(cust_id).append("'");

            sqlParams.add(busiType);

            Iterator keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if ("pageNum".equals(key) || "pageSize".equals(key) || "pid1".equals(key) || "pid2".equals(key))
                    continue;
                if ("cust_id".equals(key)) {
                    sqlstr.append(" and cust_id=?");
                } else if (key.endsWith(".c")) {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(0, key.length() - 2) + "') like '%?%'");
                } else if (key.endsWith(".start")) {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(0, key.length() - 6) + "') >= ?");
                } else if (key.endsWith(".end")) {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(0, key.length() - 6) + "') <= ?");
                } else {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key + "')=?");
                }

                sqlParams.add(params.get(key));
            }
            String verify_status = params.getString("verify_status");
            String verify_photo = params.getString("verify_photo");
            // 身份校验状态
            if (StringUtil.isNotEmpty(verify_status)) {
                if ("3".equals(verify_status)) {
                    sqlstr.append(" and ( ext_7 IS NULL OR ext_7='' OR ext_7 =3 ");
                }
            }
            //身份图片状态
            if (StringUtil.isNotEmpty(verify_photo)) {
                if ("1".equals(verify_photo)) {
                    sqlstr.append(" and ext_6 IS NOT NULL ");
                } else if ("2".equals(verify_photo)) {
                    sqlstr.append(" and (ext_6 IS NULL OR ext_6='') ");
                }

            }
            return sqlstr.toString();
        }
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub

    }

}
