package com.bdaim.rbac.service;


import com.bdaim.common.helper.JDBCHelper;
import com.bdaim.common.helper.SQLHelper;
import com.bdaim.rbac.dao.ResourceDao;
import com.bdaim.rbac.dao.UserDao;
import com.bdaim.rbac.dto.*;
import com.bdaim.util.IDHelper;
import com.bdaim.util.NumberConvertUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 */
@Service
@Transactional
public class ResourceService {
    @Resource
    private UserDao userDao;
    @Resource
    private ResourceDao resourceDao;

    @SuppressWarnings("finally")
    public CommonTreeResource save(CommonTreeResource resource) {
        CommonTreeResource rs = null;
        try {
            ;
            resourceDao.insert(resource);
            rs = resource;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return rs;
        }
    }

    @SuppressWarnings("finally")
    public boolean del(Long id) {
        boolean rs = false;
        try {
            resourceDao.delete(new CommonTreeResource(id));
            rs = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return rs;
        }
    }

//    @SuppressWarnings("finally")
//    public CommonTreeResource update(CommonTreeResource resource) {
//        CommonTreeResource rs=null;
//        try {
//            resourceDao.update(resource);
//            rs=resource;
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            return rs;
//        }
//    }

    public void saveAll(String json) {

    }

    public CommonTreeResource getResource(long id) {
        try {
            return resourceDao.getObj(new CommonTreeResource(id));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public JSONArray queryResourceSelectStatus(
            Long operateUserId, Long roleId, Long pid, boolean isAdminOperate) {
        JSONArray array = new JSONArray();
        StringBuilder builder = new StringBuilder();
        if (isAdminOperate) {
            builder.append(" select distinct r.ID,r.NAME,r.PID,r.URI,r.TYPE,case when temp.r_id is null then 1 ELSE 0 END as CHECKED from t_resource r");
            builder.append(" left join (select r_id from t_mrp_rel where role_id=");
            builder.append(roleId);
            builder.append(" and type=0) temp on temp.r_id = r.id");
            builder.append(" where r.pid=");
            builder.append(pid);
        } else {
            builder.append(" select distinct r.ID,r.NAME,r.PID,r.URI,r.TYPE,case when temp.r_id is null then 1 ELSE 0 END as CHECKED from t_mrp_rel m ");
            builder.append(" inner join t_user_role_rel ur on ur.ROLE = m.ROLE_ID");
            builder.append(" inner join t_resource r on m.R_ID = r.ID and ur.ID =");
            builder.append(operateUserId);
            builder.append(" and r.pid = ");
            builder.append(pid);
            builder.append(" left join (select r_id from t_mrp_rel where role_id=");
            builder.append(roleId);
            builder.append(" and type=0) temp on temp.r_id = r.id");
        }
        String sql = builder.toString();
        List<Map<String, Object>> list = resourceDao.sqlQuery(sql);
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
                object.put("children", queryResourceSelectStatus(operateUserId, roleId, id, isAdminOperate));
                array.add(object);
            }
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    public String resources(Long operateUserId, Long pid, String role) {
        StringBuffer rs = new StringBuffer();
        if ("admin".equals(role) || "ROLE_USER".equals(role)) {
            rs.append("[{'children':[");
            rs.append("    {'name':'数据源','pid':1000,'id':1001,'type':1,'uri':'/backend/dataSourceAndLabelManagement/data_source.html'}");
            rs.append("   ,{'name':'标签体系','pid':1000,'id':1002,'type':1,'uri':'/system'}");
            rs.append("   ,{'name':'成本价设置','pid':1000,'id':1003,'type':1,'uri':'/backend/dataSourceAndLabelManagement/price_setting.html'}");
            rs.append(" ]");
            rs.append(" ,'name':'数据源','pid':0,'id':1000,'type':1,'uri':''");
            rs.append("}");

            rs.append(",{'children':[");
            rs.append("   {'name':'行业管理','pid':1100,'id':1101,'type':1,'uri':'/backend/labelPoolManagement/industry_manage.html'}");
            rs.append("  ,{'name':'行业标签池管理','pid':1100,'id':1102,'type':1,'uri':'/backend/labelPoolManagement/label_pool_manage.html'}");
            rs.append("  ,{'name':'销售价设置','pid':1100,'id':1103,'type':1,'uri':'/backend/labelPoolManagement/sale_price_setting.html'} ");
            rs.append("]");
            rs.append("  ,'name':'行业标签池管理','pid':0,'id':1100,'type':1,'uri':''");
            rs.append("}");

            rs.append(",{'children':[");
            rs.append("   {'name':'通话号码审核','pid':1200,'id':1201,'type':1,'uri':'/backend/approvalManagement/call_auditing.html'}");
            rs.append("  ,{'name':'企业资质审核','pid':1200,'id':1202,'type':1,'uri':'/backend/approvalManagement/customerAudit.html'}");
            rs.append("  ,{'name':'邮件模板审核','pid':1200,'id':1203,'type':1,'uri':'/backend/approvalManagement/email-template.html'}");
            rs.append("  ,{'name':'行业标签池审核','pid':1200,'id':1204,'type':1,'uri':'/backend/approvalManagement/Label_pool_audit.html'}");
            rs.append("  ,{'name':'短信模板审核','pid':1200,'id':1205,'type':1,'uri':'/backend/approvalManagement/SMS-template.html'}");
            rs.append("  ]");
            rs.append("  ,'name':'审批管理','pid':0,'id':1200,'type':1,'uri':''");
            rs.append("}");

            rs.append(",{'children':[");
            rs.append("   {'name':'客户群','pid':1400,'id':1401,'type':1,'uri':'/backend/customerGroupManagement/customerGroup.html'}");
            rs.append("  ,{'name':'营销任务','pid':1400,'id':1402,'type':1,'uri':'/backend/customerGroupManagement/taskMgt.html'}]");
            rs.append(" ,'name':'客户群管理','pid':0,'id':1400,'type':1,'uri':''");
            rs.append("}");

            rs.append(",{'children':[");
            rs.append("   {'name':'客户管理','pid':1500,'id':1501,'type':1,'uri':'/backend/customerManagement/customerManagement.html'}");
            rs.append("  ,{'name':'行业标签池开通','pid':1500,'id':1502,'type':1,'uri':'/backend/customerManagement/label_pool_open.html'}");
            rs.append("  ,{'name':'项目管理','pid':1500,'id':1503,'type':1,'uri':'/backend/projectMgt/index.html'}");
            rs.append("  ,{'name':'售价设置','pid':1500,'id':1504,'type':1,'uri':'/backend/customerManagement/salePrice.html'}");
            rs.append("  ,{'name':'外显管理','pid':1500,'id':1505,'type':1,'uri':'/backend/customerManagement/showNumMgt.html'}]");
            rs.append("  ,'name':'客户管理','pid':0,'id':1500,'type':1,'uri':''}");

            rs.append(" ,{'children':[");
            rs.append("  {'name':'供应商管理','pid':1700,'id':1701,'type':1,'uri':'/backend/supplierMgt/index.html'}]");
            rs.append(",'name':'供应商管理','pid':0,'id':1700,'type':1,'uri':''}");

            rs.append(" ,{'children':[");
            rs.append("   {'name':'客户余额管理','pid':1600,'id':1601,'type':1,'uri':'/backend/fundManagement/customerBalanceManagement.html'}");
            rs.append("   ,{'name':'平台余额收支记录','pid':1600,'id':1602,'type':1,'uri':'/backend/fundManagement/Platform_balance.html'}");
            rs.append("   ,{'name':'客户账单','pid':1300,'id':1301,'type':1,'uri':'/backend/bill/Billing_overview1.html'}");
            rs.append("   ,{'name':'供应商账单','pid':1300,'id':1302,'type':1,'uri':'/backend/bill/Billing_overview2.html'}]");
            rs.append("  ,'name':'资金管理','pid':0,'id':1600,'type':1,'uri':''}");
            rs.append("]");
        } else if ("ROLE_CUSTOMER".equals(role)) {

        }

        return rs.toString();
    }


    public List<CommonTreeResource> queryUserSystem(String userName, boolean isAdminOperate) {
        StringBuilder builder = new StringBuilder();
        if (isAdminOperate) {
            builder.append(" select distinct r.ID,r.NAME,r.URI from t_resource r where  r.PID = 0");
        } else {
            builder.append(" select distinct r.ID,r.NAME,r.URI from t_user u ");
            builder.append(" inner join t_user_role_rel ur on u.ID = ur.ID and u.NAME = '");
            builder.append(userName);
            builder.append("'");
            builder.append(" inner join t_mrp_rel mr on ur.ROLE = mr.ROLE_ID");
            builder.append(" inner join t_resource r on mr.R_ID = r.ID and r.PID = 0 and mr.type=0 ");
        }

        String sql = builder.toString();
        List<Map<String, Object>> list = resourceDao.sqlQuery(sql);
        List<CommonTreeResource> resources = null;
        if (list != null && !list.isEmpty()) {
            resources = new ArrayList<CommonTreeResource>();
            for (Map<String, Object> map : list) {
                Long id = ((BigInteger) map.get("ID")).longValue();
                String name = (String) map.get("NAME");
                String uri = (String) map.get("URI");
                CommonTreeResource resource = new CommonTreeResource();
                resource.setID(id);
                resource.setName(name);
                resource.setUri(uri);
                resources.add(resource);
            }
        }
        return resources;
    }

    public JSONArray queryUserReousece(Long userid, boolean isAdminOperate) {
        String sql = "";
        if (isAdminOperate) {
            sql = "select r.id,r.name,r.uri from t_resource";
        } else {
            sql = "select r.id,r.name,r.uri from t_user_role_rel ur ,t_mrp_rel m ,t_resource r " +
                    " where ur.ROLE = m.ROLE_ID and m.R_ID = r.ID and ur.ID = " + userid;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = resourceDao.sqlQuery(sql);
        JSONArray array = new JSONArray();
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                Long id = ((BigInteger) map.get("id")).longValue();
                String name = (String) map.get("name");
                String uri = (String) map.get("uri");
                JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("name", name);
                object.put("uri", uri);
                array.add(object);
            }
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    public JSONArray queryUserResource(AbstractTreeResource params, Long userid,
                                       boolean isAdmin, JSONArray result) {
        JSONArray array = new JSONArray();
        StringBuilder builder = new StringBuilder();
        Long pid = params.getID();
        String uri = params.getUri();
        String type = params.getType();
        if (isAdmin) {
            builder.append(" select r.id,r.name,r.uri,r.type from t_resource r where 1=1");
        } else {
            builder.append(" select r.id,r.name,r.uri,r.type from t_user_role_rel ur");
            builder.append(" inner join t_mrp_rel mr on ur.ROLE = mr.ROLE_ID and mr.type=0 ");
            builder.append(" and ur.id = ").append(userid);
            builder.append(" inner join t_resource r on mr.R_ID = r.ID");
        }
        builder.append(" and r.pid = ").append(pid);

        String sql = builder.toString();
        List<Map<String, Object>> list = resourceDao.sqlQuery(sql);
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                Long id = ((BigInteger) map.get("id")).longValue();
                String name = (String) map.get("name");
                String u = (String) map.get("uri");
                int t = (Integer) map.get("type");
                JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("name", name);
                object.put("uri", u);
                object.put("type", t);
                params.setID(id);
                JSONArray children = queryUserResource(params, userid, isAdmin, result);
                object.put("children", children);
                array.add(object);
                if (StringUtils.isNotBlank(type)) {
                    if ((Integer.parseInt(type) == t)) {
                        if (StringUtils.isNotBlank(u)) {
                            if (uri.equals(u)) {
                                result.add(object);
                            }
                        } else {
                            result.add(object);
                        }
                    }
                } else {
                    if (StringUtils.isNotBlank(u)) {
                        if (uri.equals(u)) {
                            result.add(object);
                        }
                    }
                }
            }
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    public List<String> queryResourceNames(Long sn, Long id, String remark) {
        StringBuilder builder = new StringBuilder();
        if (id != 18888) {
            builder.append(" select r.name from t_user_role_rel ur,t_mrp_rel mr,t_resource r ");
            builder.append(" where r.TYPE in(3,4) and ur.ROLE=mr.ROLE_ID and mr.R_ID=r.ID and mr.type=0 ");
            builder.append(" and r.SN = ").append(sn);
            builder.append(" and r.REMARK = '").append(remark).append("'");
            builder.append(" and ur.ID=").append(id);
        } else {
            builder.append(" select r.name from t_resource r ");
            builder.append(" where r.TYPE in(3,4) ");
            builder.append(" and r.SN = ").append(sn);
            builder.append(" and r.REMARK = '").append(remark).append("'");
        }
        if (id != 18888) {
            builder.append(" and ur.ID=").append(id);
        }
        List<Map<String, Object>> list = resourceDao.sqlQuery(builder.toString());
        List<String> names = null;
        if (list != null && !list.isEmpty()) {
            names = new ArrayList<String>();
            for (Map<String, Object> map : list) {
                String name = (String) map.get("name");
                names.add(name);
            }
        }
        return names;
    }


    public void insert(CommonTreeResource t) throws SQLException {
        this.resourceDao.executeUpdateSQL("insert into t_resource(ID,URI,NAME,TYPE,REMARK,PID,SN,OPTUSER,CREATE_TIME) values(" + IDHelper.getID() + ",'" + t.getUri() + "','" + t.getName() + "','" + t.getType() + "','" + t.getRemark() + "'," + (t.getPid() == null ? "0" : t.getPid()) + "," + t.getSn() + ",'" + t.getUser() + "', now())");

    }

    /**
     * 这个删除方法会删除 当前节点及所有的子节点的资源
     *
     * @param con
     * @param t
     * @throws SQLException
     */
    public void delete(CommonTreeResource t) {
        if (t.getID() == null) throw new NullPointerException("删除记录的ID不可为空");
        AbstractTreeResource delResources = queryAllTree(t, null);

        this.resourceDao.executeUpdateSQL("delete from t_resource where ID= " + delResources.getID());
        this.resourceDao.executeUpdateSQL("delete from t_mrp_rel where R_ID=" + delResources.getID());
        delNote(delResources);
    }

    private void delNote(AbstractTreeResource tree) {
        if (tree.getNotes() != null && tree.getNotes().size() > 0) {
            for (AbstractTreeResource item : tree.getNotes()) {
                this.resourceDao.executeUpdateSQL("delete from t_resource where ID= " + item.getID());
                this.resourceDao.executeUpdateSQL("delete from t_mrp_rel where R_ID=" + item.getID());
            }
        }
    }

    public void update(CommonTreeResource t) throws Exception {
        if (t.getID() == null) throw new NullPointerException("更新记录的ID不可为空");
        StringBuffer sb = new StringBuffer();
        sb.append("update t_resource set MODIFY_TIME=now(),");
        if (t.getRemark() != null && !t.getRemark().equals("")) {
            sb.append("REMARK='" + t.getRemark() + "',");
        }
        if (t.getUser() != null && !t.getUser().equals("")) {
            sb.append("OPTUSER='" + t.getUser() + "',");
        }
        if (t.getPid() != null) {
            sb.append("PID='" + t.getPid() + "',");
        }
        if (t.getName() != null && !t.getName().equals("")) {
            sb.append("NAME='" + t.getName() + "',");
        }
        if (t.getSn() != null) {
            sb.append("sn='" + t.getSn() + "',");
        }
        if (t.getType() != null) {
            sb.append("type='" + t.getType() + "',");
        }
        this.resourceDao.executeUpdateSQL(sb.substring(0, sb.length() - 1) + " where ID=" + t.getID());

    }

    public CommonTreeResource getObj(CommonTreeResource r) {
        try {
            List rs = null;
            if (r.getUri() != null && !"".equals(r.getUri()))
                rs = this.resourceDao.sqlQuery("select ID,URI,NAME,TYPE,REMARK,PID,SN,OPTUSER,CREATE_TIME,MODIFY_TIME from t_resource where URI=" + r.getUri());
            else
                rs = this.resourceDao.sqlQuery("select ID,URI,NAME,TYPE,REMARK,PID,SN,OPTUSER,CREATE_TIME,MODIFY_TIME from t_resource where ID=" + r.getID());
            List<CommonTreeResource> list = CommonTreeResource.pop(rs);
            if (list != null && list.size() > 0) {
                List notes = this.resourceDao.sqlQuery("select ID,URI,NAME,TYPE,REMARK,PID,SN,OPTUSER,CREATE_TIME,MODIFY_TIME from t_resource where PID=" + r.getID());
                List<AbstractTreeResource> aa = new ArrayList<AbstractTreeResource>();
                List<CommonTreeResource> rsList = CommonTreeResource.pop(rs);
                if (rsList != null) aa.addAll(rsList);
                list.get(0).setNotes(aa);
                return list.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return null;
    }

    public boolean hasPermissionByUser(com.bdaim.rbac.dto.Resource r, UserDTO u) {
        if (r.getID() == null || r.getID() == 0) throw new NullPointerException("资源的ID不可为空");
        if (u.getKey() == null || u.getKey().equals("")) throw new NullPointerException("用户信息为空");
        StringBuffer sql = new StringBuffer();
        sql.append("select count(*) as amount from t_resource r,t_mrp_rel rel,t_role o where r.id=rel.R_ID and rel.ROLE_ID=o.ID\n" +
                "where o.ID=" + r.getID() + " and r.ID=" + u.getKey());
        Connection con = null;
        try {
            List list = this.resourceDao.getSQLQuery(sql.toString()).list();
            if (list.size() > 0 && Long.parseLong(String.valueOf(list.get(0))) > 0) return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCHelper.close(con);
        }
        return false;
    }

    public boolean hasPermissionByRole(com.bdaim.rbac.dto.Resource r, RoleDTO role) {
        if (r.getID() == null || r.getID() == 0) throw new NullPointerException("资源的ID不可为空");
        if (role.getKey() == null || role.getKey() == 0) throw new NullPointerException("角色信息不可为空");
        StringBuffer sql = new StringBuffer();
        sql.append("select count(*) as amount from t_mrp_rel rel o where rel.R_ID=" + r.getID() + " and rel.ROLE_ID=" + role.getKey());
        Connection con = null;
        try {
            List list = this.resourceDao.getSQLQuery(sql.toString()).list();
            if (list.size() > 0 && Long.parseLong(String.valueOf(list.get(0))) > 0) return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) try {
                con.close();
            } catch (SQLException e) {
            }
        }
        return false;
    }


    public AbstractTreeResource queryAllTree(AbstractTreeResource treeResource, String[] type) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ID,URI,NAME,TYPE,REMARK,PID,SN,OPTUSER,CREATE_TIME,MODIFY_TIME" +
                " FROM t_resource where PID=?");
        if (type != null && type.length > 0) {
            sb.append(" and TYPE in (" + SQLHelper.getInSQL(type) + ")");
        }
        sb.append(" order by SORT asc");
        AbstractTreeResource returnRS = null;
        try {
            List<Object> params = new ArrayList<Object>();
            returnRS = treeResource;//getObj(null, (CommonTreeResource) treeResource);

            returnRS.setNotes(getNextNotes(sb.toString(), treeResource.getID()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return returnRS;
    }

    public AbstractTreeResource queryUserTree(Long userId, AbstractTreeResource treeResource, String[] types) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT DISTINCT r.ID,r.URI,r.NAME,r.TYPE,r.REMARK,r.PID,r.SN,r.OPTUSER,r.CREATE_TIME,r.MODIFY_TIME\n" +
                " FROM t_user_role_rel u,t_mrp_rel m,t_resource r where r.PID=? and u.ID=" + userId + " and m.ROLE_ID=u.ROLE and m.R_ID=r.ID AND m.type=0");
        if (types != null && types.length > 0) {
            sb.append(" and r.TYPE in (" + SQLHelper.getInSQL(types) + ")");
        }
        sb.append(" order by r.sn asc");

        AbstractTreeResource returnRS = treeResource;
        try {
            returnRS.setNotes(getNextNotes(sb.toString(), treeResource.getID()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return returnRS;
    }

    public AbstractTreeResource queryRoleTree(RoleDTO role, AbstractTreeResource treeResource, String[] types) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT DISTINCT r.ID,r.URI,r.NAME,r.TYPE,r.REMARK,r.PID,r.SN,r.OPTUSER,r.CREATE_TIME,r.MODIFY_TIME" +
                " FROM t_mrp_rel m,t_resource r where r.PID=? and m.ROLE_ID=" + role.getKey() + " and m.ROLE_ID=u.ROLE and m.type=0 ");
        if (types != null && types.length > 0) {
            sb.append(" and r.TYPE in (" + SQLHelper.getInSQL(types) + ")");
        }
        sb.append(" order by r.SN asc");
        AbstractTreeResource returnRS = null;
        try {
            returnRS = getObj((CommonTreeResource) treeResource);

            returnRS.setNotes(getNextNotes(sb.toString(), treeResource.getID()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return returnRS;
    }

    /**
     * 这个方法是递归寻找的过程
     *
     * @param con
     * @param sql
     * @param params 第一个参数要是PID，这样是为了方便调用和递归查找
     * @return
     */
    private List<AbstractTreeResource> getNextNotes(String sql, Long id) throws SQLException {
        List<AbstractTreeResource> list = null;
        try {
            List rs = this.resourceDao.sqlQuery(sql.replace("?", String.valueOf(id)));
            List<CommonTreeResource> tempTrees = CommonTreeResource.pop(rs);
            if (tempTrees != null) {
                list = new ArrayList<AbstractTreeResource>();
                for (CommonTreeResource item : tempTrees) {
                    //这里只将PID进行更换继续查找
                    item.setNotes(getNextNotes(sql, item.getID()));
                    list.add(item);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
        }

        return list;
    }


    @SuppressWarnings("unchecked")
    public JSONArray queryResource(Long operateUserId, Long pid, boolean isAdminOperate) {
        JSONArray array = new JSONArray();
        StringBuilder builder = new StringBuilder();
        if (isAdminOperate) {
            builder.append(" select distinct r.ID,r.NAME,r.PID,r.URI,r.TYPE from t_resource r");
            builder.append(" where r.pid=");
            builder.append(pid);
            builder.append(" and PLATFORM=1");
        } else {
            builder.append(" select distinct r.ID,r.NAME,r.PID,r.URI,r.TYPE from t_mrp_rel m ");
            builder.append(" inner join t_user_role_rel ur on ur.ROLE = m.ROLE_ID");
            builder.append(" inner join t_resource r on m.R_ID = r.ID and ur.ID =");
            builder.append(operateUserId);
            builder.append(" and r.pid = ");
            builder.append(pid);
            builder.append(" and PLATFORM=1 and m.type=0");
        }
        String sql = builder.toString();
        List<Map<String, Object>> list = resourceDao.sqlQuery(sql);
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                Long id = NumberConvertUtil.everythingToLong(map.get("ID"));
                String name = (String) map.get("NAME");
                String uri = (String) map.get("URI");
                Long parentId = NumberConvertUtil.everythingToLong(map.get("PID"));
                int type = NumberConvertUtil.everythingToInt(map.get("TYPE"));
                JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("name", name);
                object.put("uri", uri);
                object.put("pid", parentId);
                object.put("type", type);
                object.put("children", queryResource(operateUserId, id, isAdminOperate));
                array.add(object);
            }
        }
        return array;
    }

    /**
     * 查询树形菜单
     *
     * @param operateUserId
     * @param pid
     * @param platform
     * @param isAdminOperate
     * @return
     */
    public JSONArray listTreeResource(Long operateUserId, Long pid, int platform, boolean isAdminOperate) {
        JSONArray array = new JSONArray();
        StringBuilder builder = new StringBuilder();
        if (isAdminOperate) {
            builder.append(" select distinct r.ID,r.NAME,r.PID,r.URI,r.TYPE from t_resource r");
            builder.append(" where r.pid=");
            builder.append(pid);
            builder.append(" and PLATFORM=").append(platform);
        } else {
            builder.append(" select distinct r.ID,r.NAME,r.PID,r.URI,r.TYPE from t_mrp_rel m ");
            builder.append(" inner join t_user_role_rel ur on ur.ROLE = m.ROLE_ID");
            builder.append(" inner join t_resource r on m.R_ID = r.ID and ur.ID =");
            builder.append(operateUserId);
            builder.append(" and r.pid = ");
            builder.append(pid);
            builder.append(" and PLATFORM=").append(platform).append(" and m.type=0");
        }
        List<Map<String, Object>> list = resourceDao.sqlQuery(builder.toString());
        if (list != null && !list.isEmpty()) {
            JSONObject object;
            long id, parentId;
            String name, uri;
            int type;
            for (Map<String, Object> map : list) {
                id = NumberConvertUtil.everythingToLong(map.get("ID"));
                name = (String) map.get("NAME");
                uri = (String) map.get("URI");
                parentId = NumberConvertUtil.everythingToLong(map.get("PID"));
                type = NumberConvertUtil.everythingToInt(map.get("TYPE"));
                object = new JSONObject();
                object.put("id", id);
                object.put("name", name);
                object.put("title", name);
                object.put("uri", uri);
                object.put("pid", parentId);
                object.put("type", type);
                object.put("children", listTreeResource(operateUserId, id, platform, isAdminOperate));
                array.add(object);
            }
        }
        return array;
    }

    public List<ResourceDTO> queryResource(Long operateUserId, Long pid, int platform, boolean isAdminOperate) {
        List<ResourceDTO> array = new ArrayList<>();
        List<Map<String, Object>> list = resourceDao.listAllResource(pid, operateUserId, platform, isAdminOperate);
        if (list != null && !list.isEmpty()) {
            ResourceDTO resourceDTO;
            List<ResourceDTO> tmpList;
            for (Map<String, Object> map : list) {
                Long id = NumberConvertUtil.everythingToLong(map.get("ID"));
                String name = (String) map.get("NAME");
                String uri = (String) map.get("URI");
                Long parentId = NumberConvertUtil.everythingToLong(map.get("PID"));
                int type = NumberConvertUtil.everythingToInt(map.get("TYPE"));

                resourceDTO = new ResourceDTO();
                resourceDTO.setId(id);
                resourceDTO.setName(name);
                resourceDTO.setUri(uri);
                resourceDTO.setPid(pid);
                resourceDTO.setType(type);
                array.add(resourceDTO);
                tmpList = queryResource(operateUserId, id, 1, isAdminOperate);
                if (tmpList != null && tmpList.size() > 0) {
                    array.addAll(tmpList);
                }
            }
        }
        return array;
    }

}
