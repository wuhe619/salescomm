package com.bdaim.crm.erp.admin.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.bdaim.common.dto.Page;
import com.bdaim.common.helper.SQLHelper;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.dao.*;
import com.bdaim.crm.entity.*;
import com.bdaim.crm.erp.admin.entity.AdminUser;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.crm.utils.Sort;
import com.bdaim.crm.utils.TagUtil;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.customer.user.dto.UserCallConfigDTO;
import com.bdaim.customersea.service.CustomerSeaService;
import com.bdaim.marketproject.dto.MarketProjectDTO;
import com.bdaim.marketproject.service.MarketProjectService;
import com.bdaim.rbac.dao.UserDao;
import com.bdaim.rbac.entity.User;
import com.bdaim.util.CipherUtil;
import com.bdaim.util.IDHelper;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

@Service("adminUserService")
@Transactional
public class LkAdminUserService {
    @Resource
    private LkAdminRoleService adminRoleService;
    @Resource
    private LkAdminDeptService adminDeptService;
    @Resource
    private LkCrmAdminUserDao crmAdminUserDao;
    @Resource
    private LkCrmAdminDeptDao crmAdminDeptDao;
    @Autowired
    private CustomerUserDao customerUserDao;
    @Autowired
    private CustomerSeaService customerSeaService;
    @Autowired
    private MarketProjectService marketProjectService;
    @Autowired
    private LkCrmAdminFieldDao crmAdminFieldDao;
    @Autowired
    private LkCrmAdminConfigDao crmAdminConfigDao;
    @Autowired
    private LkCrmAdminSceneDao crmAdminSceneDao;

    private void saveBpUser(long id, String userName, String realName, String password, String custId, int userType,
                            String callType, String callChannel, UserCallConfigDTO userDTO) {
        CustomerUser cu = new CustomerUser();
        cu.setId(id);
        cu.setAccount(userName);
        cu.setRealname(realName);
        if (null == password || "".equals(password)) {
            password = "123456";
        }
        //password = CipherUtil.generatePassword(password);
        cu.setPassword(password);
        cu.setStatus(0);
        cu.setCust_id(custId);
        cu.setUserType(userType); //2:添加普通员工 3:项目管理员
        cu.setCreateTime(String.valueOf(new Timestamp(System.currentTimeMillis())));
        customerUserDao.save(cu);

        List<CustomerUserPropertyDO> list = new ArrayList<>();
        CustomerUserPropertyDO mobile_num = new CustomerUserPropertyDO(cu.getId().toString(), "mobile_num", userDTO.getMobileNumber(), new Timestamp(System.currentTimeMillis()));
        CustomerUserPropertyDO email = new CustomerUserPropertyDO(cu.getId().toString(), "email", userDTO.getEmail(), new Timestamp(System.currentTimeMillis()));
        CustomerUserPropertyDO title = new CustomerUserPropertyDO(cu.getId().toString(), "title", userDTO.getTitle(), new Timestamp(System.currentTimeMillis()));
        if (2 == userType) {
            CustomerUserPropertyDO work_num = new CustomerUserPropertyDO(cu.getId().toString(), "work_num", userDTO.getWorkNum(), new Timestamp(System.currentTimeMillis()));
            //添加员工配置双呼默认审核通过
            if (StringUtil.isNotEmpty(userDTO.getWorkNum())) {
                CustomerUserPropertyDO work_num_status = new CustomerUserPropertyDO(cu.getId().toString(), "work_num_status", "1", new Timestamp(System.currentTimeMillis()));
                list.add(work_num_status);
            }
            CustomerUserPropertyDO seats_account = new CustomerUserPropertyDO(cu.getId().toString(), "seats_account", userDTO.getSeatsAccount(), new Timestamp(System.currentTimeMillis()));
            CustomerUserPropertyDO seats_password = new CustomerUserPropertyDO(cu.getId().toString(), "seats_password", userDTO.getSeatsPassword(), new Timestamp(System.currentTimeMillis()));
            CustomerUserPropertyDO extension_number = new CustomerUserPropertyDO(cu.getId().toString(), "extension_number", userDTO.getExtensionNumber(), new Timestamp(System.currentTimeMillis()));
            CustomerUserPropertyDO extension_password = new CustomerUserPropertyDO(cu.getId().toString(), "extension_password", userDTO.getExtensionPassword(), new Timestamp(System.currentTimeMillis()));
            CustomerUserPropertyDO call_type = new CustomerUserPropertyDO(cu.getId().toString(), "call_type", callType.trim(), new Timestamp(System.currentTimeMillis()));
            CustomerUserPropertyDO call_channel = new CustomerUserPropertyDO(cu.getId().toString(), "call_channel", callChannel, new Timestamp(System.currentTimeMillis()));
            if ("1".equals(userDTO.getAddAgentMethod())) {//api方式添加座席
                CustomerUserPropertyDO add_agent_method = new CustomerUserPropertyDO(cu.getId().toString(), "add_agent_method", userDTO.getAddAgentMethod(), new Timestamp(System.currentTimeMillis()));
                list.add(add_agent_method);
            }
            list.add(work_num);

            list.add(seats_account);
            list.add(seats_password);
            list.add(extension_number);
            list.add(extension_password);
            list.add(call_type);
            list.add(call_channel);
        } else if (3 == userType) { //项目管理员需要添加所分配的项目
            String hasMarketProjectStr = userDTO.getHasMarketProject();
            if (StringUtil.isNotEmpty(hasMarketProjectStr)) {
                hasMarketProjectStr = "," + hasMarketProjectStr + ",";
            }
            CustomerUserPropertyDO hasMarketProject = new CustomerUserPropertyDO(cu.getId().toString(), "hasMarketProject", hasMarketProjectStr, new Timestamp(System.currentTimeMillis()));
            list.add(hasMarketProject);
        }
        list.add(mobile_num);
        list.add(email);
        list.add(title);
        this.customerUserDao.batchSaveOrUpdate(list);
    }

