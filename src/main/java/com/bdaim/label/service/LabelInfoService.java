package com.bdaim.label.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerLabelDao;
import com.bdaim.label.dao.IndustryLabelDao;
import com.bdaim.label.dao.LabelCoverDao;
import com.bdaim.label.dao.LabelDao;
import com.bdaim.label.dao.LabelInfoDao;
import com.bdaim.label.dto.CategoryType;
import com.bdaim.label.dto.Label;
import com.bdaim.label.dto.QueryType;
import com.bdaim.label.entity.*;
import com.bdaim.rbac.dto.UserDTO;
import com.bdaim.rbac.service.UserService;
import com.bdaim.util.Constant;
import com.bdaim.util.StringHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("labelInfoService")
@Transactional
public class LabelInfoService {
    private static Log log = LogFactory.getLog(LabelInfoService.class);
    @Resource
    private LabelInfoDao labelInfoDao;
    @Resource
    private UserService userService;
    @Resource
    private LabelCategoryService labelCategoryServiceImpl;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private LabelCoverDao labelCoverDao;
    @Resource
    private LabelInterfaceService labelInterfaceService;
    @Resource
    private LabelDao labelDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private CustomerLabelDao customerLabelDao;
    @Resource
    private CommonService commonService;
    @Resource
    private IndustryLabelDao industryLabelDao;

    public <X> List<X> query(String hql, Object... values) {
        return labelDao.find(hql, values);
    }

    public LabelInfo get(Serializable id) {
        return this.labelDao.get(id);
    }

    /**
     * *****  2、获取指定节点的子节点  *****
     */
    public List<Label> children(String poolId, Integer id, String status, Integer cycle,
                                String queryType, String queryKey, Integer type, Integer categoryFlag, Boolean isLogAvailably) {
        List<Label> labels = new ArrayList<Label>();
        LabelInfo labelInfo = labelInfoDao.get(id);
        if (labelInfo == null)
            return labels;

        List<LabelInfo> labelInfos = null;
        if (null == labelInfo.getDataFormat() || labelInfo.getDataFormat().equals(Constant.DATAFORMAT_COMMON) || type == null) { //标签
            StringBuffer hql = new StringBuffer("from LabelInfo t where  ( t.availably=1  ");
            hql.append(" and t.level=3 )");
            if (labelInfo.getLevel() == 2)
                hql.append(" and t.parentId=").append(id);  //通过uri快速检索所有子标签
            else
                hql.append(" and t.id=").append(id);

            hql.append(" and t.status=3"); //审批通过的标签

            hql.append(" order by level, label_name");
            labelInfos = labelInfoDao.find(hql.toString());

        } else {
            if (type.equals(Constant.QUERY_TYPE_CATEGORY)) { //type=0:品类

            } else if (type.equals(Constant.QUERY_TYPE_BRAND)) { //type=1:品牌
            } else if (type.equals(Constant.QUERY_TYPE_ATTR)) {//type=2:属性
            }
        }

        if (labelInfos == null)
            return labels;

        for (LabelInfo li : labelInfos) {
            Label label = new Label(li);
            List<LabelInfo> LabelInfoChildren = li.getChildren();
            if (LabelInfoChildren != null) {
                for (LabelInfo lic : LabelInfoChildren) {
                    Label labelChild = new Label(lic);
                    label.addChild(labelChild);
                }
            }
            labels.add(label);
        }

        return labels;
    }

    public List<Label> childrenV1(String poolId, Integer id, String status, Integer cycle,
                                  String queryType, String queryKey, Integer type, Integer categoryFlag, Boolean isLogAvailably) {
        List<Label> labels = new ArrayList<>();
        LabelInfo labelInfo = labelInfoDao.get(id);
        if (labelInfo == null) {
            return labels;
        }

        List<IndustryPoolLabel> ipl = industryLabelDao.list(Integer.parseInt(poolId));
        Set<String> industryPoolLabelIds = new HashSet<>();
        for (IndustryPoolLabel l : ipl) {
            industryPoolLabelIds.add(l.getLabelId());
        }

        List<LabelInfo> labelInfos = null;
        if (null == labelInfo.getDataFormat() || labelInfo.getDataFormat().equals(Constant.DATAFORMAT_COMMON) || type == null) { //标签
            StringBuffer hql = new StringBuffer("from LabelInfo t where  ( t.availably=1  ");
            hql.append(" and t.level=3 )");
            if (labelInfo.getLevel() == 2)
                hql.append(" and t.parentId=").append(id);  //通过uri快速检索所有子标签
            else
                hql.append(" and t.id=").append(id);

            hql.append(" and t.status=3"); //审批通过的标签

            hql.append(" order by level, label_name");
            labelInfos = labelInfoDao.find(hql.toString());

        } else {
            if (type.equals(Constant.QUERY_TYPE_CATEGORY)) { //type=0:品类

            } else if (type.equals(Constant.QUERY_TYPE_BRAND)) { //type=1:品牌
            } else if (type.equals(Constant.QUERY_TYPE_ATTR)) {//type=2:属性
            }
        }

        if (labelInfos == null)
            return labels;

        for (LabelInfo li : labelInfos) {
            if (industryPoolLabelIds.contains(String.valueOf(li.getId()))) {
                Label label = new Label(li);
                List<LabelInfo> LabelInfoChildren = li.getChildren();
                if (LabelInfoChildren != null) {
                    for (LabelInfo lic : LabelInfoChildren) {
                        Label labelChild = new Label(lic);
                        label.addChild(labelChild);
                    }
                }
                labels.add(label);
            }
        }

        return labels;
    }

