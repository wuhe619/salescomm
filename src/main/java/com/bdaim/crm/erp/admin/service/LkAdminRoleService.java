package com.bdaim.crm.erp.admin.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.auth.service.impl.TokenServiceImpl;
import com.bdaim.common.auth.Token;
import com.bdaim.common.auth.service.TokenCacheService;
import com.bdaim.crm.common.config.cache.CaffeineCache;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.common.exception.ParamValidateException;
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
import com.bdaim.customer.dto.*;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.smscenter.service.SendSmsService;
import com.bdaim.util.CipherUtil;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
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
import java.util.*;

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
    @Autowired
    private SendSmsService sendSmsService;
    @Autowired
    private TokenCacheService tokenCacheService;
    @Autowired
    private CustomerService customerService;

    /**
     * @author wyq
     * 获取全部角色列表
     */
    public List<Record> getAllRoleList(String roleName, Integer rType) {
        List<Record> records = new ArrayList<>();
        Integer[] types = BaseConstant.ROLE_TYPES;
        if (rType != null) {
            types = new Integer[]{rType};
        }
        LoginUser user = BaseUtil.getUser();
        for (Integer roleType : types) {
            Record record = new Record();
            record.set("name", roleTypeCaseName(roleType));
            record.set("pid", roleType);

            String custId = user.getCustId();
            if (1 == roleType) {
                custId = "";
            }
            List<Record> recordList = JavaBeanUtil.mapToRecords(crmAdminRoleDao.getRoleListByRoleType(roleType, custId, roleName));
            recordList.forEach(role -> {
                List<Integer> crm = crmAdminRoleDao.getRoleMenu(role.getInt("id"), 1, 1);
                List<Integer> bi = crmAdminRoleDao.getRoleMenu(role.getInt("id"), 2, 2);

                List<Integer> manage = crmAdminRoleDao.getRoleMenu(role.getInt("id"), 3, 3);
                List<Integer> find = crmAdminRoleDao.getRoleMenu(role.getInt("id"), 169, 169);
                role.set("rules", new JSONObject().fluentPut("crm", crm).fluentPut("bi", bi)
                        .fluentPut("find", find).fluentPut("manage", manage));

                role.set("userNum", crmAdminRoleDao.queryForInt("SELECT COUNT(0) FROM lkcrm_admin_user_role a JOIN lkcrm_admin_user b on a.user_id = b.user_id WHERE a.role_id = ? AND b.cust_id = ? ", role.getInt("id"), user.getCustId()));
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
        LoginUser user = BaseUtil.getUser();
        adminRole.setCustId(user.getCustId());
        Integer number = crmAdminRoleDao.queryForInt("select count(*) from lkcrm_admin_role where role_name = ? and role_type = ? AND cust_id =?", adminRole.getRoleName(), adminRole.getRoleType(), user.getCustId());
        if (number > 0) {
            return R.error("角色名已存在");
        }
        adminRole.setCreateTime(new Date());
        adminRole.setStatus(1);
        adminRole.setDataType(5);
        adminRole.setIsHidden(1);
        return (int) crmAdminRoleDao.saveReturnPk(adminRole) > 0 ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 编辑角色
     */
    public Integer update(LkCrmAdminRoleEntity adminRole) {
        adminRole.setCustId(BaseUtil.getCustId());
        adminRole.setUpdateTime(new Date());
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
        //menuRecords 该用户拥有的所有的权限菜单
        if (roleIds.contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
            menuRecords = adminMenuService.queryAllMenu();
        } else {
            menuRecords = adminMenuService.queryMenuByUserId(userId);
        }
        //adminMenus 系统所有权限的顶级菜单
        List<LkCrmAdminMenuEntity> adminMenus = adminMenuService.queryMenuByParentId(0);
        for (LkCrmAdminMenuEntity adminMenu : adminMenus) {
            JSONObject object = new JSONObject();
            //adminMenuList 根据顶级菜单查询出来的二级菜单项
            List<LkCrmAdminMenuEntity> adminMenuList = adminMenuService.queryMenuByParentId(adminMenu.getMenuId());
            //menu 二级菜单项
            for (LkCrmAdminMenuEntity menu : adminMenuList) {
                JSONObject authObject = new JSONObject();
                //遍历用户拥有的权限菜单项，如果父级ID和二级菜单项相等，则任务有权限
                for (Map<String, Object> record : menuRecords) {
                    if (menu.getMenuId().equals(NumberConvertUtil.everythingToInt(record.get("parent_id")))) {
                        authObject.put(String.valueOf(record.get("realm")), true);
                    }
                    if (menu.getMenuId().equals(NumberConvertUtil.everythingToInt(record.get("menu_id")))) {
                        if (menu.getParentId().equals(3)) {
                            object.put(String.valueOf(record.get("realm")), true);
                        }
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
                    object.put("find", true);
                    object.put("publicSea", true);
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
            while (numberSb.toString().contains("（" + i + "）")) {
                i++;
            }
        }
        adminRole.setRoleName(pre + "（" + i + "）");
        //adminRole.setRoleId(null);
        crmAdminRoleDao.getSession().clear();
        LkCrmAdminRoleEntity newAdminRole = new LkCrmAdminRoleEntity(adminRole.getRoleName(), adminRole.getRoleType(), adminRole.getRemark(), adminRole.getStatus()
                , adminRole.getDataType(), adminRole.getIsHidden(), adminRole.getLabel(), adminRole.getCustId());
        newAdminRole.setCreateTime(new Date());
        Integer copyRoleId = (int) crmAdminRoleDao.saveReturnPk(newAdminRole);
        //Integer copyRoleId = newAdminRole.getRoleId();
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
            adminRole.setCreateTime(new Date());
            bol = (int) crmAdminRoleDao.saveReturnPk(adminRole) > 0;
        } else {
            adminRole.setRoleId(roleId);
            //crmAdminRoleDao.executeUpdateSQL("delete from `lkcrm_admin_role_menu` where role_id = ?", roleId);
            LkCrmAdminRoleEntity entity = crmAdminRoleDao.get(roleId);
            BeanUtils.copyProperties(adminRole, entity, JavaBeanUtil.getNullPropertyNames(adminRole));
            entity.setUpdateTime(new Date());
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

    @SuppressWarnings("all")
    public Token createTokenByPhone(Map<String, String> params) {
        String phone = params.get("phone");
        int type = Integer.parseInt(params.get("type"));
        String code = params.get("code");
        //1. 校验验证码是否正确
        boolean success = sendSmsService.verificationCode(phone, type, code) == 1;
        if (!success) {
            throw new ParamValidateException("0", "手机验证码不正确");
        }
        //2. 根据手机号查询用户信息，返回token
        LoginUser userdetail = null;
        CustomerUser u = getUserByPhone(phone);
        if (u != null) {
            String username = u.getAccount();
            String tokenid = (String) TokenServiceImpl.name2token.get(username);
            if (!StringUtil.isEmpty(tokenid)) {
                userdetail = (LoginUser) tokenCacheService.getToken(tokenid, LoginUser.class);
                if (userdetail != null) {
                    //前台用户权限信息
                    CustomerUserPropertyDO userProperty = customerUserDao.getProperty(String.valueOf(u.getId()), CustomerUserPropertyEnum.RESOURCE_MENU.getKey());
                    if (userProperty != null && StringUtil.isNotEmpty(userProperty.getPropertyValue())) {
                        userdetail.setResourceMenu(userProperty.getPropertyValue());
                        userdetail.setStatus(u.getStatus().toString());
                        return userdetail;
                    }
                } else {
                    TokenServiceImpl.name2token.remove(username);
                }
            }
            userdetail = new LoginUser(u.getId(), u.getAccount(), CipherUtil.encodeByMD5(u.getId() + "" +
                    System.currentTimeMillis()));
            if (1 == u.getStatus()) {
                userdetail.addAuth("USER_FREEZE");
            } else if (3 == u.getStatus()) {
                userdetail.addAuth("USER_NOT_EXIST");
            } else if (0 == u.getStatus()) {
                //user_type: 1=管理员 2=普通员工
                userdetail.addAuth("ROLE_CUSTOMER");
            }
            userdetail.setCustId(u.getCust_id());
            userdetail.setId(u.getId());
            userdetail.setUserId(u.getId());
            userdetail.setUserType(String.valueOf(u.getUserType()));
            userdetail.setRole(userdetail.getAuths().size() > 0 ? userdetail.getAuths().get(0) : "");

            userdetail.setStatus(u.getStatus().toString());
            userdetail.setStateCode("200");
            userdetail.setMsg("SUCCESS");
            userdetail.setAuth(userdetail.getAuths().size() > 0 ? userdetail.getAuths().get(0) : "");
            userdetail.setUserName(userdetail.getUsername());
            userdetail.setUser_id(userdetail.getId().toString());
            // 处理服务权限
            userdetail.setServiceMode(ServiceModeEnum.MARKET_TASK.getCode());
            CustomerPropertyDTO cpd = customerService.getCustomerProperty(u.getCust_id(), CustomerPropertyEnum.SERVICE_MODE.getKey());
            if (cpd != null && StringUtil.isNotEmpty(cpd.getPropertyValue())) {
                userdetail.setServiceMode(cpd.getPropertyValue());
            }
            CustomerPropertyDTO industry = customerService.getCustomerProperty(u.getCust_id(), CustomerPropertyEnum.INTEN_INDUCTRY.getKey());
            if (industry != null && StringUtil.isNotEmpty(industry.getPropertyValue())) {
                userdetail.setInten_industry(industry.getPropertyValue());
            }
            CustomerPropertyDTO apiToken = customerService.getCustomerProperty(u.getCust_id(), CustomerPropertyEnum.API_TOKEN.getKey());
            if (apiToken != null && StringUtil.isNotEmpty(apiToken.getPropertyValue())) {
                userdetail.setApi_token(apiToken.getPropertyValue());
            }

            //前台用户权限信息
            CustomerUserPropertyDO userProperty = customerUserDao.getProperty(String.valueOf(u.getId()), CustomerUserPropertyEnum.RESOURCE_MENU.getKey());
            if (userProperty != null && StringUtil.isNotEmpty(userProperty.getPropertyValue())) {
                userdetail.setResourceMenu(userProperty.getPropertyValue());
            }
            CustomerUserPropertyDO mobile_num = customerUserDao.getProperty(u.getId().toString(), "mobile_num");
            if (mobile_num != null && StringUtil.isNotEmpty(mobile_num.getPropertyValue())) {
                userdetail.setMobile_num(mobile_num.getPropertyValue());
            } else {
                userdetail.setMobile_num("");
            }
            // 查询用户组信息
            CustomerUserGroupRelDTO cug = customerUserDao.getCustomerUserGroupByUserId(u.getId());
            userdetail.setUserGroupId("");
            userdetail.setUserGroupRole("");
            userdetail.setJobMarketId("");
            if (cug != null) {
                userdetail.setUserGroupId(cug.getGroupId());
                userdetail.setUserGroupRole(String.valueOf(cug.getType()));
                userdetail.setJobMarketId(cug.getJobMarketId());
            }
            // 处理客户营销类型
            userdetail.setMarketingType(MarketTypeEnum.B2C.getCode());
            CustomerPropertyDTO marketingType = customerService.getCustomerProperty(u.getCust_id(), CustomerPropertyEnum.MARKET_TYPE.getKey());
            if (marketingType != null && StringUtil.isNotEmpty(marketingType.getPropertyValue())) {
                userdetail.setMarketingType(NumberConvertUtil.parseInt(marketingType.getPropertyValue()));
            }
            if (userdetail != null) {
                TokenServiceImpl.name2token.put(username, userdetail.getTokenid());
            }
        }
        return userdetail;
    }

    private CustomerUser getUserByPhone(String phone) {
        String hql = "SELECT t1 FROM CustomerUser t1,LkCrmAdminUserEntity t2 WHERE t1.id=t2.userId " +
                "AND t2.mobile = ?";
        List<CustomerUser> userList = crmAdminUserDao.find(hql, phone);
        if(!CollectionUtil.isEmpty(userList)){
            return userList.get(0);
        }
        return null;
    }
}
