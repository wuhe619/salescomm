package com.bdaim.crm.erp.admin.service;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.crm.common.config.cache.CaffeineCache;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.dao.LkCrmAdminRoleDao;
import com.bdaim.crm.dao.LkCrmAdminUserDao;
import com.bdaim.crm.entity.LkCrmAdminMenuEntity;
import com.bdaim.crm.entity.LkCrmAdminRoleEntity;
import com.bdaim.crm.entity.LkCrmAdminRoleMenuEntity;
import com.bdaim.crm.entity.LkCrmAdminUserRoleEntity;
import com.bdaim.crm.erp.admin.entity.AdminUserRole;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.NumberConvertUtil;
import com.jfinal.aop.Before;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class LkAdminRoleService {

    @Autowired
    private AdminMenuService adminMenuService;

    @Autowired
    public LkCrmAdminRoleDao crmAdminRoleDao;

    @Autowired
    public LkCrmAdminUserDao crmAdminUserDao;

    @Autowired
    public CustomerUserDao customerUserDao;

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
            String custId = BaseUtil.getCustId();
            if (1 == roleType) {
                custId = "";
            }
            List<Record> recordList = JavaBeanUtil.mapToRecords(crmAdminRoleDao.getRoleListByRoleType(roleType, custId));
            recordList.forEach(role -> {
                List<Integer> crm = crmAdminRoleDao.getRoleMenu(role.getInt("id"), 1, 1);
                List<Integer> bi = crmAdminRoleDao.getRoleMenu(role.getInt("id"), 2, 2);
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
    public List getRoleUser(Integer roleType) {
        return crmAdminRoleDao.getRoleUser(roleType);
    }

    /**
     * @author wyq
     * 新建
     */
    public R save(LkCrmAdminRoleEntity adminRole) {
        adminRole.setCustId(BaseUtil.getCustId());
        Integer number = crmAdminRoleDao.queryForInt("select count(*) from lkcrm_admin_role where role_name = ? and role_type = ?", adminRole.getRoleName(), adminRole.getRoleType());
        if (number > 0) {
            return R.error("角色名已存在");
        }
        return (int) crmAdminRoleDao.saveReturnPk(adminRole) > 0 ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 编辑角色
     */
    public Integer update(LkCrmAdminRoleEntity adminRole) {
        adminRole.setCustId(BaseUtil.getCustId());
        LkCrmAdminRoleEntity entity = crmAdminRoleDao.get(adminRole.getRoleId());
        BeanUtils.copyProperties(adminRole, entity, JavaBeanUtil.getNullPropertyNames(adminRole));
        crmAdminRoleDao.update(entity);
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
        Record record = JavaBeanUtil.mapToRecord(crmAdminRoleDao.queryUniqueSql("select count(*) as menuNum from lkcrm_admin_role_menu where role_id = ?", roleId));
        if (record.getInt("menuNum") == 0) {
            return crmAdminRoleDao.deleteRole(roleId) > 0;
            //return Db.delete(Db.getSql("admin.role.deleteRole"), roleId) > 0;
        }
        crmAdminRoleDao.deleteRole(roleId);
        crmAdminRoleDao.deleteRoleMenu(roleId);
        return true;
       /* return Db.tx(() -> {
            Db.delete(Db.getSql("admin.role.deleteRole"), roleId);
            Db.delete(Db.getSql("admin.role.deleteRoleMenu"), roleId);
            return true;
        });*/
    }

    /**
     * @author wyq
     * 删除
     */
    @Before(Tx.class)
    public boolean deleteWorkRole(Integer roleId) {
        crmAdminRoleDao.deleteRole(roleId);
        crmAdminRoleDao.deleteRoleMenu(roleId);
       /* Db.delete(Db.getSql("admin.role.deleteRole"), roleId);
        Db.delete(Db.getSql("admin.role.deleteRoleMenu"), roleId);*/
        crmAdminRoleDao.executeUpdateSQL("update `lkcrm_work_user` set role_id = ? where role_id = ?", BaseConstant.SMALL_WORK_EDIT_ROLE_ID, roleId);
        return true;
    }


    /**
     * @author wyq
     * 复制
     */
    @Before(Tx.class)
    public void copy(Integer roleId) {
        LkCrmAdminRoleEntity adminRole = crmAdminRoleDao.get(roleId);
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmAdminRoleDao.getMenuIdsList(roleId));
        //List<Record> recordList = Db.find(Db.getSql("admin.role.getMenuIdsList"), roleId);
        List<Integer> menuIdsList = new ArrayList<>(recordList.size());
        for (Record record : recordList) {
            menuIdsList.add(record.getInt("menu_id"));
        }
        String roleName = adminRole.getRoleName().trim();
        String pre = ReUtil.delFirst("[(]\\d+[)]$", roleName);
        List<LkCrmAdminRoleEntity> adminRoleList;
        if (!ReUtil.contains("^[(]\\d+[)]$", roleName)) {
            adminRoleList = crmAdminRoleDao.find("from LkCrmAdminRoleEntity where roleName like '" + pre + "%'");
        } else {
            adminRoleList = crmAdminRoleDao.find("from LkCrmAdminRoleEntity where roleName regexp '^[(]\\d+[)]$'");
        }
        StringBuffer numberSb = new StringBuffer();
        for (LkCrmAdminRoleEntity dbAdminRole : adminRoleList) {
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
        //adminRole.setRoleId(null);
        crmAdminRoleDao.save(adminRole);
        Integer copyRoleId = adminRole.getRoleId();
        adminMenuService.saveRoleMenu(copyRoleId, adminRole.getDataType(), menuIdsList);
    }

    /**
     * @author wyq
     * 角色关联员工
     */
    public R relatedUser(LkCrmAdminUserRoleEntity adminUserRole) {
        if (adminUserRole != null && adminUserRole.getUserIds() != null) {
            String[] userIdsArr = adminUserRole.getUserIds().split(",");
            String[] roleIdsArr = adminUserRole.getRoleIds().split(",");
            for (String userId : userIdsArr) {
                for (String roleId : roleIdsArr) {
                    crmAdminRoleDao.executeUpdateSQL("delete from lkcrm_admin_user_role where user_id = ? and role_id = ?", userId, roleId);
                    LkCrmAdminUserRoleEntity userRole = new LkCrmAdminUserRoleEntity();
                    userRole.setUserId(NumberUtil.parseLong(userId));
                    userRole.setRoleId(NumberUtil.parseInt(roleId));
                    crmAdminRoleDao.saveOrUpdate(userRole);
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
        /*if (adminUserRole.getUserId().equals(BaseConstant.SUPER_ADMIN_USER_ID)) {
            return R.error("超级管理员不可被更改");
        }*/
        CustomerUser user = customerUserDao.get(adminUserRole.getUserId());
        if (user == null) {
            return R.error("解除角色关联员工异常");
        }
        if (Objects.equals(user.getUserType(), 1)) {
            return R.error("超级管理员不可被更改");
        }
        return crmAdminRoleDao.executeUpdateSQL("delete from lkcrm_admin_user_role where user_id = ? and role_id = ?", adminUserRole.getUserId(), adminUserRole.getRoleId()) > 0 ? R.ok() : R.error();
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
        List<Record> roleList = JavaBeanUtil.mapToRecords(crmAdminRoleDao.sqlQuery("select * from lkcrm_admin_role where role_type in (5,6) and is_hidden = 1"));
        roleList.forEach(record -> {
            List<Map<String, Object>> role_id = crmAdminRoleDao.sqlQuery("select menu_id from lkcrm_admin_role_menu where role_id = ?", record.getInt("role_id"));
            List<Integer> rules = new ArrayList<>();
            for (Map<String, Object> m : role_id) {
                rules.add(NumberConvertUtil.parseInt(m.get("data_type")));
            }
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
        LkCrmAdminRoleEntity adminRole = new LkCrmAdminRoleEntity();
        adminRole.setRoleName(roleName);
        adminRole.setRoleType(6);
        adminRole.setRemark(remark);
        if (roleId == null) {
            bol = (int) crmAdminRoleDao.saveReturnPk(adminRole) > 0;
        } else {
            adminRole.setRoleId(roleId);
            //crmAdminRoleDao.executeUpdateSQL("delete from `lkcrm_admin_role_menu` where role_id = ?", roleId);
            LkCrmAdminRoleEntity entity = crmAdminRoleDao.get(roleId);
            BeanUtils.copyProperties(adminRole, entity, JavaBeanUtil.getNullPropertyNames(adminRole));
            crmAdminRoleDao.update(entity);
            bol = true;
        }
        rules.forEach(menuId -> {
            LkCrmAdminRoleMenuEntity adminRoleMenu = new LkCrmAdminRoleMenuEntity();
            adminRoleMenu.setRoleId(adminRole.getRoleId());
            adminRoleMenu.setMenuId((Integer) menuId);
            crmAdminRoleDao.saveOrUpdate(adminRoleMenu);
        });
        return bol ? R.ok() : R.error();
    }

    public int checkCustRoleExist(String roleName, int roleType, int roleId, String custId) {
        Integer number = crmAdminRoleDao.queryForInt("select count(*) from lkcrm_admin_role where role_name = ? and role_type = ? and role_id != ? AND cust_id = ?",
                roleName, roleType, roleId, custId);
        return number;
    }
}
