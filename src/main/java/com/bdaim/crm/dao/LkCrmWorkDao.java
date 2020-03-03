package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmWorkEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmWorkDao extends SimpleHibernateDao<LkCrmWorkEntity, Integer> {
    public List<Map<String, Object>> queryTrashList() {
        String sql = "SELECT " +
                " a.task_id,a.name,a.stop_time,a.priority,a.status, " +
                " ( SELECT count( * ) FROM lkcrm_admin_file WHERE batch_id = a.batch_id ) AS file_num, " +
                " ( SELECT count( * ) FROM lkcrm_task_comment WHERE type_id = a.task_id AND type = 1 ) AS comment_num, " +
                " ( SELECT count( * ) FROM lkcrm_task WHERE pid = a.task_id ) AS child_all_count, " +
                " ( SELECT count( * ) FROM lkcrm_task WHERE pid = a.task_id AND STATUS = 5 ) AS child_finish_count  " +
                "FROM " +
                " lkcrm_task AS a  " +
                "WHERE " +
                " pid = 0  " +
                " AND ishidden = 1  " +
                "ORDER BY " +
                " a.hidden_time DESC";
        return super.queryListBySql(sql);
    }

    public List<Map<String, Object>> queryTrashListByUserId(int userId) {
        String sql = "SELECT " +
                " a.task_id,a.name,a.stop_time,a.priority,a.status, " +
                " ( SELECT count( * ) FROM lkcrm_admin_file WHERE batch_id = a.batch_id ) AS file_num, " +
                " ( SELECT count( * ) FROM lkcrm_task_comment WHERE type_id = a.task_id AND type = 1 ) AS comment_num, " +
                " ( SELECT count( * ) FROM lkcrm_task WHERE pid = a.task_id ) AS child_all_count, " +
                " ( SELECT count( * ) FROM lkcrm_task WHERE pid = a.task_id AND STATUS = 5 ) AS child_finish_count  " +
                "FROM " +
                " lkcrm_task AS a  " +
                "WHERE " +
                " pid = 0  " +
                " AND ishidden = 1  " +
                " AND ( a.main_user_id =? OR a.owner_user_id LIKE concat( '%,',?, ',%' ) )  " +
                "ORDER BY " +
                " a.hidden_time DESC";
        return super.queryListBySql(sql, userId);
    }
}
