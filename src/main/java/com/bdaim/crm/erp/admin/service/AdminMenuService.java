package com.bdaim.crm.erp.admin.service;

import com.bdaim.crm.dao.LkCrmAdminMenuDao;
import com.bdaim.crm.entity.LkCrmAdminMenuEntity;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.bdaim.crm.erp.admin.entity.AdminMenu;
import com.bdaim.crm.erp.admin.entity.AdminRole;
import com.bdaim.crm.erp.admin.entity.AdminRoleMenu;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminMenuService {

    @Resource
    LkCrmAdminMenuDao crmAdminMenuDao;

    /**
     * 通过用户ID查询用户所拥有菜单
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    public List<Map<String, Object>> queryMenuByUserId(Long userId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT  c.realm,c.menu_id,c.parent_id FROM lkcrm_admin_user_role as a " +
                "      LEFT JOIN lkcrm_admin_role_menu as b on a.role_id=b.role_id " +
                "      LEFT JOIN lkcrm_admin_menu as c on b.menu_id=c.menu_id " +
                "      WHERE a.user_id=?");
        return crmAdminMenuDao.sqlQuery(sql.toString(), userId);
        //return Db.find(Db.getSql("admin.menu.queryMenuByUserId"), userId);
    }

    public List<Map<String, Object>> queryAllMenu() {
        String sql = "SELECT  c.realm,c.menu_id,c.parent_id FROM lkcrm_admin_menu as c";
        return crmAdminMenuDao.sqlQuery(sql);
        //return Db.find(Db.getSql("admin.menu.queryAllMenu"));
    }

    /**
     * @author wyq
     * 根据角色id查询菜单id
     */
    public List<Integer> getMenuIdByRoleId(Integer roleId) {
        List<Record> menuList = Db.find(Db.getSql("admin.role.getMenuIdByRoleId"), roleId);
        List<Integer> menuIdList = new ArrayList<>(menuList.size());
        for (Record menu : menuList) {
            menuIdList.add(menu.getInt("menu_id"));
        }
        return menuIdList;
    }

    /**
     * @author wyq
     * 展示全部菜单
     */
    public List<AdminMenu> getAllMenuList(Integer parentId, Integer deepness) {
        List<AdminMenu> adminMenus = AdminMenu.dao.find(Db.getSql("admin.menu.queryMenuByParentId"), parentId);
        adminMenus.removeIf(adminMenu -> "work".equals(adminMenu.getRealm()));
        if (deepness != 0) {
            adminMenus.forEach(adminMenu -> {
                if (!adminMenu.getMenuType().equals(3)) {
                    adminMenu.put("childMenu", getAllMenuList(adminMenu.getMenuId(), deepness - 1));
                }
            });
        }
        return adminMenus;
    }

    public List<AdminMenu> getWorkMenuList(Integer parentId, Integer deepness) {
        List<AdminMenu> adminMenus = AdminMenu.dao.find(Db.getSql("admin.menu.queryMenuByParentId"), parentId);
        if (deepness != 0) {
            adminMenus.forEach(adminMenu -> {
                if (!adminMenu.getMenuType().equals(3)) {
                    adminMenu.put("childMenu", getAllMenuList(adminMenu.getMenuId(), deepness - 1));
                }
            });
        }
        return adminMenus;
    }

    /**
     * @author zhangzhiwei
     * 根据parentId查询菜单
     */
    public List<LkCrmAdminMenuEntity> queryMenuByParentId(Integer parentId) {
        String sql = " FROM LkCrmAdminMenuEntity where parentId = ?";
        return crmAdminMenuDao.find(sql, parentId);
        //return AdminMenu.dao.find(Db.getSql("admin.menu.queryMenuByParentId"), parentId);
    }


    /**
     * @author wyq
     * 保存角色权限
     */
    public boolean saveRoleMenu(Integer roleId, Integer dateType, List<Integer> menuIdList) {
        return Db.tx(() -> {
            AdminRole adminRole = new AdminRole();
            adminRole.setRoleId(roleId);
            adminRole.setDataType(dateType);
            adminRole.update();

            Db.delete(Db.getSql("admin.role.deleteRoleMenu"), roleId);
            if (null == menuIdList || menuIdList.size() == 0) {
                return true;
            }

            for (Integer menuId : menuIdList) {
                AdminRoleMenu adminRoleMenu = new AdminRoleMenu();
                adminRoleMenu.setRoleId(roleId);
                adminRoleMenu.setMenuId(menuId);
                adminRoleMenu.save();
            }
            return true;
        });
    }
}
