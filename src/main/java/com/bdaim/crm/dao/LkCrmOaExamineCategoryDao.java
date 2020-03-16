package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmOaExamineCategoryEntity;
import com.bdaim.util.StringUtil;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmOaExamineCategoryDao extends SimpleHibernateDao<LkCrmOaExamineCategoryEntity, Integer> {
    public List<Map<String, Object>> poolTable(Record record) {
        String sql = "  select a.realname,b.name as deptName, " +
                "  (select count(type_id) from lkcrm_crm_owner_record where DATE_FORMAT(create_time,?) between ? and ? and type = 8 and pre_owner_user_id = a.user_id) as putInNum, " +
                "  (select count(type_id) from lkcrm_crm_owner_record where DATE_FORMAT(create_time,?) between ? and ? and type = 8 and post_owner_user_id = a.user_id) as receiveNum, " +
                "  IFNULL((select c.customer_num from lkcrm_crm_customer_stats as c where DATE_FORMAT(create_time,?) = ? and c.user_id = a.user_id limit 1),0) as customerNum " +
                "  from lkcrm_admin_user as a left join lkcrm_admin_dept as b on a.dept_id = b.dept_id " +
                "  where b.dept_id = ?";
        List<Object> params = new ArrayList<>();
        params.add(record.get("sqlDateFormat"));
        params.add(record.get("beginTime"));
        params.add(record.get("finalTime"));
        params.add(record.get("sqlDateFormat"));
        params.add(record.get("beginTime"));
        params.add(record.get("finalTime"));
        params.add(record.get("sqlDateFormat"));
        params.add(record.get("beginTime"));
        params.add(record.get("deptId"));
        if (record.get("userId") != null) {
            sql += "  and a.user_id = ? ";
            params.add(record.get("userId"));
        }
        return super.sqlQuery(sql, params.toArray());
    }

    public Map<String, Object> totalContract(String sqlDateFormat, Integer beginTime,
                                             Integer finalTime, String userIds) {
        String sql = "SELECT " +
                " count( a.contract_id ) AS contractNum, " +
                " IFNULL( sum( a.money ), 0 ) AS contractMoney, " +
                " IFNULL( sum( b.money ), 0 ) AS receivablesMoney, " +
                " ( IFNULL( sum( a.money ), 0 ) - IFNULL( sum( b.money ), 0 ) ) AS unreceivedMoney  " +
                "FROM " +
                " lkcrm_crm_contract AS a " +
                " LEFT JOIN lkcrm_crm_receivables AS b ON a.contract_id = b.contract_id  " +
                "WHERE " +
                " DATE_FORMAT( a.order_date,? ) BETWEEN ?  " +
                " AND ?  " +
                " AND a.check_status = 2  " +
                " AND a.owner_user_id IN ( 0";
        if (StringUtil.isNotEmpty(userIds)) {
            sql += "," + userIds;
        }
        sql += " )";
        return super.queryUniqueSql(sql, sqlDateFormat, beginTime, finalTime);

    }

    public List<Map<String, Object>> queryAllExamineCategoryList(Long userId, Integer deptId) {
        String sql = " select category_id,title,type from lkcrm_oa_examine_category where\n" +
                "        case when user_ids != '' or dept_ids != '' then (user_ids like concat('%,',?,',%') or dept_ids like concat('%,',?,',%')) and is_deleted = 0 and status = 1\n" +
                "             else is_deleted = 0 and status = 1 " +
                "        end";
        return sqlQuery(sql, userId, deptId);
    }
}
