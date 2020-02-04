package com.bdaim.rbac.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.helper.SQLHelper;
import com.bdaim.rbac.dto.*;
import com.bdaim.util.IDHelper;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Component
public class ResourceDao extends SimpleHibernateDao<Resource, Serializable> {

    public void insert(CommonTreeResource t) throws SQLException {
        this.executeUpdateSQL("insert into t_resource(ID,URI,NAME,TYPE,REMARK,PID,SN,OPTUSER,CREATE_TIME) values(" + IDHelper.getID() + ",'" + t.getUri() + "','" + t.getName() + "','" + t.getType() + "','" + t.getRemark() + "','" + t.getPid() == null ? "0" : t.getPid() + "','" + t.getSn() + "','" + t.getUser() + "', now())");

    }

    /**
     * 这个删除方法会删除 当前节点及所有的子节点的资源
     *
     * @param con
     * @param t
     * @throws SQLException
     */
    public void delete(CommonTreeResource t) throws SQLException {
        if (t.getID() == null) throw new NullPointerException("删除记录的ID不可为空");
        AbstractTreeResource delResources = queryAllTree(t, null);
        this.executeUpdateSQL("delete from t_resource where ID = ?", delResources.getID());
        this.executeUpdateSQL("delete from t_mrp_rel where R_ID= ? ", delResources.getID());
    }

    private void delNote(AbstractTreeResource tree, Statement st) throws SQLException {
        if (tree.getNotes() != null && tree.getNotes().size() > 0) {
            for (AbstractTreeResource item : tree.getNotes()) {
                this.executeUpdateSQL("delete from t_resource where ID= ? ", item.getID());
                this.executeUpdateSQL("delete from t_mrp_rel where R_ID= ?", item.getID());
                delNote(item, st);
            }
        }
    }

    public void update(CommonTreeResource t) {
        if (t.getID() == null) throw new NullPointerException("更新记录的ID不可为空");
        StringBuffer sb = new StringBuffer();
        sb.append("update t_resource set MODIFY_TIME=now(),");
        List<Object> p = new ArrayList<>();
        if (t.getRemark() != null && !t.getRemark().equals("")) {
            p.add(t.getRemark());
            sb.append("REMARK=?,");
        }
        if (t.getUser() != null && !t.getUser().equals("")) {
            p.add(t.getUser());
            sb.append("OPTUSER=?,");
        }
        if (t.getPid() != null) {
            p.add(t.getPid());
            sb.append("PID=?,");
        }
        if (t.getName() != null && !t.getName().equals("")) {
            p.add(t.getName());
            sb.append("NAME=?,");
        }
        if (t.getSn() != null) {
            p.add(t.getSn());
            sb.append("sn=?,");
        }
        if (t.getType() != null) {
            p.add(t.getType());
            sb.append("type=?,");
        }
        sb.append(" where ID=? ");
        p.add(t.getID());
        //确认SQL，绑定参数
        this.executeUpdateSQL(sb.toString(), p.toArray());
    }


