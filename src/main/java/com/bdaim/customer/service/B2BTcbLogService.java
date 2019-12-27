package com.bdaim.customer.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        info.put("ext_3", info.getString("batchId"));
        info.put("ext_4", info.getString("superId"));
        info.put("ext_5", info.getString("ext_5"));
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
        StringBuffer sqlstr = new StringBuffer("select t.id , t.cust_id, t.create_id, t.create_date,t.ext_1, t.ext_2, t.ext_3, t.ext_4, t.ext_5 , u.realname,update_date from "
                + HMetaDataDef.getTable(busiType, "") + " t left join t_customer_user u on t.cust_user_id= u.id  where type='").append(busiType).append("'");
        String userName = params.getString("userName");
        String create_date = params.getString("create_date");
        String end_date = params.getString("end_date");

        if (!"all".equals(cust_id))
            sqlstr.append(" and t.cust_id='").append(cust_id).append("'");
        String tcb_id = params.getString("tcb_id");
        if (StringUtil.isNotEmpty(tcb_id)) {
            sqlstr.append(" and t.ext_2 ='").append(tcb_id).append("'");
        }
        if (StringUtil.isNotEmpty(userName)) {
            String sql1 = "select id from t_customer_user where realname = '" + userName + "'";

            List<Object> userIdList = jdbcTemplate.queryForList(sql1, Object.class);
            if (userIdList.size() > 0) {
                StringBuffer sbf = new StringBuffer();
                userIdList.stream().forEach(userId -> {
                    sbf.append(userId);
                    sbf.append(",");
                });
                sbf.deleteCharAt(sbf.length() - 1);
                sqlstr.append(" and t.cust_user_id in (").append(sbf).append(")");
            }
        }
        if (StringUtil.isNotEmpty(create_date) && StringUtil.isNotEmpty(end_date)) {
            sqlstr.append(" and t.create_date between '").append(create_date).append("' and '").append(end_date).append("'");
        }
        sqlstr.append(" order by  create_date desc");
        return sqlstr.toString();
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