    @Before(Tx.class)
    public R setUser(LkCrmAdminUserEntity adminUser, String roleIds) {
        boolean bol;
        adminUser.setCustId(BaseUtil.getCustId());
        updateScene(adminUser);
        if (adminUser.getUserId() == 0) {
            String sql = "select count(*) from lkcrm_admin_user where username = ?";
            Integer count = crmAdminUserDao.queryForInt(sql, adminUser.getUsername());
            if (count > 0) {
                return R.error("手机号重复！");
            }
            Long userId = IDHelper.getUserID();
            adminUser.setUserId(userId);
            String salt = IdUtil.fastSimpleUUID();
            adminUser.setCustId(BaseUtil.getCustId());
            adminUser.setNum(RandomUtil.randomNumbers(15));
            adminUser.setSalt(salt);
            //adminUser.setPassword(BaseUtil.sign((adminUser.getUsername().trim() + adminUser.getPassword().trim()), salt));
            adminUser.setPassword(CipherUtil.generatePassword(adminUser.getPassword()));
            adminUser.setCreateTime(new Timestamp(System.currentTimeMillis()));
            adminUser.setMobile(adminUser.getUsername());
//            bol = adminUser.save();
            crmAdminUserDao.saveReturnPk(adminUser);
            UserCallConfigDTO userDTO = new UserCallConfigDTO();
            userDTO.setMobileNumber(adminUser.getMobile());
            userDTO.setEmail(adminUser.getEmail());
            saveBpUser(userId, adminUser.getUsername(), adminUser.getRealname(), adminUser.getPassword(), BaseUtil.getCustId(), 2, "", "", userDTO);
        } else {
            if (adminUser.getParentId() != null && adminUser.getParentId() != 0) {
                List<Record> topUserList = queryTopUserList(adminUser.getUserId());
                boolean isContain = false;
                for (Record record : topUserList) {
                    if (record.getLong("user_id").equals(adminUser.getParentId())) {
                        isContain = true;
                        break;
                    }
                }
                if (!isContain) {
                    return R.error("该员工的下级员工不能设置为直属上级");
                }
            }
            String sql = "select username from lkcrm_admin_user where user_id = ?";
            String username = crmAdminUserDao.queryForObject(sql, adminUser.getUserId());
            if (!username.equals(adminUser.getUsername())) {
                return R.error("用户名不能修改！");
            }
//            bol = adminUser.update();
            LkCrmAdminUserEntity entity = crmAdminUserDao.get(adminUser.getUserId());
            BeanUtils.copyProperties(adminUser, entity, JavaBeanUtil.getNullPropertyNames(adminUser));
            crmAdminUserDao.update(entity);
            // 修改用户信息
            CustomerUser customerUser = customerUserDao.get(adminUser.getUserId());
            if (customerUser != null) {
                customerUser.setRealname(entity.getRealname());
                customerUserDao.update(customerUser);
            }

            String delSql1 = "delete from lkcrm_admin_user_role where user_id = ?";
            crmAdminUserDao.executeUpdateSQL(delSql1, adminUser.getUserId());
            String delSql2 = "delete from lkcrm_admin_scene where user_id = ? and is_system = 1";
            crmAdminUserDao.executeUpdateSQL(delSql2, adminUser.getUserId());
        }
        if (StrUtil.isNotBlank(roleIds)) {
            Long userId = adminUser.getUserId();
            for (Integer roleId : TagUtil.toSet(roleIds)) {
                LkCrmAdminUserRoleEntity adminUserRole = new LkCrmAdminUserRoleEntity();
                adminUserRole.setUserId(userId);
                adminUserRole.setRoleId(roleId);
                crmAdminUserDao.saveOrUpdate(adminUserRole);
            }
        }
        return R.isSuccess(true);
    }

