package com.bdaim.crm.erp.admin.service;

import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.crm.dao.LkCrmAdminRoleDao;
import com.bdaim.crm.entity.LkCrmAdminMenuEntity;
import com.bdaim.crm.entity.LkCrmAdminUserRoleEntity;
import com.bdaim.util.NumberConvertUtil;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.bdaim.crm.common.config.cache.CaffeineCache;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.erp.admin.entity.AdminMenu;
import com.bdaim.crm.erp.admin.entity.AdminRole;
import com.bdaim.crm.erp.admin.entity.AdminRoleMenu;
import com.bdaim.crm.erp.admin.entity.AdminUserRole;
import com.bdaim.crm.utils.R;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminRoleService {

    @Resource
    private AdminMenuService adminMenuService;

    @Resource
    private LkCrmAdminRoleDao crmAdminRoleDao;

    /**
     * @author wyq
     * 获取全部角色列表
     */
    public List<Record> getAllRoleList() {
        List<Record> records = new ArrayList<>();
        for (Integer roleType : BaseConstant.ROLE_TYPES) {
            Record record = new Record();
            record.set("name", roleTypeCaseName(roleType));
            record.set("pid", roleType);
            List<Record> recordList = Db.find(Db.getSql("admin.role.getRoleListByRoleType"), roleType);
            recordList.forEach(role -> {
                List<Integer> crm = Db.query(Db.getSql("admin.role.getRoleMenu"), role.getInt("id"), 1, 1);
                List<Integer> bi = Db.query(Db.getSql("admin.role.getRoleMenu"), role.getInt("id"), 2, 2);
                role.set("rules", new JSONObject().fluentPut("crm", crm).fluentPut("bi", bi));
            });
            record.set("list", recordList);
            records.add(record);
        }
        return records;
    }

    /**
     * @author wyq
     * 根据角色类型查询关联员工
     */
    public List<Record> getRoleUser(Integer roleType) {
        return Db.find(Db.getSql("admin.role.getRoleUser"), roleType);
    }

    /**
     * @author wyq
     * 新建
     */
    public R save(AdminRole adminRole) {
        Integer number = Db.queryInt("select count(*) from 72crm_admin_role where role_name = ? and role_type = ?", adminRole.getRoleName(), adminRole.getRoleType());
        if (number > 0) {
            return R.error("角色名已存在");
        }
        return adminRole.save() ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 编辑角色
     */
    @Before(Tx.class)
    public Integer update(AdminRole adminRole) {
        adminRole.update();
        List<Integer> menuList;
        if (adminRole.getMenuIds() != null) {
            try {
                menuList = JSON.parseArray(URLDecoder.decode(adminRole.getMenuIds(), "utf-8"), Integer.class);
            } catch (UnsupportedEncodingException e) {
                Log.getLog(getClass()).error("", e);
                throw new RuntimeException("数据错误");
            }
            adminMenuService.saveRoleMenu(adminRole.getRoleId(), adminRole.getDataType(), menuList);
            return 1;
        }
        return 0;
    }

    @Before(Tx.class)
    public void updateRoleMenu(JSONObject jsonObject) {
        adminMenuService.saveRoleMenu(jsonObject.getInteger("id"), jsonObject.getInteger("type"), jsonObject.getJSONArray("rules").toJavaList(Integer.class));
    }

    /**
     * 查看权限
     */
    public JSONObject auth(Long userId) {
        JSONObject jsonObject = CaffeineCache.ME.get("role:permissions", userId.toString());
        if (jsonObject != null) {
            return jsonObject;
        }
        jsonObject = new JSONObject();
        List<Map<String, Object>> menuRecords;
        List<Integer> roleIds = queryRoleIdsByUserId(userId);
        if (roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
            menuRecords = adminMenuService.queryAllMenu();
        } else {
            menuRecords = adminMenuService.queryMenuByUserId(userId);
        }
        List<LkCrmAdminMenuEntity> adminMenus = adminMenuService.queryMenuByParentId(0);
        for (LkCrmAdminMenuEntity adminMenu : adminMenus) {
            JSONObject object = new JSONObject();
            List<LkCrmAdminMenuEntity> adminMenuList = adminMenuService.queryMenuByParentId(adminMenu.getMenuId());
            for (LkCrmAdminMenuEntity menu : adminMenuList) {
                JSONObject authObject = new JSONObject();
                for (Map<String, Object> record : menuRecords) {
                    if (menu.getMenuId().equals(NumberConvertUtil.everythingToInt(record.get("parent_id")))) {
                        authObject.put(String.valueOf(record.get("realm")), true);
                    }
                }
                if (!authObject.isEmpty()) {
                    object.put(menu.getRealm(), authObject);
                }
            }
            if (adminMenu.getMenuId().equals(3)) {
                if (roleIds.contains(2) || roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                    object.put("system", true);
                    object.put("user", true);
                    object.put("examineFlow", true);
                    object.put("oa", true);
                    object.put("crm", true);
                    object.put("permission", true);
                }
                if (roleIds.contains(3) || roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                    object.put("user", true);
                }
                if (roleIds.contains(4) || roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                    object.put("examineFlow", true);
                }
                if (roleIds.contains(5) || roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                    object.put("oa", true);
                }
                if (roleIds.contains(6) || roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                    object.put("crm", true);
                }
            }
            if (!object.isEmpty()) {
                jsonObject.put(adminMenu.getRealm(), object);
            }
        }
        CaffeineCache.ME.put("role:permissions:" + userId.toString(), jsonObject);
        return jsonObject;
    }

    /**
     * @author wyq
     * 删除
     */
    public boolean delete(Integer roleId) {
        Record record = Db.findFirst("select count(*) as menuNum from 72crm_admin_role_menu where role_id = ?", roleId);
        if (record.getInt("menuNum") == 0) {
            return Db.delete(Db.getSql("admin.role.deleteRole"), roleId) > 0;
        }
        return Db.tx(() -> {
            Db.delete(Db.getSql("admin.role.deleteRole"), roleId);
            Db.delete(Db.getSql("admin.role.deleteRoleMenu"), roleId);
            return true;
        });
    }

    /**
     * @author wyq
     * 删除
     */
    @Before(Tx.class)
    public boolean deleteWorkRole(Integer roleId) {
        Db.delete(Db.getSql("admin.role.deleteRole"), roleId);
        Db.delete(Db.getSql("admin.role.deleteRoleMenu"), roleId);
        Db.update("update `72crm_work_user` set role_id = ? where role_id = ?", BaseConstant.SMALL_WORK_EDIT_ROLE_ID, roleId);
        return true;
    }


    /**
     * @author wyq
     * 复制
     */
    @Before(Tx.class)
    public void copy(Integer roleId) {
        AdminRole adminRole = AdminRole.dao.findById(roleId);
        List<Record> recordList = Db.find(Db.getSql("admin.role.getMenuIdsList"), roleId);
        List<Integer> menuIdsList = new ArrayList<>(recordList.size());
        for (Record record : recordList) {
            menuIdsList.add(record.getInt("menu_id"));
        }
        String roleName = adminRole.getRoleName().trim();
        String pre = ReUtil.delFirst("[(]\\d+[)]$", roleName);
        List<AdminRole> adminRoleList;
        if (!ReUtil.contains("^[(]\\d+[)]$", roleName)) {
            adminRoleList = AdminRole.dao.find("select * from 72crm_admin_role where role_name like '" + pre + "%'");
        } else {
            adminRoleList = AdminRole.dao.find("select * from 72crm_admin_role where role_name regexp '^[(]\\d+[)]$'");
        }
        StringBuffer numberSb = new StringBuffer();
        for (AdminRole dbAdminRole : adminRoleList) {
            String endCode = ReUtil.get("[(]\\d+[)]$", dbAdminRole.getRoleName(), 0);
            if (endCode != null) {
                numberSb.append(endCode);
            }
        }
        int i = 1;
        if (numberSb.length() == 0) {
            while (numberSb.toString().contains("(" + i + ")")) {
                i++;
            }
        }
        adminRole.setRoleName(pre + "(" + i + ")");
        adminRole.setRoleId(null);
        adminRole.save();
        Integer copyRoleId = adminRole.getInt("role_id");
        adminMenuService.saveRoleMenu(copyRoleId, adminRole.getDataType(), menuIdsList);
    }

    /**
     * @author wyq
     * 角色关联员工
     */
    @Before(Tx.class)
    public R relatedUser(AdminUserRole adminUserRole) {
        if (adminUserRole != null && adminUserRole.getUserIds() != null) {
            String[] userIdsArr = adminUserRole.getUserIds().split(",");
            String[] roleIdsArr = adminUserRole.getRoleIds().split(",");
            for (String userId : userIdsArr) {
                for (String roleId : roleIdsArr) {
                    Db.delete("delete from 72crm_admin_user_role where user_id = ? and role_id = ?", Integer.valueOf(userId), Integer.valueOf(roleId));
                    AdminUserRole userRole = new AdminUserRole();
                    userRole.setUserId(Long.valueOf(userId));
                    userRole.setRoleId(Integer.valueOf(roleId));
                    userRole.save();
                }
            }
            return R.ok();
        } else {
            return R.error("请选择角色和员工");
        }
    }

    /**
     * @author wyq
     * 解除角色关联员工
     */
    public R unbindingUser(AdminUserRole adminUserRole) {
        if (adminUserRole.getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID)) {
            return R.error("超级管理员不可被更改");
        }
        return Db.delete("delete from 72crm_admin_user_role where user_id = ? and role_id = ?", adminUserRole.getUserId(), adminUserRole.getRoleId()) > 0 ? R.ok() : R.error();
    }

    public List<Integer> queryRoleIdsByUserId(Long userId) {
        String sql = "FROM LkCrmAdminUserRoleEntity WHERE userId=?";
        List<LkCrmAdminUserRoleEntity> list = crmAdminRoleDao.find(sql, userId);
        List<Integer> roleIds = new ArrayList<>();
        list.forEach(s -> roleIds.add(s.getRoleId()));
        return roleIds;
        //return Db.query(Db.getSql("admin.role.queryRoleIdsByUserId"), userId);
    }

    /**
     * 角色类型转换名称
     *
     * @param type 类型
     * @return 角色名称
     */
    private String roleTypeCaseName(Integer type) {
        String name;
        switch (type) {
            case 1:
                name = "管理角色";
                break;
            case 2:
                name = "客户管理角色";
                break;
            case 3:
                name = "人事角色";
                break;
            case 4:
                name = "财务角色";
                break;
            case 5:
                name = "项目角色";
                break;
            default:
                name = "自定义角色";
        }
        return name;
    }

    /**
     * 项目管理角色列表
     *
     * @author wyq
     */
    public R queryProjectRoleList() {
        List<Record> roleList = Db.find("select * from 72crm_admin_role where role_type in (5,6) and is_hidden = 1");
        roleList.forEach(record -> {
            List<Integer> rules = Db.query("select menu_id from 72crm_admin_role_menu where role_id = ?", record.getInt("role_id"));
            record.set("rules", rules);
        });
        return R.ok().put("data", roleList);
    }

    public R setWorkRole(JSONObject jsonObject) {
        boolean bol;
        Integer roleId = jsonObject.getInteger("roleId");
        String roleName = jsonObject.getString("roleName");
        String remark = jsonObject.getString("remark");
        JSONArray rules = jsonObject.getJSONArray("rules");
        AdminRole adminRole = new AdminRole();
        adminRole.setRoleName(roleName);
        adminRole.setRoleType(6);
        adminRole.setRemark(remark);
        if (roleId == null) {
            bol = adminRole.save();
        } else {
            adminRole.setRoleId(roleId);
            Db.delete("delete from `72crm_admin_role_menu` where role_id = ?", roleId);
            bol = adminRole.update();
        }
        rules.forEach(menuId -> {
            AdminRoleMenu adminRoleMenu = new AdminRoleMenu();
            adminRoleMenu.setRoleId(adminRole.getRoleId());
            adminRoleMenu.setMenuId((Integer) menuId);
            adminRoleMenu.save();
        });
        return bol ? R.ok() : R.error();
    }
}
