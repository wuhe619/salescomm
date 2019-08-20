package com.bdaim.rbac.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.IDHelper;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.rbac.dao.RoleDao;
import com.bdaim.rbac.dao.UserDao;
import com.bdaim.rbac.dto.Page;
import com.bdaim.rbac.dto.RoleDTO;
import com.bdaim.rbac.dto.RolesResourceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2019/3/14
 * @description
 */
@Service("RoleService")
@Transactional
public class RoleService {
    public static final Logger log = LoggerFactory.getLogger(RoleService.class);
    @Resource
    private RoleDao roleDao;
    @Resource
    private UserDao userDao;

    /**
     * 查询职位管理列表
     */
    public Page getRoleList(PageParam page, Long id) throws Exception {
        Integer pageNum = page.getPageNum();
        Integer pageSize = page.getPageSize();
        //查询所有部门和职位列表
        StringBuffer querySql = new StringBuffer("SELECT cast(t.ID as char) depeId,t.`NAME` deptName,r.CREATE_TIME createTime,r.`NAME` roleName,r.ID roleId");
        querySql.append(" FROM t_role r LEFT JOIN t_dept t ON t.ID = r.DEPTID ");
        if (id != null) {
            querySql.append(" WHERE t.ID=" + id);
        }
        querySql.append(" GROUP BY t.ID, r.ID");
        Page dataPage = roleDao.sqlPageQuery(querySql.toString(), pageNum, pageSize);
        List<Map<String, Object>> data = dataPage.getData();
        //判断当前职位是否有用户
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++) {
                String roleId = String.valueOf(data.get(i).get("roleId"));
                log.info("职位id是：" + roleId);
                List<Map<String, Object>> userNumByRoleId = roleDao.getUserNumByRoleId(roleId);
                if (userNumByRoleId.size() > 0) {
                    data.get(i).put("flag", true);
                } else {
                    data.get(i).put("flag", false);
                }
            }
        }
        return dataPage;
    }

    /**
     * 查询职位详情页面
     */
    public JSONArray queryResourceSelectStatus(Long operateUserId, Long roleId, Long pid, boolean isAdminOperate, String platform) {
        JSONArray array = new JSONArray();
        StringBuilder builder = new StringBuilder();
        //根据角色查询为0的父标签，根据父标签查询字标签  标记选中子标签为true
        //admin账户可以分配所有 不是admin只能分配自己拥有的标签
        if (isAdminOperate) {
            builder.append(" select distinct r.ID,r.NAME,r.PID,r.URI,r.TYPE,case when temp.r_id is null then 1 ELSE 0 END as CHECKED from t_resource r");
            builder.append(" left join (select r_id from t_mrp_rel where role_id=");
            builder.append(roleId);
            builder.append(" ) temp on temp.r_id = r.id");
            builder.append(" where r.pid=");
            builder.append(pid);
            if (StringUtil.isNotEmpty(platform)) {
                builder.append(" and r.platform=" + platform);
            }
        } else {
            builder.append(" select distinct r.ID,r.NAME,r.PID,r.URI,r.TYPE,case when temp.r_id is null then 1 ELSE 0 END as CHECKED from t_mrp_rel m ");
            builder.append(" inner join t_user_role_rel ur on ur.ROLE = m.ROLE_ID");
            builder.append(" inner join t_resource r on m.R_ID = r.ID and ur.ID =");
            builder.append(operateUserId);
            if (StringUtil.isNotEmpty(platform)) {
                builder.append(" and r.platform=" + platform);
            }
            builder.append(" and r.pid = ");
            builder.append(pid);
            builder.append(" left join (select r_id from t_mrp_rel where role_id=");
            builder.append(roleId);
            builder.append(" ) temp on temp.r_id = r.id");
        }
        String sql = builder.toString();
        List<Map<String, Object>> list = roleDao.sqlQuery(sql);
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                Long id = NumberConvertUtil.everythingToLong(map.get("ID"));
                String name = (String) map.get("NAME");
                String uri = (String) map.get("URI");
                Long parentId = NumberConvertUtil.everythingToLong(map.get("PID"));
                int type = NumberConvertUtil.everythingToInt(map.get("TYPE"));
                int checked = NumberConvertUtil.everythingToInt(map.get("CHECKED"));
                JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("name", name);
                object.put("uri", uri);
                object.put("pid", parentId);
                object.put("type", type);
                object.put("checked", checked == 1 ? false : true);
                object.put("children", queryResourceSelectStatus(operateUserId, roleId, id, isAdminOperate, platform));
                array.add(object);
            }
        }

        return array;
    }

    /**
     * 相同部门的角色名称是否存在
     */
    public boolean isUniqueName(String roleName, Long roleId, Long deptId) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT count(*) as NUM FROM t_role where 1=1 ");
        if (roleName != null && !"".equals(roleName)) sql.append(" and NAME ='" + roleName + "' ");
        if (deptId != null && deptId != 0) {
            sql.append(" and deptid =" + deptId + "");
        }
        if (roleId != null && roleId != 0) sql.append(" and id<>" + roleId + " ");
        List<Map<String, Object>> rs = roleDao.sqlQuery(sql.toString());
        if (NumberConvertUtil.everythingToInt(rs.get(0).get("NUM")) > 0)
            return false;
        else
            return true;
    }

    /**
     * 角色权限设置
     *
     * @return
     */
    public boolean updateRoleTree(RolesResourceDto rrPermission, Long operateUserId, boolean isAdminOperate) {
        try {
            RoleDTO role = rrPermission.getRole();
            Long roleId = role.getKey();
            //更新角色
            if (role.getName() != null && !role.getName().equals("") && roleId != null) {
                roleDao.update(rrPermission.getRole());
            }
            //更新角色和资源的关系,如果是admin操作，则删除roleid对应的所有资源后在插入，否侧删除operateUserId对应的资源范围内删除roleid分配的资源
            if (isAdminOperate) {
                //先删除职位下所有资源，然后重新添加
                roleDao.deleteSourceTree(roleId);
                roleDao.insertResource(rrPermission);
            } else {
                roleDao.deleteSourceTree(operateUserId, roleId);
                roleDao.insertResource(rrPermission);
            }
        } catch (SQLException e) {
            log.error("角色设置异常", e);
            return false;
        }
        return true;
    }

    /**
     * 添加职位信息并配置权限
     *
     * @author:duanliying
     * @method
     * @date: 2019/3/15 14:50
     */
    public boolean addRoleMessage(RolesResourceDto rResource) {
        try {
            rResource.getRole().setKey(IDHelper.getID());
            roleDao.insertRole(rResource.getRole());
            roleDao.insertResource(rResource);
        } catch (SQLException e) {
            log.error("添加角色异常", e);
            return false;
        }
        return true;
    }

    /**
     * 根据部门id查询职位信息
     *
     * @param deptId
     * @param userId
     * @return
     */
    public List<RoleDTO> queryRoleByDept(Long deptId, Long userId) {
        String sql = "";
        if (null == userId) {
            sql = "select ID,NAME from t_role where deptid=" + deptId;
        } else {
            sql = "select t2.ID,t2.NAME from t_user_role_rel t left join t_role t2 on t.ROLE = t2.ID where t.id=" + userId;
        }
        List<Map<String, Object>> list = roleDao.sqlQuery(sql);
        List<RoleDTO> infos = null;
        if (list != null && !list.isEmpty()) {
            infos = new ArrayList<RoleDTO>();
            for (Map<String, Object> map : list) {
                Long id = NumberConvertUtil.everythingToLong(map.get("ID"));
                String name = (String) map.get("NAME");
                RoleDTO info = new RoleDTO();
                info.setId(id);
                info.setName(name);
                infos.add(info);
            }
        }
        return infos;
    }

    /**
     * 根据用户职位查询用户资源树
     *
     * @author:duanliying
     * @method
     * @date: 2019/3/19 18:32
     */
    public JSONArray queryResourceTreeByRole(Long userId, long pid, String platform) throws Exception {
        //根据userId查询用户职位信息
        String roleId = null;
        if (userId != null) {
            List<Map<String, Object>> userRoleId = roleDao.getRoleByUserId(userId);
            if (userRoleId.size() > 0) {
                roleId = String.valueOf(userRoleId.get(0).get("roleId"));
            }
        }
        JSONArray array = new JSONArray();
        StringBuilder builder = new StringBuilder();
        builder.append(" select distinct r.ID,r.NAME,r.PID,r.URI,r.TYPE FROM t_mrp_rel m ");
        builder.append(" inner join t_user_role_rel ur on ur.ROLE = m.ROLE_ID");
        builder.append(" inner join t_resource r on m.R_ID = r.ID and ur.ID =");
        builder.append(userId);
        if (StringUtil.isNotEmpty(platform)) {
            builder.append(" and r.platform = " + platform);
        }
        builder.append(" and r.pid = ");
        builder.append(pid);
        builder.append(" left join (select r_id from t_mrp_rel where role_id=");
        builder.append(roleId);
        builder.append(" ) temp on temp.r_id = r.id");
        List<Map<String, Object>> list = roleDao.sqlQuery(builder.toString());
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                Long id = NumberConvertUtil.everythingToLong(map.get("ID"));
                String name = (String) map.get("NAME");
                String uri = (String) map.get("URI");
                Long parentId = NumberConvertUtil.everythingToLong(map.get("PID"));
                JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("name", name);
                object.put("uri", uri);
                object.put("pid", parentId);
                object.put("children", queryResourceTreeByRole(userId, id, platform));
                array.add(object);
            }
        }
        return array;
    }

    /**
     * 查询角色信息
     */
    public List<Map<String, Object>> queryUserListByRoleId(String id) throws Exception {
        String querySql = "SELECT u.id,u.`name` FROM t_user_role_rel r LEFT JOIN t_user u on r.ID = u.ID where u.status=0 and r.ROLE  = ? GROUP BY u.ID ";
        List<Map<String, Object>> list = roleDao.sqlQuery(querySql, id);
        return list;
    }
}

