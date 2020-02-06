package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminMenuEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LkCrmAdminMenuDao extends SimpleHibernateDao<LkCrmAdminMenuEntity, Integer> {

    public List queryRealmByRoleId(int roleId) {
        String sql = "select concat((select realm from `lkcrm_admin_menu` where menu_id = b.parent_id),':',b.realm) from `lkcrm_admin_role_menu` a left join `lkcrm_admin_menu` b on a.menu_id = b.menu_id where a.role_id = ? and b.menu_type = 3";
        return this.queryListBySql(sql, roleId);
    }
}