    /***
     * 3.获取标签值
     */
    public List<Label> values(Integer id, String status) {
        StringBuffer hql = new StringBuffer("from LabelInfo t where t.availably=1 ");
        hql.append(" and t.level=4 ");

        hql.append(" and t.parentId=").append(id);  //通过uri快速检索所有子标签
        hql.append(" and t.status=3"); //审批通过的标签

        hql.append(" order by level, label_name");
        List<LabelInfo> labelInfos = labelInfoDao.find(hql.toString());

        List<Label> labels = new ArrayList();
        for (LabelInfo labelInfo : labelInfos) {
            Label label = new Label(labelInfo);
            labels.add(label);
        }
        return labels;
    }


    /**
     * 根据条件查询标签信息
     *
     * @param map     精确查询的条件集合
     * @param likeMap 模糊查询的条件集合
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<LabelInfo> getLabelsByCondition(Map<String, ?> map, Map<String, ?> likeMap) {
        if (null == map || null == likeMap)
            throw new NullPointerException("查询条件不允许为空");
        String hql = "From LabelInfo t where availably =1 and type>1 and 1 = 1";
        List<LabelInfo> labelList = (List<LabelInfo>) labelInfoDao.getHqlQuery(hql, map,
                likeMap, "orderNum").list();
        return labelList;
    }

    @SuppressWarnings("unchecked")
    public List<LabelInfo> getLabelByCondition(Map<String, ?> map, Map<String, ?> likeMap) {
        if (map == null)
            map = new HashMap<String, Object>();
        if (likeMap == null)
            likeMap = new HashMap<String, Object>();
        String hql = "From LabelInfo t where t.availably=1 and type>1 and 1 = 1";
        List<LabelInfo> labelList = labelInfoDao.getHqlQuery(hql, map, likeMap,
                null).list();
        return labelList;
    }

    public LabelInfo getLabelById(Integer id) {
        return labelInfoDao.get(id);
    }

    /**
     * 标签信息的模糊搜索 根据标签名称 、规则、部门等条件 返回结果结合[{},{}]
     */
    public List<Map<String, Object>> getLabels(Map<String, Object> map, Map<String, Object> likeMap) {
        List<LabelInfo> list = getLabelsByCondition(map, likeMap);
        List<Map<String, Object>> resultList = getLabelTree(list);
        return resultList;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTreeById(Integer id) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        List<LabelInfo> list = labelInfoDao.createQuery("from LabelInfo where availably=1 and parent.id=?", id).list();
        if (list.size() == 0)
            return result;
        else {
            //将查询出来的标签列表转化成相应的字典结构
            result = commonService.getLabelMapList(list);
        }
        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<Map<String, Object>> getTreeDataByMap(Map<String, Object> map,
                                                      Map<String, Object> orLikeMap, Map<String, Object> andLikeMap) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Integer firstLid = Integer.valueOf(map.get(
                Constant.FILTER_KEY_PREFIX + "id").toString());
        LabelInfo firstLabel = getLabelById(firstLid);
        //获取登录用户信息
        List<DataNode> nodeList = new ArrayList<DataNode>();

        UserDTO user = (UserDTO) map.get(Constant.FILTER_KEY_PREFIX + "user");
        String hql = "";

        if (null == firstLabel.getDataFormat()
                || firstLabel.getDataFormat().equals(-1)) {
            //处理普通数据类型的情况
            hql = "from LabelInfo t where attr_id is null and level<4 and availably =1  and  (( uri like '/"
                    + firstLid + "/%' )) ";
            List<LabelInfo> list = labelInfoDao.getHqlQuery(hql, map, orLikeMap, andLikeMap,
                    "level asc").list();
            if (null != list) {
                result = getLabelTree(list);
                return result;
            } else {
                return result;
            }

        } else {
            if (firstLabel.getDataFormat().equals(Constant.DATAFORMAT_EB)) {// 商品品类
                //查询有权限的电商品类信息
                nodeList = labelDao.getCategoryList(user, null, 10, QueryType.ALL, CategoryType.PRODUCT);
            } else if (firstLabel.getDataFormat().equals(Constant.DATAFORMAT_MC)) {// 媒体品类
                //查询有权限的媒体品类信息
                nodeList = labelDao.getCategoryList(user, null, 10, QueryType.ALL, CategoryType.MEDIA);
            }

            result = CommonService.getDataNodeMapList(nodeList);
            return result;
        }
    }

