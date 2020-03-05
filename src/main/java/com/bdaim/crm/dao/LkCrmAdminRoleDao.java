package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminRoleEntity;
import com.bdaim.util.NumberConvertUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmAdminRoleDao extends SimpleHibernateDao<LkCrmAdminRoleEntity, Integer> {

    public List<Integer> queryDataTypeByUserId(Long userId) {
        String sql = "SELECT DISTINCT a.data_type FROM lkcrm_admin_role as a LEFT JOIN lkcrm_admin_user_role as b on a.role_id=b.role_id WHERE b.user_id=?  ORDER BY a.data_type desc";
        List<Map<String, Object>> maps = sqlQuery(sql, userId);
        List<Integer> data = new ArrayList<>();
        for (Map<String, Object> m : maps) {
            data.add(NumberConvertUtil.parseInt(m.get("data_type")));
        }
        return data;
    }

    public List<Map<String, Object>> queryDataTypeByUserId(Integer roleId) {
        String sql = " select menu_id from lkcrm_admin_role_menu where role_id = ?";
        List<Map<String, Object>> maps = sqlQuery(sql, roleId);
        return maps;
    }

    public int deleteRoleMenu(Integer roleId) {
        String sql = "  delete from lkcrm_admin_role_menu where role_id = ?";
        return executeUpdateSQL(sql, roleId);
    }

    public int deleteRole(Integer roleId) {
        String sql = "   delete from lkcrm_admin_role where role_id = ?";
        return executeUpdateSQL(sql, roleId);
    }

    public List<Map<String, Object>> getRoleListByRoleType(Integer roleType) {
        String sql = " select role_id as id ,role_name as title,role_name as remark,data_type as type,status,role_type as pid,label from lkcrm_admin_role WHERE role_type=? and is_hidden = 1";
        List<Map<String, Object>> maps = sqlQuery(sql, roleType);
        return maps;
    }

    public List<Integer> getRoleMenu(Integer role_id, int parent_id, int parent_id2) {
        String sql = " select b.menu_id\n" +
                "      from lkcrm_admin_role_menu as a inner join lkcrm_admin_menu as b on a.menu_id = b.menu_id\n" +
                "      where a.role_id = ?\n" +
                "       and (b.parent_id in (SELECT menu_id FROM lkcrm_admin_menu WHERE parent_id = ?) or b.parent_id = ?)";
        List<Map<String, Object>> maps = sqlQuery(sql, role_id, parent_id, parent_id2);
        List<Integer> data = new ArrayList<>();
        for (Map<String, Object> m : maps) {
            data.add(NumberConvertUtil.parseInt(m.get("menu_id")));
        }
        return data;
    }


    public List<Map<String, Object>> getRoleUser(Integer roleType) {
        String sql = "  select *,b.name as dept_name,c.name as post_name\n" +
                "      from lkcrm_admin_user as a inner join lkcrm_admin_dept as b inner join lkcrm_admin_post as c inner join\n" +
                "      lkcrm_admin_user_role as d inner join lkcrm_admin_role as e\n" +
                "      where a.dept_id = b.dept_id and a.post = c.id and a.user_id = d.user_id and d.role_id = e.role_id and role_type = ?";
        List<Map<String, Object>> maps = sqlQuery(sql, roleType);
        return maps;
    }

    public List<Map<String, Object>> getMenuIdsList(Integer role_id) {
        String sql = " select menu_id from lkcrm_admin_role_menu where role_id = ?";
        List<Map<String, Object>> maps = sqlQuery(sql, role_id);
        return maps;
    }
}
