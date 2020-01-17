package com.bdaim.industry.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.service.InitService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.industry.dto.IndustryLabelsDTO;
import com.bdaim.industry.dto.IndustryPoolDTO;
import com.bdaim.industry.dto.IndustryPoolPriceDTO;
import com.bdaim.industry.dto.MarketResourceTypeEnum;
import com.bdaim.label.dao.*;
import com.bdaim.label.dto.CategoryType;
import com.bdaim.label.dto.QueryType;
import com.bdaim.label.entity.*;
import com.bdaim.label.service.CommonService;
import com.bdaim.label.service.LabelCategoryService;
import com.bdaim.label.service.LabelInterfaceService;
import com.bdaim.rbac.dto.UserDTO;
import com.bdaim.rbac.service.UserService;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.util.Constant;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringHelper;
import com.bdaim.util.StringUtil;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
@Service("IndustryPoolService")
@Transactional
public class IndustryPoolService {
    private static Logger log = LoggerFactory.getLogger(IndustryPoolService.class);
    @Resource
    private IndustryPoolDao industryPoolDao;

    @Resource
    private IndustryLabelDao industryLabelDao;

    @Resource
    private IndustryInfoDao industryInfoDao;

    @Resource
    private UserService userService;
    @Resource
    private LabelCategoryService labelCategoryServiceImpl;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private LabelInterfaceService labelInterfaceService;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private LabelDao labelDao;
    @Resource
    private CommonService commonService;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private MarketResourceDao marketResourceDao;

    @Resource
    private LabelInfoDao labelInfoDao;

    /**
     * 查看客户行业标签池
     */
    public JSONObject getIndustryPool(String customerId) {
        JSONObject json = new JSONObject();
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            //查看客户行业标签池SQL
            StringBuffer sb = new StringBuffer();
            sb.append(" SELECT pool.`NAME` as name, pool.industry_pool_id, GROUP_CONCAT(info.industy_name)  as industy_name, pool.label_num, ins.`STATUS` as custState");
            sb.append(" FROM  t_industry_pool pool ");
            sb.append(" LEFT JOIN t_cust_industry ins ON pool.industry_pool_id = ins.industry_pool_id AND ins.cust_id = '");
            sb.append(customerId);
            sb.append("'");
            sb.append(" LEFT JOIN t_industry_info_rel rel ON pool.industry_pool_id = rel.industry_pool_id ");
            sb.append(" LEFT JOIN t_industry_info info ON info.industry_info_id = rel.industry_info_id ");
            sb.append(" where pool.`STATUS`=3");
            sb.append(" and (ins.`STATUS`=1 or ins.`STATUS` is null)");
            sb.append(" GROUP BY industry_pool_id");
            sb.append(" ORDER BY ins. STATUS DESC");
            list = industryPoolDao.getSQLQuery(sb.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
            json.put("listIndustry", list);
        } catch (Exception e) {
            log.error("查看客户行业标签池出错！" + e.getMessage());
        }
        return json;
    }

