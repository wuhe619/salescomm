package com.bdaim.rbac.service;

import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.CipherUtil;
import com.bdaim.common.util.IDHelper;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.rbac.DataFromEnum;
import com.bdaim.rbac.dao.RoleDao;
import com.bdaim.rbac.dao.UserDao;
import com.bdaim.rbac.dto.Page;
import com.bdaim.rbac.dto.UserDTO;
import com.bdaim.rbac.entity.User;
import com.bdaim.rbac.entity.UserDO;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import static com.bdaim.rbac.controller.RoleAction.log;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service("userService")
@Transactional
public class UserService {
    private static Logger logger = Logger.getLogger(UserService.class);
    @Resource
    private UserDao userDao;
    @Resource
    private RoleDao roleDao;

    @SuppressWarnings("unchecked")
    public List<User> getAllUsers() {
        return userDao.createQuery("From User").list();
    }

    @SuppressWarnings("unchecked")
    public List<User> getUsersByCondition(Map<String, Object> map,
                                          Map<String, Object> likeMap) {
        String hql = "From User t";
        return userDao.getHqlQuery(hql, map, likeMap, null).list();
    }

    public User getUserById(Long uid) {
        return userDao.get(uid);
    }


    public Boolean saveUserMessage(String loginUserName, Long loginId, boolean isAdminOperate, UserDTO userDTO) {
        Boolean flag = true;
        try {
            //构造用户信息
            userDTO.setStatus(0);
            userDTO.setSource(DataFromEnum.SYSTEM.getValue());
            //加密密码
            if (StringUtil.isNotEmpty(userDTO.getPassword())){
                String passwordMd5 = CipherUtil.generatePassword(userDTO.getPassword());
                userDTO.setPassword(passwordMd5);
            }
            userDTO.setOptuser(loginUserName);
            userDTO.setCreateTime(new Date());
            userDTO.setUserType(2);
            //根据id查询user对象
            //判断id是否为空，空做新增，非空修改
            if (userDTO.getId() == null) {
                Long userId = IDHelper.getID();
                userDTO.setId(userId);
                //添加用户信息
                userDao.insertUser(userDTO);
            } else {
                //修改用户基本信息
                userDao.updateUserMessage(userDTO);
                if (isAdminOperate) {
                    userDao.deleteByUserId(userDTO.getId());
                } else {
                    userDao.deleteRoleByUserId(loginId, userDTO.getId());
                }
            }
            //添加用户职位信息
            insertUserRole(userDTO.getId(), userDTO.getRoles(), loginUserName);
        } catch (Exception e) {
            logger.error("员工信息编辑异常" + e);
            flag = false;
        }
        return flag;
    }

    /**
     * 添加用户职位信息
     *
     * @param
     */
    public void insertUserRole(Long id, String roleId, String loginUserName) {
        if (StringUtil.isNotEmpty(roleId)) {
            String[] roleIds = roleId.split(",");
            if (roleIds.length > 0) {
                for (int i = 0; i < roleIds.length; i++) {
                    int insertNum = userDao.executeUpdateSQL("insert into t_user_role_rel(ID,ROLE,OPTUSER,CREATE_TIME) VALUES(" + id + "," + roleIds[i] + ",'" + loginUserName + "',now())");
                    logger.info("添加职位信息数量是：" + insertNum + "用户id是：" + id);
                }
            }
        }
    }

    /**
     * 检查用户名是否唯一
     *
     * @return
     */

    public boolean checkUsernameUnique(String userName, Long id) {
        String sql = "";
        if (id == null) {
            sql = "select count(*) as COUNT from t_user where name = '" + userName + "'";
        } else {
            sql = "select count(*) as COUNT from t_user where name = '" + userName + "' and id<>" + id;
        }
        List<Map<String, Object>> list = userDao.sqlQuery(sql);
        if (list != null && !list.isEmpty()) {
            Map<String, Object> map = list.get(0);
            int count = NumberConvertUtil.everythingToInt(map.get("COUNT"));
            if (count > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 删除用户信息（逻辑删除）
     *
     * @return
     */
    public boolean deleteUser(Long userId, int status) {
        //查看当前删除的用户是否是admin
        UserDO userDo = userDao.getUserMessage(userId);
        //如果待删除的用户是管理员，则不可删除
        if ("admin".equals(userDo.getName())) return false;
        try {
            //删除用户信息
            userDao.updateUserStatus(userId, status);
            //删除用户权限信息
            roleDao.deleteByUserId(userId);
        } catch (Exception e) {
            log.error("删除用户信息异常" + e);
            return false;
        } finally {
        }
        return true;
    }

    /**
     * 更改用户状态
     *
     * @param userId
     * @param status
     */
    public boolean updateUserStatus(Long userId, Integer status) {
        Boolean flag = false;
        try {
            //查看当前删除的用户是否是admin
            UserDO userDo = userDao.getUserMessage(userId);
            //如果待修改状态的用户是管理员，则不可修改状态
            if ("admin".equals(userDo.getName())) return false;
            userDao.updateUserStatus(userId, status);
            flag = true;
        } catch (Exception e) {
            logger.error("修改用户状态异常" + e);
        }
        return flag;
    }

    public Page queryUserList(PageParam page, UserDTO userDTO, LoginUser loginUser) {
        Long loginId = loginUser.getId();
        boolean ifAdmin = loginUser.isAdmin();
        StringBuffer sql = new StringBuffer("SELECT cast(u.ID as char) id,u.PASSWORD password,u.REALNAME realName,u.name account,u.mobile_num phone,d.`NAME` deptName,GROUP_CONCAT(r.`NAME`) roles ,cast(r.ID as char) roleId,cast(d.ID as char)deptId,u.`STATUS` ");
        sql.append("FROM t_user u LEFT JOIN t_user_role_rel p ON u.ID = p.ID\n");
        sql.append("LEFT JOIN t_role r ON p.ROLE = r.ID ");
        sql.append("LEFT JOIN t_dept d ON u.DEPTID = d.ID ");
        sql.append("WHERE 1=1 ");
        //admin可以查询所有部门信息  普通用户只能查本部门的
        if (ifAdmin == false) {
            sql.append(" and d.id in (SELECT u.DEPTID FROM t_user u WHERE u.ID = " + loginId + ")");
        }
        if (userDTO.getId() != null) {
            sql.append(" and u.id = " + userDTO.getId());
        }
        if (StringUtil.isNotEmpty(userDTO.getRealName())) {
            sql.append(" and u.REALNAME like '%" + userDTO.getRealName() + "%'");
        }
        if (StringUtil.isNotEmpty(userDTO.getUserName())) {
            sql.append(" and u.name = '" + userDTO.getUserName() + "'");
        }
        if (StringUtil.isNotEmpty(userDTO.getMobileNumber())) {
            sql.append(" and u.mobile_num = " + userDTO.getMobileNumber());
        }
        if (userDTO.getDeptId() != null) {
            sql.append(" and u.DEPTID = " + userDTO.getDeptId());
        }
        if (userDTO.getStatus() != null) {
            sql.append(" and u.status = " + userDTO.getStatus());
        }
        sql.append(" and u.status!=2 ");
        sql.append(" GROUP BY u.ID ORDER BY d.`NAME` ");
        Page dataPage = userDao.sqlPageQuery(sql.toString(), page.getPageNum(), page.getPageSize());
        return dataPage;
    }
}
