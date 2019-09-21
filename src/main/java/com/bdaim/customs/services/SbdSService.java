package com.bdaim.customs.services;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;
import org.springframework.stereotype.Service;

import java.util.List;

/***
 * 申报单.税单
 */
@Service("busi_sbd_s")
public class SbdSService implements BusiService {

    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id, JSONObject info) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id, JSONObject info) {
        // TODO Auto-generated method stub

    }

    @Override
    public void getInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id, JSONObject info, JSONObject param) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, Long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, String cust_user_id, JSONObject params, List sqlParams) {
       /* String sql = null;
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
                if ("pageNum".equals(key) || "pageSize".equals(key)|| "pid1".equals(key)|| "pid2".equals(key)) continue;
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
            String pidO = params.getString("pidO");
            String pidS = params.getString("pidO");
            if(StringUtil.isNotEmpty(pidO) && StringUtil.isNotEmpty(pidS)) {
                sqlstr.append(" and JSON_EXTRACT(content, '$.pid')= SELECT id FROM h_data_manager WHERE type =? AND JSON_EXTRACT(content, '$.pid')=?");
            }
            sql = sqlstr.toString();
        }*/
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, String cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub

    }

}
