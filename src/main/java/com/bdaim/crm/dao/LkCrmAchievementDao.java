package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAchievementEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmAchievementDao extends SimpleHibernateDao<LkCrmAchievementEntity, Integer> {

    public Map<String, Object> queryDeptInfo(String year, int type, int deptId, int status) {
        String sql = " select a.* from lkcrm_crm_achievement a right join lkcrm_admin_dept b on a.obj_id = b.dept_id  where a.year = ? and a.type = ? and b.dept_id = ? and a.status = ?";
        List<Map<String, Object>> list = this.sqlQuery(sql, year, type, deptId, status);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }
    public List<Map<String, Object>> queryDeptByPid(int pid) {
        String sql = " select dept_id,name from lkcrm_admin_dept where pid = ?";
        List<Map<String, Object>> list = this.sqlQuery(sql, pid);
        return list;
    }

    public List<Map<String, Object>> queryUserByDeptId(int deptId) {
        String sql = "select user_id,realname as name  from lkcrm_admin_user where dept_id = ?";
        List<Map<String, Object>> list = this.sqlQuery(sql, deptId);
        return list;
    }

    public List<Map<String, Object>> queryUserById(String userId) {
        String sql = "select user_id,realname as name from lkcrm_admin_user where user_id = ?";
        List<Map<String, Object>> list = this.sqlQuery(sql, userId);
        return list;
    }

    public Map<String, Object> queryUserInfo(String year, int type, String userId, int status) {
        String sql = " select a.* from lkcrm_crm_achievement a right join lkcrm_admin_user b on a.user_id = b.user_id  where a.year = ? and a.type = ? and b.user_id = ? and a.status = ?";
        List<Map<String, Object>> list = this.sqlQuery(sql, year, type, userId, status);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }
    //   select a.* from lkcrm_crm_achievement a right join lkcrm_admin_user b on a.obj_id = b.user_id  where a.year = ? and a.type = ? and b.user_id = ? and a.status = ?
}