    public CommonTreeResource getObj(CommonTreeResource r) {
        try {
            List list = null;
            if (r.getUri() != null && !"".equals(r.getUri()))
                list = this.getSQLQuery("select ID,URI,NAME,TYPE,REMARK,PID,SN,OPTUSER,CREATE_TIME,MODIFY_TIME from t_resource where URI='" + r.getUri() + "'").list();
            else
                list = this.getSQLQuery("select ID,URI,NAME,TYPE,REMARK,PID,SN,OPTUSER,CREATE_TIME,MODIFY_TIME from t_resource where ID=" + r.getID()).list();
            if (list != null && list.size() > 0) {
                List notes = this.getSQLQuery("select ID,URI,NAME,TYPE,REMARK,PID,SN,OPTUSER,CREATE_TIME,MODIFY_TIME from t_resource where PID=" + r.getID()).list();
                List<AbstractTreeResource> aa = new ArrayList<AbstractTreeResource>();

//                List<CommonTreeResource> rsList=CommonTreeResource.pop(rs);
//                if (rsList!=null)aa.addAll(rsList);

                CommonTreeResource ctr = new CommonTreeResource();
                Object[] obj = (Object[]) list.get(0);
                ctr.setNotes(aa);

                return ctr;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return null;
    }

    public boolean hasPermissionByUser(Resource r, UserDTO u) {
        if (r.getID() == null || r.getID() == 0) throw new NullPointerException("资源的ID不可为空");
        if (u.getKey() == null || u.getKey().equals("")) throw new NullPointerException("用户信息为空");
        StringBuffer sql = new StringBuffer();
        sql.append("select count(*) as amount from t_resource r,t_mrp_rel rel,t_role o where r.id=rel.R_ID and rel.ROLE_ID=o.ID" +
                "where o.ID=? and r.ID=? ");
        try {
            List list = this.sqlQuery(sql.toString(), r.getID(), u.getKey());
            if (list.size() > 0 && Long.parseLong((String.valueOf(list.get(0)))) > 0)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return false;
    }

    public boolean hasPermissionByRole(Resource r, RoleDTO role) {
        if (r.getID() == null || r.getID() == 0) throw new NullPointerException("资源的ID不可为空");
        if (role.getKey() == null || role.getKey() == 0) throw new NullPointerException("角色信息不可为空");
        StringBuffer sql = new StringBuffer();
        sql.append("select count(*) as amount from t_mrp_rel rel o where rel.R_ID=? and type=0 and rel.ROLE_ID=?");
        try {
            List list = this.sqlQuery(sql.toString(), r.getID(), role.getKey());
            if (list.size() > 0 && Long.parseLong(String.valueOf(list.get(0))) > 0) return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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

    public AbstractTreeResource queryUserTree(UserDTO user, AbstractTreeResource treeResource, String[] types) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT DISTINCT r.ID,r.URI,r.NAME,r.TYPE,r.REMARK,r.PID,r.SN,r.OPTUSER,r.CREATE_TIME,r.MODIFY_TIME" +
                " FROM t_user_role_rel u,t_mrp_rel m,t_resource r where r.PID=? and u.ID=" + user.getKey() + " and m.ROLE_ID=u.ROLE and m.R_ID=r.ID and m.type=0");
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
                " FROM t_mrp_rel m,t_resource r where r.PID=? and m.ROLE_ID=" + role.getKey() + " and m.ROLE_ID=u.ROLE and m.type=0");
        if (types != null && types.length > 0) {
            sb.append(" and r.TYPE in (" + SQLHelper.getInSQL(types) + ")");
        }
        sb.append(" order by r.SN asc");
        AbstractTreeResource returnRS = null;
        try {
            List<Object> params = new ArrayList<Object>();
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
            List rs = this.sqlQuery(sql.replace("?", String.valueOf(id)));
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

    /**
     * 根据平台查询所有资源
     *
     * @param platform
     * @return
     */
    public List<Resource> listAllResource(int platform) {
        StringBuffer hql = new StringBuffer();
        hql.append(" FROM ResourceDO m WHERE m.platform=?");
        return this.find(hql.toString(), platform);
    }

    public List<Map<String, Object>> listAllResource(long pid, long operateUserId, int platform, boolean isAdminOperate) {
        StringBuilder builder = new StringBuilder();
        if (isAdminOperate) {
            builder.append(" select distinct r.ID,r.NAME,r.PID,r.URI,r.TYPE from t_resource r");
            builder.append(" where r.pid=");
            builder.append(pid);
            builder.append(" and PLATFORM=?");
        } else {
            builder.append(" select distinct r.ID,r.NAME,r.PID,r.URI,r.TYPE from t_mrp_rel m ");
            builder.append(" inner join t_user_role_rel ur on ur.ROLE = m.ROLE_ID");
            builder.append(" inner join t_resource r on m.R_ID = r.ID and ur.ID =");
            builder.append(operateUserId);
            builder.append(" and r.pid = ");
            builder.append(pid);
            builder.append(" and m.type=0 and PLATFORM=?");
        }
        String sql = builder.toString();
        List<Map<String, Object>> tmpList = this.sqlQuery(sql, platform);
        return tmpList;
    }

}
