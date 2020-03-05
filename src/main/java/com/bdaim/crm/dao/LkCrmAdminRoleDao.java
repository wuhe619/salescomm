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
        String sql = "  delete from 72crm_admin_role_menu where role_id = ?";
        return executeUpdateSQL(sql, roleId);
    }
}
