package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmTaskEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmWorkbenchDao extends SimpleHibernateDao<LkCrmTaskEntity, Integer> {

    public List myTask(Long userId, Integer isTop) {
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
                " ( a.owner_user_id LIKE concat( '%,', ?, ',%' ) OR a.main_user_id = ? )  " +
                " AND a.pid = 0  " +
                " AND a.ishidden = 0  " +
                " AND a.is_top = ?  " +
                " AND ( a.STATUS = 1 OR a.STATUS = 2 )  " +
                " AND a.is_archive = 0";
        return super.sqlQuery(sql, userId, userId, isTop);
    }

    public Map<String, Object> getMainUser(Long mainUserId) {
        String sql = "select user_id,realname,img from lkcrm_admin_user where user_id = ?";
        List<Map<String, Object>> list = sqlQuery(sql, mainUserId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public Map<String, Object> getCreateUser(Long create_user_id) {
        String sql = "select user_id,realname,img from lkcrm_admin_user where user_id = ?";
        List<Map<String, Object>> list = sqlQuery(sql, create_user_id);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public void updateFromToTop(String updateSql, Integer fromTopId, int i, Object o) {
        super.executeUpdateSQL(updateSql, fromTopId, i, o);
    }

    public Map<String, Object> getLableById(String lableId) {
        String sql = "select label_id,name as labelName,color from lkcrm_work_task_label where label_id = ?";
        List<Map<String, Object>> list = super.sqlQuery(sql, lableId);
        if (!CollectionUtils.isEmpty(list)) {
            return list.get(0);
        } else {
            return null;
        }
    }
}