    public JSONObject getIndustryPoolV1(String customerId) {
        JSONObject json = new JSONObject();
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            //查看客户行业标签池SQL
            StringBuffer sb = new StringBuffer();
            sb.append(" SELECT pool.`NAME` as name, pool.industry_pool_id, GROUP_CONCAT(info.industy_name)  as industy_name, pool.label_num, ins.`STATUS` as custState");
            sb.append(" FROM  t_industry_pool pool ");
            sb.append(" LEFT JOIN t_cust_industry ins ON pool.industry_pool_id = ins.industry_pool_id AND ins.cust_id = ?");
//            sb.append(customerId);
//            sb.append("'");
            sb.append(" LEFT JOIN t_industry_info_rel rel ON pool.industry_pool_id = rel.industry_pool_id ");
            sb.append(" LEFT JOIN t_industry_info info ON info.industry_info_id = rel.industry_info_id ");
            sb.append(" where pool.`STATUS`=3");
            sb.append(" and (ins.`STATUS`=1 or ins.`STATUS` is null)");
            sb.append(" AND ins.`status` = 1 ");
            sb.append(" GROUP BY industry_pool_id");
            sb.append(" ORDER BY ins.create_time DESC, ins.modify_time DESC");
            //list = industryPoolDao.getSQLQuery(sb.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
            list = jdbcTemplate.queryForList(sb.toString(),new Object[]{customerId});
            json.put("listIndustry", list);
        } catch (Exception e) {
            log.error("查看客户行业标签池出错！" + e.getMessage());
        }
        return json;
    }

    /**
     * 根据条件查询标签信息
     *
     * @param map     精确查询的条件集合
     * @param likeMap 模糊查询的条件集合
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<LabelInfo> getLabelsByCondition(Map<String, ?> map,
                                                Map<String, ?> likeMap) {
        if (null == map || null == likeMap)
            throw new NullPointerException("查询条件不允许为空");
        String hql = "From LabelInfo t where availably =1 and type>1 and 1 = 1";
        List<LabelInfo> labelList = (List<LabelInfo>) industryPoolDao.getHqlQuery(hql, map, likeMap, "orderNum").list();
        return labelList;
    }

    @SuppressWarnings("unchecked")
    public List<LabelInfo> getLabelByCondition(Map<String, ?> map,
                                               Map<String, ?> likeMap) {
        if (map == null)
            map = new HashMap<String, Object>();
        if (likeMap == null)
            likeMap = new HashMap<String, Object>();
        String hql = "From LabelInfo t where t.availably=1 and type>1 and 1 = 1";
        List<LabelInfo> labelList = industryPoolDao.getHqlQuery(hql, map, likeMap,
                null).list();
        return labelList;
    }

    /**
     * 标签信息的模糊搜索 根据标签名称 、规则、部门等条件 返回结果结合[{},{}]
     */
    public List<Map<String, Object>> getLabels(Map<String, Object> map,
                                               Map<String, Object> likeMap) {
        List<LabelInfo> list = getLabelsByCondition(map, likeMap);
        List<Map<String, Object>> resultList = getLabelTree(list);
        return resultList;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTreeById(Integer id) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        List<LabelInfo> list = industryPoolDao.createQuery(
                "from LabelInfo where availably=1 and parent.id=?", id).list();
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
        Integer firstLid = Integer.valueOf(map.get(Constant.FILTER_KEY_PREFIX + "id").toString());
        LabelInfo firstLabel = labelDao.get(firstLid);
        //获取登录用户信息
        List<DataNode> nodeList = new ArrayList<DataNode>();
        UserDTO user = (UserDTO) map.get(Constant.FILTER_KEY_PREFIX + "user");
        String hql = "";

        if (null == firstLabel.getDataFormat()
                || firstLabel.getDataFormat().equals(-1)) {
            //处理普通数据类型的情况
            hql = "SELECT t.* FROM ( SELECT id,label_id,label_name,parent_id,uri,`level`,`status`FROM label_info  t where attr_id is null and level<4 and availably =1  and  (( uri like '/" + firstLid + "/%' )) ";
            hql += " AND STATUS = 3 )t  LEFT JOIN t_industry_label b ON t.label_id = b.label_id and b.industry_pool_id=" + map.get("poolId") + " AND b.`STATUS`=1  WHERE b.label_id IS NOT NULL OR t. LEVEL < 3 ";
            //查询权限
            List<LabelInfo> list = new ArrayList<LabelInfo>();
            map.remove("poolId");
            StringBuffer sql = this.getSql(hql, map, orLikeMap, andLikeMap, "level asc");
            List<Map<String, Object>> list2 = jdbcTemplate.queryForList(sql.toString());
            if (list2.size() > 0) {
                for (int i = 0; i < list2.size(); i++) {
                    LabelInfo listLabel = new LabelInfo();
                    LabelInfo info = new LabelInfo();
                    listLabel.setId(Integer.parseInt(list2.get(i).get("id").toString()));
                    listLabel.setLabelId(list2.get(i).get("label_id").toString());
                    listLabel.setLabelName(list2.get(i).get("label_name").toString());
                    String parent_id = list2.get(i).get("parent_id") + "";
                    if (null != parent_id && !"null".equals(parent_id) && !"".equals(parent_id)) {
                        info.setId(Integer.parseInt(list2.get(i).get("parent_id") + ""));
                    }
                    listLabel.setParent(info);
                    listLabel.setStatus(Integer.parseInt(list2.get(i).get("status").toString()));
                    listLabel.setLevel(Integer.parseInt(list2.get(i).get("level").toString()));
                    listLabel.setUri(list2.get(i).get("uri").toString());
                    list.add(listLabel);
                }
            }
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
        List<LabelInfo> labs = new ArrayList<LabelInfo>();
        for (Map<String, Object> cover : covers) {
            if (labelMap.containsKey(cover.get("labelId")))
                labs.add(labelMap.get(cover.get("labelId")));
        }
        return labs;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Map<String, Object>> getTreeByCondition(
            Map<String, Object> map, Map<String, Object> orLikeMap,
            Map<String, Object> andLikeMap, Integer cycle) {
        Integer type = (Integer) map.get(Constant.FILTER_KEY_PREFIX + "type");
        Integer categoryId = (Integer) map.get("parentCategory.id");
        List<LabelInfo> labels = new ArrayList<LabelInfo>();
        LabelInfo label = (LabelInfo) map.get(Constant.FILTER_KEY_PREFIX + "id");
        if (null == label.getDataFormat()
                || label.getDataFormat().equals(Constant.DATAFORMAT_COMMON)
                || type == null) {
            String hql = "  select  t.*  from  (select id,label_id,label_name,parent_id,IFNULL(customer_num,0)customer_num,label_rule,path,level,uri,status  from label_info t where  ( t.availably=1  ";
            //判断组合标签取前两级 非组合标签取前三级
            if (andLikeMap.containsKey("uri") && "/40000/".equals(andLikeMap.get("uri"))) {
                hql += " and t.level<4 )";
            } else {
                hql += " or t.level<4 )";
            }
            hql += "  ) t LEFT JOIN t_industry_label b ON t.label_id = b.label_id and b.industry_pool_id=" + map.get("poolId") + " AND b.`STATUS`=1 WHERE b.label_id IS NOT NULL  ";
            map.remove("poolId");
            StringBuffer sb = this.getSql(hql, map, orLikeMap, andLikeMap, "level asc");
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sb.toString());
            LabelInfo labelInfo;
            LabelInfo parentLabelInfo;
            for (int i = 0; i < list.size(); i++) {
                labelInfo = new LabelInfo();
                labelInfo.setId(Integer.parseInt(list.get(i).get("id").toString()));
                labelInfo.setLabelId(list.get(i).get("label_id").toString());
                labelInfo.setLabelName(list.get(i).get("label_name").toString());
                labelInfo.setCustomerNum(Long.parseLong(list.get(i).get("customer_num").toString()));
                labelInfo.setLabelRule(list.get(i).get("label_rule").toString());
                labelInfo.setPath(list.get(i).get("path").toString());
                labelInfo.setLevel(Integer.parseInt(list.get(i).get("level").toString()));
                labelInfo.setUri(list.get(i).get("uri").toString());
                labelInfo.setStatus(Integer.parseInt(list.get(i).get("status").toString()));
                // 通过父级标签ID查询子标签
                parentLabelInfo = industryPoolDao.findUnique("FROM LabelInfo t WHERE t.availably=1 AND t.id=" + labelInfo.getId());
                if (parentLabelInfo != null) {
                    labelInfo.setChildren(parentLabelInfo.getChildren());
                }
                labels.add(labelInfo);

            }
            if (labels.size() == 0) {
                if (map.containsKey(Constant.FILTER_KEY_PREFIX + "id")) {
                    labels = industryPoolDao.createQuery("FROM LabelInfo t WHERE t.availably=1 AND t.id=" + label.getId()).list();
                }
            }
        } else {
            if (type.equals(Constant.QUERY_TYPE_CATEGORY)) {
                UserDTO user = (UserDTO) map.get(Constant.FILTER_KEY_PREFIX + "user");
                DataNode cateNode = new DataNode();
                cateNode.setId(categoryId);
                List<DataNode> nodes = new ArrayList<DataNode>();
                List<LabelCategory> categorys = new ArrayList<LabelCategory>();
                List<Integer> cateList = new ArrayList<Integer>();
                cateList.add(categoryId);
                // 添加所选品类
                LabelCategory parentCategory = InitService.CATEGORY_MAP.get(categoryId);
                categorys.add(parentCategory);
                if (label.getDataFormat().equals(Constant.DATAFORMAT_EB)) {
                    nodes = labelDao.getCategoryList(user, cateNode, 10, QueryType.ALL, CategoryType.PRODUCT);
                    if (null == nodes) {
                        nodes = new ArrayList<DataNode>();
                    }
                } else if (label.getDataFormat().equals(Constant.DATAFORMAT_MC)) {
                    nodes = labelDao.getCategoryList(user, cateNode, 10, QueryType.ALL, CategoryType.MEDIA);
                    if (null == nodes) {
                        nodes = new ArrayList<DataNode>();
                    }
                }
                return getChildrenByCategory(nodes, cateList, label);
            } else if (type.equals(Constant.QUERY_TYPE_BRAND)) {
                String hql = "from LabelInfo t where t.availably=1 and t.type>1 and attrId=10001 and parent.id="
                        + label.getId();
                labels = industryPoolDao.getHqlQuery(hql, map, orLikeMap,
                        andLikeMap, null).list();
            } else if (type.equals(Constant.QUERY_TYPE_ATTR)) {
                String hql = "from LabelInfo t where t.availably=1 and t.type>1 and attrId != 10001 and attrId is not null and parent.id="
                        + label.getId();
                labels = industryPoolDao.getHqlQuery(hql, map, orLikeMap,
                        andLikeMap, null).list();
            }
        }
        List<Map<String, Object>> covers = new ArrayList<Map<String, Object>>();

        List<Integer> lids = new ArrayList<Integer>();
        Map<Integer, LabelInfo> labelMap = new HashMap<Integer, LabelInfo>();
        lids.add(label.getId());
        labelMap.put(label.getId(), label);
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

        List<LabelInfo> labs = new ArrayList<LabelInfo>();
        for (Map<String, Object> cover : covers) {
            if (labelMap.containsKey(cover.get("labelId")))
                labs.add(labelMap.get(cover.get("labelId")));
        }

        List<Map<String, Object>> result = getSearchLabelTree(labs);

        // 根据lable_id去对比获取label_cover中的客户数据和总客户数
        addCoverNum(result, covers, label.getId().toString());
        return result;
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

    /**
     * 根据父级id和层级获取子集
     * @param map
     * @param orLikeMap
     * @param andLikeMap
     * @param cycle
     * @return
     */
   /* public List<Map<String, Object>> getChildrenById(Map<String, Object> map,
                                                     Map<String, Object> orLikeMap, Map<String, Object> andLikeMap, Integer cycle) {
        return getTreeByCondition(map, orLikeMap, andLikeMap, cycle);
    }*/

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTreeByLevel(Integer pid, Integer level) {
        List<LabelInfo> list = industryPoolDao.createQuery("from LabelInfo where availably=1 and status=3 and level<=?", level).list();
        List<Map<String, Object>> result = getLabelTree(list);
        return result;
    }

    public List<Map<String, Object>> getChildrenByIdAndLevel(Integer pid,
                                                             Integer level) {
        return getTreeByLevel(pid, level);
    }

    @SuppressWarnings("unchecked")

    public List<Map<String, Object>> getAllLabelsByCategoryId(Integer categoryId) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> list = industryPoolDao.createQuery("select new map(id as id,labelName) from LabelInfo where availably =1 and parentCategory.id=?", categoryId).list();
        for (Map<String, Object> m : list) {
            m.put("children", getChildrenById(Integer.parseInt(m.get("id").toString())));
            result.add(m);
        }
        return result;
    }

    @SuppressWarnings("unchecked")

    public List<LabelInfo> getAllLabels() {
        List<LabelInfo> labels = industryPoolDao.createQuery("from LabelInfo where availably=1").list();
        return labels;
    }


    public List<Map<String, Object>> getChildrenById(Integer pid) {
        return getTreeById(pid);
    }


    public void updateLabelInfo(LabelInfo label) {
        labelInfoDao.update(label);
    }

/*
    public List<Map<String, Object>> getLabelsByCondition(
            Map<String, Object> map) {
        // return getTreeByLevel(map);
        return getLabelMenuByCondition(map);
    }*/

    @SuppressWarnings({"rawtypes", "unchecked"})
    /*private List<Map<String, Object>> getLabelMenuByCondition(Map<String, Object> condition) {
        List<Map<String, Object>> result;
        List<Map<String, Object>> cateList = new ArrayList<Map<String, Object>>();

        LoginUser loginUser = (LoginUser) condition.get(Constant.FILTER_KEY_PREFIX + "user");
        UserDTO user = new UserDTO();
        user.setId(loginUser.getId());

        Integer firstLid = null;
        //新增权限查看
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT ");
        sb.append(" 	(SELECT id from label_info b where SUBSTR(a.label_id, 1, 5) = b.label_id )id,a.label_id as L3,SUBSTR(a.label_id,1,5) as L1,SUBSTR(a.label_id,1,11)as L2");
        sb.append(" FROM");
        sb.append(" 	t_industry_label a  ");
        sb.append(" WHERE");
        sb.append(" 	a.industry_pool_id =" + condition.get("poolId"));
        sb.append(" ORDER BY SUBSTR(a.label_id,1,5)");
        List<Map<String, Object>> listForId = jdbcTemplate.queryForList(sb.toString());
        Object first_label_id = listForId.get(0).get("id");
        LabelInfo firstLabel = null;
        if (first_label_id != null) {
            firstLabel = labelDao.get(Integer.parseInt(String.valueOf(first_label_id)));
        }
        //getid
        StringBuffer sbList = new StringBuffer();
        for (int i = 0; i < listForId.size(); i++) {
            sbList.append("'" + listForId.get(i).get("L1") + "',");
            sbList.append("'" + listForId.get(i).get("L2") + "',");
            sbList.append("'" + listForId.get(i).get("L3") + "',");
        }
        String idList = sbList.toString().substring(0, sbList.toString().length() - 1).toString();
        List<DataNode> categoryNodes = new ArrayList<DataNode>();
        String hql = "";
        hql = " select a.* from (select * from label_info where attr_id is null and level<4 and availably =1  and  id in(:lids) ";
        if (firstLabel == null || null == firstLabel.getDataFormat()) {
            hql = " select a.* from (select * from label_info  where attr_id is null and level<4 and availably =1  and (label_id in(" + idList + ") )";
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
        if (condition.containsKey("poolId")) {
            hql += " AND STATUS = 3 )a LEFT JOIN t_industry_label b ON a.label_id = b.label_id   and b.industry_pool_id=" + condition.get("poolId") + " AND b.`STATUS`=1 WHERE b.label_id IS NOT NULL OR a. LEVEL < 3 ";
        } else {
            hql += " AND STATUS = 3 )a LEFT JOIN t_industry_label b ON a.label_id = b.label_id  WHERE b.label_id IS NOT NULL OR a. LEVEL < 3 ";
        }
        hql += " order by level asc";

        List<Map<String, Object>> list = labelDao.sqlQuery(hql);
        List<LabelInfo> listLabel = new ArrayList<LabelInfo>();
        for (int i = 0; i < list.size(); i++) {
            Map r = list.get(i);
            LabelInfo info = new LabelInfo();
            LabelInfo labe = new LabelInfo();
            labe.setId(Integer.parseInt(r.get("id") + ""));
            labe.setLabelId(r.get("label_id") + "");
            labe.setLabelName(r.get("label_name") + "");
            labe.setUri(r.get("uri") + "");
            String parent_id = r.get("parent_id") + "";
            if (null != parent_id && !"null".equals(parent_id) && !"".equals(parent_id)) {
                info.setId(Integer.parseInt(r.get("parent_id") + ""));
            }
            labe.setParent(info);
            labe.setLevel(Integer.parseInt(r.get("level") + ""));
            labe.setStatus(Integer.parseInt(r.get("status") + ""));
            String uodate_cycle = r.get("update_cycle") + "";
            if (null != uodate_cycle && !"null".equals(uodate_cycle) && !"".equals(uodate_cycle)) {
                labe.setUpdateCycle(Integer.parseInt(r.get("update_cycle") + ""));
            }
            String data_format = r.get("data_format") + "";
            if (null != data_format && !"null".equals(data_format) && !"".equals(data_format)) {
                labe.setDataFormat(Integer.parseInt(r.get("data_format") + ""));
            }
            listLabel.add(labe);
        }
        if (list.size() == 0) {
            return new ArrayList<Map<String, Object>>();
        } else {
            result = getLabelTree(listLabel);
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
        if (firstMap.size() > 0) {
            result.add(0, firstMap);
        }
        return result;

    }*/


//    @SuppressWarnings({ "rawtypes", "unchecked" })
//	private List<Map<String, Object>> getLabelMenuByCondition(
//			Map<String, Object> condition) {
//		List<Map<String, Object>> result;
//		List<Map<String, Object>> cateList = new ArrayList<Map<String, Object>>();
//
//		LoginUser user = (LoginUser) condition.get(Constant.FILTER_KEY_PREFIX + "user");
//		UserDTO userDTO = new UserDTO();
//		userDTO.setId(user.getId());
//		List<DataNode> labelNodes = labelDao.getLabelList(userDTO, null, 1, QueryType.ALL);
//		
//		Integer firstLid = null;
//		List<Integer> lids = new ArrayList<Integer>();
//		if (null != labelNodes && labelNodes.size() > 0) {
//			for (DataNode n : labelNodes) {
//				Integer nodeId = Integer.valueOf(n.getId().toString());
//				if (firstLid == null || firstLid > nodeId) {
//					firstLid = nodeId;
//				}
//				lids.add(nodeId);
//			}
//			for(DataNode n:labelNodes){
//				if(n.getName().equals("组合标签")){
//					firstLid = Integer.valueOf(n.getId().toString());
//					break;
//				}
//			}
//		} else {
//			throw new RuntimeException("该用户没有任何标签权限!");
//		}
//		LabelInfo firstLabel = labelDao.get(firstLid);
//		//getid
//		StringBuffer sb = new StringBuffer();
//		for(int i=0;i<lids.size();i++){
//			if(i==(lids.size()-1)){
//				sb.append("'"+lids.get(i)+"'");
//			}else{
//				sb.append("'"+lids.get(i)+"',");
//			}
//		}
//		List<DataNode> categoryNodes = new ArrayList<DataNode>();
//		String hql = "";
//		hql = " select a.* from (select * from label_info where attr_id is null and level<4 and availably =1  and  id in(:lids) ";
//		if (null == firstLabel.getDataFormat()) {
//			hql = " select a.* from (select * from label_info  where attr_id is null and level<4 and availably =1  and (( uri like '/"
//					+ firstLid + "/%' )  or id in("+sb+") )";
//		} else {
//			if (firstLabel.getDataFormat().equals(Constant.DATAFORMAT_EB)) {// 商品品类
//				categoryNodes = labelDao.getCategoryList(userDTO, null, 10, QueryType.ALL,CategoryType.PRODUCT);
//			} else if (firstLabel.getDataFormat().equals(Constant.DATAFORMAT_MC)) {// 媒体品类
//				categoryNodes = labelDao.getCategoryList(userDTO, null, 10, QueryType.ALL,CategoryType.MEDIA);
//			}
//			cateList = CommonService.getDataNodeMapList(categoryNodes);
//		}
//		if (condition.containsKey("status")) {
//			hql += " and status=" + condition.get("status");
//		}
//		if (condition.containsKey("createUid")) {
//			hql += " and (labelCreateUser.id=" + condition.get("createUid")
//			+ " or level<3)";
//		}
//		hql += " AND STATUS = 3 )a LEFT JOIN t_industry_label b ON a.label_id = b.label_id  WHERE b.label_id IS NOT NULL OR a. LEVEL < 3 ";
//		hql += " order by level asc";
//		List<Map<String, Object>>list =jdbcTemplate.queryForList(hql);
//		List<LabelInfo> listLabel=new ArrayList<LabelInfo>();
//		for(int i=0;i<list.size();i++){
//			LabelInfo info =new LabelInfo();
//			LabelInfo labe=new LabelInfo();
//			labe.setId(Integer.parseInt(list.get(i).get("id")+""));
//			labe.setLabelId(list.get(i).get("label_id")+"");
//			labe.setLabelName(list.get(i).get("label_name")+"");
//			labe.setUri(list.get(i).get("uri")+"");
//			String parent_id=list.get(i).get("parent_id")+"";
//			if(null!=parent_id&&!"null".equals(parent_id)&&!"".equals(parent_id)){
//				info.setId(Integer.parseInt(list.get(i).get("parent_id")+""));
//			}
//			labe.setParent(info);
//			labe.setLevel(Integer.parseInt(list.get(i).get("level")+""));
//			labe.setStatus(Integer.parseInt(list.get(i).get("status")+""));
//			String uodate_cycle=list.get(i).get("update_cycle")+"";
//			if(null!=uodate_cycle&&!"null".equals(uodate_cycle)&&!"".equals(uodate_cycle)){
//				labe.setUpdateCycle(Integer.parseInt(list.get(i).get("update_cycle")+""));
//			}
//			String data_format =list.get(i).get("data_format")+"";
//			if(null!=data_format&&!"null".equals(data_format)&&!"".equals(data_format)){
//				labe.setDataFormat(Integer.parseInt(list.get(i).get("data_format")+""));
//			}
//			listLabel.add(labe);
//		}
//		if (list.size() == 0) {
//			return new ArrayList<Map<String, Object>>();
//		} else {
//			result = getLabelTree(listLabel);
//		}
//		Map<String, Object> firstMap = new TreeMap<String, Object>();
//		for (Map<String, Object> data : result) {
//			Integer id = Integer.valueOf(data.get("id").toString());
//			if (id.equals(firstLid)) {
//				firstMap = data;
//				result.remove(data);
//				break;
//			}
//		}
//		if (!cateList.isEmpty()) {
//			firstMap.put("category", cateList);
//		}
//		result.add(0, firstMap);
//		return result;
//
//	}

/*
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
        List<LabelInfo> list = industryPoolDao.createQuery(hql, condition).list();
        if (list.size() == 0) {
            return new ArrayList<Map<String, Object>>();
        } else {
            result = getLabelTree(list);
        }
        return result;
    }*/

/*
    public LabelInfo getLabelInfoByParentAndName(LabelInfo parent, String name) {
        Criteria c = industryPoolDao.createCriteria(Restrictions.eq("parent", parent));
        c.add(Restrictions.eq("labelName", name));
        LabelInfo label = null;
        List<LabelInfo> list = (List<LabelInfo>) c.list();
        if (list.size() > 0)
            label = list.get(0);
        return label;
    }*/


    public LabelInfo getLabelInfoByLabelId(String labelId) {
        Criteria c = industryPoolDao.createCriteria(Restrictions.eq("labelId",
                labelId));
        LabelInfo label = null;
        List<LabelInfo> list = (List<LabelInfo>) c.list();
        if (list.size() > 0)
            label = list.get(0);
        return label;
    }


    public List<LabelInfo> getChildrenByLabelId(String labelId) {
        LabelInfo pLabel = getLabelInfoByLabelId(labelId);
        Criteria c = industryPoolDao.createCriteria(Restrictions.eq("parent",
                pLabel));
        return c.list();
    }


    public List<String> getChildrenNameByPid(Integer id) {
        Criteria c = industryPoolDao.createCriteria(Restrictions.eq("parent.id",
                id));
        c.setProjection(Projections.property("labelName"));
        return c.list();
    }


    public List<Map<String, Object>> getAllLabelByCategoryId(Integer categoryId) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        List<LabelInfo> list = industryPoolDao.createQuery(
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
            return industryPoolDao
                    .createQuery(
                            "select new map(labelId as labelId,type as type) from LabelInfo where availably=1 and parentCategory is null  and type=? and level=? and labelId is not null ",
                            1, level).list();
        }
        if (type == 3 && level == 2) {
            return industryPoolDao
                    .createQuery(
                            "select new map(labelId as labelId,type as type) from LabelInfo where availably=1 and parent.labelId='20001'  and type=? and level=? and labelId is not null ",
                            1, level).list();
        }
        if (type != 4) {
            return industryPoolDao
                    .createQuery(
                            "select new map(labelId as labelId,type as type) from LabelInfo where availably=1 and parentCategory is null  and type=? and level=? and labelId is not null ",
                            type, level).list();
        } else {
            List<Map<String, Object>> list = industryPoolDao
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
            LabelInfo info = industryPoolDao.findUnique(hql, pid, labelName);
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
                            LabelInfo parentLabel = labelDao.get(Integer.valueOf(id));
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
                    // Map<Integer, List<LabelCategory>> cateMap = new
                    // TreeMap<Integer, List<LabelCategory>>();
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
                            LabelInfo parentLabel = labelDao.get(pid);
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
                        LabelInfo parentLabel = labelDao.get(parentId);
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
                        LabelInfo parentLabel = labelDao.get(pid);
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
        List<Map<String, Object>> childrenMapList = industryPoolDao
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
        int result = industryPoolDao.batchExecute(hql, map, likeMap);
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
        List<Object> objs = industryPoolDao.createQuery("select id from LabelInfo where availably=1 and id in(:idList)").setParameterList("idList", idList).list();
        for (Object obj : objs) {
            if (null != obj) {
                lids.add(Integer.valueOf(obj.toString()));
            }
        }

        return lids;
    }


    public List<LabelInfo> getChildrenByParentId(Integer parentId) {
        return industryPoolDao
                .createQuery(
                        "from LabelInfo where status=3 and availably=1 and parent.id=? ",
                        parentId).list();
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
                        LabelInfo label = labelDao.get(id);
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
                LabelInfo label = labelDao.get(id);
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
        return industryPoolDao.createQuery("From LabelInfo t where t.parentCategory.id=? and t.parent.id=1000", new Object[]{categoryId}).list();
    }


    public List<Map<String, Object>> getLabelsByParentId(Integer labelId) {
        return industryPoolDao.createQuery("select new map(t.id as id,t.labelId as labelId,t.labelName as labelName) From LabelInfo t where t.parent.id=? and t.status=3", new Object[]{labelId}).list();
    }


    public StringBuffer getSql(final String queryString,
                               final Map<String, ?> map, final Map<String, ?> orLikeMap,
                               final Map<String, ?> andLikeMap, String orderBy) {
        org.springframework.util.Assert.hasText(queryString, "queryString不能为空");
        StringBuffer sql = new StringBuffer(queryString);
        // 一般查询
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(Constant.FILTER_KEY_PREFIX))
                continue;
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {
                sql.append(" And ");
                sql.append("t." + key);
                sql.append(" =" + value);
            }
        }
        for (Map.Entry<String, ?> entry : andLikeMap.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(Constant.FILTER_KEY_PREFIX))
                continue;
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {
                sql.append(" And ");
                sql.append("t." + key);
                sql.append(" like ");
                if (key.equals("uri")) {
                    sql.append("'" + value + "%'");
                } else {
                    sql.append("'%" + value + "%'");
                }
            }
        }
        if (!orLikeMap.isEmpty()) {
            boolean is = false;
            for (Map.Entry<String, ?> entry : orLikeMap.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(Constant.FILTER_KEY_PREFIX))
                    continue;
                Object value = entry.getValue();
                if (value != null && StringHelper.isNotBlank(value.toString())) {
                    if (is) {
                        sql.append(" Or ");
                        sql.append("t." + key);
                        sql.append(" like ");
                        if (key.equals("uri")) {
                            sql.append("'" + value + "%'");
                        } else {
                            sql.append("'%" + value + "%'");
                        }

                    } else {
                        sql.append(" And ");
                        sql.append("(");
                        sql.append("t." + key);
                        sql.append(" like ");
                        if (key.equals("uri")) {
                            sql.append("'" + value + "%'");
                        } else {
                            sql.append("'%" + value + "%'");
                        }
                        is = true;
                    }
                }
            }
            sql.append(") ");
        }
        if (null != orderBy && (!orderBy.isEmpty())) {
            sql.append(" order by t." + orderBy);
        }
        return sql;
    }


    public Integer addIndustryPoolInfo(String name, String description, Integer labelnum, String operator) {

        Integer induPoolId = null;
        try {
            IndustryPool ip = new IndustryPool();
            ip.setIndustryPoolType(1);
            ip.setStatus(0);
            ip.setApprovelStatus(1);
            ip.setLabelNum(labelnum);
            ip.setName(name);
            ;
            ip.setDescription(description);
            ip.setCreateTime(new Timestamp((new Date()).getTime()));
            ip.setUpdateTime(new Timestamp((new Date()).getTime()));
            ip.setCreator(operator);
            ip.setOperator(operator);
            ip.setSourceId(0);
            ip.setSourceName("");

            this.industryInfoDao.saveOrUpdate(ip);

//            String sql = "INSERT INTO t_industry_pool (industry_pool_type,STATUS,approvel_status,label_num,NAME,description,create_time,update_time,creator,operator)  VALUES(1,0,1,?,?,?,now(),now(),?,?)";
//            jdbcTemplate.update(sql, new Object[]{labelnum, name, description, operator, operator});
//
//            induPoolId = jdbcTemplate.queryForObject("select last_insert_id()", Integer.class);
//            log.info("新增行业标签池，sql：" + sql);
            induPoolId = ip.getIndustryPoolId();
        } catch (Exception e) {
            log.info("新增行业标签池出错");
        }

        return induPoolId;
    }

    public Integer addIndustryPoolInfoV1(String name, String description, Integer labelnum, String operator, String resourceId,Integer autoExtraction) {

        Integer induPoolId = null;
        try {
            IndustryPool ip = new IndustryPool();
            ip.setIndustryPoolType(1);
            ip.setStatus(0);
            ip.setApprovelStatus(1);
            ip.setLabelNum(labelnum);
            ip.setName(name);
            ip.setDescription(description);
            ip.setCreateTime(new Timestamp((new Date()).getTime()));
            ip.setUpdateTime(new Timestamp((new Date()).getTime()));
            ip.setCreator(operator);
            ip.setOperator(operator);
            if(autoExtraction==null)autoExtraction = 0;
            ip.setAutoExtraction(autoExtraction);
            ip.setSourceId(NumberConvertUtil.parseInt(resourceId));
            ip.setSourceName("");
            this.industryInfoDao.saveOrUpdate(ip);
            induPoolId = ip.getIndustryPoolId();
        } catch (Exception e) {
            log.info("新增行业标签池出错");
        }

        return induPoolId;
    }

    public Integer addIndustryInfoRel(Integer industryPoolId, Integer industryInfoId) {
        String sql = "INSERT INTO t_industry_info_rel (industry_pool_id,industry_info_id,create_time,modify_time)  VALUES(?,?,now(),now()) ";
        int code = jdbcTemplate.update(sql, new Object[]{industryPoolId, industryInfoId});
        log.info("新增行业标签对应关系，sql：" + sql);
        return code;
    }


    public Integer addIndustryPoolSourceRel(String sourceName, Integer sourceId, Integer industryPoolId) {
        String sql = "INSERT INTO t_industry_pool_source_rel (source_name,source_id,industry_pool_id,create_time,update_time)  VALUES(?,?,?,now(),now())";
        int code = jdbcTemplate.update(sql, new Object[]{sourceName, sourceId, industryPoolId});
        log.info("新增数据源中对应关系，sql：" + sql);
        return code;
    }


    public Integer addIndustryLabel(Integer industryPoolId, String labelId) {
        try {
            IndustryPoolLabel ipl = new IndustryPoolLabel();
            ipl.setIndustryPoolId(industryPoolId);
            ipl.setLabelId(labelId);
            ipl.setStatus(1);
            ipl.setCreateTime(new Timestamp((new Date()).getTime()));
            ipl.setModifyTime(new Timestamp((new Date()).getTime()));
            ipl.setPrice(55); //销售价格暂定 0.55
            this.industryInfoDao.saveOrUpdate(ipl);
            return 1;
        } catch (Exception e) {
            return 0;
        }
//        String sql = "INSERT INTO t_industry_label (industry_pool_id,label_id,STATUS,create_time,modify_time)  VALUES(?,?,1,now(),now())";
//        int code = jdbcTemplate.update(sql, new Object[]{industryPoolId, labelId});
//        log.info("新增行业标签，sql：" + sql);
//        return code;
    }


    public Integer addLabelSalePrice(String industryLabelId, Integer sourceId, Integer industryPoolId, Integer price) {

        Integer salePriceId = null;

        try {

            String sql = "INSERT INTO t_label_sale_price (industry_label_id,source_id,industry_pool_id,price,create_time,modify_time)  VALUES(?,?,?,?,now(),now())";
            jdbcTemplate.update(sql, new Object[]{industryLabelId, sourceId, industryPoolId, price});

            salePriceId = jdbcTemplate.queryForObject(
                    "select last_insert_id()", Integer.class);

            log.info("新增销售价格，sql：" + sql);
        } catch (Exception e) {
            log.info("新增销售价格出错");
        }

        return salePriceId;
    }


    public Integer addLabelSalePriceModifyLog(Integer priceId, Integer oldPrice, Integer newPrice, String operator) {
        String sql = "INSERT INTO t_label_sale_price_modify_log (price_id,old_price,new_price,create_time,modify_time,operator)  VALUES(?,?,?,now(),now(),?) ";
        int code = jdbcTemplate.update(sql, new Object[]{priceId, oldPrice, newPrice, operator});
        log.info("新增销售价格日志，sql：" + sql);
        return code;
    }


    public List getIndustryInfoList() {

        List rows = jdbcTemplate.queryForList("SELECT industry_info_id FROM t_industry_info");
        return rows;

    }


    public JSONObject getlistIndustryPoolByCondition(Integer pageNum, Integer pageSize, String name, Integer status, Integer industryPoolId,
                                                     Integer industryPoolType) {
        JSONObject json = new JSONObject();
        List obj = new ArrayList();
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            //条件查询行业标签池
            StringBuffer sb = new StringBuffer();
            sb.append(" SELECT ");
            sb.append(" 	pool.`NAME` as name, ");
            sb.append(" 	pool.industry_pool_id,");
            sb.append(" 	GROUP_CONCAT((SELECT industy_name from t_industry_info where industry_info_id = rel.industry_info_id))  as industyName, ");
            sb.append(" 	pool.label_num AS labelNum, ");
            sb.append(" 	pool. STATUS as status,");
            sb.append(" 	pool.industry_pool_type  AS industryPoolType,");
            sb.append(" 	(SELECT COUNT(distinct industry_pool_id) from t_cust_industry where industry_pool_id = pool.industry_pool_id) AS customerNum,");
            sb.append(" 	pool.creator,");
            sb.append(" 	pool.operator,");
            sb.append(" 	pool.update_time AS updateTime");

            sb.append(" FROM  ");
            sb.append(" 	t_industry_pool pool ");
            sb.append(" LEFT JOIN t_industry_info_rel rel ON pool.industry_pool_id = rel.industry_pool_id ");

            sb.append("  WHERE ");
            sb.append(" 1=1");

            if (!"".equals(name) && name != null) {
                sb.append(" AND  pool.name LIKE ?");
                obj.add(name);
            }

            if (!"".equals(industryPoolId) && industryPoolId != null) {
                sb.append("  AND  pool. industry_pool_id LIKE ?");
//                sb.append(industryPoolId);
//                sb.append("%'");
                obj.add(industryPoolId);
            }

            if (!"".equals(status) && status != null) {
                sb.append("  AND pool.STATUS =?");
//                sb.append(status);
//                sb.append("' ");
                obj.add(status);
            }

            if (!"".equals(industryPoolType) && industryPoolType != null) {
                sb.append("  AND pool.industry_pool_type = ?");
//                sb.append(industryPoolType);
//                sb.append("'");
                obj.add(industryPoolType);
            }

            sb.append(" GROUP BY industry_pool_id");
            //sb.append(" ORDER BY pool.create_time DESC, pool.STATUS DESC");
            sb.append(" ORDER BY pool.create_time DESC ");

//            List total = industryPoolDao.getSQLQuery(sb.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
            List total = jdbcTemplate.queryForList(sb.toString(),obj.toArray());
            sb.append(" LIMIT " + pageNum + "," + pageSize);

            //list = industryPoolDao.getSQLQuery(sb.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
            list = jdbcTemplate.queryForList(sb.toString(),obj.toArray());
            json.put("industryPoolCondition", list);
            json.put("total", total.size());
        } catch (Exception e) {
            log.error("条件查询行业标签池出错！" + e.getMessage());
        }
        return json;
    }


    public Integer getIndustryPoolExist(String name) {

        Integer result = null;
        try {
            List list = jdbcTemplate.queryForList(
                    "SELECT pool.name FROM  t_industry_pool pool WHERE pool.name = ?", new Object[]{name}, String.class);
            result = list.size();
        } catch (Exception e) {
            log.info("查询行业标签池名称是否存在出错");
        }
        return result;
    }


    public String updateIndustryPoolStatus(Integer industryPoolId, Integer status) {

        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE  t_industry_pool pool SET ");

        if (!"".equals(status) && null != status) {
            sb.append(" pool.STATUS=?");
        }

        sb.append(" where pool.industry_pool_id=?");
        int code = jdbcTemplate.update(sb.toString(),
                new Object[]{status, industryPoolId});
        map.put("code", code);
        if (code == 1) {
            map.put("message", "更新成功");
        } else {
            map.put("message", "更新失败");
        }
        log.info("修改行业标签池状态，sql：" + sb.toString());
        json.put("data", map);
        return json.toJSONString();
    }


    public IndustryPoolDTO getIndustryPool(Integer industryPoolId) {

        StringBuffer sql = new StringBuffer
                ("SELECT pool.NAME AS name, pool.industry_pool_id as industryPoolId, pool.STATUS AS status,pool.description,pool.creator,pool.create_time as createTime");
        sql.append(" FROM  t_industry_pool pool  ")
                .append(" WHERE 1=1 ")
                .append(" AND pool.industry_pool_id =?");
        IndustryPoolDTO industryPool = jdbcTemplate.queryForObject(sql.toString(), new IndustryPoolDTO(),
                new Object[]{industryPoolId});

        return industryPool;
    }


    public List<Map<String, Object>> getIndustryInfoById(Integer industryPoolId) {
        List<Map<String, Object>> list = null;
        StringBuffer sql = new StringBuffer
                ("SELECT pool.industry_pool_id,info.industry_info_id,info.industy_name AS industryInfoName");
        sql.append(" FROM  t_industry_pool  pool  ")
                .append(" LEFT JOIN t_industry_info_rel rel ")
                .append(" ON pool.industry_pool_id = rel.industry_pool_id ")
                .append(" INNER JOIN t_industry_info info ")
                .append(" ON info.industry_info_id = rel.industry_info_id ")
                .append(" where 1=1")
                .append(" AND pool.industry_pool_id =?");
        list = jdbcTemplate.queryForList(sql.toString(), new Object[]{industryPoolId});
        return list;
    }


    public List<Map<String, Object>> getSourceById(Integer industryPoolId) {
        List<Map<String, Object>> list = null;
        StringBuffer sql = new StringBuffer
                ("SELECT pool.industry_pool_id, source.source_id,source.source_name as sourceName ");
        sql.append(" FROM  t_industry_pool  pool  ")
                .append(" LEFT JOIN t_industry_pool_source_rel rel ")
                .append(" ON pool.industry_pool_id = rel.industry_pool_id ")
                .append(" INNER JOIN t_source  source ")
                .append(" ON source.source_id = rel.source_id ")
                .append(" where 1=1")
                .append(" AND pool.industry_pool_id =?");
        list = jdbcTemplate.queryForList(sql.toString(), new Object[]{industryPoolId});
        return list;
    }


    public List getLabelListById(Integer industryPoolId) {
        String hql = "select new map(a.id as labelId,a.labelName as labelName) from LabelInfo a , IndustryPoolLabel b where a.id=b.labelId and b.industryPoolId=?";
        return labelDao.find(hql, industryPoolId);

    }


    public String getIndustryLabelsByCondition(IndustryLabelsDTO industryLabelsDTO) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sql = new StringBuffer();

        try {
            Integer pageNum = industryLabelsDTO.getPageNum();
            Integer pageSize = industryLabelsDTO.getPageSize();
            List args=new ArrayList();
            sql.append(" SELECT info.label_name as name,info.path as path,label.industry_label_id as labelId,label.industry_label_id as priceId, label.price/100 as price ")
                    .append(" FROM  t_industry_label label")
                    .append(" INNER JOIN label_info info ON label.label_id= info.id")
                    .append(" WHERE 1=1")
                    .append(" AND label.industry_pool_id =?");//+ industryLabelsDTO.getIndustryPoolId());
            //.append(" AND price.industry_pool_id =" + industryLabelsDTO.getIndustryPoolId())
            //.append(" AND price.source_id  =" + industryLabelsDTO.getSourceId());
            args.add(industryLabelsDTO.getIndustryPoolId());
            if (null != industryLabelsDTO.getSecondCategory() && !"".equals(industryLabelsDTO.getSecondCategory())) {
                sql.append(" AND info.parent_id =?");
                args.add(industryLabelsDTO.getSecondCategory());
            }
            if (!"".equals(industryLabelsDTO.getLabelName()) && null != industryLabelsDTO.getLabelName()) {
                sql.append(" AND   info.label_name LIKE ?");
                args.add( "%"+industryLabelsDTO.getLabelName()+"%");
            }
            if (!"".equals(industryLabelsDTO.getLabelId()) && null != industryLabelsDTO.getLabelId()) {
                sql.append(" AND   label.industry_label_id LIKE ?");
                args.add("%"+industryLabelsDTO.getLabelId()+"%");
            }

            if (null != industryLabelsDTO.getCreateTimeStart() && !"".equals(industryLabelsDTO.getCreateTimeStart()) && null != industryLabelsDTO.getCreateTimeEnd()
                    && !"".equals(industryLabelsDTO.getCreateTimeEnd())) {
                sql.append(" AND label.create_time BETWEEN ? and ? ");
                args.add(industryLabelsDTO.getCreateTimeStart());
                args.add(industryLabelsDTO.getCreateTimeEnd());
            } else {
                if (null != industryLabelsDTO.getCreateTimeStart() && !"".equals(industryLabelsDTO.getCreateTimeStart())) {
                    sql.append(" AND label.create_time > ?");
                    args.add(industryLabelsDTO.getCreateTimeStart());
                }
                if (null != industryLabelsDTO.getCreateTimeEnd() && !"".equals(industryLabelsDTO.getCreateTimeEnd())) {
                    sql.append(" AND label.create_time < ?");
                    args.add(industryLabelsDTO.getCreateTimeEnd());
                }
            }

            sql.append(" ORDER BY label.industry_label_id ASC");

//            List list = industryLabelDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
            List list = jdbcTemplate.queryForList(sql.toString(),args.toArray());
            Integer total = list.size();

            if (list.size() != 0) {
                sql.append(" LIMIT " + pageNum + "," + pageSize);
            }
//            list = industryLabelDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
            list = jdbcTemplate.queryForList(sql.toString(),args.toArray());
            map.put("total", total);
            map.put("IndustryLabel", list);
            json.put("data", map);
            log.info("查看行业行业标签池标签，sql：" + sql + "，list：" + list);
        } catch (Exception e) {
            log.info("查看行业行业标签池标签出错");
        }
        return json.toJSONString();
    }


    public Integer getLabelSalePriceOld(Integer priceId) {

        return jdbcTemplate.queryForObject(
                "SELECT price.price AS a FROM t_industry_label price where price.industry_label_id = ?", new Object[]{priceId}, Integer.class);
    }


    public String updateSalePriceOld(Integer priceId, Integer price) {
        Map<Object, Object> map = new HashMap<Object, Object>();

        try {
            IndustryPoolLabel ipl = (IndustryPoolLabel) this.industryPoolDao.get(IndustryPoolLabel.class, priceId);
            if (ipl != null) {
                ipl.setPrice(price);
                this.industryInfoDao.saveOrUpdate(ipl);
                map.put("code", 1);
                map.put("message", "成功");
            } else {
                map.put("code", 0);
                map.put("message", "失败");
            }
        } catch (Exception e) {
            map.put("code", 0);
            map.put("message", "失败");
        }

        JSONObject json = new JSONObject();

//        StringBuffer sb = new StringBuffer();
//        sb.append("UPDATE t_label_sale_price price SET ");
//
//        if (!"".equals(price) && null != price) {
//            sb.append(" price.price = ? ");
//        }
//
//        sb.append(" where  price.price_id= ?");
//        int code = jdbcTemplate.update(sb.toString(),
//                new Object[]{price, priceId});
//        map.put("code", code);
//        if (code == 1) {
//            map.put("message", "成功");
//        } else {
//            map.put("message", "失败");
//        }
//        log.info("修改行业标签池状态，sql：" + sb.toString());
        json.put("data", map);
        return json.toJSONString();
    }


    public Map<Object, Object> getListLabelSalePriceLog(Integer pageNum, Integer pageSize, Integer priceId) {
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sql = new StringBuffer();
        List obj=new ArrayList();
        try {

            sql.append("SELECT  priceLog.price_log_id as priceLogId,"
                    + "priceLog.operator, "
                    + "priceLog.new_price/100 AS newPrice,"
                    + "date_format(priceLog.create_time, '%Y-%m-%d' ) AS createTime")
                    .append(" FROM  t_label_sale_price_modify_log priceLog  ")
                    .append(" WHERE 1=1");

            if (!"".equals(priceId) && null != priceId) {
                sql.append(" AND   priceLog.price_id = ?");
                obj.add(priceId);
            }

            sql.append(" LIMIT " + pageNum + "," + pageSize);

//            List list = industryLabelDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
            List list = jdbcTemplate.queryForList(sql.toString(),obj.toArray());
            map.put("list", list);
            map.put("total", list.size());
            log.info("查询销售价定价记录，sql：" + sql + "，list：" + list);


        } catch (Exception e) {
            log.info("查询销售价定价记录出错");
        }
        return map;
    }


    public String getIndustryPoolName() {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sql = new StringBuffer();

        try {
            sql.append("select industry_pool_id,NAME from t_industry_pool").append(" WHERE 1=1");
//            List list = industryLabelDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
            List list = jdbcTemplate.queryForList(sql.toString());
            json.put("data", list);
            log.info("查询行业标签池名称，sql：" + sql + "，list：" + list);

        } catch (Exception e) {
            log.info("查询行业标签池名称");
        }
        return json.toJSONString();
    }

    public String getSourceByPoolId(Integer industryPoolId) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sql = new StringBuffer();

        try {
            sql.append(" SELECT pool.industry_pool_id, pool.name,source.source_id, source.source_name ")
                    .append("  FROM  t_industry_pool  pool")
                    .append("  LEFT JOIN t_industry_pool_source_rel rel ON pool.industry_pool_id = rel.industry_pool_id ")
                    .append("  INNER JOIN t_source  source ON source.source_id = rel.source_id ")
                    .append("   WHERE 1=1 AND pool.industry_pool_id = ?" );

//            List list = industryLabelDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
            List list = jdbcTemplate.queryForList(sql.toString(),new Object[]{industryPoolId});
            json.put("data", list);
            log.info("根据行业标签池ID查询数据源，sql：" + sql + "，list：" + list);

        } catch (Exception e) {
            log.info("根据行业标签池ID查询数据源");
        }
        return json.toJSONString();
    }

    public Integer getSalePrice(String groupCondition, Integer industryPoolId) {
        Integer sumPrice = 0;

        // 1查询标签成本价、销售价、订单金额、订单成本价
        JSONArray arr = JSONArray.parseArray(groupCondition);
        StringBuilder labelsString = new StringBuilder();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject json = arr.getJSONObject(i);
            String labelId = json.getString("labelId");
            labelsString.append("'");
            labelsString.append(labelId);
            labelsString.append("'");
            labelsString.append(",");
        }
        // 去掉最后一个逗号
        if (labelsString.length() > 0) {
            labelsString.deleteCharAt(labelsString.length() - 1);
        }
        Map map=new HashMap();
        map.put("industryPoolId",industryPoolId);
        map.put("labelsString",labelsString);
        String hql = "select sum(coalesce(m.price,0)) from IndustryPoolLabel m where m.industryPoolId=:industryPoolId and m.labelId in (select id from LabelInfo where id in (:labelsString))";