    public R createInitData(LkCrmAdminUserEntity adminUser, Long userId, String custId) {
        boolean bol;
        //updateScene(adminUser);
        // 创建部门
        LkCrmAdminDeptEntity dept = new LkCrmAdminDeptEntity(0, "办公室", null, "", custId);
        int deptId = (int) crmAdminDeptDao.saveReturnPk(dept);
        //创建crm用户
        String sql = "select count(*) from lkcrm_admin_user where username = ?";
        Integer count = crmAdminUserDao.queryForInt(sql, adminUser.getUsername());
        if (count > 0) {
            return R.error("用户名称重复！");
        }
        adminUser.setUserId(userId);
        String salt = IdUtil.fastSimpleUUID();
        adminUser.setCustId(custId);
        adminUser.setNum(RandomUtil.randomNumbers(15));
        adminUser.setSalt(salt);
        //adminUser.setPassword(BaseUtil.sign((adminUser.getUsername().trim() + adminUser.getPassword().trim()), salt));
        adminUser.setPassword(adminUser.getPassword());
        adminUser.setCreateTime(new Timestamp(System.currentTimeMillis()));
        adminUser.setMobile(adminUser.getUsername());
        adminUser.setDeptId(deptId);
        crmAdminUserDao.saveReturnPk(adminUser);
        // 关联管理员角色
        LkCrmAdminUserRoleEntity adminUserRole = new LkCrmAdminUserRoleEntity();
        adminUserRole.setUserId(userId);
        adminUserRole.setRoleId(1);
        crmAdminUserDao.saveOrUpdate(adminUserRole);
        //创建默认公海
        MarketProjectDTO dto = new MarketProjectDTO();
        dto.setIndustryId(-1);
        dto.setName("默认公海项目");
        dto.setType("2");
        marketProjectService.saveMarketProjectAndSeaReturnId(dto, custId, userId);
        // 初始化自定义字段
        List<LkCrmAdminFieldEntity> defaultFieldList = crmAdminFieldDao.queryDefaultCustomerFieldList();
        crmAdminFieldDao.getSession().clear();
        List<LkCrmAdminFieldEntity> customerFieldList = new ArrayList<>();
        LkCrmAdminFieldEntity newEntity;
        for (LkCrmAdminFieldEntity db : defaultFieldList) {
            newEntity = new LkCrmAdminFieldEntity();
            BeanUtils.copyProperties(db, newEntity, "fieldId");
            newEntity.setCustId(custId);
            customerFieldList.add(newEntity);
        }
        crmAdminFieldDao.batchSaveOrUpdate(customerFieldList);
        // 初始化跟进记录类型
        String[] names = new String[]{"打电话", "发短信", "上门拜访"};
        for (String n : names) {
            LkCrmAdminConfigEntity entity = new LkCrmAdminConfigEntity();
            entity.setCustId(custId);
            entity.setIsSystem(1);
            entity.setStatus(1);
            entity.setName("followRecordOption");
            entity.setValue(n);
            entity.setDescription("跟进记录选项");
            crmAdminConfigDao.saveOrUpdate(entity);
        }
        // 初始化场景数据
        List<LkCrmAdminSceneEntity> defaultSceneList = crmAdminSceneDao.queryDefaultSceneList();
        crmAdminFieldDao.getSession().clear();
        List<LkCrmAdminSceneEntity> sceneFieldList = new ArrayList<>();
        LkCrmAdminSceneEntity newScene;
        for (LkCrmAdminSceneEntity db : defaultSceneList) {
            newScene = new LkCrmAdminSceneEntity();
            BeanUtils.copyProperties(db, newScene, "sceneId");
            newScene.setCustId(custId);
            sceneFieldList.add(newScene);
        }
        crmAdminFieldDao.batchSaveOrUpdate(sceneFieldList);
        //默认产品分类
        crmAdminFieldDao.executeUpdateSQL("INSERT INTO `lkcrm_crm_product_category` (`cust_id`, `name`, `pid`) VALUES (?, '默认分类', '0');", custId);
        //默认默认商机租
        crmAdminFieldDao.executeUpdateSQL("INSERT INTO `lkcrm_crm_business_type` (`cust_id`, `name`, `dept_ids`, `create_user_id`, `create_time`, `update_time`, `status`) VALUES (?, '默认商机组', '', ?, ?, NULL, '1');", custId, userId, new Date());
        return R.isSuccess(true);
    }

