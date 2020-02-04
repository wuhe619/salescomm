package com.bdaim.label.dao;


import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.label.dto.CategoryType;
import com.bdaim.label.dto.QueryType;
import com.bdaim.label.entity.DataNode;
import com.bdaim.label.entity.LabelInfo;
import com.bdaim.rbac.dto.UserDTO;
import com.bdaim.util.SqlKeywordUtils;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据权限对外接口实现类
 */
@Component
public class LabelDao extends SimpleHibernateDao<LabelInfo, Serializable> {

    public LabelDao() {
    }

    //标签分类
    public List<DataNode> getLabelList(UserDTO user, DataNode root, Integer deep, QueryType type) {
        List<DataNode> lst = new ArrayList<DataNode>();
        try {
            switch (type) {
                case ALL:
                    lst = getLabelListAll(user, root, deep);
                    break;
                case PRIVILEGE:
                    lst = getLabelListPrivilege(user, root, deep);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return lst;
    }

    //品类权限  包括电商与媒体
    public List<DataNode> getCategoryList(UserDTO user, DataNode root, Integer deep, QueryType type, CategoryType categoryType) {
        List<DataNode> lst = new ArrayList<DataNode>();
        try {
            switch (type) {
                case ALL:
                    lst = getCategoryListAll(user, root, deep, type, categoryType);
                    break;
                case PRIVILEGE:
                    lst = getCategoryListPrivilege(user, root, deep, categoryType);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return lst;
    }

    //标签分类 全部权限（可包括用户权限）
    private List<DataNode> getLabelListAll(UserDTO user, DataNode root, Integer deep) {
        Map<Object, List<DataNode>> map = new HashMap<Object, List<DataNode>>();
        List<DataNode> lst = new ArrayList<DataNode>();
//		PreparedStatement st = null;
        List rs = new ArrayList();
        Object parentId = 0;
        try {
            if (user != null && user.getId() != null) {
                if (root != null && root.getId() != null) {
                    Boolean boo = validPermission(user, root, true);//检验节点是否有权限
                    if (!boo) {
                        return null;
                    }
                    Integer dp = getDeep(rs, deep, root, true);
                    String uri = getUri(root, true);
                    String sql = "select t1.id,t1.label_id,COALESCE(t1.parent_id,0) parent_id,t1.label_name,0 LABELID " +
                            " from label_info t1 " +
                            " where t1.LEVEL<=" + dp + " and t1.uri like '" + uri + "' " +
                            " order by t1.parent_id";
                    sql = SqlKeywordUtils.processKeyword(sql, "com.mysql.jdbc.Driver");
                    rs = this.sqlQuery(sql);

                } else {
                   /* String sql = "select t1.id,t1.label_id,COALESCE(t1.parent_id,0) parent_id,t1.label_name,COALESCE(t2.LABEL_ID,'0') as LABELID " +
                            "	  from label_info t1 " +
                            "     left join t_user_label_rel t2 on t1.id = t2.LABEL_ID and t2.user_id=" + user.getId() + " " +
                            "     where t1.LEVEL<=" + deep + " order by t1.parent_id";
                    sql = SqlKeywordUtils.processKeyword(sql, "com.mysql.jdbc.Driver");
                    rs = this.sqlQuery(sql);*/
                }
            }

            for (int i = 0; i < rs.size(); i++) {
                Map r = (Map) rs.get(i);
                DataNode dn = new DataNode();
                dn.setId(r.get("id"));
                dn.setName(String.valueOf(r.get("label_name")));
                dn.setLabelId(String.valueOf(r.get("label_id")));
                if (Integer.parseInt(String.valueOf(r.get("LABELID"))) == 0) {
                    dn.setChecked(false);
                } else {
                    dn.setChecked(true);
                }

                if (parentId.equals(r.get("parent_id"))) {
                    lst.add(dn);
                    if (i == rs.size() - 1) {
                        map.put(parentId, lst);
                    }
                } else {
                    map.put(parentId, lst);
                    parentId = r.get("parent_id");
                    lst = new ArrayList<DataNode>();
                    lst.add(dn);
                    if (i == rs.size() - 1) {
                        map.put(parentId, lst);
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return getTree(map, root);
    }

    //标签分类 只有用户权限
    private List<DataNode> getLabelListPrivilege(UserDTO user, DataNode root, Integer deep) {
        Map<Object, List<DataNode>> map = new HashMap<Object, List<DataNode>>();
        List<DataNode> lst = new ArrayList<DataNode>();
//		PreparedStatement st = null;
        List rs = new ArrayList();
        Object parentId = 0;
        try {
            if (user != null && user.getId() != null) {
                if (root != null && root.getId() != null && root.getId() != "") {
                    Boolean boo = validPermission(user, root, true);//检验节点是否有权限
                    if (!boo) {
                        return null;
                    }
                    Integer dp = getDeep(rs, deep, root, true);
                    String uri = getUri(root, true);
                    //rs = this.sqlQuery("select t2.id,t2.label_id,t2.label_name,COALESCE(t2.parent_id,0) parent_id from t_user_label_rel t1 left join label_info t2 on t1.label_id = t2.id and t1.user_id = " + user.getId() + " where t2.`LEVEL` <= " + dp + " and t2.uri like '" + uri + "' order by t2.parent_id");
                } else {
                    //rs = this.sqlQuery("select t2.id,t2.label_id,t2.label_name,COALESCE(t2.parent_id,0) parent_id from t_user_label_rel t1 left join label_info t2 on t1.label_id = t2.id and t1.user_id = " + user.getId() + " where t2.`LEVEL` <= " + deep + " order by t2.parent_id");
                }

                for (int i = 0; i < rs.size(); i++) {
                    Map r = (Map) rs.get(i);
                    DataNode dn = new DataNode();
                    dn.setId(r.get("id"));
                    dn.setName(String.valueOf(r.get("label_name")));
                    dn.setLabelId(String.valueOf(r.get("label_id")));
                    dn.setChecked(true);

                    if (parentId.equals(r.get("parent_id"))) {
                        lst.add(dn);
                        if (i == rs.size() - 1) {
                            map.put(parentId, lst);
                        }
                    } else {
                        map.put(parentId, lst);
                        parentId = r.get("parent_id");
                        lst = new ArrayList<DataNode>();
                        lst.add(dn);
                        if (i == rs.size() - 1) {
                            map.put(parentId, lst);
                        }
                    }

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getTree(map, root);
    }

    //所有电商（媒体）权限 （可包括用户权限）
    private List<DataNode> getCategoryListAll(UserDTO user, DataNode root, Integer deep, QueryType type, CategoryType categoryType) {
        Map<Object, List<DataNode>> map = new HashMap<Object, List<DataNode>>();
        List<DataNode> lst = new ArrayList<DataNode>();
        List rs = null;
        Object parentId = 0;
        try {
            if (user != null && user.getId() != null) {
                if (root != null && root.getId() != null) {
                    Integer dp = getDeep(rs, deep, root, false);
                    String uri = getUri(root, false);
                    String sql = "select t1.id,COALESCE(t1.parent_id,0) parent_id,t1.name,t1.id category_id from label_category t1  where t1.`LEVEL`<=? and t1.uri like ? and t1.type = ? order by t1.parent_id";
                    rs = jdbcTemplate.queryForList(sql,new Object[]{dp,"%"+uri+"%",categoryType.ordinal()});
//                    rs = this.sqlQuery(sql);

                } else {
                    String sql = "select t1.id,COALESCE(t1.parent_id,0) parent_id,t1.name,t1.id category_id from label_category t1 where t1.`LEVEL`<=" + deep + " and t1.type = " + categoryType.ordinal() + " order by t1.parent_id";
//                    rs = this.sqlQuery(sql);
                    rs = jdbcTemplate.queryForList(sql,new Object[]{deep,categoryType.ordinal()});
                }
            }
            for (int i = 0; i < rs.size(); i++) {
                Map r = (Map) rs.get(i);
                DataNode dn = new DataNode();
                dn.setId(r.get("id"));
                dn.setName(String.valueOf(r.get("name")));
                if (Integer.parseInt(String.valueOf(r.get("category_id"))) == 0) {
                    dn.setChecked(false);
                } else {
                    dn.setChecked(true);
                }

                if (parentId.equals(r.get("parent_id"))) {
                    lst.add(dn);
                    if (i == rs.size() - 1) {
                        map.put(parentId, lst);
                    }
                } else {
                    map.put(parentId, lst);
                    parentId = r.get("parent_id");
                    lst = new ArrayList<DataNode>();
                    lst.add(dn);
                    if (i == rs.size() - 1) {
                        map.put(parentId, lst);
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return getTree(map, root);
    }

    //电商与媒体 只包函用户权限
    private List<DataNode> getCategoryListPrivilege(UserDTO user, DataNode root, Integer deep, CategoryType categoryType) {
        Map<Object, List<DataNode>> map = new HashMap<Object, List<DataNode>>();
        List<DataNode> lst = new ArrayList<DataNode>();

        List rs = new ArrayList();
        Object parentId = 0;
        try {
            if (user != null && user.getId() != null) {
                if (root != null && root.getId() != null && root.getId() != "") {
                    Boolean boo = validPermission(user, root, false);//检验节点是否有权限
                    if (!boo) {
                        return null;
                    }
                    Integer dp = getDeep(rs, deep, root, false);
                    String uri = getUri(root, false);
                    //rs = this.sqlQuery("select t2.id,t2.name,COALESCE(t2.parent_id,0) parent_id from t_user_category_rel t1 left join label_category t2 on t1.category_id = t2.id and t1.user_id = " + user.getId() + " where t2.`LEVEL` <= " + dp + " and t2.uri like '" + uri + "' and t2.type = " + categoryType.ordinal() + " order by t2.parent_id");
                } else {
                    //rs = this.sqlQuery("select t2.id,t2.name,COALESCE(t2.parent_id,0) parent_id from t_user_category_rel t1 left join label_category t2 on t1.category_id = t2.id and t1.user_id = " + user.getId() + " where t2.`LEVEL` <= " + deep + " and t2.type = " + categoryType.ordinal() + " order by t2.parent_id");
                }
                for (int i = 0; i < rs.size(); i++) {
                    Map r = (Map) rs.get(i);
                    DataNode dn = new DataNode();
                    dn.setId(r.get("id"));
                    dn.setName(String.valueOf(r.get("name")));
                    dn.setChecked(true);

                    if (parentId.equals(r.get("parent_id"))) {
                        lst.add(dn);
                        if (i == rs.size() - 1) {
                            map.put(parentId, lst);
                        }
                    } else {
                        map.put(parentId, lst);
                        parentId = r.get("parent_id");
                        lst = new ArrayList<DataNode>();
                        lst.add(dn);
                        if (i == rs.size() - 1) {
                            map.put(parentId, lst);
                        }
                    }

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getTree(map, root);
    }

    //对权限生成树形结构
    private List<DataNode> getTree(Map<Object, List<DataNode>> map, DataNode dataNode) {
        for (Object obj : map.keySet()) {
            List<DataNode> lst = map.get(obj);
            for (int i = 0; i < lst.size(); i++) {
                DataNode dn = lst.get(i);
                if (map.get(dn.getId()) != null) {
                    map.get(obj).get(i).getChildren().addAll(map.get(dn.getId()));
                }
            }
        }
        if (dataNode != null && dataNode.getId() != null) {
            return map.get(Integer.parseInt(dataNode.getId().toString()));
        }
        return map.get(0);
    }

    private Integer getDeep(List rs, Integer deep, DataNode root, boolean isLabel) throws SQLException {
        if (isLabel) {
//            rs = this.sqlQuery("select id, `LEVEL` from label_info where id=" + root.getId());
            rs =  jdbcTemplate.queryForList("select id, `LEVEL` from label_info where id=?",new Object[]{root.getId()});
        } else {
//            rs = this.sqlQuery("select id, `LEVEL` from label_category where id=" + root.getId());
            rs = jdbcTemplate.queryForList("select id, `LEVEL` from label_category where id=?",new Object[]{root.getId()});
        }

        Integer dp = 0;
        for (int i = 0; i < rs.size(); i++) {
            Map r = (Map) rs.get(0);
            dp = Integer.parseInt(String.valueOf(r.get("LEVEL"))) + deep;
        }
        return dp;
    }

    private String getUri(DataNode root, boolean isLabel) throws SQLException {
        String ret = "";
        List rs = null;
        if (isLabel) {
//            rs = this.getSQLQuery("select id,uri from label_info where id=" + root.getId()).list();
            rs =  jdbcTemplate.queryForList("select id,uri from label_info where id=?",new Object[]{root.getId()});
        } else {
//            rs = this.getSQLQuery("select id,uri from label_category where id=" + root.getId()).list();
            rs =  jdbcTemplate.queryForList("select id,uri from label_category where id=?",new Object[]{root.getId()});
        }
        if (rs.size() > 0) {
            Object[] r = (Object[]) rs.get(0);
            ret = String.valueOf(r[1]) + String.valueOf(r[0]) + "/%";
        }
        return ret;
    }

    private Boolean validPermission(UserDTO user, DataNode root, boolean isLabel) throws SQLException {
        List rs = new ArrayList();
        if (isLabel) {
            //rs = this.getSQLQuery("select OPT_TIME from t_user_label_rel where USER_ID=" + user.getId() + " and LABEL_ID=" + root.getId()).list();
        } else {
            //rs = this.getSQLQuery("select OPT_TIME from t_user_category_rel where USER_ID=" + user.getId() + " and CATEGORY_ID=" + root.getId()).list();
        }
        if (rs.size() > 0) {
            return true;
        }
        System.out.println(root.getId() + " 该节点没有权限");
        return false;
    }

}
