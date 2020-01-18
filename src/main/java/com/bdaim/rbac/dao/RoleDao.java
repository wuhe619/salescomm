package com.bdaim.rbac.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.rbac.dto.ManagerType;
import com.bdaim.rbac.dto.RoleDTO;
import com.bdaim.rbac.dto.RolesResourceDto;
import com.bdaim.rbac.dto.UserDTO;
import com.bdaim.rbac.entity.RoleEntity;
import com.bdaim.util.DateUtil;
import com.bdaim.util.IDHelper;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
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
        List<Object> params = new ArrayList<>();
        String roleName = role.getName();
        if (!StringUtils.isEmpty(roleName)) {
            sb.append(",NAME=?");
            params.add(roleName);
        }
        String optUser = role.getUser();
        if (!StringUtils.isEmpty(optUser)) {
            sb.append(" ,OPTUSER=?");
            params.add(optUser);
        }
        Long deptId = role.getDeptId();
        if (deptId != null) {
            sb.append(", DEPTID=?");
            params.add(deptId);
        }
        sb.append(" where ID= ?");
        params.add(role.getKey());
        this.executeUpdateSQL(sb.toString(), params.toArray());
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
        List<Object> params = new ArrayList<>();
        builder.append(" delete from t_mrp_rel where role_id = " + roleId + " and r_id in");
        builder.append(" (select temp.r_id from (select m.r_id from t_mrp_rel m ,t_user_role_rel ur where m.ROLE_ID = ur.ROLE and ur.id = " + operateUserId + ")temp)");
        params.add(roleId);
        params.add(operateUserId);
        String sql = builder.toString();

        this.executeUpdateSQL(builder.toString(), params.toArray());
    }

    /**
     * 删除admin登录所拥有的标签资源
     *
     * @throws SQLException
     */
    public void deleteSourceTree(Long roleId) throws SQLException {
        String sql = "delete from t_mrp_rel where role_id = ?";
        List<Object> params = new ArrayList<>();
        params.add(roleId);
        this.executeUpdateSQL(sql, params.toArray());
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
        String sql = "delete from t_user_role_rel where id = ?";
        List<Object> params = new ArrayList<>();
        params.add(userId);
        this.executeUpdateSQL(sql, params.toArray());
    }

    /**
     * 根据用户id查询角色id
     *
     * @author:duanliyin
     * @date: 2019/3/19 18:56
     */
    public List<Map<String, Object>> getRoleByUserId(Long userId) throws SQLException {
        String sql = "select ID userId,ROLE roleId  from t_user_role_rel where id = ?";
        List<Object> params = new ArrayList<>();
        params.add(userId);
        List<Map<String, Object>> list = this.sqlQuery(sql, params.toArray());
        return list;
    }

    /**
     * 根据职位id查询用户数
     *
     * @date: 2019/3/19 18:56
     */
    public List<Map<String, Object>> getUserNumByRoleId(String roleId) throws SQLException {
        String sql = "select ID userId,ROLE roleId  from t_user_role_rel where ROLE = ?";
        List<Object> params = new ArrayList<>();
        params.add(roleId);
        List<Map<String, Object>> list = this.sqlQuery(sql, params.toArray());
        return list;
    }

    /**
     * 根据用户id查询角色id 和 角色名称
     *
     * @date: 2019/3/19 18:56
     */
    public List<Map<String, Object>> getRoleInfoByUserId(String userId) {
        String sql = "SELECT R.type,r.ID id,r.`NAME` name from t_user_role_rel  ur LEFT JOIN t_role r ON ur.ROLE = r.ID WHERE ur.ID = ?";
        List<Object> params = new ArrayList<>();
        params.add(userId);
        List<Map<String, Object>> list = this.sqlQuery(sql, params.toArray());
        return list;
    }

    /**
     * 根据type查询角色信息
     *
     * @date: 2019/3/19 18:56
     */
    public RoleEntity getRoleInfoBytype(int type) {
        RoleEntity cp = null;
        String hql = "from RoleEntity m where m.type=?";
        List<RoleEntity> list = this.find(hql, type);
        if (list.size() > 0)
            cp = (RoleEntity) list.get(0);
        return cp;
    }

    public void insert(RoleDTO role) {
        this.executeUpdateSQL("insert into t_role(ID,NAME,OPTUSER,CREATE_TIME,DEPTID) values('" + role.getKey() + "','" + role.getName() + "','" + role.getUser() + "',now(),'" + role.getDeptId() + "')");
    }

    public void insert0(RoleDTO role) {
        this.executeUpdateSQL("insert into t_role(ID,NAME,OPTUSER,CREATE_TIME,DEPTID) values('" + IDHelper.getID() + "','" + role.getName() + "','" + role.getUser() + "',now(),'" + role.getDeptId() + "')");
    }

    public void delete(RoleDTO role) {
        this.executeUpdateSQL("delete from t_role where ID=" + role.getKey());
        this.executeUpdateSQL("delete from t_mrp_rel where ROLE_ID=" + role.getKey());
    }

    public RoleDTO getObj(RoleDTO role) {
        try {
            List<Object> params = new ArrayList<>();
            params.add(role.getKey());
            List list = this.sqlQuery("select ID,NAME,OPTUSER,CREATE_TIME,MODIFY_TIME,TYPE from t_role where id=?", params.toArray());

            RoleDTO resultRole = new RoleDTO();
            for (int i = 0; i < list.size(); i++) {
                Object[] obj = (Object[]) list.get(i);
                resultRole.setKey(Long.parseLong(String.valueOf(obj[0])));
                resultRole.setName(String.valueOf(obj[1]));
                resultRole.setModifyDate(DateUtil.fmtStrToDate(String.valueOf(obj[2])));
                resultRole.setCreateDate(DateUtil.fmtStrToDate(String.valueOf(obj[3])));
                resultRole.setUser(String.valueOf(obj[4]));
                resultRole.setType(ManagerType.getManagerType(Integer.parseInt(String.valueOf(obj[5]))));
            }
            return resultRole;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * 这个方法还没有想好怎么实现
     *
     * @param role
     * @return
     */
    public List<RoleDTO> query(RoleDTO role) {
        List<Object> params = new ArrayList<>();
        params.add(role.getKey());
        List list = this.sqlQuery("select ID,NAME,OPTUSER,CREATE_TIME,MODIFY_TIME,TYPE from t_user_role_rel where role=?", params.toArray());

        List<RoleDTO> roles = new ArrayList<RoleDTO>();
        try {
            for (int i = 0; i < list.size(); i++) {
                Object[] obj = (Object[]) list.get(i);
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setKey(Long.parseLong(String.valueOf(obj[0])));
                roleDTO.setName(String.valueOf(obj[1]));
                roleDTO.setUser(String.valueOf(obj[2]));
                roleDTO.setCreateDate(new Date(Long.parseLong(String.valueOf(obj[3]))));
                roleDTO.setModifyDate(new Date(Long.parseLong(String.valueOf(obj[4]))));
                roles.add(roleDTO);
            }
            return roles;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<RoleDTO> queryUserRoles(UserDTO user) {
        try {
            List list = this.getSQLQuery("SELECT r.ID,r.NAME,r.OPTUSER,r.CREATE_TIME,r.MODIFY_TIME FROM t_role r,t_user_role_rel rel where r.ID=rel.ROLE " +
                    "and rel.ID= " + user.getKey()).list();

            List<RoleDTO> roles = new ArrayList<RoleDTO>();
            for (int i = 0; i < list.size(); i++) {
                Object[] obj = (Object[]) list.get(i);
                RoleDTO role = new RoleDTO();
                role.setKey(Long.parseLong(String.valueOf(obj[0])));
                role.setName(String.valueOf(obj[1]));
                role.setUser(String.valueOf(obj[2]));
                role.setCreateDate(new Date(Long.parseLong(String.valueOf(obj[3]))));
                role.setModifyDate(new Date(Long.parseLong(String.valueOf(obj[4]))));
                roles.add(role);
            }
            return roles;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return null;
    }
}
