package com.bdaim.rbac.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.rbac.dto.RoleDTO;
import com.bdaim.rbac.dto.RolesResourceDto;
import com.bdaim.rbac.entity.RoleEntity;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2019/3/14
 * @description
 */
@Component
public class RoleDao extends SimpleHibernateDao<RoleEntity, Serializable> {
    /**
     * 更新职位信息
     */
    public void update(RoleDTO role) {
        StringBuffer sb = new StringBuffer();
        sb.append("update t_role set MODIFY_TIME=now()");
        String roleName = role.getName();
        if (!StringUtils.isEmpty(roleName)) {
            sb.append(",NAME='" + roleName + "'");
        }
        String optUser = role.getUser();
        if (!StringUtils.isEmpty(optUser)) {
            sb.append(" ,OPTUSER='" + optUser + "'");
        }
        Long deptId = role.getDeptId();
        if (deptId != null) {
            sb.append(", DEPTID='" + deptId + "'");
        }
        sb.append(" where ID= " + role.getKey());

        this.executeUpdateSQL(sb.toString());
    }

    /**
     * 为职位配置资源（设置资源树）
     *
     * @author:duanliying
     * @method
     * @date: 2019/3/15 11:28
     */
    public void insertResource(RolesResourceDto rolesResource) throws SQLException {
        if (rolesResource.getResources() != null && rolesResource.getResources().size() > 0 && rolesResource.getRole() != null) {
            String sql = "insert into t_mrp_rel(ROLE_ID,R_ID,OPTUSER,CREATE_TIME) VALUES(?,?,?,?)";
            for (int i = 0; i < rolesResource.getResources().size(); i++) {
                this.executeUpdateSQL(sql, rolesResource.getRole().getKey(), rolesResource.getResources().get(i), rolesResource.getUser(), rolesResource.getCreateDate());
            }
        }
    }

    /**
     * 非admin账户登录删除标签树
     *
     * @throws SQLException
     */

    public void deleteSourceTree(Long operateUserId, Long roleId) throws SQLException {
        StringBuilder builder = new StringBuilder();
        builder.append(" delete from t_mrp_rel where role_id = " + roleId + " and r_id in");
        builder.append(" (select temp.r_id from (select m.r_id from t_mrp_rel m ,t_user_role_rel ur where m.ROLE_ID = ur.ROLE and ur.id = " + operateUserId + ")temp)");
        String sql = builder.toString();

        this.executeUpdateSQL(builder.toString());
    }

    /**
     * 删除admin登录所拥有的标签资源
     *
     * @throws SQLException
     */
    public void deleteSourceTree(Long roleId) throws SQLException {
        String sql = "delete from t_mrp_rel where role_id = " + roleId;

        this.executeUpdateSQL(sql);
    }

    /**
     * 添加职位sql
     */
    public void insertRole(RoleDTO role) {
        this.executeUpdateSQL("insert into t_role(ID,NAME,OPTUSER,CREATE_TIME,DEPTID) values('" + role.getKey() + "','" + role.getName() + "','" + role.getUser() + "',now(),'" + role.getDeptId() + "')");
    }


    /**
     * 根据userid删除用户角色信息(admin)
     */
    public void deleteByUserId(Long userId) throws SQLException {
        String sql = "delete from t_user_role_rel where id = " + userId;
        this.executeUpdateSQL(sql);
    }

    /**
     * 根据用户id查询角色id
     *
     * @author:duanliyin
     * @date: 2019/3/19 18:56
     */
    public List<Map<String, Object>> getRoleByUserId(Long userId) throws SQLException {
        String sql = "select ID userId,ROLE roleId  from t_user_role_rel where id = " + userId;
        List<Map<String, Object>> list = this.sqlQuery(sql);
        return list;
    }

    /**
     * 根据职位id查询用户数
     *
     * @author:duanliyin
     * @date: 2019/3/19 18:56
     */
    public List<Map<String, Object>> getUserNumByRoleId(String roleId) throws SQLException {
        String sql = "select ID userId,ROLE roleId  from t_user_role_rel where ROLE = " + roleId;
        List<Map<String, Object>> list = this.sqlQuery(sql);
        return list;
    }
}