    private void updateScene(LkCrmAdminUserEntity adminUser) {
        List<Long> ids = new ArrayList<>();
        if (adminUser.getUserId() == 0 && adminUser.getParentId() != null) {
            ids.add(adminUser.getParentId());
        } else if (adminUser.getUserId() != 0) {
//            AdminUser oldAdminUser = AdminUser.dao.findById(adminUser.getUserId());
            LkCrmAdminUserEntity oldAdminUser = crmAdminUserDao.get(adminUser.getUserId());
            if (oldAdminUser.getParentId() == null && adminUser.getParentId() != null) {
                ids.add(adminUser.getParentId());
            } else if (oldAdminUser.getParentId() != null && !oldAdminUser.getParentId().equals(adminUser.getParentId())) {
                ids.add(oldAdminUser.getParentId());
                ids.add(adminUser.getParentId());
            }
        }
        if (ids.size() > 0) {
            Set<Long> idsSet = new HashSet<>();
            ids.forEach(id -> idsSet.addAll(queryTopUserId(id, BaseConstant.AUTH_DATA_RECURSION_NUM)));
            String sql = "delete from lkcrm_admin_scene where user_id in (" + SQLHelper.getInSQL(idsSet) + ")";
            crmAdminUserDao.executeUpdateSQL(sql);
        }
    }

    /**
     * @param userId
     * @author wyq
     * 查询上级id
     */
    private List<Long> queryTopUserId(Long userId, Integer deepness) {
        List<Long> arrUsers = new ArrayList<>();
        if (deepness-- > 0) {
            LkCrmAdminUserEntity adminUser = crmAdminUserDao.get(userId);
            if (adminUser.getParentId() != null && !adminUser.getParentId().equals(0L)) {
                arrUsers.addAll(queryTopUserId(adminUser.getParentId(), deepness));
            }
            arrUsers.add(adminUser.getUserId());
        }
        return arrUsers;
    }

    /**
     * 重置用户信息
     *
     * @return 用户信息
     */
    public LkCrmAdminUserEntity resetUser() {
        String sql = "select a.*,(SELECT name FROM lkcrm_admin_dept WHERE dept_id = a.dept_id) as deptName," +
                "(SELECT realname FROM lkcrm_admin_user WHERE user_id=a.parent_id) as parentName from " +
                "lkcrm_admin_user as a where a.user_id = ?";
        List<LkCrmAdminUserEntity> maps = crmAdminUserDao.queryListBySql(sql, LkCrmAdminUserEntity.class, BaseUtil.getUserId());
        LkCrmAdminUserEntity adminUser = maps != null && maps.size() > 0 ? maps.get(0) : new LkCrmAdminUserEntity();
        adminUser.setRoles(adminRoleService.queryRoleIdsByUserId(adminUser.getUserId()));
        //RedisManager.getRedis().setex(BaseUtil.getToken(), 360000, adminUser);
        //adminUser.remove("password", "salt");
        return adminUser;
    }

    public R queryUserList(BasePageRequest<AdminUser> request, String roleId, String roleName) {
        List<Integer> deptIdList = new ArrayList<>();
        if (request.getData().getDeptId() != null) {
            deptIdList.add(request.getData().getDeptId());
            deptIdList.addAll(queryChileDeptIds(request.getData().getDeptId(), BaseConstant.AUTH_DATA_RECURSION_NUM));
        }
        if (request.getPageType() != null && request.getPageType() == 0) {
            List<Map<String, Object>> recordMaps = crmAdminUserDao.queryUserList(request.getData().getRealname(),
                    deptIdList, request.getData().getStatus(), roleId, roleName);
            List<Record> recordList = JavaBeanUtil.mapToRecords(recordMaps);
            return R.ok().put("data", recordList);
        } else {
            Page page = crmAdminUserDao.queryUserListByPage(request.getPage(), request.getLimit(),
                    request.getData().getRealname(), deptIdList, request.getData().getStatus(), roleId, roleName);
            return R.ok().put("data", BaseUtil.crmPage(page));
        }
    }

