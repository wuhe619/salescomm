package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmSqlParams;
import com.bdaim.crm.entity.LkCrmTaskEntity;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.SqlAppendUtil;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmTaskDao extends SimpleHibernateDao<LkCrmTaskEntity, Integer> {
    public Page queryTaskRelation(int pageNum, int pageSize, String businessIds, String contactsIds, String contractIds, String customerIds) {
        String sql = "SELECT st.*,\n" +
                "      (select count(*) from lkcrm_task_comment where type_id = st.task_id and type = 1) as commentCount,\n" +
                "      (select count(*) from lkcrm_task where pid = st.task_id and status = 5) as childWCCount,\n" +
                "      (select count(*) from lkcrm_task where pid = st.task_id) as childAllCount,\n" +
                "      (select count(*) from lkcrm_admin_file where batch_id = st.batch_id) as fileCount\n" +
                "      FROM lkcrm_task as st\n" +
                "      LEFT JOIN lkcrm_task_relation as str on str.task_id = st.task_id\n" +
                "      where 1 = 2 ";
        List param = new ArrayList();
        if (StringUtil.isNotEmpty(businessIds)) {
            param.add(businessIds);
            sql += " or str.business_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(contactsIds)) {
            param.add(contactsIds);
            sql += " or str.contacts_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(contractIds)) {
            param.add(contractIds);
            sql += " or str.contract_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(customerIds)) {
            param.add(customerIds);
            sql += " or str.customer_ids like concat('%,',?,',%')";
        }
        return super.sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public List<Map<String, Object>> dateList(Long userId, String startTime, String endTime) {
        String sql = "SELECT task_id,name,date_format( start_time, '%Y-%m-%d' ) AS start_time, " +
                " date_format( stop_time, '%Y-%m-%d' ) AS stop_time, " +
                " priority,create_time,update_time  " +
                " FROM lkcrm_task a WHERE " +
                " ( a.main_user_id = ? OR a.owner_user_id LIKE concat( '%,', ?, ',%' ) )  " +
                " AND ( a.stop_time BETWEEN ? AND ? OR a.stop_time IS NULL )  " +
                " AND a.pid = 0  " +
                " AND ( a.STATUS = 1 OR a.STATUS = 2 )  " +
                " AND a.is_archive = 0  " +
                " AND a.ishidden = 0 AND a.cust_id = ? ";
        return super.sqlQuery(sql, userId, userId, startTime, endTime, BaseUtil.getCustId());
    }

    public LkCrmSqlParams getTaskList(Integer type, List<Long> userIds, Integer status,
                                      Integer priority, Integer date, String name) {
        StringBuffer sqlBuffer = new StringBuffer();
        List<Object> params = new ArrayList<>();
        sqlBuffer.append("select a.*,")
                .append("(select count(*) from lkcrm_task_comment where type_id = a.task_id and type = 1) as commentCount,")
                .append("(select count(*) from lkcrm_task where pid = a.task_id and status = 5) as childWCCount,")
                .append("(select count(*) from lkcrm_task where pid = a.task_id) as childAllCount,")
                .append("(select count(*) from lkcrm_admin_file where batch_id = a.batch_id) as fileCount")
                .append(" from lkcrm_task a where a.pid = 0 and a.ishidden = 0 AND a.cust_id = ? ");
        params.add(BaseUtil.getCustId());
        if (type == null || type == 0) {
            sqlBuffer.append(" and ( a.main_user_id in ( ")
                    .append(SqlAppendUtil.sqlAppendWhereIn(userIds))
                    .append(" ) or a.create_user_id in ( ")
                    .append(SqlAppendUtil.sqlAppendWhereIn(userIds))
                    .append(" ) or ( ");
            for (int i = 0; i < userIds.size(); i++) {
                if (i != 0) {
                    sqlBuffer.append(" or ");
                }
                sqlBuffer.append(" a.owner_user_id like concat('%,', ?,',%') ");
                params.add(userIds.get(i));
            }
            sqlBuffer.append("    )   ) ");
        } else if (type == 2) {
            sqlBuffer.append(" and  a.create_user_id in ( ")
                    .append(SqlAppendUtil.sqlAppendWhereIn(userIds))
                    .append(" ) ");
        } else if (type == 3) {
            sqlBuffer.append(" and   ( ");
            for (int i = 0; i < userIds.size(); i++) {
                if (i != 0) {
                    sqlBuffer.append(" or ");
                }
                sqlBuffer.append(" a.owner_user_id like concat('%,', ?,',%') ");
                params.add(userIds.get(i));
            }
            sqlBuffer.append(" ) ");
        }
        if (status != null) {
            sqlBuffer.append(" and a.status = ? ");
            params.add(status);
        }
        if (priority != null) {
            sqlBuffer.append(" and a.priority = ? ");
            params.add(priority);
        }
        if (date != null) {
            if (date == 1) {
                sqlBuffer.append(" and TO_DAYS(a.stop_time) = TO_DAYS(now()) ");
            }
            if (date == 2) {
                sqlBuffer.append(" and to_days(NOW()) - TO_DAYS(a.stop_time) = -1 ");
            }

            if (date == 3) {
                sqlBuffer.append(" and to_days(NOW()) - TO_DAYS(a.stop_time) >= -7 and to_days(NOW()) - TO_DAYS(a.stop_time) <= 0 ");
            }
            if (date == 4) {
                sqlBuffer.append(" and to_days(NOW()) - TO_DAYS(a.stop_time) >= -30 and to_days(NOW()) - TO_DAYS(a.stop_time) <= 0 ");
            }

        }
        if (StringUtil.isNotEmpty(name)) {
            sqlBuffer.append(" and a.name like concat('%', ?,'%') ");
            params.add(name);
        }
        sqlBuffer.append(" order by a.create_time desc ");

        LkCrmSqlParams sqlParams = new LkCrmSqlParams();
        sqlParams.setSql(sqlBuffer.toString());
        sqlParams.setParams(params);
        return sqlParams;
    }
}
