package com.bdaim.label.service;

import com.alibaba.fastjson.JSON;
import com.bdaim.common.service.InitService;
import com.bdaim.common.spring.SpringContextHelper;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.dataexport.entity.DataExportApply;
import com.bdaim.label.dao.LabelCategoryDao;
import com.bdaim.label.dao.LabelInfoDao;
import com.bdaim.label.entity.*;
import com.bdaim.rbac.entity.User;
import com.bdaim.util.CalendarUtil;
import com.bdaim.util.Constant;
import com.bdaim.util.DateUtil;

import org.hibernate.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service("commonService")
@Transactional
public class CommonService {
    @Resource
    private LabelInfoDao labelInfoDao;
    @Resource
    private LabelInfoService labelInfoService;
    @Resource
    private LabelCategoryDao labelCategoryDao;
    @Resource
    private LabelCategoryService labelCategoryService;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private JdbcTemplate jdbcTemplate;
    public static boolean INIT_CATE_FLAG = false;

    /**
     * 获得精简label
     *
     * @param label
     * @return
     */
    public static Map<String, Object> getSimpleLabelMap(LabelInfo label) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", label.getId());
        map.put("labelName", label.getLabelName());
        map.put("labelId", label.getLabelId());
        map.put("status", label.getStatus());
        Integer dataFormat = label.getDataFormat();
        List<LabelInfo> children = label.getChildren();
        if (null != dataFormat) {
            // List<LabelCategory> cateList = label.getCateList();
            // if(null != cateList){
            // List<Map<String,Object>> mapList = ((LabelCategoryService)
            // SpringContextHelper.getBean("labelCategoryServiceImpl")).getCategoryTree(cateList);
            // map.put("cate", mapList);
            // Map<String,Object> cateMap =
            // getFirstObjectFromTree(mapList,"children");
            // Object idObj = cateMap.get("id");
            // if(null != idObj){
            // filterChildren(children, Integer.valueOf(idObj.toString()));
            // }
            // }
            map.put("dataFormat", dataFormat);
        } else {
            map.put("dataFormat", Constant.DATAFORMAT_COMMON);
        }
        if (null != children && (!children.isEmpty())) {
            map.put("children", getSimpleLabelMapList(children));
        }
        return map;
    }

    public static List<Map<String, Object>> getSimpleLabelMapList(
            List<LabelInfo> labels) {
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        for (LabelInfo label : labels) {
            mapList.add(getSimpleLabelMap(label));
        }
        return mapList;
    }

    /**
     * 将标签实体转换成map结构,填充部分关键字段
     *
     * @param label
     * @return
     */
    public Map<String, Object> getLabelMap(LabelInfo label) {
        Integer id = label.getId();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("labelName", label.getLabelName());
        map.put("path", label.getPath());
        map.put("labelId", label.getLabelId());
        Integer updateCycle = label.getUpdateCycle();
        if (null != updateCycle) {
            map.put("updateCycle", updateCycle);
            map.put("updateCycleCn", Constant.UPDATE_CYCLE_MAP.get(updateCycle));
        }
        map.put("businessMean", label.getBusinessMean());
        map.put("labelRule", label.getLabelRule());
        Integer dataFormat = label.getDataFormat();
        if (null != dataFormat) {
            map.put("dataFormat", label.getDataFormat());
            map.put("dataFormatCn", Constant.DATA_FORMAT_MAP.get(dataFormat));
        }
        Integer labelSource = label.getLabelSource();
        if (null != labelSource) {
            map.put("labelSource", labelSource);
            map.put("labelSourceCn", Constant.LABEL_SOURCE_MAP.get(labelSource));
        }
        long num = label.getCustomerNum() == null ? 0 : label.getCustomerNum();
        long total = label.getTotal() == null ? 0 : label.getTotal();
        if (num > total)
            num = total;
        map.put("customerNum", num);
        map.put("total", total);
        Integer status = label.getStatus();
        if (null != status) {
            map.put("status", status);
            map.put("statusCn", Constant.STATUS_MAP.get(status));
        }
        Integer methodType = label.getMethodType();
        if (null != methodType) {
            map.put("methodType", methodType);
            map.put("methodTypeCn", Constant.METHOD_TYPE_MAP.get(methodType));
        }
        map.put("methodContent",
                label.getMethodContent() == null ? "" : label
                        .getMethodContent());
        map.put("mutex", label.getMutex() == null ? "" : label.getMutex());
        map.put("dimensions",
                label.getDimensions() == null ? "" : label.getDimensions());
        map.put("uri", label.getUri() == null ? "" : label.getUri());
        map.put("config", label.getConfig() == null ? "" : label.getConfig());
        List<LabelInfo> children = label.getChildren();
        if (children == null)
            children = labelInfoService.getChildrenByParentId(id);
        if (null == children || children.size() == 0) {
            map.put("leafCounts", 0);
        } else {
            map.put("leafCounts", children.size());
            map.put("children", getLabelMapList(children));
        }
        String labelContent = label.getLabelContent();
        if (null != labelContent && (!labelContent.isEmpty())) {
            map.put("labelContent", JSON.parseArray(labelContent));
        }
        List<LabelInfo> labels = label.getLabels();
        Integer level = label.getLevel();
        if (null == level) {
            map.put("level", "");
        } else {
            if (level == 3) {
                Integer sigatureCount = 0;
                if (null != children && children.size() > 0) {
                    for (LabelInfo lab : children) {
                        List<LabelInfo> labs = lab.getLabels();
                        if (labs != null) {
                            sigatureCount += labs.size();
                        }
                    }
                }
                map.put("signatureCounts", sigatureCount);
            } else {
                if (null == labels) {
                    map.put("signatureCounts", 0);
                } else if (labels.size() == 0) {
                    map.put("signatureCounts", 0);
                } else {
                    map.put("signatureCounts", labels.size());
                    map.put("signature", getLabelMapList(labels));
                }
            }
            map.put("level", level);
        }

        map.put("viewStatus", Constant.VIEW_STATUS_MAP.get(label.getStatus()));
        map.put("prior", label.getPrior());

        Integer type = label.getType();
        if (null != type) {
            map.put("type", type);
            map.put("typeCn", Constant.TYPE_MAP.get(type));
        }
        Date createTime = label.getCreateTime();
        if (null != createTime) {
            map.put("createTime", CalendarUtil.getDateString(
                    label.getCreateTime(), CalendarUtil.SHORT_DATE_FORMAT));
        }
        User createUser = label.getLabelCreateUser();
        if (null != createUser) {
            map.put("createUser", createUser.getName());
        }
        Date updateTime = label.getUpdateTime();
        if (null != updateTime) {
            map.put("updateTime", CalendarUtil.getDateString(updateTime,
                    CalendarUtil.SHORT_DATE_FORMAT));
        }
        User updateUser = label.getLabelUpdateUser();
        if (null != updateUser) {
            map.put("updateUser", updateUser.getName());
        }
        User offlineUser = label.getLabelOfflineUser();
        if (null != offlineUser) {
            map.put("offlineUser", offlineUser.getName());
        }
        Date offlineTime = label.getOfflineTime();
        if (null != offlineTime) {
            map.put("offlineTime", CalendarUtil.getDateString(offlineTime,
                    CalendarUtil.SHORT_DATE_FORMAT));
        }
        return map;
    }

    /**
     * 获得精简字段的标签列表
     *
     * @param list
     * @return
     */
    public List<Map<String, Object>> getLabelMapList(List<LabelInfo> list) {
        List<Map<String, Object>> labels = new ArrayList<Map<String, Object>>();
        if (null == list || list.size() < 1)
            return labels;
        for (LabelInfo label : list) {
            labels.add(getLabelMap(label));
        }
        return labels;

    }

    /**
     * 将list转换成map结构
     *
     * @param list
     * @return
     */
    public static Map<Integer, Object> convertListToMap(
            List<Map<String, Object>> list) {
        Map<Integer, Object> map = new ConcurrentHashMap<Integer, Object>();
        for (Map<String, Object> dataMap : list) {
            map.put((Integer) dataMap.get("id"), dataMap);
        }
        return map;

    }

    public static class Entity2Tree {
        private Integer id;
        private String labelId;
        private String labelName;
        private String path;
        private Integer level;
        private List<Entity2Tree> children;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getLabelId() {
            return labelId;
        }

        public void setLabelId(String labelId) {
            this.labelId = labelId;
        }

        public String getLabelName() {
            return labelName;
        }

        public void setLabelName(String labelName) {
            this.labelName = labelName;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public List<Entity2Tree> getChildren() {
            return children;
        }

        public void setChildren(List<Entity2Tree> children) {
            this.children = children;
        }
    }

    /**
     * 根据标签结果集获取标签树
     *
     * @param list
     * @return
     */
//	public static String getTreeJsonStringByMap(List<Map<String, Object>> list,
//			Integer root) {
//		Map<Integer, List<Map<String, Object>>> arrayListMap = new HashMap<Integer, List<Map<String, Object>>>();
//
//		for (Map<String, Object> e : list) {
//			Map<String, Object> e2t = new HashMap<String, Object>();
//			for (String key : e.keySet()) {
//				e2t.put(key, e.get(key));
//			}
//			String uri = e.get("uri").toString();
//			String[] ids = uri.split("/");
//			Integer fatherId = Integer.parseInt(ids[ids.length - 1]);
//			if (arrayListMap.get(fatherId) == null) {
//				List<Map<String, Object>> list0 = new ArrayList<Map<String, Object>>();
//				list0.add(e2t);
//				arrayListMap.put(fatherId, list0);
//			} else {
//				List<Map<String, Object>> valueList = arrayListMap
//						.get(fatherId);
//				valueList.add(e2t);
//				arrayListMap.put(fatherId, valueList);
//			}
//		}
//		for (Map.Entry<Integer, List<Map<String, Object>>> entry : arrayListMap
//				.entrySet()) {
//			List<Map<String, Object>> smallTreeList = new ArrayList<Map<String, Object>>();
//			smallTreeList = entry.getValue();
//			int nodeListSize = smallTreeList.size();
//			for (int i = 0; i < nodeListSize; i++) {
//				Integer findID = Integer.parseInt(smallTreeList.get(i)
//						.get("id").toString());
//				List<Map<String, Object>> findList = arrayListMap.get(findID);
//				smallTreeList.get(i).put("children", findList);
//			}
//		}
//		List<Map<String, Object>> rootNodeList = arrayListMap.get(root);
//		if (null == rootNodeList) {
//			return new JSONArray().toString();
//		} else {
//			JSONArray jsonArray = JSONArray.fromObject(rootNodeList);
//			return jsonArray.toString();
//		}
//	}

    /**
     * 将labelinfo列表经过处理返回树形结构 三级标签要带全部子节点 叶子标签要带上一级标签及叶子本身节点
     *
     * @param labels
     * @return
     */
    public List<Map<String, Object>> getSearchLabelTree(
            List<LabelInfo> labels) {
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        Map<Integer, LabelInfo> map = new HashMap<Integer, LabelInfo>();
        for (LabelInfo label : labels) {
            if (label.getType() < 2)
                continue;
            String uri = label.getUri();
            String[] arr = uri.split("/");
            if (arr.length == 3) {
                map.put(label.getId(), label);
            } else if (arr.length == 4) {
                LabelInfo parent = label.getParent();
                if (!map.containsKey(parent.getId())) {
                    LabelInfo leafParent = label.getParent();
                    leafParent.setChildren(new ArrayList<LabelInfo>());
                    leafParent.getChildren().add(label);
                    map.put(leafParent.getId(), leafParent);
                } else {
                    LabelInfo leafParent = map.get(parent.getId());
                    if (!leafParent.getChildren().contains(label)) {
                        leafParent.getChildren().add(label);
                    }
                }
            }
        }
        for (Integer id : map.keySet()) {
            mapList.add(getLabelMap(map.get(id)));
        }
        return mapList;
    }

    public static Map<String, Object> getLabelCategoryMap(LabelCategory category) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (null == category)
            return map;
        map.put("id", category.getId());
        map.put("name", category.getName());
        map.put("categoryId", category.getCategoryId());
        List<LabelCategory> children = category.getChildren();
        if (null != children && (!children.isEmpty())) {
            map.put("children", getLabelCategoryMapList(children));
        }
        return map;
    }

    public static List<Map<String, Object>> getLabelCategoryMapList(
            List<LabelCategory> categorys) {
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        for (LabelCategory category : categorys) {
            mapList.add(getLabelCategoryMap(category));
        }
        return mapList;
    }

    public List<Map<String, Object>> getCustomGroupMapList(
            List<CustomGroup> groups) {
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        for (CustomGroup group : groups) {
            mapList.add(getCustomGroupMap(group));
        }
        return mapList;
    }

    public static List<Map<String, Object>> getCustomMapList(
            List<User> users) {
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        for (User user : users) {
            mapList.add(getCustomerMap(user));
        }
        return mapList;
    }

    /**
     * 根据用户群组对象返回相应map数据
     *
     * @param group
     * @return
     */
    public Map<String, Object> getCustomGroupMap(CustomGroup group) {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> labelMap = new HashMap<String, Object>();
        Integer id = group.getId();
        try {
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (String key : labelMap.keySet()) {
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("key", key);
            dataMap.put("value", labelMap.get(key));
            list.add(dataMap);
        }
        long num = group.getUserCount() == null ? 0 : group.getUserCount();
        long total = group.getTotal() == null ? 0 : group.getTotal();
        if (num > total)
            num = total;
        map.put("customerNum", num);
        map.put("downloadCount", Math.rint(Math.random() * 15000));
        map.put("total", total);
        map.put("id", group.getId());
        map.put("downloadStatus", group.getDownloadStatus() == null ? Integer.valueOf(0) : group.getDownloadStatus());
        map.put("name", group.getName());
        map.put("labels", list);
        String createUserId = group.getCreateUserId();
        if (createUserId != null) {
            map.put("createUser", customerUserDao.getName(createUserId));
        }
        map.put("desc", group.getDesc());
        Date createTime = group.getCreateTime();
        if (null != createTime) {
            map.put("createTime", CalendarUtil.getDateString(createTime,
                    CalendarUtil.SHORT_DATE_FORMAT));
        }
        Integer status = group.getStatus();
        if (null != status) {
            map.put("groupStatus", status);
            map.put("groupStatusCn", Constant.STATUS_MAP.get(status));
        }
        Date updateTime = group.getUpdateTime();
        if (null != updateTime) {
            map.put("updateTime", CalendarUtil.getDateString(updateTime,
                    CalendarUtil.SHORT_DATE_FORMAT));
        }
        map.put("updateUser", group.getUpdateUserId() == null ? "" : customerUserDao.getName(group.getUpdateUserId()));
        map.put("userCount", num);
        map.put("groupCondition", JSON.parseArray(group.getGroupCondition()));
        map.put("api", group.getApi());
        map.put("purpose", group.getPurpose());
        map.put("updateCycle", group.getUpdateCycle());
        map.put("updateCycleCn",
                Constant.UPDATE_CYCLE_MAP.get(group.getUpdateCycle()));
        Date startTime = group.getStartTime();
        if (null != startTime) {
            map.put("startTime", CalendarUtil.getDateString(startTime,
                    CalendarUtil.SHORT_DATE_FORMAT));
        }
        Date endTime = group.getEndTime();
        if (null != endTime) {
            map.put("endTime", CalendarUtil.getDateString(endTime,
                    CalendarUtil.SHORT_DATE_FORMAT));
        }
        return map;
    }

    /**
     * 创建新标签时必填的基本信息
     *
     * @param parentLabel
     * @return
     */
    public static LabelInfo getLabelInfo(LabelInfo parentLabel) {
        Date date = new Date();
        LabelInfo label = new LabelInfo();
        label.setAvailably(Constant.AVAILABLY);
        label.setCreateTime(date);
        label.setUpdateTime(date);
        label.setParent(parentLabel);
        label.setPath(parentLabel.getPath() + parentLabel.getLabelName() + "/");
        label.setUri(parentLabel.getUri() + parentLabel.getId() + "/");
        label.setType(Constant.LABLE_TYPE_BASE);
        label.setLevel(parentLabel.getLevel() + 1);
        return label;
    }
    
    public static void getLabelInfoMap(Map<String, Object> map, LabelInfo linfo) {
        map.put("id", linfo.getId());
        map.put("LabelId", linfo.getLabelId());
        map.put("LabelName", linfo.getLabelName());
    }

    /**
     * @param apply
     * @return
     */
    public static Map<String, Object> getDataExportApplyMap(DataExportApply apply) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", apply.getId());
        map.put("applyReason", apply.getApplyReason());
        map.put("applyReasonDetail", apply.getApplyReasonDetail());
        Integer status = apply.getStatus();
        if (null == status) {
            map.put("applyStatus", null);
        } else {
            map.put("applyStatus", status);
            map.put("applyStatusCn", Constant.STATUS_MAP.get(status));
        }
        Date startTime = apply.getStartTime();
        if (null != startTime) {
            map.put("startTime", CalendarUtil.getDateString(startTime,
                    CalendarUtil.SHORT_DATE_FORMAT));
        }
        Date endTime = apply.getEndTime();
        if (null != endTime) {
            map.put("endTime", CalendarUtil.getDateString(endTime,
                    CalendarUtil.SHORT_DATE_FORMAT));
        }

        Date createTime = apply.getCreateTime();
        if (null != createTime) {
            map.put("createTime", CalendarUtil.getDateString(createTime,
                    CalendarUtil.SHORT_DATE_FORMAT));
        }
        Date updateTime = apply.getUpdateTime();
        if (null != updateTime) {
            map.put("updateTime", CalendarUtil.getDateString(updateTime,
                    CalendarUtil.SHORT_DATE_FORMAT));
        }
        User updateUser = apply.getUpdateUser();
        if (null != updateUser) {
            map.put("updateUser", updateUser);
        }
        return map;
    }

    /**
     * 根据指定的key获取整棵树的第一个节点信息
     *
     * @param list
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getFirstObjectFromTree(
            List<Map<String, Object>> list, String key) {
        if (null == list)
            return null;
        Map<String, Object> map = list.get(0);
        if (map.containsKey(key)) {
            map = getFirstObjectFromTree(
                    (List<Map<String, Object>>) map.get(key), key);
        }
        return map;
    }

    public void init() {
        if (!INIT_CATE_FLAG) {

            try {
                initCategorys();
                // initFirstCategory();
                // initPicinfo();
                INIT_CATE_FLAG = true;
            } catch (Exception e) {
                INIT_CATE_FLAG = false;
                e.printStackTrace();
            }
        }
    }

    private void initPicinfo() {
        // PICTURE_MAP
        List<Map<String, Object>> mapList = jdbcTemplate
                .queryForList("select id as \"id\",label_id as \"label_id\",label_name as \"label_name\",uri as \"uri\",category_id as \"category_id\",path as \"path\" from label_info where \"LEVEL\"<4 ");
        for (Map<String, Object> m : mapList) {
            String labelId = m.get("label_id").toString();
            InitService.PICTURE_MAP.put(labelId, m);
        }
    }

    @SuppressWarnings("unchecked")
    private void initCategorys() {
        String sql = "From LabelCategory";
        List<LabelCategory> categorys = labelCategoryDao.createQuery(sql)
                .list();
        for (LabelCategory category : categorys) {
            InitService.CATEGORY_MAP.put(category.getId(), category);
        }
    }

    private void initFirstCategory() {
        List<Map<String, Object>> mapList = jdbcTemplate
                .queryForList("select \"lid\",\"category_id\" from label_category_rel order by lid asc");
        for (Map<String, Object> m : mapList) {
            Integer lid = Integer.valueOf(m.get("lid").toString());
            Integer cid = Integer.valueOf(m.get("category_id").toString());
            if (InitService.LID_CID_LIST_MAP.containsKey(lid)) {
                InitService.LID_CID_LIST_MAP.get(lid).add(cid);
            } else {
                List<Integer> list = new ArrayList<Integer>();
                list.add(cid);
                InitService.LID_CID_LIST_MAP.put(lid, list);
            }
        }
        List<LabelCategory> list = new ArrayList<LabelCategory>();
        for (Integer lid : InitService.LID_CID_LIST_MAP.keySet()) {
            for (Integer categoryId : InitService.LID_CID_LIST_MAP.get(lid)) {
                if (null != InitService.CATEGORY_MAP.get(categoryId))
                    list.add(InitService.CATEGORY_MAP.get(categoryId));
            }
            List<Map<String, Object>> treeList = labelCategoryService
                    .getCategoryTree(list);
            Object obj = getFirstObjectFromTree(treeList, "children").get(
                    "categoryId");
            InitService.LID_CID_MAP.put(lid, Integer.valueOf(obj.toString()));
            list.clear();
        }
    }

    public static Map<String, Object> getDataNodeMap(DataNode node) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (null == node)
            return map;
        Integer id = (Integer) node.getId();
        String name = node.getName();
        LabelCategory category = ((LabelCategoryService) SpringContextHelper
                .getBean("labelCategoryServiceImpl")).loadLabelCategoryById(id);
        map.put("id", id);
        map.put("name", name);
        map.put("categoryId", node.getId());
        map.put("path", category.getPath() + name);
        map.put("viewStatus", 1);
        map.put("statusCn", "已上线");
        List<DataNode> children = node.getChildren();
        if (null != children && (!children.isEmpty())) {
            map.put("children", getDataNodeMapList(children));
        }
        return map;
    }

    public static List<Map<String, Object>> getDataNodeMapList(
            List<DataNode> nodes) {
        if (null == nodes)
            return new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        for (DataNode node : nodes) {
            mapList.add(getDataNodeMap(node));
        }
        return mapList;
    }

    public static Map<String, Object> getCustomerMap(User info) {
        Map<String, Object> map = new HashMap<>();
        if (null != info) {
            map.put("userName", info.getName());
            map.put("userId", info.getId());
            map.put("mobileNum", info.getMobileNum());
            map.put("enterpriseName", info.getEnterprise_name());
            map.put("createTime", DateUtil.timstampToString(info.getCreateTime(),DateUtil.YYYY_MM_DD_HH_mm_ss));
            map.put("source", info.getSource());
            map.put("status", info.getStatus());
            map.put("customerId",info.getCustId());
        }
        return map;
    }

}
