package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmAdminUserEntity;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.SqlAppendUtil;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmAdminUserDao extends SimpleHibernateDao<LkCrmAdminUserEntity, Long> {

    public List queryUserIdByDeptId(List<String> deptIds) {
        String sql = " select DISTINCT user_id from lkcrm_admin_user where dept_id in (? )";
        return this.queryListBySql(sql, SqlAppendUtil.sqlAppendWhereIn(deptIds));
    }

    public List<Long> queryUserIdByDeptId(String deptIds) {
        String sql = " select DISTINCT user_id from lkcrm_admin_user where dept_id in (? )";
        return this.queryListBySql(sql, deptIds);
    }

    public List<Map<String, Object>> queryUserByRealName(String name) {
        String sql = "SELECT " +
                " au.realname,au.mobile,au.post AS postName,ad.name AS deptName  " +
                "FROM " +
                " lkcrm_admin_user AS au " +
                " LEFT JOIN lkcrm_admin_dept AS ad ON au.dept_id = ad.dept_id  " +
                "WHERE " +
                " 1 = 1  " +
                " AND au.realname LIKE concat( '%', ?, '%' )";
        return super.queryListBySql(sql, name);
    }

    public List<Map<String, Object>> querySuperior(String realName) {
        String sql = "SELECT id,realname FROM lkcrm_admin_user " +
                "WHERE 1 = 1 AND realname LIKE concat( '%', ?, '%' )";
        return super.queryListBySql(sql, realName);
    }

    public List<Map<String, Object>> queryUsersByDeptId(Integer dept_id, String name) {
        String sql = "SELECT " +
                " au.realname,au.mobile,au.post AS postName,ad.NAME AS deptName  " +
                "FROM " +
                " lkcrm_admin_user AS au " +
                " LEFT JOIN lkcrm_admin_dept AS ad ON au.dept_id = ad.dept_id  " +
                "WHERE " +
                " 1 = 1  " +
                " AND au.dept_id = ?  " +
                " AND au.realname LIKE concat( '%', ?, '%' )";
        return super.queryListBySql(sql, dept_id, name);
    }

    public List<Map<String, Object>> queryUserList(String name, List<Integer> deptId, Integer status, String roleId) {
        String sql = "SELECT " +
                " a.realname,a.username,a.user_id,a.sex,a.mobile,a.email, " +
                " e.NAME AS deptName,a.STATUS,a.create_time,a.dept_id,a.post, " +
                " a.parent_id,a.img, " +
                " ( SELECT b.realname FROM lkcrm_admin_user b WHERE b.user_id = a.parent_id ) AS parentName, " +
                " ( " +
                "SELECT group_concat( d.role_id ) FROM lkcrm_admin_user_role AS c " +
                " LEFT JOIN lkcrm_admin_role AS d ON c.role_id = d.role_id  " +
                "WHERE c.user_id = a.user_id  " +
                " ) AS roleId, " +
                " ( " +
                "SELECT " +
                " group_concat( d.role_name )  " +
                "FROM " +
                " lkcrm_admin_user_role AS c " +
                " LEFT JOIN lkcrm_admin_role AS d ON c.role_id = d.role_id  " +
                "WHERE " +
                " c.user_id = a.user_id  " +
                " ) AS roleName  " +
                "FROM " +
                " lkcrm_admin_user a " +
                " LEFT JOIN lkcrm_admin_dept e ON a.dept_id = e.dept_id " +
                " INNER JOIN t_customer_user h ON a.user_id = h.id";
        if (StringUtil.isNotEmpty(roleId)) {
            sql += " LEFT JOIN lkcrm_admin_user_role f ON a.user_id = f.user_id ";
        }
        sql += " where  1=1 ";
        List<Object> params = new ArrayList<>();
        if (StringUtil.isNotEmpty(name)) {
            sql += " AND a.realname LIKE concat( '%', ?, '%' ) ";
            params.add(name);
        }
        if (!CollectionUtils.isEmpty(deptId)) {
            sql += " and a.dept_id in (" + SqlAppendUtil.sqlAppendWhereIn(deptId) + ") ";
        }
        if (status != null) {
            sql += " AND a.status = ? ";
            params.add(status);
        }
        if (StringUtil.isNotEmpty(roleId)) {
            sql += " and f.role_id = ? ";
            params.add(roleId);
        }
        sql+=" AND h.cust_id = ? ";
        params.add(BaseUtil.getCustId());
        return super.sqlQuery(sql, params.toArray());
    }

    public Page queryUserListByPage(int page, int limit, String name, List<Integer> deptId, Integer status, String roleId) {
        String sql = "SELECT a.realname,a.username,a.user_id,a.sex,a.mobile,a.email, e.NAME AS deptName,a.STATUS,a.create_time,a.dept_id,a.post, a.parent_id,a.img, ( SELECT b.realname FROM lkcrm_admin_user b WHERE b.user_id = a.parent_id ) AS parentName, ( SELECT group_concat( d.role_id ) FROM lkcrm_admin_user_role AS c LEFT JOIN lkcrm_admin_role AS d ON c.role_id = d.role_id WHERE c.user_id = a.user_id ) AS roleId, ( SELECT group_concat( d.role_name ) FROM lkcrm_admin_user_role AS c LEFT JOIN lkcrm_admin_role AS d ON c.role_id = d.role_id WHERE c.user_id = a.user_id ) AS roleName FROM lkcrm_admin_user a LEFT JOIN lkcrm_admin_dept e ON a.dept_id = e.dept_id  ";
        sql += " INNER JOIN t_customer_user h ON a.user_id = h.id ";
        if (StringUtil.isNotEmpty(roleId)) {
            sql += " LEFT JOIN lkcrm_admin_user_role f ON a.user_id = f.user_id ";
        }
        sql += " where  1=1 ";
        List<Object> params = new ArrayList<>();
        if (StringUtil.isNotEmpty(name)) {
            sql += " AND a.realname LIKE concat( '%', ?, '%' ) ";
            params.add(name);
        }
        if (!CollectionUtils.isEmpty(deptId)) {
            sql += " and a.dept_id in (" + SqlAppendUtil.sqlAppendWhereIn(deptId) + ") ";
        }
        if (status != null) {
            sql += " AND a.status = ? ";
            params.add(status);
        }
        if (StringUtil.isNotEmpty(roleId)) {
            sql += " and f.role_id = ? ";
            params.add(roleId);
        }
        sql+=" AND h.cust_id = ? ";
        params.add(BaseUtil.getCustId());
        return super.sqlPageQuery(sql, page, limit, params.toArray());
    }
}
