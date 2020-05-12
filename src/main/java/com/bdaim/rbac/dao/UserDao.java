package com.bdaim.rbac.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.rbac.dto.UserDTO;
import com.bdaim.rbac.entity.User;
import com.bdaim.rbac.entity.UserDO;
import com.bdaim.rbac.entity.UserProperty;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class UserDao extends SimpleHibernateDao<User, Serializable> {
    /**
     * 添加用戶信息
     */
    public void insertUser(UserDTO t) throws SQLException {
        this.executeUpdateSQL("insert into t_user(ID,NAME,PASSWORD,REALNAME," +
                        "DEPTID,OPTUSER,CREATE_TIME,SOURCE,STATUS,user_type,mobile_num) " +
                        "values(?,?,?,?,?,?,now(),?,?,?,?)", t.getId(),
                t.getUserName(), t.getPassword(), t.getRealName(), t.getDeptId(), t.getOptuser(),
                t.getSource(), t.getStatus(), t.getUserType(), t.getMobileNumber());
    }

    /**
     * 修改用户信息
     */
    public void updateUserMessage(UserDTO t) {
        Long id = t.getId();
        if (id == null) {
            throw new NullPointerException("更新记录的ID不可为空");
        }
        List<Object> params = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        builder.append("update t_user set modify_time = now()");

        String optuser = t.getOptuser();
        if (!StringUtils.isEmpty(optuser)) {
            builder.append(" ,optuser =  ? ");
            params.add(optuser);
        }
        Long deptId = t.getDeptId();
        if (deptId != null) {
            builder.append(" ,DEPTID =  ? ");
            params.add(deptId);
        }
        String realname = t.getRealName();
        if (!StringUtils.isEmpty(realname)) {
            builder.append(" ,realname =  ? ");
            params.add(realname);
        }
        String mobileNumber = t.getMobileNumber();
        if (!StringUtils.isEmpty(mobileNumber)) {
            builder.append(" ,mobile_num = ? ");
            params.add(mobileNumber);
        }
        if (StringUtils.isNotBlank(t.getPassword())) {
            builder.append(",password = ?");
            params.add(t.getPassword());
        }
        if (t.getStatus() == 0 || t.getStatus() == 1) {
            builder.append(",status = ?");
            params.add(t.getStatus());
        }
        builder.append(" where id = ?");
        params.add(id);

        this.executeUpdateSQL(builder.toString(), params.toArray());
    }

    /**
     * 根据userid删除用户角色信息(admin)
     */
    public void deleteByUserId(Long userId) throws SQLException {
        String sql = "delete from t_user_role_rel where id = ?";
        this.executeUpdateSQL(sql, userId);
    }

    /**
     * 根据userid删除用户角色信息(其他账户)
     */
    public void deleteRoleByUserId(Long operateUserId, Long userId) throws SQLException {
        StringBuilder builder = new StringBuilder();
        List<Object> params = new ArrayList<>();
        builder.append(" delete from t_user_role_rel where id = ? and role in (select temp.id from");
        builder.append(" (select r.id from t_role r inner join t_user_role_rel ur on ur.ROLE = r.ID and ur.id = ?)temp)");
        params.add(userId);
        params.add(operateUserId);
        this.executeUpdateSQL(builder.toString());
    }


    public void updateUserStatus(Long userId, Integer status) {
        String sql = "update t_user set status=? where id=?";
        this.executeUpdateSQL(sql, status, userId);

    }

    //根据userId查询用户信息
    public UserDO getUserMessage(Long userId) {
        UserDO cp = null;
        String hql = "from UserDO m where m.id=?";
        List<UserDO> list = this.find(hql, userId);
        if (list.size() > 0) {
            cp = (UserDO) list.get(0);
        }
        return cp;
    }


    //根据userId查询用户全部信息（部门职业）
    public List<Map<String, Object>> getUserAllMessage(Long userId) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT u.ID id, u.REALNAME realName, u.`name` account,u.`PASSWORD` password,u.mobile_num phone," +
                "d.`NAME` deptName,GROUP_CONCAT(r.`NAME`) roleName ");
        sql.append("FROM t_user u LEFT JOIN t_dept d ON u.DEPTID = d.ID LEFT JOIN t_user_role_rel re ON u.ID = re.ID ");
        sql.append("LEFT JOIN t_role r ON re.ROLE = r.ID WHERE u.ID = ?  GROUP BY u.ID");
        List<Map<String, Object>> list = this.sqlQuery(sql.toString(), userId);
        return list;
    }

    //根据userId查询用户信息
    public String getUserRealName(Long userId) {
        UserDO cp = null;
        String hql = "from UserDO m where m.id=?";
        List<UserDO> list = this.find(hql, userId);
        if (list.size() > 0)
            cp = (UserDO) list.get(0);
        if (cp != null) {
            return cp.getRealname();
        }
        return null;
    }

    public void insert(UserDTO t) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "insert into t_user(ID,NAME,PASSWORD,REALNAME,DEPTID,OPTUSER,CREATE_TIME,SOURCE," +
                "STATUS,authorize) values(?,?,?,?,?,?,now(),?,?,?)";
        params.add(t.getId());
        params.add(t.getUserName());
        params.add(t.getPassword());
        params.add(t.getRealName());
        params.add(t.getDeptId());
        params.add(t.getOptuser());
        params.add(t.getSource());
        params.add(t.getStatus());
        params.add(t.getAuthorize());
        this.executeUpdateSQL(sql, params.toArray());

    }

    public String getName(String userId) {
        try {
            User cu = this.get(Long.parseLong(userId));
            if (cu != null)
                return cu.getRealname();
        } catch (Exception e) {

        }
        return "";
    }

    public void delete(UserDTO t) {
        String sql = "update t_user set status=9 where id=?";
        this.executeUpdateSQL(sql, t.getId());

    }

    public void update(UserDTO t) {
        Long id = t.getId();
        if (id == null) {
            throw new NullPointerException("更新记录的ID不可为空");
        }
        StringBuilder builder = new StringBuilder();
        List<Object> params = new ArrayList<>();
        builder.append("update t_user set modify_time = now()");

        String optuser = t.getOptuser();
        if (!StringUtils.isEmpty(optuser)) {
            builder.append(" ,optuser = ? ");
            params.add(optuser);
        }
        if (StringUtils.isNotBlank(t.getPassword())) {
            builder.append(",password = ? ");
            params.add(t.getPassword());
        }
        if (t.getStatus() != null && (t.getStatus() == 0 || t.getStatus() == 1)) {
            builder.append(",status = ?");
            params.add(t.getStatus());
        }
        builder.append(" where id = ?");
        params.add(id);

        this.executeUpdateSQL(builder.toString());
    }

    public User getObj(User t) {

        return null;
    }

    public UserProperty getProperty(long userId, String propertyName) {
        UserProperty cp = null;
        String hql = "from UserProperty m where m.userId=? and m.propertyName=?";
        List<UserProperty> list = this.find(hql, userId, propertyName);
        if (list.size() > 0)
            cp = (UserProperty) list.get(0);
        return cp;
    }


    public UserProperty checkProperty(String propertyName, String propertyValue) {
        UserProperty cp = null;
        String hql = "from UserProperty m where m.propertyName=? and m.propertyValue=?";
        List<UserProperty> list = this.find(hql, propertyName, propertyValue);
        if (list.size() > 0)
            cp = (UserProperty) list.get(0);
        return cp;
    }

    public User getUserMessage(long userId) {
        User cp = null;
        String hql = "from User m where m.userId=? and status =0 ";
        List<User> list = this.find(hql, userId);
        if (list.size() > 0)
            cp = (User) list.get(0);
        return cp;
    }



    /**
     * 代理商属性编辑与新增
     */
    public void dealUserInfo(Long userId, String propertyName,String propertyValue) {
        UserProperty propertyInfo = this.getProperty(userId,propertyName);
        if (propertyInfo == null) {
            propertyInfo = new UserProperty();
            propertyInfo.setCreateTime(new Timestamp(new Date().getTime()));
            propertyInfo.setUserId(userId);
            propertyInfo.setPropertyValue(propertyValue);
            logger.info(userId + " 属性不存在，新建该属性" + "\tpropertyName:" + propertyName + "\tpropertyValue:" + propertyValue);
            propertyInfo.setPropertyName(propertyName);
        } else {
            propertyInfo.setPropertyValue(propertyValue);
        }
        this.saveOrUpdate(propertyInfo);
    }




}
