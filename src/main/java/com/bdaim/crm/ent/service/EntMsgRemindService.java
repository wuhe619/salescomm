package com.bdaim.crm.ent.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.Page;
import com.bdaim.common.service.BusiService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.SqlAppendUtil;
import com.bdaim.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @description 企业消息提醒
 * @date 2020/4/20
 */
@Service("busi_ent_msg_remind")
@Transactional
public class EntMsgRemindService implements BusiService {

    @Autowired
    private MarketResourceDao marketResourceDao;

    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        info.put("ext_1", info.getString("exportTaskId"));
        info.put("ext_2", info.getString("msgType"));
        info.put("ext_3", info.getString("msgStatus"));
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {

    }

    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) throws Exception {

    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws Exception {

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) throws Exception {
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {

    }

    public Page page(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params) throws Exception {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT id, create_date, content FROM " + HMetaDataDef.getTable(busiType, "") + " WHERE cust_id = ? AND create_id = ? ");
        int pageNum = 1;
        int pageSize = 10;
        try {
            pageNum = params.getIntValue("pageNum");
        } catch (Exception e) {
        }
        try {
            pageSize = params.getIntValue("pageSize");
        } catch (Exception e) {
        }

        Page page = marketResourceDao.sqlPageQuery(sql.toString(), pageNum, pageSize, cust_id, cust_user_id);
        return page;
    }

    /**
     * 查询所有未读消息数量
     *
     * @param busiType
     * @param cust_id
     * @param cust_group_id
     * @param cust_user_id
     * @param params
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> unReadMsgCount(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params) throws Exception {
        List<Map<String, Object>> data = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        // 查询所有未读信息
        sql.append("SELECT ext_2 msgType, COUNT(`ext_3` = '1' OR null) AS unRead FROM " + HMetaDataDef.getTable(busiType, "") + " WHERE cust_id = ? AND create_id = ?  AND ext_3 = '1' GROUP BY ext_2 ");
        List<Map<String, Object>> list = marketResourceDao.sqlQuery(sql.toString(), cust_id, cust_user_id);
        int allCount = 0;
        for (int i = 0; i < list.size(); i++) {
            data.add(list.get(i));
            allCount += NumberConvertUtil.parseInt(list.get(i).get("unRead"));
        }
        // 所有未读消息
        Map<String, Object> all = new HashMap<>();
        all.put("msgType", "0");
        all.put("unRead", allCount);
        data.add(all);
        return data;
    }


    /**
     * 消息读取
     *
     * @param params
     * @return
     */
    public int readMsg(JSONObject params) {
        JSONArray id = params.getJSONArray("id");
        String msgType = params.getString("msgType");
        StringBuffer sql = new StringBuffer();
        LoginUser user = BaseUtil.getUser();
        List sqlParams = new ArrayList();
        sqlParams.add("2");
        sqlParams.add("2");
        sqlParams.add(user.getCustId());
        sqlParams.add(user.getId());
        sql.append("UPDATE " + HMetaDataDef.getTable(BusiTypeEnum.ENT_MSG_REMIND.getType(), "") + " SET ext_3 = ?, content = JSON_SET(content,'$.msgStatus', ?) WHERE cust_id = ? AND create_id = ? ");
        if (id != null && id.size() > 0) {
            List ids = id;
            sql.append(" AND id IN(" + SqlAppendUtil.sqlAppendWhereIn(ids) + ") ");
        }
        if (StringUtil.isNotEmpty(msgType)) {
            sql.append(" AND ext_2 = ? ");
            sqlParams.add(msgType);
        }
        return marketResourceDao.executeUpdateSQL(sql.toString(), sqlParams.toArray());
    }
}