//        List list = this.industryPoolDao.createQuery(hql).list();

        List list = industryPoolDao.createQuery(hql,map).list();
        if (list.size() > 0 && list.get(0) != null) {
            sumPrice = Integer.parseInt(String.valueOf(list.get(0)));
        }
        return sumPrice;
    }

    public Integer getSalePriceV1(String groupCondition, Integer industryPoolId, String custId) {
        Integer sumPrice = 0;

        IndustryPool industryPool = (IndustryPool) industryPoolDao.get(IndustryPool.class, industryPoolId);
        String resourceId = null;
        if (industryPool != null) {
            resourceId = String.valueOf(industryPool.getSourceId());
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            CustomerProperty cu = customerDao.getProperty(custId, MarketResourceTypeEnum.LABEL.getPropertyName());
            if (cu != null) {
                JSONArray jsonArray = JSON.parseArray(cu.getPropertyValue());
                JSONObject jsonObject = null;
                for (int i = 0; i < jsonArray.size(); i++) {
                    if (resourceId.equals(jsonArray.getJSONObject(i).getString("resourceId"))) {
                        jsonObject = jsonArray.getJSONObject(i);
                        break;
                    }
                }
                //type：1-按提取条数 2-按标签计费 3-按呼通计费
                if (jsonObject != null) {
                    if (1 == jsonObject.getIntValue("type")) {
                        sumPrice = NumberConvertUtil.changeY2L(jsonObject.getDoubleValue("price"));
                    } else if (2 == jsonObject.getIntValue("type")) {
                        //[{"symbol":0,"leafs":[{"name":"健康","id":92},{"name":"良好","id":93},{"name":"一般","id":94},{"name":"有慢性病","id":96},{"name":"残疾","id":97}],"type":1,"labelId":"91","parentName":"健康状况","path":"人口统计学/基本信息/健康状况"}]
                        JSONArray arr = JSONArray.parseArray(groupCondition);
                        JSONArray labelList;
                        int labelCount = 0;
                        for (int i = 0; i < arr.size(); i++) {
                            labelList = arr.getJSONObject(i).getJSONArray("leafs");
                            labelCount += labelList.size();
                        }
                        sumPrice = NumberConvertUtil.changeY2L(jsonObject.getDoubleValue("price")) * labelCount;
                    } else if (3 == jsonObject.getIntValue("type") || 4 == jsonObject.getIntValue("type")) {
                        sumPrice = 0;
                    }
                } else {
                    log.warn("客户:" + custId + ",标签池ID:" + industryPoolId + "未配置供应商渠道");
                }
            }
        }

        return sumPrice;
    }

    public IndustryPoolPriceDTO getIndustryPoolPrice(String groupCondition, Integer industryPoolId, String custId) {
        IndustryPoolPriceDTO industryPoolPriceDTO = new IndustryPoolPriceDTO();
        industryPoolPriceDTO.setIndustryPoolId(industryPoolId);
        Integer sumPrice = 0;
        industryPoolPriceDTO.setPrice(sumPrice);
        industryPoolPriceDTO.setStatus(2);

        IndustryPool industryPool = (IndustryPool) industryPoolDao.get(IndustryPool.class, industryPoolId);
        String resourceId = null;
        if (industryPool != null) {
            resourceId = String.valueOf(industryPool.getSourceId());
        }
        if (StringUtil.isEmpty(resourceId)) {
            log.warn("客户:" + custId + ",标签池ID:" + industryPoolId + "未配置渠道ID");
            return industryPoolPriceDTO;
        }
        CustomerProperty cu = customerDao.getProperty(custId, MarketResourceTypeEnum.LABEL.getPropertyName());
        if (cu == null || StringUtil.isEmpty(cu.getPropertyValue())) {
            log.warn("客户:" + custId + ",未配置数据资源售价");
            return industryPoolPriceDTO;
        }

        JSONArray jsonArray = JSON.parseArray(cu.getPropertyValue());
        JSONObject jsonObject = null;
        for (int i = 0; i < jsonArray.size(); i++) {
            if (resourceId.equals(jsonArray.getJSONObject(i).getString("resourceId"))) {
                jsonObject = jsonArray.getJSONObject(i);
                break;
            }
        }
        if (jsonObject == null || jsonObject.size() == 0) {
            log.warn("客户:" + custId + ",标签池ID:" + industryPoolId + "未配置供应商渠道");
            return industryPoolPriceDTO;
        }

        // 标识已经定价
        industryPoolPriceDTO.setStatus(1);
        // 定价json字符串
        industryPoolPriceDTO.setDataCustConfig(jsonObject.toJSONString());
        if (1 == jsonObject.getIntValue("type")) {
            sumPrice = NumberConvertUtil.changeY2L(jsonObject.getDoubleValue("price"));
        } else if (2 == jsonObject.getIntValue("type")) {
            JSONArray arr = JSONArray.parseArray(groupCondition);
            JSONArray labelList;
            int labelCount = 0;
            for (int i = 0; i < arr.size(); i++) {
                labelList = arr.getJSONObject(i).getJSONArray("leafs");
                labelCount += labelList.size();
            }
            sumPrice = NumberConvertUtil.changeY2L(jsonObject.getDoubleValue("price")) * labelCount;
        } else if (3 == jsonObject.getIntValue("type") || 4 == jsonObject.getIntValue("type")) {
            // 处理呼通计费和按效果计费
            sumPrice = 0;
        } else {
            industryPoolPriceDTO.setStatus(2);
        }
        industryPoolPriceDTO.setPrice(sumPrice);
        return industryPoolPriceDTO;
    }

    public Integer getCostPriceV1(String groupCondition, Integer industryPoolId) {
        Integer sumPrice = 0;
        IndustryPool industryPool = (IndustryPool) industryPoolDao.get(IndustryPool.class, industryPoolId);
        String resourceId = null;
        if (industryPool != null) {
            resourceId = String.valueOf(industryPool.getSourceId());
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            ResourcePropertyEntity cu = marketResourceDao.getProperty(resourceId, "price_config");
            if (cu != null) {
                JSONObject jsonObject = JSON.parseObject(cu.getPropertyValue());
                //type：1-按条单一计费 2-按条阶梯计费 3-按标签计费 4-按呼通计费 5-按效果计费
                if (jsonObject != null) {
                    if (1 == jsonObject.getIntValue("type")) {
                        sumPrice = NumberConvertUtil.changeY2L(jsonObject.getDoubleValue("price"));
                    } else if (3 == jsonObject.getIntValue("type")) {
                        //[{"symbol":0,"leafs":[{"name":"健康","id":92},{"name":"良好","id":93},{"name":"一般","id":94},{"name":"有慢性病","id":96},{"name":"残疾","id":97}],"type":1,"labelId":"91","parentName":"健康状况","path":"人口统计学/基本信息/健康状况"}]
                        JSONArray arr = JSONArray.parseArray(groupCondition);
                        JSONArray labelList;
                        int labelCount = 0;
                        for (int i = 0; i < arr.size(); i++) {
                            labelList = arr.getJSONObject(i).getJSONArray("leafs");
                            labelCount += labelList.size();
                        }
                        sumPrice = NumberConvertUtil.changeY2L(jsonObject.getDoubleValue("price")) * labelCount;
                    } else if (4 == jsonObject.getIntValue("type") || 5 == jsonObject.getIntValue("type")) {
                        sumPrice = 0;
                    }
                } else {
                    log.warn("资源:" + resourceId + ",标签池ID:" + industryPoolId + "未查询到渠道");
                }
            }
        }

        return sumPrice;
    }

    public Integer getCostPrice0(String groupCondition, Integer industryPoolId, int quantity) {
        Integer sumPrice = 0;
        IndustryPool industryPool = (IndustryPool) industryPoolDao.get(IndustryPool.class, industryPoolId);
        String resourceId = null;
        if (industryPool != null) {
            resourceId = String.valueOf(industryPool.getSourceId());
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            ResourcePropertyEntity cu = marketResourceDao.getProperty(resourceId, "price_config");
            if (cu != null) {
                JSONObject jsonObject = JSON.parseObject(cu.getPropertyValue());
                //type：1-按条单一计费 2-按条阶梯计费 3-按标签计费 4-按呼通计费 5-按效果计费
                if (jsonObject != null) {
                    if (1 == jsonObject.getIntValue("type")) {
                        sumPrice = NumberConvertUtil.changeY2L(jsonObject.getDoubleValue("price"));
                    } else if (2 == jsonObject.getIntValue("type")) {
                        // 阶梯计费
                        int step = jsonObject.getIntValue("step");
                        String stepConfig = jsonObject.getString("step_config");
                        if (StringUtil.isNotEmpty(stepConfig)) {
                            // 查询当前渠道数据提取数量
                            ResourcePropertyEntity crp = marketResourceDao.getProperty(resourceId, "data_extract_sum");
                            int dataExtractSum = 0;
                            if (crp != null) {
                                dataExtractSum = NumberConvertUtil.parseInt(crp.getPropertyValue());
                            }
                            String[] stepConfigs = stepConfig.split("\\|");
                            if (stepConfigs.length == step) {
                                for (int j = 0; j < step; j++) {
                                    // 首次取第一阶梯的价格
                                    if (crp == null) {
                                        sumPrice = NumberConvertUtil.changeY2L(stepConfigs[j].split(",")[2]);
                                        break;
                                    } else {
                                        if (j == step - 1 && NumberConvertUtil.parseInt(stepConfigs[j].split(",")[0]) <= dataExtractSum) {
                                            sumPrice = NumberConvertUtil.changeY2L(stepConfigs[j].split(",")[2]);
                                            break;
                                        } else {
                                            if (NumberConvertUtil.parseInt(stepConfigs[j].split(",")[0]) <= dataExtractSum
                                                    && dataExtractSum <= NumberConvertUtil.parseInt(stepConfigs[j].split(",")[1])) {
                                                sumPrice = NumberConvertUtil.changeY2L(stepConfigs[j].split(",")[2]);
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (crp == null) {
                                    crp = new ResourcePropertyEntity(NumberConvertUtil.parseInt(resourceId), "data_extract_sum", String.valueOf(dataExtractSum + quantity), new Timestamp(System.currentTimeMillis()));
                                }
                                crp.setPropertyValue(String.valueOf(dataExtractSum + quantity));
                                marketResourceDao.saveOrUpdate(crp);
                            }
                        }
                    } else if (3 == jsonObject.getIntValue("type")) {
                        JSONArray arr = JSONArray.parseArray(groupCondition);
                        JSONArray labelList;
                        int labelCount = 0;
                        for (int i = 0; i < arr.size(); i++) {
                            labelList = arr.getJSONObject(i).getJSONArray("leafs");
                            labelCount += labelList.size();
                        }
                        sumPrice = NumberConvertUtil.changeY2L(jsonObject.getDoubleValue("price")) * labelCount;
                    } else if (4 == jsonObject.getIntValue("type") || 5 == jsonObject.getIntValue("type")) {
                        sumPrice = 0;
                    }
                } else {
                    log.warn("资源:" + resourceId + ",标签池ID:" + industryPoolId + "未查询到渠道");
                }
            }
        }

        return sumPrice;
    }

    /**
     * 获取标签池下的标签
     *
     * @param poolId
     * @param custId
     * @return
     * @throws Exception
     */
    public JSONObject getPoolLabels(String poolId, String custId) throws Exception {
        JSONObject result = new JSONObject();
        String sql = " select count(0)num from t_industry_pool pool,t_cust_industry ct " +
                " where pool.industry_pool_id=ct.industry_pool_id " +
                " and ct.cust_id=?" +
                " and ct.industry_pool_id=? and ct.`status`=1 and pool.`status`=3";
        List ls = jdbcTemplate.queryForList(sql,new Object[]{custId,poolId});
//        List ls = industryLabelDao.getSQLQuery(sql).list();

        if (ls == null || "0".equals(ls.get(0).toString())) {
            result.put("errorDesc", "01");
            return result;
        }
        sql = " select  distinct info.id,info.label_id,info.label_name,label.industry_pool_id" +
                " from label_info info LEFT JOIN t_industry_label label on info.id=label.label_id " +
                " where info.level=3 and info.status=3 and info.availably=1 " +
                " and label.status=1  and label.industry_pool_id = ?" ;

//        List list = industryLabelDao.getSQLQuery(sql).list();
        List list =jdbcTemplate.queryForList(sql,new Object []{poolId});
        if (list == null || list.size() == 0) {
            result.put("errorDesc", "01");
            return result;
        }
        List array = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = new HashMap();
            Object[] arr = (Object[]) list.get(i);
            map.put("labelId", arr[0]);
            map.put("labelName", arr[2]);
            String hql = "From LabelInfo t where parentId=?";
            List<LabelInfo> labelChildren = industryPoolDao.find(hql, map.get("labelId"));
            if (labelChildren != null && labelChildren.size() > 0) {
                List<String> child = new ArrayList<>();
                for (LabelInfo label : labelChildren) {
                    child.add(label.getLabelName());
                }
                map.put("lavelValue", child);
            }
            array.add(map);
        }
        result.put("data", array);
        result.put("errorDesc", "00");
        return result;
    }


}