    /**
     * 获取品类类型的数据
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Map<String, Object>> getChildrenByCategory(List<DataNode> nodes, List<Integer> cateList, LabelInfo label) {
        for (DataNode node : nodes) {
            List<DataNode> children = node.getChildren();
            if (null != children) {
                for (DataNode n : children) {
                    cateList.add(Integer.valueOf(n.getId().toString()));
                }
            }
            cateList.add(Integer.valueOf(node.getId().toString()));
        }
        List<Map<String, Object>> categoryTree = CommonService.getDataNodeMapList(nodes);
        //查询指定一级分类下对应品类的覆盖信息
        if (cateList.size() > 0) {
            List<Map<String, Object>> covers = labelCoverDao
                    .createQuery(
                            "select new map(label.id as labelId,category.id as categoryId,coverNum as num,total as total ) from LabelCover where label.id=(:label) and category.id in(:cateList)")
                    .setParameter("label", label.getId())
                    .setParameterList("cateList", cateList).list();

            Map<String, String> coverMap = new HashMap<String, String>();
            for (Map<String, Object> map : covers) {
                String labelID = map.get("labelId") == null ? null : map.get(
                        "labelId").toString();
                String categoryID = map.get("categoryId") == null ? null : map.get(
                        "categoryId").toString();
                String num = map.get("num") == null ? "0" : map.get("num")
                        .toString();
                String total = map.get("total") == null ? "0" : map.get("total")
                        .toString();
                if (null == labelID)
                    continue;
                if (null == categoryID) {
                    coverMap.put(labelID, num + "_" + total);
                } else {
                    coverMap.put(labelID + "_" + categoryID, num + "_" + total);
                }
            }
            for (Map<String, Object> map : categoryTree) {
                List<Map<String, Object>> children = (List<Map<String, Object>>) map.get("children");
                // 待删除children
                List<Map<String, Object>> delete = new ArrayList<Map<String, Object>>();
                String cid = map.get("categoryId") == null ? null : map.get("categoryId").toString();

                String key = label.getId() + "_" + cid;
                String value = coverMap.get(key);
                if (null != value) {
                    String[] vArr = value.split("_");
                    map.put("customerNum", vArr[0]);
                    map.put("total", vArr[1]);
                    map.put("viewStatus", Constant.VIEW_STATUS_ONLINE);
                    map.put("statusCn", Constant.ONLINE_CN);
                } else {
                    map.put("customerNum", 0);
                    map.put("total", 0);
                    map.put("viewStatus", Constant.VIEW_STATUS_ONLINE);
                    map.put("statusCn", Constant.ONLINE_CN);
                }
                if (null != children) {
                    for (Map<String, Object> m : children) {
                        String c = m.get("categoryId").toString();
                        String k = label.getId() + "_" + c;
                        String v = coverMap.get(k);
                        if (null != v) {
                            String[] vArr = v.split("_");
                            m.put("customerNum", vArr[0]);
                            m.put("total", vArr[1]);
                            m.put("viewStatus", Constant.VIEW_STATUS_ONLINE);
                            m.put("statusCn", Constant.ONLINE_CN);
                        } else {
                            m.put("customerNum", 0);
                            m.put("total", 0);
                            m.put("viewStatus", Constant.VIEW_STATUS_ONLINE);
                            m.put("statusCn", Constant.ONLINE_CN);
                        }
                    }
                }
            }
        }
        return categoryTree;
    }

    /**
     * 获取标签覆盖信息
     */
    @SuppressWarnings("unchecked")
    private List<LabelInfo> getLabelCoverInfo(List<LabelInfo> labels, List<Map<String, Object>> covers, int cycle) {
        List<Integer> lids = new ArrayList<Integer>();
        Map<Integer, LabelInfo> labelMap = new HashMap<Integer, LabelInfo>();
        for (LabelInfo lab : labels) {
            List<LabelInfo> children = lab.getChildren();
            if (null != children) {
                for (LabelInfo child : children) {
                    lids.add(child.getId());
                }
            }
            lids.add(lab.getId());
            labelMap.put(lab.getId(), lab);
        }
        //根据标签id和标签周期查询标签覆盖用户数
        String queryHQL = "select new map(label.id as labelId,category.id as categoryId,coverNum as num,total as total )  from LabelCover t where t.cycle=:cycle and t.label.id in(:lids)";
        if (lids.size() > 0)
            covers = labelCoverDao.createQuery(queryHQL).setParameterList("lids", lids).setParameter("cycle", cycle).list();
        List<LabelInfo> labs = new ArrayList<LabelInfo>();
        for (Map<String, Object> cover : covers) {
            if (labelMap.containsKey(cover.get("labelId")))
                labs.add(labelMap.get(cover.get("labelId")));
        }
        return labs;
    }

    private List<Map<String, Object>> getLbelCovers(List<Integer> lids, Integer cycle) {
        String queryHQL = "select new map(label.id as labelId,category.id as categoryId,coverNum as num,total as total )  from LabelCover t where t.cycle=:cycle and t.label.id in(:lids)";
        return labelCoverDao.createQuery(queryHQL).setParameterList("lids", lids).setParameter("cycle", cycle).list();
    }


