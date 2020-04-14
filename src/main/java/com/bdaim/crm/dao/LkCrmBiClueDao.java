package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.utils.BaseUtil;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmBiClueDao extends SimpleHibernateDao {
    public List<Map<String, Object>> poolTable(Record record) {
        String sql = "  select a.realname,b.name as deptName, " +
                "  (select count(type_id) from lkcrm_crm_owner_record where DATE_FORMAT(create_time,?) between ? and ? and type = 9 and pre_owner_user_id = a.user_id) as putInNum, " +
                "  (select count(type_id) from lkcrm_crm_owner_record where DATE_FORMAT(create_time,?) between ? and ? and type = 9 and post_owner_user_id = a.user_id) as receiveNum " +
//                "  IFNULL((select c.customer_num from lkcrm_crm_customer_stats as c where DATE_FORMAT(create_time,?) = ? and c.user_id = a.user_id limit 1),0) as customerNum " +
                "  from lkcrm_admin_user as a left join lkcrm_admin_dept as b on a.dept_id = b.dept_id " +
                "  where b.dept_id = ? and b.cust_id = ? ";
        List<Object> params = new ArrayList<>();
        params.add(record.get("sqlDateFormat"));
        params.add(record.get("beginTime"));
        params.add(record.get("finalTime"));
        params.add(record.get("sqlDateFormat"));
        params.add(record.get("beginTime"));
        params.add(record.get("finalTime"));
//        params.add(record.get("sqlDateFormat"));
//        params.add(record.get("beginTime"));
        params.add(record.get("deptId"));
        params.add(BaseUtil.getCustId());
        if (record.get("userId") != null) {
            sql += "  and a.user_id = ? ";
            params.add(record.get("userId"));
        }
        return super.sqlQuery(sql, params.toArray());
    }
}
