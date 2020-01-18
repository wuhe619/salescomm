package com.bdaim.rbac.dao;


import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.rbac.dto.*;
import com.bdaim.rbac.entity.UserDO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */

@Component
public class RoleResourceDao extends SimpleHibernateDao<UserDO, Serializable> {

    @javax.annotation.Resource
    private JdbcTemplate jdbcTemplate;

    public void insert(RolesResource rolesResource) throws SQLException {
        if (rolesResource.getResources() != null && rolesResource.getResources().size() > 0 && rolesResource.getRole() != null) {
            String sql = "insert into t_mrp_rel(ROLE_ID,R_ID,OPTUSER,CREATE_TIME,TYPE) VALUES(?,?,?,?,?)";
            for (int i = 0; i < rolesResource.getResources().size(); i++) {
                this.executeUpdateSQL(sql, rolesResource.getRole().getKey(), rolesResource.getResources().get(i).getID(), rolesResource.getUser(), rolesResource.getCreateDate(), 0);
            }
        }
    }

    public void insert0(RolesResource rolesResource) throws SQLException {
        if (rolesResource.getResources() != null && rolesResource.getResources().size() > 0 && rolesResource.getRole() != null && rolesResource.getRole().getKey() != null) {
            String sql = "insert into t_mrp_rel(ROLE_ID,R_ID,OPTUSER,CREATE_TIME,TYPE) VALUES(?,?,?,?,?)";

            this.executeUpdateSQL(sql);
        }
    }

    /**
     * 如果资源列表为空，则表示删除当前角色的所有资源授权，否则只删除指定角色下的指定资源的权限
     */
    public void delete(RolesResource rolesResource) throws SQLException {
        if (rolesResource.getResources() != null && rolesResource.getResources().size() > 0) {
            for (Resource resource : rolesResource.getResources()) {
                List<Object> params = new ArrayList<>();
                params.add(rolesResource.getRole().getKey());
                params.add(resource.getID());
                this.executeUpdateSQL("delete from t_mrp_rel where ROLE_ID=? and R_ID=?", params.toArray());
            }
        } else {

            this.executeUpdateSQL("delete from t_mrp_rel where ROLE_ID =?", rolesResource.getRole().getKey());
        }
    }

    /**
     * 对角色授予资源
     *
     * @param
     * @param rolesResource
     */
    public void update(RolesResource rolesResource) throws SQLException {

    }

    public RolesResource getObj(RolesResource rolesResource) {
        try {
            List list = this.sqlQuery("select rel.ROLE_ID,rel.R_ID,r.NAME  from t_resource r left join t_mrp_rel rel \n" +
                    "on r.ID=rel.R_ID and rel.type=0 and rel.ROLE_ID=?", rolesResource.getRole().getKey());
            if (list.size() > 0) {
                RolesResource RResource = new RolesResource();
                RResource.setRole(rolesResource.getRole());
                RResource.setResources(new ArrayList<AbstractTreeResource>());

                for (int i = 0; i < list.size(); i++) {
                    AbstractTreeResource resource = new CommonTreeResource();
                    Object[] rs = (Object[]) list.get(i);
                    resource.setID(Long.parseLong(String.valueOf(rs[0])));
                    resource.setName(String.valueOf(rs[1]));
                    RResource.getResources().add(resource);
                }

                return RResource;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return null;
    }

    public void delete(Long operateUserId, Long roleId) throws SQLException {
        StringBuilder builder = new StringBuilder();
        builder.append(" delete from t_mrp_rel where role_id = ? and r_id in");
        builder.append(" (select temp.r_id from (select m.r_id from t_mrp_rel m ,t_user_role_rel ur where m.ROLE_ID = ur.ROLE and ur.id = ?) temp)");
        String sql = builder.toString();

        this.executeUpdateSQL(builder.toString(), roleId, operateUserId);
    }

    public void delete(Connection con, Long roleId) throws SQLException {
        String sql = "delete from t_mrp_rel where role_id = ?";

        this.executeUpdateSQL(sql, roleId);
    }

    public int deleteByRoleId(Long roleId) throws SQLException {
        String sql = "delete from t_mrp_rel where role_id = ?";
        return this.executeUpdateSQL(sql, roleId);
    }

    /**
     * 根据类型获取后台用户的数据权限
     *
     * @param userId
     * @param type
     * @return
     */
    public List<RoleDataPermissonDTO> getUserDataPermissonListByRoleId(String userId, String type) {
        List<RoleDataPermissonDTO> permissonsList = null;
        String sql = "select role_id roleId,type as type,r_id as rId from t_mrp_rel  where role_id in(SELECT r.ID FROM t_role r,t_user_role_rel rel where r.ID=rel.ROLE and rel.ID=?) and type=?";
        permissonsList = jdbcTemplate.queryForList(sql, RoleDataPermissonDTO.class, userId, type);

        return permissonsList;
    }

    /**
     * 插入数据
     *
     * @param roleId
     * @param type
     * @param rId
     * @return
     */
    public String insertIntoRoleDataPermission(String roleId, Integer type, String rId, String opUser) throws Exception {
        String sql = "insert into t_mrp_rel(`role_id`,`type`,`r_id`,`OPTUSER`,`create_time`) values(?,?,?,?,now())";
        this.executeUpdateSQL(sql, roleId, type, rId, opUser);
        return "success";
    }
}
