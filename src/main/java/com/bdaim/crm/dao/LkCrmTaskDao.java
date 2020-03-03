package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmTaskEntity;
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
        String sql = "SELECT " +
                " task_id,name,date_format( start_time, '%Y-%m-%d' ) AS start_time, " +
                " date_format( stop_time, '%Y-%m-%d' ) AS stop_time, " +
                " priority,create_time,update_time  " +
                "FROM " +
                " lkcrm_task a  " +
                "WHERE " +
                " ( a.main_user_id = ? OR a.owner_user_id LIKE concat( '%,', ?, ',%' ) )  " +
                " AND ( a.stop_time BETWEEN ? AND ? OR a.stop_time IS NULL )  " +
                " AND a.pid = 0  " +
                " AND ( a.STATUS = 1 OR a.STATUS = 2 )  " +
                " AND a.is_archive = 0  " +
                " AND a.ishidden = 0";
        return super.queryListBySql(sql, userId, userId, startTime, endTime);
    }
}
