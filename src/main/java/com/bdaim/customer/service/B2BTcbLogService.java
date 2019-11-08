package com.bdaim.customer.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HMetaDataDef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2019-11-04 17:13
 */
@Service("busi_b2b_tcb_log")
@Transactional
public class B2BTcbLogService implements BusiService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        info.put("ext_2", info.getString("tcbId"));
        info.put("ext_3", info.getString("userId"));
        info.put("ext_4", info.getString("superId"));
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
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) {
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {

    }

    /**
     * 判断企业是否领取过该线索
     *
     * @param cust_id
     * @param companyId
     * @return
     */
    public boolean checkClueGetStatus(String cust_id, String companyId) {
        String sql = "select id,content from " + HMetaDataDef.getTable(BusiTypeEnum.B2B_TC_LOG.getType(), "") + " where type=? and cust_id = ? and ext_1 = ? ";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, BusiTypeEnum.B2B_TC_LOG.getType(), cust_id, companyId);
        if (list != null && list.size() > 0) {
            return true;
        }
        return false;
    }

}
