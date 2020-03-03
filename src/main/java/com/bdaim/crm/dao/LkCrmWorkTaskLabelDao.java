package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmWorkTaskLabelEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmWorkTaskLabelDao extends SimpleHibernateDao<LkCrmWorkTaskLabelEntity, Integer> {
    public List<Map<String, Object>> queryTaskList(Integer labelId, Long userId) {
        String sql = "SELECT " +
                " a.task_id,a.name,a.stop_time,a.priority,a.work_id,a.status, " +
                " ( SELECT count( * ) FROM lkcrm_admin_file WHERE batch_id = a.batch_id ) AS file_count, " +
                " ( SELECT count( * ) FROM lkcrm_task_comment WHERE type_id = a.task_id AND type = 1 ) AS comment_count, " +
                " ( SELECT count( * ) FROM lkcrm_task WHERE pid = a.task_id ) AS child_all_count, " +
                " ( SELECT count( * ) FROM lkcrm_task WHERE pid = a.task_id AND STATUS = 5 ) AS child_WC_count  " +
                "FROM " +
                " lkcrm_task AS a  " +
                "WHERE " +
                " pid = 0  " +
                " AND find_in_set( ?, a.label_id )  " +
                " AND a.ishidden = 0  " +
                " AND ( a.main_user_id = ? OR a.owner_user_id LIKE concat( '%,', ?, ',%' ) )";
        return super.queryListBySql(sql, labelId, userId, userId);
    }

    public List<Map<String, Object>> queryWorkList(Integer labelId, Long userId) {
        String sql = "SELECT DISTINCT " +
                " a.work_id,a.name,a.color  " +
                "FROM " +
                " lkcrm_work AS a " +
                " LEFT JOIN lkcrm_task AS b ON a.work_id = b.work_id  " +
                "WHERE " +
                " find_in_set( ?, b.label_id )  " +
                " AND b.ishidden = 0  " +
                " AND ( b.main_user_id = ? OR b.owner_user_id LIKE concat( '%,',?, ',%' ) )";
        return super.queryListBySql(sql, labelId, userId, userId);
    }
}