    /**
     * 查询可设置为上级的user
     */
    public List<Record> queryTopUserList(Long userId) {
        String sql = "select user_id,realname,parent_id from lkcrm_admin_user";
        List<Map<String, Object>> recordMaps = crmAdminUserDao.queryListBySql(sql);
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordMaps);
        List<Long> subUserList = queryChileUserIds(userId, BaseConstant.AUTH_DATA_RECURSION_NUM);
        recordList.removeIf(record -> subUserList.contains(record.getLong("user_id")));
        recordList.removeIf(record -> record.getLong("user_id").equals(userId));
        return recordList;
    }

    /**
     * 查询本部门下的所有部门id
     *
     * @param deptId 当前部门id
     */
    public List<Integer> queryChileDeptIds(Integer deptId, Integer deepness) {
        String sql = "select dept_id from lkcrm_admin_dept where pid = ?";
        List<Integer> list = crmAdminUserDao.queryListForInteger(sql, deptId);
        if (list != null && list.size() != 0 && deepness > 0) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                list.addAll(queryChileDeptIds(list.get(i), deepness - 1));
            }
        }
        return list;
    }

    /**
     * 查询本用户下的所有下级id
     *
     * @param userId 当前用户id
     */
    public List<Long> queryChileUserIds(Long userId, Integer deepness) {
        List<Long> query = crmAdminUserDao.queryListBySql("select user_id from lkcrm_admin_user where parent_id = ?", userId);
        if (deepness > 0) {
            for (int i = 0, size = query.size(); i < size; i++) {
                query.addAll(queryChileUserIds(query.get(i), deepness - 1));
            }
        }
        HashSet<Long> set = new HashSet<>(query);
        query.clear();
        query.addAll(set);
        return query;
    }


    public R resetPassword(String ids, String pwd) {
        for (String id : ids.split(",")) {
            //LkCrmAdminUserEntity adminUser = crmAdminUserDao.get(NumberUtil.parseLong(id));
            //String password = BaseUtil.sign(adminUser.getUsername() + pwd, adminUser.getSalt());
            String password = CipherUtil.generatePassword(pwd);
            String updateSql = "update lkcrm_admin_user set password = ? where user_id = ?";
            crmAdminUserDao.executeUpdateSQL(updateSql, password, id);
            updateSql = "update t_customer_user set password = ? where id = ?";
            crmAdminUserDao.executeUpdateSQL(updateSql, password, id);
        }
        return R.ok();
    }

    public R querySuperior(String realName) {
        List<Map<String, Object>> superiorMaps = crmAdminUserDao.querySuperior(realName);
        List<Record> recordList = JavaBeanUtil.mapToRecords(superiorMaps);
        return R.ok().put("data", recordList);
    }

    public R queryListName(String name) {
        List<Map<String, Object>> userMaps = crmAdminUserDao.queryUserByRealName(name);
        List<Record> users = JavaBeanUtil.mapToRecords(userMaps);
        Sort sort = new Sort();
        Map<String, List<Record>> map = sort.sort(users);
        return R.ok().put("data", map);
    }

    /**
     * @author Chacker
     * 查询系统下属用户列表
     */
    public List<Long> queryUserIdsByParentId(Long userId) {
        String sql = "select user_id from lkcrm_admin_user where parent_id = ? ";
        List<Record> records = JavaBeanUtil.mapToRecords(crmAdminUserDao.sqlQuery(sql, userId));
        List<Long> userIds = new ArrayList<>();
        for (Record record : records) {
            userIds.add(record.getLong("user_id"));
        }
        return userIds;
    }

    /**
     * @author Chacker
     * 查询部门属用户列表
     */
    public R queryListNameByDept(String name) {
        String sql = "select name,dept_id from lkcrm_admin_dept WHERE cust_id = ? ORDER BY num";
        List<Map<String, Object>> recordMaps = crmAdminUserDao.sqlQuery(sql, BaseUtil.getCustId());
        List<Record> records = JavaBeanUtil.mapToRecords(recordMaps);
        for (Record record : records) {
            List<Map<String, Object>> usersMap = crmAdminUserDao.queryUsersByDeptId(
                    record.getInt("dept_id"), name);
            List<Record> users = JavaBeanUtil.mapToRecords(usersMap);
            record.set("userList", users);
            record.set("userNumber", users.size());
        }
        return R.ok().put("data", records);
    }

    /**
     * @author Chacker
     * 根据部门查询用户id
     */
    public String queryUserIdsByDept(String deptIds) {
        if (StrUtil.isEmpty(deptIds)) {
            return null;
        }
        List<Long> users = crmAdminUserDao.queryUserIdByDeptId(deptIds);
        return StrUtil.join(",", users);
    }

    public R queryAllUserList() {
        List<Map<String, Object>> recordMap = crmAdminUserDao.queryUserList(null, null, null, null, "");
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordMap);
        return R.ok().put("data", recordList);
    }

    public R setUserStatus(String ids, String status) {
        for (Long id : TagUtil.toLongSet(ids)) {
            String sql = "update lkcrm_admin_user set status = ? where user_id = ?";
            crmAdminUserDao.executeUpdateSQL(sql, status, id);
            sql = "update t_customer_user set status = ? where id = ?";
            int userStatus = 0;
            if ("0".equals(status)) {
                //禁用
                userStatus = 1;
            }
            if ("1".equals(status)) {
                //激活
                userStatus = 0;
            }
            crmAdminUserDao.executeUpdateSQL(sql, userStatus, id);
        }
        return R.ok();
    }

    public boolean updateImg(String url, Long userId) {
        LkCrmAdminUserEntity adminUser = crmAdminUserDao.get(userId);
        adminUser.setImg(url);
//        return adminUser.update();
        crmAdminUserDao.update(adminUser);
        return true;
    }

    public boolean updateUser(LkCrmAdminUserEntity adminUser) {
        if (!BaseUtil.getUser().getUsername().equals(adminUser.getUsername())) {
            return false;
        }
        adminUser.setUserId(BaseUtil.getUserId());
        if (StrUtil.isNotEmpty(adminUser.getPassword())) {
            adminUser.setSalt(IdUtil.simpleUUID());
            adminUser.setPassword(BaseUtil.sign((adminUser.getUsername().trim() + adminUser.getPassword().trim()), adminUser.getSalt()));
        }
//        return adminUser.update();
        crmAdminUserDao.update(adminUser);
        return true;
    }

    /**
     * 查询当前用户权限 用于菜单展示 FFF
     *
     * @param userId 用户id
     * @param realm  当前操作的菜单 链接地址
     * @return
     */
    public List<Long> queryUserByAuth(Long userId, String realm) {
        List<Long> adminUsers = new ArrayList<>();
        //查询用户数据权限，从高到低排序
        String sql = "SELECT DISTINCT a.data_type FROM lkcrm_admin_role as a LEFT JOIN lkcrm_admin_user_role as b on a.role_id=b.role_id WHERE b.user_id=?  ORDER BY a.data_type desc";
        List<Integer> list = crmAdminUserDao.queryListBySql(sql, userId);
        if (list.size() == 0) {
            //无权限查询自己的数据
            adminUsers.add(userId);
            return adminUsers;
        }
        List<Object> param = new ArrayList<>();
        //只要上面list的长度不为0 那么这个也不会为0
        sql = "SELECT k.data_type, k.menu_id,am.menu_name,am.parent_realm AS realm FROM " +
                "\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tt.*, arm.menu_id\n" +
                "\t\tFROM\n" +
                "\t\t\t(\n" +
                "\t\t\t\tSELECT DISTINCT\n" +
                "\t\t\t\t\ta.data_type,\n" +
                "\t\t\t\t\ta.role_name,\n" +
                "\t\t\t\t\ta.role_id,\n" +
                "\t\t\t\t\tb.user_id\n" +
                "\t\t\t\tFROM\n" +
                "\t\t\t\t\tlkcrm_admin_role AS a\n" +
                "\t\t\t\tLEFT JOIN lkcrm_admin_user_role AS b ON a.role_id = b.role_id\n" +
                "\t\t\t) t\n" +
                "\t\tLEFT JOIN lkcrm_admin_role_menu arm ON t.role_id = arm.role_id\n" +
                "\t) k\n" +
                "INNER JOIN (\n" +
                "\tSELECT\n" +
                "\t\tx.*, y.realm AS parent_realm\n" +
                "\tFROM\n" +
                "\t\tlkcrm_admin_menu AS x\n" +
                "\tLEFT JOIN lkcrm_admin_menu AS y ON x.parent_id = y.menu_id\n" +
                ") am ON k.menu_id = am.menu_id\n" +
                "WHERE\n" +
                "\tk.user_id = ? ";
        param.add(userId);
        if (realm != null) {
            sql += " and am.parent_realm = ? and am.realm = 'index' ";
            param.add(realm);
        }
        sql += "ORDER BY k.data_type DESC";
        List<Record> userRoleList = JavaBeanUtil.mapToRecords(crmAdminUserDao.sqlQuery(sql, param.toArray()));
        if (list.size() == 1 && userRoleList.size() == 1) {//如果为1的话 验证是否有最高权限，否则及有多个权限
            //拥有最高数据权限
            if (list.contains(5)) {
                return null;
            } else {
                //AdminUser adminUser = AdminUser.dao.findById(userId);
                LkCrmAdminUserEntity adminUser = crmAdminUserDao.get(userId);
                if (list.contains(4)) {
                    List<Record> records = adminDeptService.queryDeptByParentDept(adminUser.getDeptId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
                    List<String> deptIds = new ArrayList<>();
                    deptIds.add(adminUser.getDeptId().toString());
                    records.forEach(record -> deptIds.add(record.getStr("id")));

                    adminUsers.addAll(crmAdminUserDao.queryUserIdByDeptId(deptIds));
                } else if (list.contains(3)) {
                    queryUserByDeptId(adminUser.getDeptId()).forEach(record -> adminUsers.add(record.getLong("id")));
                }

                if (list.contains(2)) {
                    adminUsers.addAll(queryUserByParentUser(adminUser.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM));
                }
                adminUsers.add(adminUser.getUserId());
            }
        } else {//多个权限
            if (realm != null && !"".equals(realm)) {
                LkCrmAdminUserEntity adminUser = crmAdminUserDao.get(userId);
                for (Record r : userRoleList) {//如果有多个权限 验证当前用户是否对当前管理 是否为本人操作
                    if (r.getStr("realm").equals(realm) && r.getStr("data_type").equals("1")) {//当前操作的管理链接地址
                        adminUsers.add(userId);
                        HashSet<Long> hashSet = new HashSet<>(adminUsers);
                        adminUsers.clear();
                        adminUsers.addAll(hashSet);
                        return adminUsers;
                    } else if (r.getStr("realm").equals(realm) && r.getStr("data_type").equals("2")) {//本人及其
                        adminUsers.addAll(queryUserByParentUser(adminUser.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM));
                        adminUsers.add(userId);
                        HashSet<Long> hashSet = new HashSet<>(adminUsers);
                        adminUsers.clear();
                        adminUsers.addAll(hashSet);
                        return adminUsers;
                    } else if (r.getStr("realm").equals(realm) && r.getStr("data_type").equals("3")) {//本部门
                        queryUserByDeptId(adminUser.getDeptId()).forEach(record -> adminUsers.add(record.getLong("id")));
                        adminUsers.add(userId);
                        HashSet<Long> hashSet = new HashSet<>(adminUsers);
                        adminUsers.clear();
                        adminUsers.addAll(hashSet);
                        return adminUsers;
                    } else if (r.getStr("realm").equals(realm) && r.getStr("data_type").equals("4")) {//本部门及下属部门
                        List<Record> records = adminDeptService.queryDeptByParentDept(adminUser.getDeptId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
                        List<String> deptIds = new ArrayList<>();
                        deptIds.add(adminUser.getDeptId().toString());
                        records.forEach(record -> {
                            deptIds.add(record.getStr("id"));
                        });
                        adminUsers.addAll(crmAdminUserDao.queryUserIdByDeptId(deptIds));
                        adminUsers.add(userId);
                        HashSet<Long> hashSet = new HashSet<>(adminUsers);
                        adminUsers.clear();
                        adminUsers.addAll(hashSet);
                        return adminUsers;
                    } else if (r.getStr("realm").equals(realm) && r.getStr("data_type").equals("5")) {//全部
                        return null;
                    }
                }
            } else {
                if (list.contains(5)) {
                    return null;
                } else {
                    AdminUser adminUser = AdminUser.dao.findById(userId);
                    if (list.contains(4)) {
                        List<Record> records = adminDeptService.queryDeptByParentDept(adminUser.getDeptId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
                        List<String> deptIds = new ArrayList<>();
                        deptIds.add(adminUser.getDeptId().toString());
                        records.forEach(record -> deptIds.add(record.getStr("id")));
                        adminUsers.addAll(crmAdminUserDao.queryUserIdByDeptId(deptIds));
                    } else if (list.contains(3)) {
                        queryUserByDeptId(adminUser.getDeptId()).forEach(record -> adminUsers.add(record.getLong("id")));
                    }

                    if (list.contains(2)) {
                        adminUsers.addAll(queryUserByParentUser(adminUser.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM));
                    }
                    adminUsers.add(adminUser.getUserId());
                }
            }
        }

        adminUsers.add(userId);
        HashSet<Long> hashSet = new HashSet<>(adminUsers);
        adminUsers.clear();
        adminUsers.addAll(hashSet);
        return adminUsers;
    }

    public List<Long> queryUserByParentUser(Long userId, Integer deepness) {
        List<Long> recordList = new ArrayList<>();
        if (deepness > 0) {
            List<Long> records = crmAdminDeptDao.queryListBySql("SELECT b.user_id FROM lkcrm_admin_user AS b WHERE b.parent_id = ?", userId);
            recordList.addAll(records);
            int size = recordList.size();
            for (int i = 0; i < size; i++) {
                recordList.addAll(queryUserByParentUser(recordList.get(i), deepness - 1));
            }
        }
        return recordList;
    }

    public List<Record> queryUserByDeptId(Integer deptId) {
        List<Map<String, Object>> objects = crmAdminDeptDao
                .sqlQuery(" SELECT * FROM lkcrm_admin_dept WHERE dept_id = ? ", deptId);
        return JavaBeanUtil.mapToRecords(objects);
    }

    /**
     * @author Chacker
     * 根据部门id和用户ID 去重 （仪盘表中业绩指标用）
     */
    public Record queryByDeptIds(String deptIds, String userIds) {
        Record record = new Record();
        String sql = "select * from lkcrm_admin_dept where dept_id in ( ? )";
        List<Map<String, Object>> recordListMaps = crmAdminUserDao.queryListBySql(sql, deptIds);
        List<Record> allDepts = JavaBeanUtil.mapToRecords(recordListMaps);
        deptIds = getDeptIds(null, allDepts);

        String arrUserIds = queryUserIdsByDept(deptIds);
        if (StrUtil.isNotEmpty(userIds)) {
            userIds = getUserIds(deptIds, userIds);
        }
        record.set("deptIds", deptIds);
        record.set("userIds", userIds);
        record.set("arrUserIds", arrUserIds);
        return record;
    }

    private String getDeptIds(String deptIds, List<Record> allDepts) {
        for (Record dept : allDepts) {
            Integer pid = dept.getInt("pid");
            if (pid != 0) {
                String sql = "select * from lkcrm_admin_dept where dept_id in ( ? )";
                List<Map<String, Object>> recordListMaps = crmAdminUserDao.queryListBySql(sql, pid);
                List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMaps);
                deptIds = getDeptIds(deptIds, recordList);
            } else {
                if (deptIds == null) {
                    deptIds = dept.getStr("dept_id");
                } else {
                    deptIds = deptIds + "," + dept.getStr("dept_id");
                }
            }
        }
        return deptIds;
    }


    /**
     * 修改用户账号功能
     *
     * @param id       用户ID
     * @param username 新的用户名
     * @param password 新的密码
     * @return 操作状态
     */
    @Before(Tx.class)
    public R usernameEdit(Long id, String username, String password) {
        LkCrmAdminUserEntity adminUser = crmAdminUserDao.get(id);
        CustomerUser originalUser = customerUserDao.get(id);
        if (adminUser == null || originalUser == null) {
            return R.error("用户不存在！");
        }
        if (adminUser.getUsername().equals(username) || originalUser.getAccount().equals(username)) {
            return R.error("账号不能和原账号相同");
        }

        String intSql = "select count(*) from lkcrm_admin_user where username = ?";
        Integer count = crmAdminUserDao.queryForInt(intSql, username);
        String intSql2 = "select count(*) from t_customer_user where account = ?";
        Integer count2 = crmAdminUserDao.queryForInt(intSql2, username);
        if (count > 0 || count2 > 0) {
            return R.error("手机号重复！");
        }

        adminUser.setUsername(username);
        adminUser.setPassword(CipherUtil.generatePassword(password));
//        adminUser.setPassword(BaseUtil.sign(username + password, adminUser.getSalt()));
//        return R.isSuccess(adminUser.update());
        crmAdminUserDao.update(adminUser);

        originalUser.setAccount(username);
        originalUser.setPassword(CipherUtil.generatePassword(password));
        customerUserDao.update(originalUser);
        return R.isSuccess(true);
    }

    private String getUserIds(String deptIds, String userIds) {
        String sql = "select * from lkcrm_admin_user where dept_id   NOT in ( ? ) and user_id in (?)";
        List<Map<String, Object>> mapList = crmAdminUserDao.queryListBySql(sql, deptIds, userIds);
        List<Record> allUsers = JavaBeanUtil.mapToRecords(mapList);
        userIds = null;
        for (Record user : allUsers) {
            if (userIds == null) {
                userIds = user.getStr("user_id");
            } else {
                userIds = deptIds + "," + user.getStr("user_id");
            }
        }
        return userIds;
    }
}
