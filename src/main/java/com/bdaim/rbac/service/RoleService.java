package com.bdaim.rbac.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.rbac.dao.RoleDao;
import com.bdaim.rbac.dao.RoleResourceDao;
import com.bdaim.rbac.dao.UserDao;
import com.bdaim.rbac.dao.UserRoleDao;
import com.bdaim.rbac.dto.*;
import com.bdaim.rbac.vo.QueryDataParam;
import com.bdaim.rbac.vo.RoleInfo;
import com.bdaim.util.DateUtil;
import com.bdaim.util.IDHelper;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
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
                String type = String.valueOf(map.get("TYPE"));
                Long parentId = NumberConvertUtil.everythingToLong(map.get("PID"));
                JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("name", name);
                object.put("uri", uri);
                object.put("type", type);
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
        String querySql = "SELECT u.id,u.`name`,u.REALNAME realName FROM t_user_role_rel r LEFT JOIN t_user u on r.ID = u.ID where u.status=0 and r.ROLE  = ? GROUP BY u.ID ";
        List<Map<String, Object>> list = roleDao.sqlQuery(querySql, id);
        return list;
    }

    @Resource
    private RoleResourceDao roleResourceDao;
    @Resource
    private UserRoleDao userRoleDao;


    public boolean addRole(RolesResource rResource) {
        try {
            rResource.getRole().setKey(IDHelper.getID());
            roleDao.insert(rResource.getRole());
            roleResourceDao.insert(rResource);
        } catch (SQLException e) {
            log.error("添加角色异常", e);
            return false;
        } finally {

        }
        return true;
    }

    public boolean delRole(Long roleID) {
        boolean success = false;
        try {
            if (!canDelete(roleID)) return false;
            RoleDTO role = new RoleDTO(roleID);
            roleDao.delete(role);
            UserRoles userRoles = new UserRoles();
            List<RoleDTO> roles = new ArrayList<RoleDTO>();
            roles.add(role);
            userRoles.setRoles(roles);
            userRoleDao.delete(userRoles);
            success = true;
        } catch (SQLException e) {
            log.error("删除角色异常", e);
            return false;
        } finally {
            return success;
        }
    }

    private boolean canDelete(Long roleID) throws SQLException {
        List list = this.roleDao.getSQLQuery("select * from t_user_role_rel where role=" + roleID).list();
        if (list.size() > 0) {
            return false;
        } else {
            return true;
        }
    }

    public List<Map<String, Object>> query(String roleName, Page page) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ID,NAME,OPTUSER,CREATE_TIME,MODIFY_TIME,TYPE FROM t_role where 1=1 ");
        if (roleName != null && !"".equals(roleName)) sql.append(" and NAME like '%" + roleName + "%' ");
        sql.append(" order by ID ");
        List<Map<String, Object>> rs = this.roleDao.sqlQuery(sql.toString(), new Page(page.getPageIndex(), page.getCountPerPage()));
        for (Map<String, Object> item : rs) {
            item.put("CREATE_TIME", DateUtil.formatDate("yyyy-MM-dd", (Date) item.get("CREATE_TIME")));
            if (item.get("MODIFY_TIME") != null)
                item.put("MODIFY_TIME", DateUtil.formatDate("yyyy-MM-dd", (Date) item.get("MODIFY_TIME")));
            item.put("TYPE", ManagerType.getManagerType(Integer.valueOf(item.get("TYPE").toString())).getCnName());
        }
        return rs;
    }

    public int queryCount(String roleName) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT count(*) as num FROM t_role where 1=1 ");
        if (roleName != null && !"".equals(roleName)) sql.append(" and NAME like '%" + roleName + "%' ");
        List<Map<String, Object>> rs = roleDao.sqlQuery(sql.toString());
        return ((BigInteger) rs.get(0).get("num")).intValue();
    }


    public boolean updateRRP(RolesResource rrPermission, Long operateUserId, boolean isAdminOperate) {
        try {
            RoleDTO role = rrPermission.getRole();
            Long roleId = role.getKey();
            //更新角色
            if (role.getName() != null && !role.getName().equals("") && roleId != null) {
                roleDao.update(rrPermission.getRole());
            }

            //更新角色和资源的关系,如果是admin操作，则删除roleid对应的所有资源后在插入，否侧删除operateUserId对应的资源范围内删除roleid分配的资源
            if (isAdminOperate) {
                roleResourceDao.deleteByRoleId(roleId);
                roleResourceDao.insert(rrPermission);
            } else {
                roleResourceDao.delete(operateUserId, roleId);
                roleResourceDao.insert(rrPermission);
            }
        } catch (SQLException e) {
            log.error("更新角色异常", e);
            return false;
        } finally {
        }
        return true;
    }

    public RoleDTO queryById(Long rId) {
        RoleDTO rs = null;
        try {
            RoleDTO role = new RoleDTO(rId);
            rs = roleDao.getObj(role);
        } catch (Exception e) {
            log.error("查询角色异常", e);
        } finally {
            return rs;
        }
    }

   /* public boolean isUniqueName(String roleName, Long roleId, Long deptId) {
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
    }*/

    /**
     * 递规查询数据
     *
     * @param
     * @param sql
     * @param pid
     * @return
     */
    private net.sf.json.JSONArray getChildJson(String sql, Long pid) {
        net.sf.json.JSONArray childJson = new net.sf.json.JSONArray();
        net.sf.json.JSONArray childArray = null;
        try {
            List list = this.roleDao.getSQLQuery(sql + pid).list();
            for (int i = 0; i < list.size(); i++) {
                Object[] obj = (Object[]) list.get(i);
                net.sf.json.JSONObject item = new net.sf.json.JSONObject();
                item.put("id", Long.parseLong(String.valueOf(obj[0])));
                item.put("text", String.valueOf(obj[1]));

                if ((childArray = getChildJson(sql, Long.parseLong(String.valueOf(obj[1])))) != null) {
                    item.put("children", childArray);
                } else {
                    item.put("type", "file");
                    if (Integer.parseInt(String.valueOf(obj[2])) == 1) {
                        net.sf.json.JSONObject state = new net.sf.json.JSONObject();
                        state.put("selected", true);
                        item.put("state", state);
                    }
                }
                childJson.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("递规查询数据异常", e.getStackTrace());
        }
        if (childJson.size() == 0) childJson = null;
        return childJson;
    }

    @SuppressWarnings("unchecked")
    public List<RoleInfo> queryRole(QueryDataParam param) {
        StringBuilder queryCount = new StringBuilder();
        StringBuilder queryData = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        builder.append(" from t_role r left join t_dept d on r.DEPTID = d.ID ");
        if (!(18888 == (param.getUserId() == null ? 0 : param.getUserId().longValue()))) {
            builder.append(" left join t_user u on r.deptid=u.deptid and u.id=").append(param.getUserId());
        }
        builder.append(" where 1=1 ");
        Long deptId = param.getDeptId();
        if (deptId != null) {
            builder.append(" and d.ID = " + deptId);
        }
        String condition = param.getCondition();
        if (!StringUtils.isEmpty(condition)) {
            builder.append(" and r.name like '%" + condition + "%'");
        }
        builder.append(" order by r.create_time desc");
        Page page = param.getPage();
        queryData.append(" select r.ID,r.NAME,r.OPTUSER,r.CREATE_TIME,r.MODIFY_TIME ,d.name as DEPTNAME,d.id as DEPTID");
        queryData.append(builder);
        String queryDataSql = queryData.toString();
        List<Map<String, Object>> list = roleDao.sqlQuery(queryDataSql, new Page(page.getPageIndex(), page.getCountPerPage()));
        List<RoleInfo> vos = null;
        if (list != null && !list.isEmpty()) {
            vos = new ArrayList<RoleInfo>();
            for (Map<String, Object> map : list) {
                Long id = NumberConvertUtil.everythingToLong(map.get("ID"));
                Long bigInteger = NumberConvertUtil.everythingToLong(map.get("DEPTID"));
                //Long dId = ((BigInteger)map.get("deptId")).longValue();
                String name = (String) map.get("NAME");
                String optuser = (String) map.get("OPTUSER");
                Object create = map.get("CREATE_TIME");
                Object modify = map.get("MODIFY_TIME");
                String deptname = (String) map.get("DEPTNAME");

                String createTime = create == null ? "" : DateUtil.format((Date) create, "yyyy/MM/dd HH:mm:ss");
                String modifyTime = modify == null ? "" : DateUtil.format((Date) modify, "yyyy/MM/dd HH:mm:ss");
                RoleInfo info = new RoleInfo();
                info.setId(id);
                info.setCreatetime(createTime);
                info.setModifytime(modifyTime);
                info.setDeptname(deptname);
                info.setName(name);
                info.setOptuser(optuser);
                info.setDeptId(bigInteger == null ? null : bigInteger);
                vos.add(info);
            }
        }

        queryCount.append(" select count(*) as COUNT ");
        queryCount.append(builder);
        String queryCountSql = queryCount.toString();
        List<Map<String, Object>> list1 = roleDao.sqlQuery(queryCountSql);
        if (list1 != null && !list.isEmpty()) {
            Map<String, Object> map = list1.get(0);
            int count = NumberConvertUtil.everythingToInt(map.get("COUNT"));
            page.setCount(count);
            param.getPage().setCount(count);
        }
        return vos;
    }

    public List<RoleInfo> queryRoleV1(QueryDataParam param) {
        StringBuilder queryData = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        builder.append(" from t_role r left join t_dept d on r.DEPTID = d.ID ");
        if (!(18888 == (param.getUserId() == null ? 0 : param.getUserId().longValue()))) {
            builder.append(" left join t_user u on r.deptid=u.deptid and u.id=").append(param.getUserId());
        }
        builder.append(" where 1=1 ");
        Long deptId = param.getDeptId();
        if (deptId != null) {
            builder.append(" and d.ID = " + deptId);
        }
        String condition = param.getCondition();
        if (!StringUtils.isEmpty(condition)) {
            builder.append(" and r.name like '%" + condition + "%'");
        }
        builder.append(" order by r.create_time desc");
        Page page = param.getPage();
        queryData.append(" select r.ID,r.NAME,r.OPTUSER,r.CREATE_TIME,r.MODIFY_TIME ,d.name as DEPTNAME,d.id as DEPTID");
        queryData.append(builder);
        String queryDataSql = queryData.toString();
        int countPerpage = page.getCountPerPage();
        int index = page.getPageIndex();
        int start = index * countPerpage;
        Page pageData = roleDao.sqlPageQuery(queryDataSql, start, countPerpage);
        List<RoleInfo> vos = null;
        if (pageData.getData() != null) {
            vos = new ArrayList<RoleInfo>();
            Map<String, Object> map;
            for (int i = 0; i < pageData.getData().size(); i++) {
                map = (Map<String, Object>) pageData.getData().get(i);
                Long id = NumberConvertUtil.everythingToLong(map.get("ID"));
                Long bigInteger = NumberConvertUtil.everythingToLong(map.get("DEPTID"));
                //Long dId = ((BigInteger)map.get("deptId")).longValue();
                String name = (String) map.get("NAME");
                String optuser = (String) map.get("OPTUSER");
                Object create = map.get("CREATE_TIME");
                Object modify = map.get("MODIFY_TIME");
                String deptname = (String) map.get("DEPTNAME");

                String createTime = create == null ? "" : DateUtil.format((Date) create, "yyyy/MM/dd HH:mm:ss");
                String modifyTime = modify == null ? "" : DateUtil.format((Date) modify, "yyyy/MM/dd HH:mm:ss");
                RoleInfo info = new RoleInfo();
                info.setId(id);
                info.setCreatetime(createTime);
                info.setModifytime(modifyTime);
                info.setDeptname(deptname);
                info.setName(name);
                info.setOptuser(optuser);
                info.setDeptId(bigInteger == null ? null : bigInteger);
                vos.add(info);
            }
        }
        page.setCount(pageData.getTotal());
        param.getPage().setCount(pageData.getTotal());
        return vos;
    }


    /*@SuppressWarnings("unchecked")
    public List<RoleInfo> queryRoleByDept(Long deptId, Long userId) {
        String sql = "";
        if (null == userId) {
            sql = "select ID,NAME from t_role where deptid=" + deptId;
        } else {
            sql = "select t2.ID,t2.NAME from t_user_role_rel t left join t_role t2 on t.ROLE = t2.ID where t.id=" + userId;
        }
        List<Map<String, Object>> list = roleDao.sqlQuery(sql);
        List<RoleInfo> infos = null;
        if (list != null && !list.isEmpty()) {
            infos = new ArrayList<RoleInfo>();
            for (Map<String, Object> map : list) {
                Long id = NumberConvertUtil.everythingToLong(map.get("ID"));
                String name = (String) map.get("NAME");
                RoleInfo info = new RoleInfo();
                info.setId(id);
                info.setName(name);
                infos.add(info);
            }
        }
        return infos;
    }*/

    @SuppressWarnings("unchecked")
    public List<RoleDTO> queryAll() {
        String sql = "select ID,NAME,create_time,modify_time,deptid,optuser from t_role";
        List<Map<String, Object>> list = roleDao.sqlQuery(sql);
        List<RoleDTO> roles = null;
        if (list != null && !list.isEmpty()) {
            roles = new ArrayList<RoleDTO>();
            for (Map<String, Object> map : list) {
                Long id = NumberConvertUtil.everythingToLong(map.get("ID"));
                String name = (String) map.get("NAME");
                Long deptid = map.get("DEPTID") == null ? null : NumberConvertUtil.everythingToLong(map.get("DEPTID"));
                RoleDTO role = new RoleDTO();
                role.setKey(id);
                role.setId(id);
                role.setName(name);

                role.setDeptId(deptid);
                roles.add(role);
            }
        }
        return roles;
    }

    @SuppressWarnings("unchecked")
    public List<RoleDTO> queryRoleByUserid(long userId) {
        StringBuilder builder = new StringBuilder();
        builder.append("select r.id,r.name,r.create_time,r.modify_time,r.optuser,r.optuser from t_role r ");
        builder.append("inner join t_user_role_rel ur on ur.ROLE = r.ID and ur.id = ");
        builder.append(userId);
        List<Map<String, Object>> list = roleDao.sqlQuery(builder.toString());
        List<RoleDTO> roles = null;
        if (list != null && !list.isEmpty()) {
            roles = new ArrayList<RoleDTO>();
            for (Map<String, Object> map : list) {
                Long id = ((BigInteger) map.get("id")).longValue();
                String name = (String) map.get("name");
                Date createTime = (Date) map.get("create_time");
                Date modifyTime = (Date) map.get("modify_time");
                Long deptid = ((BigInteger) map.get("deptid")).longValue();
                String optuser = (String) map.get("optuser");
                RoleDTO role = new RoleDTO();
                role.setKey(id);
                role.setName(name);
                role.setCreateDate(createTime);
                role.setModifyDate(modifyTime);
                role.setUser(optuser);
                role.setDeptId(deptid);
                roles.add(role);
            }
        }
        return roles;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> queryRoleSelectStatus(Long operateUserId, Long userId, boolean isAdminOperate) {
        StringBuilder builder = new StringBuilder();
        //如果是admin操作，则查询所有觉得中userid的分配情况，否则在operateUserId对应的角色范围内
        if (isAdminOperate) {
            builder.append(" select distinct d.id as deptid,d.name as deptname, r.id,r.name,r.create_time,r.modify_time,r.optuser,ISNULL(temp.role) as checked from t_role r");
            builder.append(" inner join t_dept d on r.DEPTID = d.ID");
            builder.append(" left join (select role from t_user_role_rel where id = ");
            builder.append(userId);
            builder.append(" )temp on r.id = temp.role");
        } else {
            builder.append(" select distinct d.id as deptid,d.name as deptname,r.id,r.name,r.create_time,r.modify_time,r.optuser,ISNULL(temp.role) as checked from t_role r");
            builder.append(" inner join t_user_role_rel ur on ur.ROLE = r.ID and ur.id =");
            builder.append(operateUserId);
            builder.append(" inner join t_dept d on r.DEPTID = d.ID");
            builder.append(" left join (select role from t_user_role_rel where id = ");
            builder.append(userId);
            builder.append(" )temp on r.id = temp.role");
        }
        String sql = builder.toString();
        List<Map<String, Object>> list = roleDao.sqlQuery(sql);
        return list;
    }

    public List<Map<String, Object>> queryRole(Long operateUserId,
                                               boolean isAdminOperate) {
        StringBuilder builder = new StringBuilder();
        if (isAdminOperate) {
            builder.append(" select distinct d.id as deptid,d.name as deptname, r.id,r.name,r.create_time,r.modify_time,r.optuser from t_role r");
            builder.append(" inner join t_dept d on r.DEPTID = d.ID");
        } else {
            builder.append(" select distinct d.id as deptid,d.name as deptname,r.id,r.name,r.create_time,r.modify_time,r.optuser from t_role r");
            builder.append(" inner join t_user_role_rel ur on ur.ROLE = r.ID and ur.id =");
            builder.append(operateUserId);
            builder.append(" inner join t_dept d on r.DEPTID = d.ID");
        }
        String sql = builder.toString();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = roleDao.sqlQuery(sql);
        return list;
    }

    @SuppressWarnings("unchecked")
    public RoleInfo queryRoleInfo(Long id) {
        StringBuilder builder = new StringBuilder();
        builder.append(" select r.ID,r.NAME,r.OPTUSER,r.CREATE_TIME,r.MODIFY_TIME ,d.name as DEPTNAME,d.id as DEPTID");
        builder.append(" from t_role r left join t_dept d on r.DEPTID = d.ID ");
        builder.append(" where r.id= ");
        builder.append(id);
        List<Map<String, Object>> list = roleDao.sqlQuery(builder.toString());
        RoleInfo info = null;
        if (list != null && !list.isEmpty()) {
            info = new RoleInfo();
            Map<String, Object> map = list.get(0);
            Long roleId = NumberConvertUtil.everythingToLong(map.get("ID"));
            Long bigInteger = NumberConvertUtil.everythingToLong(map.get("DEPTID"));
            //Long deptId = ((BigInteger)map.get("deptId")).longValue();
            String name = (String) map.get("NAME");
            String optuser = (String) map.get("OPTUSER");
            Object create = map.get("CREATE_TIME");
            Object modify = map.get("MODIFY_TIME");
            String deptname = (String) map.get("DEPTNAME");
            String createTime = create == null ? "" : DateUtil.format((Date) create, "yyyy/MM/dd HH:mm:ss");
            String modifyTime = modify == null ? "" : DateUtil.format((Date) modify, "yyyy/MM/dd HH:mm:ss");
            info.setId(roleId);
            info.setDeptId(bigInteger == null ? null : bigInteger);
            info.setCreatetime(createTime);
            info.setModifytime(modifyTime);
            info.setDeptname(deptname);
            info.setName(name);
            info.setOptuser(optuser);
        }
        return info;
    }

    public String insertIntoRoleDataPermission(String roleId, Integer type, String rId, String opUser) throws Exception {
        return roleResourceDao.insertIntoRoleDataPermission(roleId, type, rId, opUser);
    }
}

