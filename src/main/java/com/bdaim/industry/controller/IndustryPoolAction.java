package com.bdaim.industry.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.filter.FiledFilter;
import com.bdaim.common.util.AuthPassport;
import com.bdaim.common.util.Constant;
import com.bdaim.common.util.StringUtil;
import com.bdaim.dataexport.service.DataPermissionService;
import com.bdaim.industry.dto.IndustryLabelsDTO;
import com.bdaim.industry.dto.IndustryPoolDTO;
import com.bdaim.industry.service.IndustryPoolService;
import com.bdaim.label.dao.IndustryPoolDao;
import com.bdaim.label.dao.LabelInfoDao;
import com.bdaim.label.dto.Label;
import com.bdaim.label.dto.LabelGroup;
import com.bdaim.label.dto.QueryParam;
import com.bdaim.label.dto.QueryType;
import com.bdaim.label.entity.*;
import com.bdaim.label.service.LabelAuditService;
import com.bdaim.label.service.LabelCategoryService;
import com.bdaim.label.service.LabelInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Controller
@RequestMapping("/industryPool")

/**
 * 行业标签池
 */
public class IndustryPoolAction extends BasicAction {
    @Resource
    private IndustryPoolService industryPoolService;
    @Resource
    private IndustryPoolDao industryPoolDao;
    @Resource
    private LabelCategoryService labelCategoryService;
    @Resource
    private LabelAuditService labelAuditService;
    @Resource
    private DataPermissionService dataPermissionService;
    @Resource
    private LabelInfoService labelInfoService;
    @Resource
    private LabelInfoDao labelInfoDao;