    private void addCoverNum(List<Map<String, Object>> tree,
                             List<Map<String, Object>> covers, String labelId) {
        Map<String, String> coverMap = new HashMap<String, String>();
        for (Map<String, Object> map : covers) {
            String labelID = map.get("labelId") == null ? null : map.get("labelId").toString();
            String categoryID = map.get("categoryId") == null ? null : map.get("categoryId").toString();
            String num = map.get("num") == null ? "0" : map.get("num").toString();
            String total = map.get("total") == null ? "0" : map.get("total").toString();
            if (null == labelID)
                continue;
            if (null == categoryID) {
                coverMap.put(labelID, num + "_" + total);
            } else {
                coverMap.put(labelID + "_" + categoryID, num + "_" + total);
            }
        }
        for (Map<String, Object> map : tree) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) map.get("children");
            // 待删除children
            List<Map<String, Object>> delete = new ArrayList<Map<String, Object>>();
            String cid = map.get("categoryId") == null ? null : map.get("categoryId").toString();
            if (null == cid) {
                String key = map.get("id").toString();
                String value = coverMap.get(key);
                if (null != value) {
                    String[] vArr = value.split("_");
                    map.put("customerNum", vArr[0]);
                    map.put("total", vArr[1]);
                }
                if (null != children) {
                    for (Map<String, Object> m : children) {
                        String k = m.get("id").toString();
                        String v = coverMap.get(k);
                        if (null != v) {
                            String[] vArr = v.split("_");
                            m.put("customerNum", vArr[0]);
                            m.put("total", vArr[1]);
                        }
                    }
                }
            } else {
                String key = labelId + "_" + cid;
                String value = coverMap.get(key);
                if (null != value) {
                    String[] vArr = value.split("_");
                    map.put("customerNum", vArr[0]);
                    map.put("total", vArr[1]);
                    map.put("viewStatus", Constant.VIEW_STATUS_ONLINE);
                    map.put("statusCn", Constant.ONLINE_CN);
                }
                if (null != children) {
                    for (Map<String, Object> m : children) {
                        String c = m.get("categoryId").toString();
                        String k = labelId + "_" + c;
                        String v = coverMap.get(k);
                        if (null != v) {
                            String[] vArr = v.split("_");
                            m.put("customerNum", vArr[0]);
                            m.put("total", vArr[1]);
                            m.put("viewStatus", Constant.VIEW_STATUS_ONLINE);
                            m.put("statusCn", Constant.ONLINE_CN);
                        } else {
                            delete.add(m);
                        }
                    }
                    children.removeAll(delete);
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTreeByLevel(Integer pid, Integer level) {
        List<LabelInfo> list = labelInfoDao.createQuery("from LabelInfo where availably=1 and status=3 and level<=?", level).list();
        List<Map<String, Object>> result = getLabelTree(list);
        return result;
    }

    public List<Map<String, Object>> getChildrenByIdAndLevel(Integer pid, Integer level) {
        return getTreeByLevel(pid, level);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAllLabelsByCategoryId(Integer categoryId) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> list = labelInfoDao.createQuery("select new map(id as id,labelName) from LabelInfo where availably =1 and parentCategory.id=?", categoryId).list();
        for (Map<String, Object> m : list) {
            m.put("children", getChildrenById(Integer.parseInt(m.get("id").toString())));
            result.add(m);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<LabelInfo> getAllLabels() {
        List<LabelInfo> labels = labelInfoDao.createQuery("from LabelInfo where availably=1").list();
        return labels;
    }


    public List<Map<String, Object>> getChildrenById(Integer pid) {
        return getTreeById(pid);
    }

    public void updateLabelInfo(LabelInfo label) {
        labelInfoDao.update(label);
    }

    public List<Map<String, Object>> getLabelsByCondition(Map<String, Object> map) {
        // return getTreeByLevel(map);
        return getLabelMenuByCondition(map);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Map<String, Object>> getLabelMenuByCondition(
            Map<String, Object> condition) {
        List<Map<String, Object>> result;
        List<Map<String, Object>> cateList = new ArrayList<Map<String, Object>>();

        UserDTO user = (UserDTO) condition.get(Constant.FILTER_KEY_PREFIX + "user");
        List<DataNode> labelNodes = labelDao.getLabelList(user, null, 1, QueryType.ALL);
        Integer firstLid = null;
        List<Integer> lids = new ArrayList<Integer>();
        if (null != labelNodes && labelNodes.size() > 0) {
            for (DataNode n : labelNodes) {
                Integer nodeId = Integer.valueOf(n.getId().toString());
                if (firstLid == null || firstLid > nodeId) {
                    firstLid = nodeId;
                }
                lids.add(nodeId);
            }
            for (DataNode n : labelNodes) {
                if (n.getName().equals("组合标签")) {
                    firstLid = Integer.valueOf(n.getId().toString());
                    break;
                }
            }
        } else {
            throw new RuntimeException("该用户没有任何标签权限!");
        }
        LabelInfo firstLabel = getLabelById(firstLid);
        List<DataNode> categoryNodes = new ArrayList<DataNode>();
        String hql = "";
        hql = "from LabelInfo where attr_id is null and level<4 and availably =1  and  id in(:lids) ";
        if (null == firstLabel.getDataFormat()) {
            hql = "from LabelInfo where attr_id is null and level<4 and availably =1  and (( uri like '/"
                    + firstLid + "/%' )  or id in(:lids) )";
        } else {
            if (firstLabel.getDataFormat().equals(Constant.DATAFORMAT_EB)) {// 商品品类
                categoryNodes = labelDao.getCategoryList(user, null, 10, QueryType.ALL, CategoryType.PRODUCT);
            } else if (firstLabel.getDataFormat().equals(Constant.DATAFORMAT_MC)) {// 媒体品类
                categoryNodes = labelDao.getCategoryList(user, null, 10, QueryType.ALL, CategoryType.MEDIA);
            }
            cateList = CommonService.getDataNodeMapList(categoryNodes);
        }
        if (condition.containsKey("status")) {
            hql += " and status=" + condition.get("status");
        }
        if (condition.containsKey("createUid")) {
            hql += " and (labelCreateUser.id=" + condition.get("createUid") + " or level<3)";
        }
        hql += " order by level asc";
        List<LabelInfo> list = labelInfoDao.createQuery(hql, condition).setParameterList("lids", lids).list();
        if (list.size() == 0) {
            return new ArrayList<Map<String, Object>>();
        } else {
            result = getLabelTree(list);
        }
        Map<String, Object> firstMap = new TreeMap<String, Object>();
        for (Map<String, Object> data : result) {
            Integer id = Integer.valueOf(data.get("id").toString());
            if (id.equals(firstLid)) {
                firstMap = data;
                result.remove(data);
                break;
            }
        }
        if (!cateList.isEmpty()) {
            firstMap.put("category", cateList);
        }
        result.add(0, firstMap);
        return result;

    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTreeByLevel(
            Map<String, Object> condition) {
        List<Map<String, Object>> result;
        if (condition.containsKey("pid")) {
            condition.remove("pid");
        }
        // String hql =
        String hql = "from LabelInfo where availably =1  and \"LEVEL\"<=:level";
        if (condition.containsKey("cate")) {
            hql = "from LabelInfo where  availably =1  and parentCategory=:cate and \"LEVEL\"<=:level";
        }
        if (condition.containsKey("status")) {
            hql += " and status=:status";
        }
        if (condition.containsKey("createUid")) {
            hql += " and labelCreateUser.id=:createUid";
        }
        List<LabelInfo> list = labelInfoDao.createQuery(hql, condition).list();
        if (list.size() == 0) {
            return new ArrayList<Map<String, Object>>();
        } else {
            result = getLabelTree(list);
        }
        return result;
    }

    public LabelInfo getLabelInfoByParentAndName(LabelInfo parent, String name) {
        Criteria c = labelInfoDao.createCriteria(Restrictions.eq("parent",
                parent));
        c.add(Restrictions.eq("labelName", name));
        LabelInfo label = null;
        List<LabelInfo> list = (List<LabelInfo>) c.list();
        if (list.size() > 0)
            label = list.get(0);
        return label;
    }

    public LabelInfo getLabelInfoByLabelId(String labelId) {
        Criteria c = labelInfoDao.createCriteria(Restrictions.eq("labelId",
                labelId));
        LabelInfo label = null;
        List<LabelInfo> list = (List<LabelInfo>) c.list();
        if (list.size() > 0)
            label = list.get(0);
        return label;
    }

    public List<LabelInfo> getChildrenByLabelId(String labelId) {
        LabelInfo pLabel = getLabelInfoByLabelId(labelId);
        Criteria c = labelInfoDao.createCriteria(Restrictions.eq("parent",
                pLabel));
        return c.list();
    }

    public List<String> getChildrenNameByPid(Integer id) {
        Criteria c = labelInfoDao.createCriteria(Restrictions.eq("parent.id",
                id));
        c.setProjection(Projections.property("labelName"));
        return c.list();
    }

    public List<Map<String, Object>> getAllLabelByCategoryId(Integer categoryId) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        List<LabelInfo> list = labelInfoDao.createQuery(
                "from LabelInfo where availably=1 and parentCategory.id=?",
                categoryId).list();
        for (LabelInfo label : list) {
            Map<String, Object> m = commonService.getLabelMap(label);
            if (m.get("id") != null)
                m.put("children", getChildrenById(Integer.parseInt(m.get("id")
                        .toString())));
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> getLabelsByLevel(Integer type,
                                                      Integer level) {
        if (type == 2 && level < 3) {
            return labelInfoDao
                    .createQuery(
                            "select new map(labelId as labelId,type as type) from LabelInfo where availably=1 and parentCategory is null  and type=? and level=? and labelId is not null ",
                            1, level).list();
        }
        if (type == 3 && level == 2) {
            return labelInfoDao
                    .createQuery(
                            "select new map(labelId as labelId,type as type) from LabelInfo where availably=1 and parent.labelId='20001'  and type=? and level=? and labelId is not null ",
                            1, level).list();
        }
        if (type != 4) {
            return labelInfoDao
                    .createQuery(
                            "select new map(labelId as labelId,type as type) from LabelInfo where availably=1 and parentCategory is null  and type=? and level=? and labelId is not null ",
                            type, level).list();
        } else {
            List<Map<String, Object>> list = labelInfoDao
                    .createQuery(
                            "select new map(labelId as labelId,type as type,parentCategory.categoryId as categoryId) from LabelInfo where availably=1 and type=2 and level=? and labelId is not null ",
                            level).list();
            Set<Map<String, Object>> set = new HashSet<Map<String, Object>>();
            for (Map<String, Object> m : list) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("labelId",
                        m.get("labelId")
                                .toString()
                                .replace(m.get("categoryId").toString(),
                                        "00000"));
                map.put("type", type);
                set.add(map);
            }
            return new ArrayList<Map<String, Object>>(set);
        }
    }

    public Map<String, Object> previewSignatureLabel(Integer cycle, LabelInfo label) {
        String str = labelInterfaceService.previewSignatureLabel(label, cycle);
        JSONObject json = JSONObject.parseObject(str);
        if (json.getInteger("isSuccess") == 1) {
            JSONObject result = json.getJSONObject("data");
            Map<String, Object> map = new HashMap<String, Object>();
            map.putAll(result);
            return map;
        } else
            return null;
    }

    public boolean isExistLabelName(Integer pid, String labelName) {
        String hql = "from LabelInfo t where availably =1 and t.parent.id=? and t.labelName=?";
        try {
            LabelInfo info = labelInfoDao.findUnique(hql, pid, labelName);
            if (null == info)
                return true;
        } catch (Exception e) {
            throw new RuntimeException("校验标签名称是否存在出现错误");
        }
        return false;
    }

    /**
     * 根据labelinfo的list生成树
     *
     * @param labels
     * @return
     */
    public List<Map<String, Object>> getLabelTree(List<LabelInfo> labels) {
        List<Map<String, Object>> mapList = new LinkedList<Map<String, Object>>();
        Map<Integer, Map<Integer, LabelInfo>> levelMap = new TreeMap<Integer, Map<Integer, LabelInfo>>();
        for (LabelInfo label : labels) {
            Integer level = label.getLevel();
            if (null == level)
                continue;
            label.setChildren(new ArrayList<LabelInfo>());
            Integer lid = label.getId();
            if (levelMap.containsKey(level)) {
                Map<Integer, LabelInfo> map = levelMap.get(level);
                map.put(lid, label);
            } else {
                Map<Integer, LabelInfo> map = new TreeMap<Integer, LabelInfo>();
                map.put(lid, label);
                levelMap.put(level, map);
                String[] uriArray = label.getUri().split("/");
                for (String id : uriArray) {
                    if (id.matches("[0-9]+")) {
                        try {
                            LabelInfo parentLabel = getLabelById(Integer
                                    .valueOf(id));
                            Map<Integer, LabelInfo> parentMap = levelMap.get(parentLabel.getLevel());
                            parentLabel.setChildren(new ArrayList<LabelInfo>());
                            parentMap.put(parentLabel.getId(), parentLabel);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if (!levelMap.isEmpty()) {
            Integer[] levels = new Integer[levelMap.size()];
            levelMap.keySet().toArray(levels);
            for (int i = 0; i < levels.length; i++) {
                if (i == levels.length - 1) {
                    Map<Integer, LabelInfo> map = levelMap
                            .get(levels[levels.length - i - 1]);
                    for (Integer id : map.keySet()) {
                        LabelInfo label = map.get(id);
                        Map<String, Object> dataMap = CommonService
                                .getSimpleLabelMap(label);
                        mapList.add(dataMap);
                    }
                } else {
                    Map<Integer, LabelInfo> map = levelMap
                            .get(levels[levels.length - i - 1]);
                    Map<Integer, LabelInfo> parentMap = levelMap
                            .get(levels[levels.length - i - 2]);
                    Map<Integer, List<LabelInfo>> childrenMap = new TreeMap<Integer, List<LabelInfo>>();
                    for (LabelInfo label : map.values()) {
                        Integer pid = label.getParent().getId();
                        if (childrenMap.containsKey(pid)) {
                            List<LabelInfo> childrenList = childrenMap.get(pid);
                            childrenList.add(label);
                        } else {
                            List<LabelInfo> childrenList = new ArrayList<LabelInfo>();
                            childrenList.add(label);
                            childrenMap.put(pid, childrenList);
                        }

                    }
                    final String reg = "^.*\\d+.*$";
                    for (Integer _key : childrenMap.keySet()) {
                        List<LabelInfo> _list = childrenMap.get(_key);
                        Collections.sort(_list, new Comparator<LabelInfo>() {

                            @Override
                            public int compare(LabelInfo o1, LabelInfo o2) {
                                if (o1.getLevel() == 4) {
                                    String labelName1 = o1.getLabelName();
                                    String labelName2 = o2.getLabelName();
                                    if (labelName1.matches(reg)
                                            && labelName2.matches(reg)) {
                                        String _reg = "\\d+";
                                        Pattern p = Pattern.compile(_reg);
                                        Matcher m1 = p.matcher(labelName1);
                                        Integer i1 = 0;
                                        Integer i2 = 0;
                                        if (m1.find())
                                            i1 = Integer.parseInt(m1.group());
                                        Matcher m2 = p.matcher(labelName2);
                                        if (m2.find())
                                            i2 = Integer.parseInt(m2.group());
                                        return i1.compareTo(i2);
                                    }
                                }
                                return 0;
                            }
                        });
                    }

                    for (Integer pid : childrenMap.keySet()) {
                        if (parentMap.containsKey(pid)) {
                            LabelInfo parentLabel = parentMap.get(pid);
                            List<LabelInfo> children = childrenMap.get(pid);
                            if (null != children) {
                                parentLabel.setChildren(children);
                            }
                        } else {
                            LabelInfo parentLabel = getLabelById(pid);
                            List<LabelInfo> children = childrenMap.get(pid);
                            parentLabel.setChildren(children);
                            parentMap.put(pid, parentLabel);
                        }
                    }
                }
            }
        }
        return mapList;
    }

    /**
     * 处理组合标签功能
     *
     * @param mapList
     */
    @SuppressWarnings("unchecked")
    private void proceeSignature(List<Map<String, Object>> mapList) {
        if (null != mapList && (!mapList.isEmpty())) {
            int index = 0;
            boolean flag = true;
            for (Map<String, Object> map : mapList) {
                if (flag)
                    index++;
                if (map.containsKey("labelName")) {
                    String labelName = (String) map.get("labelName");
                    if (labelName.contains("组合标签")) {
                        flag = false;
                        List<Map<String, Object>> children = (List<Map<String, Object>>) map
                                .get("children");
                        if (null != children && children.size() > 0) {
                            for (Map<String, Object> dataMap : children) {
                                if (dataMap.containsKey("children")) {
                                    dataMap.remove("children");
                                }
                            }
                        }
                    }
                }
            }
            mapList.add(0, mapList.get(index - 1));
            mapList.remove(index);
        }
    }

    private List<Map<String, Object>> getSearchLabelTree(List<LabelInfo> labels) {
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        Map<Integer, Map<Integer, LabelInfo>> levelMap = new TreeMap<Integer, Map<Integer, LabelInfo>>();
        for (LabelInfo label : labels) {
            if (null == label)
                continue;
            Integer level = label.getLevel();
            if (null == level)
                continue;
            Integer lid = label.getId();
            if (levelMap.containsKey(level)) {
                Map<Integer, LabelInfo> map = levelMap.get(level);
                map.put(lid, label);
            } else {
                Map<Integer, LabelInfo> map = new TreeMap<Integer, LabelInfo>();
                map.put(lid, label);
                levelMap.put(level, map);
                String[] uriArray = label.getUri().split("/");
                int index = 0;
                for (String id : uriArray) {
                    if (id.matches("[0-9]+")) {
                        index++;
                        if (index < 3)
                            continue;
                        Integer parentId = Integer.valueOf(id);
                        LabelInfo parentLabel = getLabelById(parentId);
                        if (null != parentLabel) {
                            parentLabel.setChildren(new ArrayList<LabelInfo>());
                            Integer parentLevel = parentLabel.getLevel();
                            if (levelMap.containsKey(parentLevel)) {
                                Map<Integer, LabelInfo> parentMap = levelMap
                                        .get(parentLevel);
                                parentMap.put(parentLabel.getId(), parentLabel);
                            } else {
                                Map<Integer, LabelInfo> parentMap = new TreeMap<Integer, LabelInfo>();
                                parentMap.put(parentLabel.getId(), parentLabel);
                                levelMap.put(parentLevel, parentMap);
                            }
                        }
                    }
                }
            }
        }
        if (!levelMap.isEmpty()) {
            Map<Integer, LabelInfo> parentMap = levelMap.get(3);
            Map<Integer, List<LabelInfo>> childrenMap = new TreeMap<Integer, List<LabelInfo>>();
            if (levelMap.containsKey(4)) {
                Map<Integer, LabelInfo> map = levelMap.get(4);
                for (LabelInfo label : map.values()) {
                    Integer pid = label.getParent().getId();
                    if (childrenMap.containsKey(pid)) {
                        List<LabelInfo> childrenList = childrenMap.get(pid);
                        childrenList.add(label);
                    } else {
                        List<LabelInfo> childrenList = new ArrayList<LabelInfo>();
                        childrenList.add(label);
                        childrenMap.put(pid, childrenList);
                    }
                }
            }
            final String reg = "^.*\\d+.*$";
            for (Integer _key : childrenMap.keySet()) {
                List<LabelInfo> _list = childrenMap.get(_key);
                Collections.sort(_list, new Comparator<LabelInfo>() {

                    @Override
                    public int compare(LabelInfo o1, LabelInfo o2) {
                        if (o1.getLevel() == 4) {
                            String labelName1 = o1.getLabelName();
                            String labelName2 = o2.getLabelName();
                            if (labelName1.matches(reg)
                                    && labelName2.matches(reg)) {
                                String _reg = "\\d+";
                                Pattern p = Pattern.compile(_reg);
                                Matcher m1 = p.matcher(labelName1);
                                Integer i1 = 0;
                                Integer i2 = 0;
                                if (m1.find())
                                    i1 = Integer.parseInt(m1.group());
                                Matcher m2 = p.matcher(labelName2);
                                if (m2.find())
                                    i2 = Integer.parseInt(m2.group());
                                return i1.compareTo(i2);
                            }
                        }
                        return 0;
                    }
                });
            }
            if (levelMap.containsKey(3)) {
                for (Integer pid : childrenMap.keySet()) {
                    if (parentMap.containsKey(pid)) {
                        LabelInfo parentLabel = parentMap.get(pid);
                        List<LabelInfo> children = childrenMap.get(pid);
                        if (null != children) {
                            parentLabel.setChildren(children);
                        }
                    } else {
                        LabelInfo parentLabel = getLabelById(pid);
                        if (null != parentLabel) {
                            List<LabelInfo> children = childrenMap.get(pid);
                            parentLabel.setChildren(children);
                            parentMap.put(pid, parentLabel);
                        }
                    }
                }
                for (Integer id : parentMap.keySet()) {
                    mapList.add(commonService.getLabelMap(parentMap.get(id)));
                }
            }
        }
        // proceeSignature(mapList);
        return mapList;
    }

    @SuppressWarnings("unchecked")
    public Map<Integer, Integer> getLabelChildrenMap() {
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        List<Map<String, Object>> childrenMapList = labelInfoDao
                .createQuery(
                        "select new map(parent.id as id,count(id) as total) from LabelInfo where availably =1 and status=3 group by parent.id")
                .list();
        for (Map<String, Object> map : childrenMapList) {
            Object idObj = map.get("id");
            Object totalObj = map.get("total");
            if (idObj == null)
                continue;
            Integer id = Integer.valueOf(idObj.toString());
            Integer count = Integer.valueOf(totalObj == null ? "0" : totalObj
                    .toString());
            result.put(id, count);
        }
        return result;
    }


    public int updateLabelAvailablyByCondition(Map<String, ?> map,
                                               Map<String, ?> likeMap) {
        if (map == null)
            return 0;
        if (likeMap == null)
            likeMap = new HashMap<String, Object>();
        String hql = "update LabelInfo t set t.availably=0 where 1 = 1";
        int result = labelInfoDao.batchExecute(hql, map, likeMap);
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<Integer> isAvailabeLabel(String ids) {
        JSONObject idJson = JSON.parseObject(ids);
        JSONArray idsArr = idJson.getJSONArray(String.valueOf(Constant.LABLE_TYPE_BASE));
        JSONArray categoryIdArr = idJson.getJSONArray(String.valueOf(Constant.LABLE_TYPE_SIGNATURE));
        List<Integer> lids = new ArrayList<Integer>();
        List<Integer> idList = new ArrayList<Integer>();
        for (int i = 0; i < categoryIdArr.size(); i++) {
            String id = categoryIdArr.getString(i);
            if (id.matches("[0-9]+")) {
                lids.add(Integer.valueOf(id));
            }
        }
        for (int i = 0; i < idsArr.size(); i++) {
            String id = idsArr.getString(i);
            if (id.matches("[0-9]+")) {
                idList.add(Integer.valueOf(id));
            }
        }
        List<Object> objs = labelInfoDao.createQuery("select id from LabelInfo where availably=1 and id in(:idList)").setParameterList("idList", idList).list();
        for (Object obj : objs) {
            if (null != obj) {
                lids.add(Integer.valueOf(obj.toString()));
            }
        }

        return lids;
    }

    public List<LabelInfo> getChildrenByParentId(Integer parentId) {
        return labelInfoDao.createQuery("from LabelInfo where status=3 and availably=1 and parent.id=? ", parentId).list();
    }

    private void getAllDataNodes(List<DataNode> nodes, List<DataNode> allNodes) {
        if (null == nodes)
            return;
        for (DataNode node : nodes) {
            List<DataNode> children = node.getChildren();
            if (null != children && children.size() > 0) {
                getAllDataNodes(children, allNodes);
            }
            allNodes.add(node);
        }
    }

    private List<Object> transform(JSONArray arr) {
        List<Object> postfixStack = new ArrayList<Object>();
        Stack<String> opStack = new Stack<String>();
        opStack.push(",");
        int currentIndex = 0;
        int count = 0;
        String currentOp, peekOp;
        for (int i = 0; i < arr.size(); i++) {
            currentOp = arr.getJSONObject(i).getString("value");
            if (isOperator(currentOp)) {
                if (count > 0) {
                    for (int s = 0; s < count; s++) {
                        Integer id = arr.getJSONObject(currentIndex + s)
                                .getJSONObject("value").getInteger("id");
                        LabelInfo label = getLabelById(id);
                        LabelInfo parent = label.getParent();
                        JSONObject tmp = new JSONObject();
                        Integer type = parent.getType();
                        if (type == 2) {
                            LabelCategory cate = parent.getParentCategory();
                            if (cate != null)
                                type = 4;
                        }
                        tmp.put("labelID", parent.getLabelId());
                        tmp.put("type", Integer.toString(type));
                        tmp.put("value", label.getLabelName());
                        postfixStack.add(tmp);
                    }
                }
                peekOp = opStack.peek();
                if (currentOp.equalsIgnoreCase(")")) {
                    while (!opStack.peek().equalsIgnoreCase("(")) {
                        postfixStack.add(String.valueOf(opStack.pop()));
                    }
                    opStack.pop();
                } else {
                    while (!currentOp.equalsIgnoreCase("(")
                            && !peekOp.equalsIgnoreCase(",")
                            && compare(currentOp, peekOp)) {
                        postfixStack.add(String.valueOf(opStack.pop()));
                        peekOp = opStack.peek();
                    }
                    opStack.push(currentOp);
                }
                count = 0;
                currentIndex = i + 1;
            } else {
                count++;
            }
        }
        if (count > 1
                || (count == 1 && !isOperator(arr.getString(currentIndex)))) {
            for (int s = 0; s < count; s++) {
                Integer id = arr.getJSONObject(currentIndex + s)
                        .getJSONObject("value").getInteger("id");
                LabelInfo label = getLabelById(id);
                JSONObject tmp = new JSONObject();
                tmp.put("labelID", label.getParent().getLabelId());
                tmp.put("value", label.getLabelName());
                postfixStack.add(tmp);
            }
        }

        while (!opStack.peek().equalsIgnoreCase(",")) {
            postfixStack.add(String.valueOf(opStack.pop()));
        }
        return postfixStack;
    }

    /**
     * 判断是否为符号
     *
     * @param c
     * @return
     */
    private boolean isOperator(String c) {
        return c.equalsIgnoreCase("and") || c.equalsIgnoreCase("or")
                || c.equalsIgnoreCase("not") || c.equalsIgnoreCase("(")
                || c.equalsIgnoreCase(")");
    }

    /**
     * or>not>and
     *
     * @param cur
     * @param peek
     * @return
     */
    public boolean compare(String cur, String peek) {
        boolean result = false;
        if (cur.equalsIgnoreCase("and")) {
            if (peek.equalsIgnoreCase("or")) {
                result = true;
            } else if (peek.equalsIgnoreCase("not")) {
                result = true;
            } else {
                result = false;
            }
        } else if (cur.equalsIgnoreCase("not")) {
            if (peek.equalsIgnoreCase("or")) {
                result = true;
            } else if (peek.equalsIgnoreCase("and")) {
                result = false;
            } else {
                result = false;
            }
        }
        return result;
    }

    public List<LabelInfo> getLabelInfoByCategoryId(Integer categoryId) {
        return labelInfoDao.createQuery("From LabelInfo t where t.parentCategory.id=? and t.parent.id=1000", new Object[]{categoryId}).list();
    }

    public List<Map<String, Object>> getLabelsByParentId(Integer labelId) {
        return labelInfoDao.createQuery("select new map(t.id as id,t.labelId as labelId,t.labelName as labelName) From LabelInfo t where t.parent.id=? and t.status=3", new Object[]{labelId}).list();
    }


}
