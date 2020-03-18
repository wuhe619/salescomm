package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmWorkTaskClassEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmWorkTaskClassDao extends SimpleHibernateDao<LkCrmWorkTaskClassEntity, Integer> {
    public List<Map<String, Object>> myWorkLog(Integer taskId) {
        String sql = "SELECT wtl.log_id,wtl.content, " +
                " wtl.create_time,au.img,au.realname  " +
                " FROM lkcrm_work_task_log AS wtl " +
                " LEFT JOIN lkcrm_admin_user AS au ON au.user_id = wtl.user_id  " +
                " WHERE wtl.task_id = ?  " +
                " AND wtl.STATUS != 4  " +
                " ORDER BY wtl.create_time DESC";
        return super.queryListBySql(sql, taskId);
    }
}
