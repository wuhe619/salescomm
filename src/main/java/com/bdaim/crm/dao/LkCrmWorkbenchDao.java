package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmTaskEntity;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LkCrmWorkbenchDao extends SimpleHibernateDao<LkCrmTaskEntity, Integer> {

    public List<Record> myTask(Integer userId, Integer isTop) {
        String sql = "SELECT " +
                " a.*,b.NAME AS workName, " +
                " ( SELECT count( * ) FROM lkcrm_task_comment WHERE type_id = a.task_id AND type = 1 ) AS commentCount, " +
                " ( SELECT count( * ) FROM lkcrm_task WHERE pid = a.task_id AND STATUS = 5 ) AS childWCCount, " +
                " ( SELECT count( * ) FROM lkcrm_task WHERE pid = a.task_id ) AS childAllCount, " +
                " ( SELECT count( * ) FROM lkcrm_admin_file WHERE batch_id = a.batch_id ) AS fileCount  " +
                "FROM " +
                " lkcrm_task a " +
                " LEFT JOIN lkcrm_work b ON a.work_id = b.work_id  " +
                "WHERE " +
                " ( a.owner_user_id LIKE concat( '%,', '" + userId + "', ',%' ) OR a.main_user_id = '" + userId + "' )  " +
                " AND a.pid = 0  " +
                " AND a.ishidden = 0  " +
                " AND a.is_top = '" + isTop + "'  " +
                " AND ( a.STATUS = 1 OR a.STATUS = 2 )  " +
                " AND a.is_archive = 0";
        return super.queryListBySql(sql, Record.class);
    }
}
