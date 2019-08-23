package com.bdaim.rbac.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.rbac.dto.UserDTO;
import com.bdaim.rbac.entity.User;
import com.bdaim.rbac.entity.UserDO;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Component
public class UserDao extends SimpleHibernateDao<User, Serializable> {
    /**
     * 添加用戶信息
     */
    public void insertUser(UserDTO t) throws SQLException {
        this.executeUpdateSQL("insert into t_user(ID,NAME,PASSWORD,REALNAME,DEPTID,OPTUSER,CREATE_TIME,SOURCE,STATUS,user_type,mobile_num) values(" + t.getId() + ",'" + t.getUserName() + "','" + t.getPassword() + "','" + t.getRealName() + "','" + t.getDeptId() + "','" + t.getOptuser() + "',now(),'" + t.getSource() + "','" + t.getStatus() + "','" + t.getUserType() + "','" + t.getMobileNumber() + "')");
    }

    /**
     * 修改用户信息
     */
    public void updateUserMessage(UserDTO t) {
        Long id = t.getId();
        if (id == null) {
            throw new NullPointerException("更新记录的ID不可为空");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("update t_user set modify_time = now()");

        String optuser = t.getOptuser();
        if (!StringUtils.isEmpty(optuser)) {
            builder.append(" ,optuser =  '" + optuser + "' ");
        }
        Long deptId = t.getDeptId();
        if (deptId != null) {
            builder.append(" ,DEPTID =  '" + deptId + "' ");
        }
        String realname = t.getRealName();
        if (!StringUtils.isEmpty(realname)) {
            builder.append(" ,realname =  '" + realname + "' ");
        }
        String mobileNumber = t.getMobileNumber();
        if (!StringUtils.isEmpty(mobileNumber)) {
            builder.append(" ,mobile_num =  '" + mobileNumber + "' ");
        }
        if (StringUtils.isNotBlank(t.getPassword())) {
            builder.append(",password = '" + t.getPassword() + "' ");
        }
        if (t.getStatus() == 0 || t.getStatus() == 1) {
            builder.append(",status = " + t.getStatus());
        }
        builder.append(" where id = " + id);

        this.executeUpdateSQL(builder.toString());
    }

    /**
     * 根据userid删除用户角色信息(admin)
     */
    public void deleteByUserId(Long userId) throws SQLException {
        String sql = "delete from t_user_role_rel where id = " + userId;
        this.executeUpdateSQL(sql);
    }

    /**
     * 根据userid删除用户角色信息(其他账户)
     */
    public void deleteRoleByUserId(Long operateUserId, Long userId) throws SQLException {
        StringBuilder builder = new StringBuilder();
        builder.append(" delete from t_user_role_rel where id = " + userId + " and role in (select temp.id from");
        builder.append(" (select r.id from t_role r inner join t_user_role_rel ur on ur.ROLE = r.ID and ur.id = " + operateUserId + ")temp)");
        this.executeUpdateSQL(builder.toString());
    }


    public void updateUserStatus(Long userId, Integer status) {
        String sql = "update t_user set status=" + status + " where id=" + userId;
        this.executeUpdateSQL(sql);

    }

    //根据userId查询用户信息
    public UserDO getUserMessage(Long userId) {
        UserDO cp = null;
        String hql = "from UserDO m where m.id=?";
        List<UserDO> list = this.find(hql, userId);
        if (list.size() > 0)
            cp = (UserDO) list.get(0);
        return cp;
    }


    //根据userId查询用户全部信息（部门职业）
    public List<Map<String, Object>> getUserAllMessage(Long userId) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT u.ID id, u.REALNAME realName, u.`name` account,u.`PASSWORD` password,u.mobile_num phone,d.`NAME` deptName,GROUP_CONCAT(r.`NAME`) roleName ");
        sql.append("FROM t_user u LEFT JOIN t_dept d ON u.DEPTID = d.ID LEFT JOIN t_user_role_rel re ON u.ID = re.ID ");
        sql.append("LEFT JOIN t_role r ON re.ROLE = r.ID WHERE u.ID = '" + userId + "' GROUP BY u.ID");
        List<Map<String, Object>> list = this.sqlQuery(sql.toString());
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
}