    /**
     * 查看客户行业标签池
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/listIndustryPoolByCustomerId")
    @CacheAnnotation
    public String getIndustryPool() {
        String customerId = opUser().getCustId();
        JSONObject json = industryPoolService.getIndustryPoolV1(customerId);
        return json.toJSONString();
    }

    public IndustryPoolAction() {
        super.pageName = "标签体系";
    }

    /**
     * 根据id查询标签
     *
     * @return
     */
    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/getLabelById")
    // @CacheAnnotation
    public String getLabelById(Integer id, HttpServletRequest request) {
        LabelInfo label = labelInfoService.get(id);
        return JSON.toJSONString(super.commonService.getLabelMap(label), new FiledFilter());
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public String getLabelById(@PathVariable Integer id) {
        LabelInfo label = labelInfoService.get(id);
        return JSON.toJSONString(label.getChildren(), new FiledFilter());
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public String deleteLabelById(@PathVariable Integer id) {
        LabelInfo label = labelInfoService.get(id);
        return JSON.toJSONString(label.getChildren(), new FiledFilter());
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/getLabelDetailById")
    public String getLabelDetailById(HttpServletRequest request, Integer id) {
        LabelInfo label = labelInfoService.get(id);
        Map<String, Object> map = super.commonService.getLabelMap(label);
        String hql = "from LabelCover t where t.label.id=" + label.getId();
        List<LabelCover> covers = industryPoolDao.createQuery(hql).list();
        if (null != covers && covers.size() > 0) {
            map.put("customerNum", covers.get(0).getCoverNum());
            map.put("total", covers.get(0).getTotal());
        }

        // operation log
//		super.operlog(request, label.getId());

        return JSON.toJSONString(map);
    }

    /**
     * **** 1、根据条件查询左侧标签分类 ******
     */
    @ResponseBody
    @RequestMapping("/listLabelsByCondition")
    @CacheAnnotation
    public String getLabelsByCondition(HttpServletRequest request, LabelInfo labelInfo, QueryParam params, Integer poolId) {

        //labelId, cycle, userId, poolId
        LoginUser lu = opUser();
        List<LabelGroup> lgs = new ArrayList();
        List<LabelInfo> labelInfos = null;
        Integer id = labelInfo.getId();
        if (id == null) {
            String hql = "from LabelInfo m where parentId is null and status=3 and availably=1 and attr_id is null and m.id in (select distinct a.parent.parentId from LabelInfo a,IndustryPoolLabel b where a.id=b.labelId and b.industryPoolId='" + poolId + "')";
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole()))
                hql = "from LabelInfo m where parentId is null and status=3 and availably=1 and attr_id is null";
            labelInfos = labelInfoService.query(hql);
        } else {
            String hql = "from LabelInfo m where parentId=? and status=3 and availably=1 and attr_id is null and m.id in (select distinct a.parentId from LabelInfo a,IndustryPoolLabel b where a.id=b.labelId and b.industryPoolId='" + poolId + "')";
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole()))
                hql = "from LabelInfo m where parentId=? and status=3 and availably=1 and attr_id is null";
            labelInfos = labelInfoService.query(hql, id);
        }
        for (int i = 0; i < labelInfos.size(); i++) {
            LabelInfo li = labelInfos.get(i);
            LabelGroup lg = new LabelGroup(li);
            lgs.add(lg);

            if (li.getLevel() != null && li.getLevel() == 1 && i == 0) {//一级节点
                String hql_2 = "from LabelInfo m where parentId=? and status=3 and availably=1 and attr_id is null and m.id in (select distinct a.parentId from LabelInfo a,IndustryPoolLabel b where a.id=b.labelId and b.industryPoolId='" + poolId + "')";
                if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole()))
                    hql_2 = "from LabelInfo m where parentId=? and status=3 and availably=1 and attr_id is null ";
                List<LabelInfo> labelInfos_2 = labelInfoService.query(hql_2, li.getId());
                for (LabelInfo li_2 : labelInfos_2) {
                    LabelGroup lg_2 = new LabelGroup(li_2);
                    lg.addChile(lg_2);

                    String hql_3 = "from LabelInfo m where parentId=? and status=3 and availably=1 and attr_id is null and m.id in (select distinct b.labelId from IndustryPoolLabel b where b.industryPoolId='" + poolId + "')";
                    if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole()))
                        hql_3 = "from LabelInfo m where parentId=? and status=3 and availably=1 and attr_id is null ";
                    List<LabelInfo> labelInfos_3 = labelInfoService.query(hql_3, li_2.getId());
                    for (LabelInfo li_3 : labelInfos_3) {
                        LabelGroup lg_3 = new LabelGroup(li_3);
                        lg_2.addChile(lg_3);
                    }
                }
            } else if (li.getLevel() != null && li.getLevel() == 2) {//二级节点
                String hql_3 = "from LabelInfo m where parentId=? and status=3 and availably=1 and attr_id is null and m.id in (select distinct b.labelId from IndustryPoolLabel b where b.industryPoolId='" + poolId + "')";
                if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole()))
                    hql_3 = "from LabelInfo m where parentId=? and status=3 and availably=1 and attr_id is null ";
                List<LabelInfo> labelInfos_3 = labelInfoService.query(hql_3, li.getId());
                for (LabelInfo li_3 : labelInfos_3) {
                    LabelGroup lg_3 = new LabelGroup(li_3);
                    lg.addChile(lg_3);
                }
            }

        }

        JSONObject json = new JSONObject();
        json.put("stores", lgs);
        return json.toJSONString();
    }

    @ResponseBody
    @RequestMapping("/getAllLabelsByCategoryId")
    @CacheAnnotation
    public String getAllLabelsByCategoryId(Integer id) {
        String result = null;
        List<Map<String, Object>> list = industryPoolService.getAllLabelsByCategoryId(id);
        result = JSONArray.toJSONString(list);
        return result;
    }

    /**
     * 2、根据标签分类 查询标签值
     */
    @ResponseBody
    @RequestMapping("/listLabelsChildrenById")
    @CacheAnnotation
    public String getChildrenById(String poolId, Integer id, String status, Integer cycle
            , HttpServletRequest request, String queryType, String queryKey, Integer type, Integer categoryFlag, Boolean isLogAvailably) {

        if (id == null || id == 0)
            return null;

        String mineFlag = request.getParameter("mine");
        Map<String, Object> orLikeMap = new HashMap<String, Object>();
        if (null != queryKey && !"".equals(queryKey)) {
            if (null == queryType) {
                orLikeMap.put("labelName", queryKey);
                orLikeMap.put("labelRule", queryKey);
            } else {
                if (queryType.equals("labelName") || queryType.equals("all")) {
                    orLikeMap.put("labelName", queryKey);
                }
                if (queryType.equals("labelRule") || queryType.equals("all")) {
                    orLikeMap.put("labelRule", queryKey);
                }
            }
        }

        List<Label> labels = labelInfoService.childrenV1(poolId, id, status, cycle, queryType, queryKey, type, categoryFlag, isLogAvailably);

        return JSON.toJSONString(labels);
    }


    /**
     * 3、通过标签ID 获取标签值
     */
    @ResponseBody
    @RequestMapping("/values")
    @CacheAnnotation
    public String children(Integer id, String status, HttpServletRequest request) {
        if (id == null || id == 0)
            return null;

        List<Label> labels = labelInfoService.values(id, status);

        return JSON.toJSONString(labels);
    }


    @ResponseBody
    @RequestMapping("/getChildrenByIdAndLevel")
    @CacheAnnotation
    public String getChildrenByIdAndLevel(Integer pid, Integer level) {
        List<Map<String, Object>> list = industryPoolService.getChildrenByIdAndLevel(pid, level);
        JSONObject json = new JSONObject();
        json.put("stores", list);
        return json.toJSONString();
    }

    @ResponseBody
    @RequestMapping("/addBaseLabelInfo")
    @CacheAnnotation
    public String addBaseLabelInfo(LabelInfo label, HttpServletRequest request) {
        String parentId = request.getParameter("parentId");
        String labelName = label.getLabelName();
        String cateId = request.getParameter("parentCategory.id");
        if (null == parentId)
            throw new NullPointerException("标签分类不允许为空");
        boolean isExist = industryPoolService.isExistLabelName(Integer.valueOf(parentId), labelName);
        if (!isExist)
            throw new RuntimeException("标签名称重复,请确认");
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (null != cateId && (!cateId.isEmpty())) {
                LabelCategory cate = labelCategoryService.loadLabelCategoryById(Integer.valueOf(cateId));
                label.setParentCategory(cate);
            }
            Date date = new Date();
            LabelInfo parentLabel = labelInfoService.get(Integer.valueOf(parentId));
            label.setParent(parentLabel);
            label.setPath(parentLabel.getPath() + parentLabel.getLabelName() + "/");
            label.setUri(parentLabel.getUri() + parentLabel.getId() + "/");
            label.setCreateTime(date);
            label.setUpdateTime(date);
            label.setAvailably(Constant.AVAILABLY);
            label.setCreateUid(opUser().getId());
            label.setLabelCreateUser(opUser().getUser());
            label.setLabelUpdateUser(opUser().getUser());
            label.setLevel(parentLabel.getLevel() + 1);
            label.setStatus(Constant.AUDITING);
            Integer lid = industryPoolService.addBaseLabel(label);
            map.put("labelId", lid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JSON.toJSONString(map);
    }

    @ResponseBody
    @RequestMapping(value = "/updateBaseLabelInfo", method = RequestMethod.POST)
    @CacheAnnotation
    public String updateBaseLabelInfo(LabelInfo label, boolean isNameChanged, String parentId) {
        if (null == parentId)
            throw new NullPointerException("标签分类不允许为空");
        if (isNameChanged) {
            boolean isExist = industryPoolService.isExistLabelName(Integer.valueOf(parentId), label.getLabelName());
            if (!isExist)
                throw new RuntimeException("标签名称重复,请确认");
        }
        LabelInfo labelInfo = labelInfoService.get(label.getId());
        labelInfo.setLabelName(label.getLabelName());
        labelInfo.setBusinessMean(label.getBusinessMean());
        labelInfo.setLabelRule(label.getLabelRule());
        labelInfo.setLabelUpdateUser(opUser().getUser());
        labelInfo.setUpdateTime(new Date());
        industryPoolService.updateLabelInfo(labelInfo);
        return "{}";
    }

    @ResponseBody
    @RequestMapping("/addSignatureLabelInfo")
    @CacheAnnotation
    public String addSignatureLabelInfo(HttpServletRequest request, LabelInfo label, QueryParam params) {
        String parentId = request.getParameter("parentId");
        if (null == parentId)
            throw new NullPointerException("组合标签分类不允许为空");
        String labelName = label.getLabelName();
        boolean isExist = industryPoolService.isExistLabelName(Integer.valueOf(parentId), labelName);
        if (!isExist)
            throw new RuntimeException("标签名称重复,请确认");
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Date date = new Date();
            LabelInfo parentLabel = labelInfoService.get(Integer.valueOf(parentId));
            label.setType(Constant.LABLE_TYPE_SIGNATURE);
            label.setParent(parentLabel);
            label.setPath(parentLabel.getPath() + parentLabel.getLabelName() + "/");
            label.setUri(parentLabel.getUri() + parentLabel.getId() + "/");
            label.setCreateTime(date);
            label.setUpdateTime(date);
            label.setAvailably(Constant.AVAILABLY);
            label.setCreateUid(opUser().getId());
            label.setLabelUpdateUser(opUser().getUser());
            label.setLevel(parentLabel.getLevel() + 1);
            label.setLabelCreateUser(opUser().getUser());
            String ids = label.getIds();
            JSONArray arr = JSON.parseArray(ids);
            List<LabelInfo> labels = new ArrayList<LabelInfo>();
            for (int i = 0; i < arr.size(); i++) {
                labels.add(labelInfoDao.get(arr.getInteger(i)));
            }
            label.setSignatures(labels);
            Integer cycle = params.getCycle();
            Integer lid = industryPoolService.addSignatureLabel(label, cycle);
            map.put("labelId", lid);
            map.put("_message", "组合标签保存成功");
            // operation log
//			super.operlog(request, lid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return JSON.toJSONString(map);
    }

    @ResponseBody
    @RequestMapping("/offlineLabel")
    @CacheAnnotation
    public String offlineBaseLabel(LabelAudit audit) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            audit.setApplyUser(opUser().getUser());
            if (audit.getApplyType().equals(Constant.APPLY_TYPE_SIGNATURE_OFFLINE)) {
                audit.setAuditType(Constant.AUDIT_TYPE_SIGNATURE);
            } else {
                audit.setAuditType(Constant.AUDIT_TYPE_LABEL);
            }
            audit.setAvailably(Constant.AVAILABLY);
            audit.setStatus(Constant.AUDITING);
            audit = labelAuditService.getLabelAudit(audit, null);
            labelAuditService.addAuditInfo(audit);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JSON.toJSONString(map);
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/previewSignatureLabel")
    public String previewSignatureLabel(Integer cycle, LabelInfo label) {
        try {
            JSONObject result = new JSONObject();
            // 如果周期不存在，默认设置为0
            Map<String, Object> map = industryPoolService
                    .previewSignatureLabel(cycle == null ? Integer.valueOf(0) : cycle, label);
            if (map == null) {
                result.put("code", 300);
                result.put("_message", "组合标签预览失败！");
            } else {
                result.put("code", 200);
                result.put("_message", "组合标签预览成功！");
                JSONObject data = new JSONObject();
                data.put("store", map);
                result.put("data", data);
            }
            return JSON.toJSONString(result);
        } catch (Exception ex) {
            JSONObject result = new JSONObject();
            result.put("code", 300);
            result.put("_message", "规则错误");
            ex.printStackTrace();
            return JSON.toJSONString(result);
        }
    }

    @ResponseBody
    @RequestMapping("/isExistLabelName")
    @CacheAnnotation
    public String isExistLabelName(@RequestParam(required = true) Integer pid,
                                   @RequestParam(required = true) String labelName) {
        if (null == pid || null == labelName) {
            throw new RuntimeException("上一级分类或者标签名称不存在");
        }
        JSONObject obj = new JSONObject();
        obj.put("isExist", industryPoolService.isExistLabelName(pid, labelName));
        return obj.toString();
    }

    @ResponseBody
    @RequestMapping("/isAvailabeLabel")
    @CacheAnnotation
    public String isAvailabeLabel(String ids) {
        if (null == ids || ids.isEmpty())
            throw new RuntimeException("待校验标签信息为空,请确认");
        List<Integer> idList = industryPoolService.isAvailabeLabel(ids);
        return JSON.toJSONString(idList);
    }

    private List<Map<String, Object>> getLabelTreeWithPermission(List<Map<String, Object>> mapList) {
        List<DataNode> lst = dataPermissionService.getLabelList(opUser().getId(), 1, null, QueryType.PRIVILEGE);
        for (Iterator it = mapList.iterator(); it.hasNext(); ) {
            Map<String, Object> map = (Map<String, Object>) it.next();
            boolean b = false;
            for (DataNode dn : lst) {
                if (dn == null)
                    continue;
                String labelId = (String) map.get("labelId");
                String labelId2 = dn.getLabelId();
                if (labelId.equals(labelId2)) {
                    b = true;
                }
            }
            if (!b) {
                it.remove();
            }
        }

        return mapList;
    }

    private String getTreeDataById(HttpServletRequest request, LabelInfo labelInfo, QueryParam params, Integer poolId) {
        String queryType = params.getQueryType();
        String queryKey = params.getKey();
        String mineFlag = request.getParameter("mine");
        Integer categoryFlag = params.getCategoryFlag();
//		UserManager manager = new UserManagerImpl();
        Map<String, Object> orLikeMap = new HashMap<String, Object>();
        if (null != queryKey && (!queryKey.isEmpty())) {
            if (null == queryType) {
                orLikeMap.put("labelName", queryKey);
                orLikeMap.put("labelRule", queryKey);
            } else {
                if (queryType.equals("labelName") || queryType.equals("all")) {
                    orLikeMap.put("labelName", queryKey);
                }
                if (queryType.equals("labelRule") || queryType.equals("all")) {
                    orLikeMap.put("labelRule", queryKey);
                }
            }
        }
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> andLikeMap = new HashMap<String, Object>();
        LabelInfo label = labelInfoService.get(labelInfo.getId());
        Integer level = label.getLevel();
        if (labelInfo != null) {
            if (labelInfo.getStatus() != null) {
                map.put("status", labelInfo.getStatus());
            }
            if (null != mineFlag && mineFlag.equals("on")) {
                map.put("labelCreateUser.id", opUser().getId());
            }
            if (labelInfo.getParentCategory() != null) {
                map.put("parentCategory.id", labelInfo.getParentCategory().getId());
            }
            //poolId
            map.put("poolId", poolId);
            if (null == categoryFlag || categoryFlag == 0) {
                map.put(Constant.FILTER_KEY_PREFIX + "id", label.getId());
            }
        }
        LoginUser user = opUser();
        map.put(Constant.FILTER_KEY_PREFIX + "user", user);
        List<Map<String, Object>> list = industryPoolService.getTreeDataByMap(map, orLikeMap, andLikeMap);
        if (null != categoryFlag && categoryFlag == 1) {
            if (null != list && list.size() > 0 && level == 3) {
                list = (List<Map<String, Object>>) list.get(0).get("children");
            }
        }
        JSONObject json = new JSONObject();
        json.put("stores", list);

        // operation log
//		super.operlog(request, list);

        return json.toString();
    }

    /**
     * 新增行业标签池
     *
     * @param labelIds          标签id数组
     * @param name              行业标签池名称
     * @param description       描述
     * @param industryInfoId    行业id
     * @param sourceDetail      行业id
     * @param allIndustryStatus true-全部行业 false-部分行业
     * @return
     */
    @SuppressWarnings("null")
    @RequestMapping(value = "/addIndustryPool", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String addIndustryPool(@RequestBody JSONObject jsonO) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();

        JSONArray labelIds = jsonO.getJSONArray("labelIds");
        String name = jsonO.getString("name");
        String description = jsonO.getString("description");
        JSONArray industryInfoId = jsonO.getJSONArray("industryInfoId");

        String resourceId = jsonO.getString("resourceId");
        Integer autoExtraction = jsonO.getInteger("autoExtraction");

        //"admin";
        String operator = String.valueOf(opUser().getId());
        // 是否为全部行业
        boolean allIndustryStatus = jsonO.getBooleanValue("allIndustryStatus");

        try {
            Integer induPoolId = null;
            Integer labelLength = 1;
            // 1.将行业标签(名字和描述)保存在行业标签池中，同时返回行业标签池ID
            // (1).计算行业标签的数量
            if (labelIds != null || labelIds.size() != 0) {
                labelLength = labelIds.size();
            }
            // (2).保存名字和描述在行业标签池中，并且返回存入的ID
            induPoolId = industryPoolService.addIndustryPoolInfoV1(name, description, labelLength, operator, resourceId,autoExtraction);
            if (allIndustryStatus) {
                // -1标识全部行业
                industryPoolService.addIndustryInfoRel(induPoolId, -1);
            } else {
                for (int i = 0; i < industryInfoId.size(); i++) {
                    Integer industryInfoIdOne = Integer.parseInt(industryInfoId.getString(i));
                    industryPoolService.addIndustryInfoRel(induPoolId, industryInfoIdOne);
                }
            }

            // 3.将行业标签池ID和和已选标签保存在行业池和行业标签对应中
            if (labelIds != null || labelIds.size() != 0) {
                for (int j = 0; j < labelIds.size(); j++) {
                    String labelIdOne = labelIds.getString(j);
                    industryPoolService.addIndustryLabel(induPoolId, labelIdOne);
                    // 6.将行业标签ID和初始价格放入价格日志表中。
//					industryPoolService.addLabelSalePriceModifyLog(labelSalePriceId, priceData, priceData, operator);
                }
                // 4.将行业标签池ID和数据源保存在数据源中对应关系中
//				for (int i = 0; i < sourceDetailJson.size(); i++) {
//					
//					JSONObject jsonObject2 = sourceDetailJson.getJSONObject(i);
//					Integer sourceId = Integer.parseInt(jsonObject2.getString("sourceId"));
//					String sourceName = jsonObject2.getString("sourceName");
//					industryPoolService.addIndustryPoolSourceRel(sourceName, sourceId, InduPoolId);

                // 价格和行业标签及数据源相对应
//					for (int j = 0; j < labelIds.size(); j++) {
//						String labelIdOne = labelIds.getString(j);
                // 5.将行业标签ID和初始价格存入标签销售定价表中并且返回销售价ID。
//						float price = (float) 5.55;// 销售价格暂定
                //将价格乘100后存入数据库，取出数据后除以100
//						Integer priceData =Integer.valueOf((int)(price*100)) ;

//						Integer labelSalePriceId = industryPoolService.addLabelSalePrice(labelIdOne, sourceId,InduPoolId, priceData);

//						// 6.将行业标签ID和初始价格放入价格日志表中。
//						industryPoolService.addLabelSalePriceModifyLog(labelSalePriceId, priceData, priceData, operator);
//					}
//				}

            }
            map.put("code", 1);
            map.put("message", "添加成功");
            json.put("data", map);
        } catch (Exception e) {
            map.put("code", 2);
            map.put("message", "添加失败");
            json.put("data", map);
        }
        return json.toJSONString();
    }

    /**
     * 条件查询行业标签池
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/listIndustryPoolByCondition", method = RequestMethod.GET)
    @CacheAnnotation
    public String getIndustryPoolByCondition(Integer pageNum, Integer pageSize, String name, Integer status, Integer industryPoolId,
                                             Integer industryPoolType) {
        JSONObject json = industryPoolService.getlistIndustryPoolByCondition(pageNum, pageSize, name, status, industryPoolId,
                industryPoolType);
        return json.toJSONString();
    }

    /**
     * 查看行业标签池名称是否存在
     *
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/getIndustryPoolExist", method = RequestMethod.GET)
    @CacheAnnotation
    public String getIndustryPoolExist(String name) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();

        Integer status = industryPoolService.getIndustryPoolExist(name);

        if (status == 0) {// 1:存在;2:不存在
            map.put("status", 2);
        } else {
            map.put("status", 1);
        }
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 修改行业标签池状态
     *
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/updateIndustryPoolStatus", method = {RequestMethod.PUT,RequestMethod.POST})
    @CacheAnnotation
    public String updateIndustryPoolStatus(Integer industryPoolId, Integer status) {

        return industryPoolService.updateIndustryPoolStatus(industryPoolId, status);
    }

    /**
     * 查看行业标签池详情
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getIndustryPoolDetailById", method = RequestMethod.GET)
    @CacheAnnotation
    public String getIndustryPoolDetailById(Integer industryPoolId) {

        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>(); // 存放最终拼装的参数
        // 1.查询行业池
        IndustryPoolDTO industryPoolDTO = industryPoolService.getIndustryPool(industryPoolId);

        map.put("industryPoolId", industryPoolDTO.getIndustryPoolId());
        map.put("name", industryPoolDTO.getName());
        map.put("description", industryPoolDTO.getDescription());
        map.put("status", industryPoolDTO.getStatus());
        map.put("creator", industryPoolDTO.getCreator());

        Date createTime = industryPoolDTO.getCreateTime();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(createTime);
        map.put("createTime", dateStr);

        // 2.查询适用行业list(industryInfo)
        List<Map<String, Object>> industryInfo = industryPoolService.getIndustryInfoById(industryPoolId);

        // 3.查询数据源list(source)
        List<Map<String, Object>> source = industryPoolService.getSourceById(industryPoolId);

        // 4.查询已选标签list(labelList)
        List labelList = industryPoolService.getLabelListById(industryPoolId);

        map.put("industryInfo", industryInfo);
        map.put("source", source);
        map.put("labelList", labelList);

        json.put("json", map);
        return json.toJSONString();

    }

    /**
     * 查询行业标签池标签
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/listIndustryLabelsByCondition", method = RequestMethod.GET)
    @CacheAnnotation
    public String getlistIndustryLabelsByCondition(Integer pageNum, Integer pageSize, Integer industryPoolId,
                                                   Integer sourceId, Integer secondCategory, String labelName, String labelId, String createTimeStart,
                                                   String createTimeEnd) {

        IndustryLabelsDTO industryLabelsDTO = new IndustryLabelsDTO();

        industryLabelsDTO.setPageNum(pageNum);
        industryLabelsDTO.setPageSize(pageSize);
        industryLabelsDTO.setIndustryPoolId(industryPoolId);
        industryLabelsDTO.setSourceId(sourceId);
        industryLabelsDTO.setSecondCategory(secondCategory);
        industryLabelsDTO.setLabelName(labelName);
        industryLabelsDTO.setLabelId(labelId);
        industryLabelsDTO.setCreateTimeStart(createTimeStart);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 小写的mm表示的是分钟
        Date date = null;
        try {
            date = sdf.parse(createTimeEnd);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, 1);// 把日期往后增加一天
            date = calendar.getTime();
        } catch (ParseException e) {

        }

        if (date != null)
            industryLabelsDTO.setCreateTimeEnd(sdf.format(date));

        return industryPoolService.getIndustryLabelsByCondition(industryLabelsDTO);
    }

    /**
     * 设置销售价
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateLabelSalePrice", method = {RequestMethod.PUT,RequestMethod.POST})
    @CacheAnnotation
    public String updateLabelSalePrice(Integer priceId, float price) {

        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();

        String operator = String.valueOf(opUser().getId()); //"admin";

        try {

            //将价格乘100后存入数据库，取出数据后除以100
            Integer priceData = Integer.valueOf((int) (price * 100));
            // 1.查询销售价格表的价格
            Integer oldPrice = industryPoolService.getLabelSalePriceOld(priceId);
            // 2.将销售价格表价格更新
            industryPoolService.updateSalePriceOld(priceId, priceData);
            // 3.将销售价格的记录增加到销售价格日志表中
            industryPoolService.addLabelSalePriceModifyLog(priceId, oldPrice, priceData, operator);

            map.put("codeStaus", 1);
            map.put("message", "更新价格成功");

        } catch (Exception e) {
            map.put("codeStatus", 2);
            map.put("message", "更新价格失败");
        }
        json.put("data", map);
        return json.toJSONString();

    }


    /**
     * 查询销售价定价记录
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/listLabelSalePriceLog", method = RequestMethod.GET)
    @CacheAnnotation
    public String getListLabelSalePriceLog(Integer pageNum, Integer pageSize, Integer priceId) {
        Map<Object, Object> map = industryPoolService.getListLabelSalePriceLog(pageNum, pageSize, priceId);
        JSONObject json = new JSONObject();
        json.put("data", map);
        return json.toJSONString();
    }


    /**
     * 查询行业标签池名称(全部)
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getindustryPoolName", method = RequestMethod.GET)
    @CacheAnnotation
    public String getIndustryPoolName() {

        return industryPoolService.getIndustryPoolName();
    }

    /**
     * 根据行业标签池ID查询数据源
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getSourceByPoolId", method = RequestMethod.GET)
    @CacheAnnotation
    public String getSourceByPoolId(Integer industryPoolId) {

        return industryPoolService.getSourceByPoolId(industryPoolId);
    }


    /**
     * 设置销售价
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateLabelSalePrice", method = RequestMethod.GET)
    @CacheAnnotation
    public String updateLabelSalePrice(Integer priceId, double price) {

        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();

        String operator = String.valueOf(opUser().getId()); //"admin";

        try {

            //将价格乘100后存入数据库，取出数据后除以100
            Integer priceData = (int) (price * 100);
            // 1.查询销售价格表的价格
            Integer oldPrice = industryPoolService.getLabelSalePriceOld(priceId);
            // 2.将销售价格表价格更新
            industryPoolService.updateSalePriceOld(priceId, priceData);
            // 3.将销售价格的记录增加到销售价格日志表中
            industryPoolService.addLabelSalePriceModifyLog(priceId, oldPrice, priceData, operator);

            map.put("codeStaus", 1);
            map.put("message", "更新价格成功");

        } catch (Exception e) {
            map.put("codeStatus", 2);
            map.put("message", "更新价格失败");
        }
        json.put("data", map);
        return json.toJSONString();

    }

    /**
     * 查询指定标签池下的标签定义
     * @param poolId
     */
    @AuthPassport
    @RequestMapping(value = "/labels", method = RequestMethod.GET)
    public void getPoolLabels(String poolId, HttpServletResponse response){
        JSONObject result=new JSONObject();
        response.setContentType("application/json; charset=utf-8");
        if(StringUtil.isEmpty(poolId)){
            result.put("errorDesc","02");
            try {
                response.getWriter().write(result.toJSONString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        LoginUser lu=opUser();
        if(lu==null || StringUtil.isEmpty(lu.getCustId())){
            result.put("errorDesc","04");
            try {
                response.getWriter().write(result.toJSONString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        String custId = lu.getCustId();
        try {
            result = industryPoolService.getPoolLabels(poolId,custId);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("errorDesc","05");
        }
        try {
            response.getWriter().write(result.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
