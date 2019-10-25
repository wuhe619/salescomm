package com.bdaim.label.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.filter.FiledFilter;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.dataexport.service.DataPermissionService;
import com.bdaim.label.dto.Label;
import com.bdaim.label.dto.LabelGroup;
import com.bdaim.label.dto.QueryParam;
import com.bdaim.label.dto.QueryType;
import com.bdaim.label.entity.*;
import com.bdaim.label.service.LabelCategoryService;
import com.bdaim.label.service.LabelInfoService;
import com.bdaim.util.AuthPassport;
import com.bdaim.util.Constant;
import com.bdaim.util.StringUtil;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/label")
/*
 * 标签
 */
public class LabelAction extends BasicAction {
    @Resource
    private LabelInfoService labelInfoService;
    @Resource
    private LabelCategoryService labelCategoryService;
    @Resource
    private DataPermissionService dataPermissionService;
    @Resource
    private CustomGroupService groupService;


    public LabelAction() {
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
        LabelInfo label = labelInfoService.getLabelById(id);
        return JSON.toJSONString(super.commonService.getLabelMap(label),
                new FiledFilter());
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public String getLabelById(@PathVariable Integer id) {
        LabelInfo label = labelInfoService.getLabelById(id);
        return JSON.toJSONString(label.getChildren(), new FiledFilter());
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/getLabelDetailById")
    public String getLabelDetailById(HttpServletRequest request, Integer id) {
        LabelInfo label = labelInfoService.getLabelById(id);
        Map<String, Object> map = super.commonService.getLabelMap(label);

        return JSON.toJSONString(map);
    }

    /**
     * 1、查询标签分类
     */
    @ResponseBody
    @RequestMapping("/getLabelsByCondition")
    @CacheAnnotation
    public String getLabelsByCondition(HttpServletRequest request, LabelInfo labelInfo, QueryParam params) {
        //labelId, cycle, userId, poolId
        LoginUser user = opUser();
        List<LabelGroup> lgs = new ArrayList();
        List<LabelInfo> labelInfos = null;
        Integer id = labelInfo.getId();
        if (id == null) {
            labelInfos = labelInfoService.query("from LabelInfo where parentId is null and status=3 and availably=1 and attr_id is null");
        } else {
            labelInfos = labelInfoService.query("from LabelInfo where parentId=? and status=3 and availably=1 and attr_id is null", id);
        }
        for (int i = 0; i < labelInfos.size(); i++) {
            LabelInfo li = labelInfos.get(i);
            LabelGroup lg = new LabelGroup(li);
            lgs.add(lg);

            if (li.getLevel() != null && li.getLevel() == 1 && i == 0) {
                List<LabelInfo> labelInfos_1 = labelInfoService.query("from LabelInfo where parentId=? and status=3 and availably=1 and attr_id is null", li.getId());
                for (LabelInfo li_1 : labelInfos_1) {
                    LabelGroup lg_1 = new LabelGroup(li_1);
                    lg.addChile(lg_1);

                    List<LabelInfo> labelInfos_2 = labelInfoService.query("from LabelInfo where parentId=? and status=3 and availably=1 and attr_id is null", li_1.getId());
                    for (LabelInfo li_2 : labelInfos_2) {
                        LabelGroup lg_2 = new LabelGroup(li_2);
                        lg_1.addChile(lg_2);
                    }
                }
            } else if (li.getLevel() != null && li.getLevel() == 2) {
                List<LabelInfo> labelInfos_2 = labelInfoService.query("from LabelInfo where parentId=? and status=3 and availably=1 and attr_id is null", li.getId());
                for (LabelInfo li_2 : labelInfos_2) {
                    LabelGroup lg_2 = new LabelGroup(li_2);
                    lg.addChile(lg_2);
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
        List<Map<String, Object>> list = labelInfoService
                .getAllLabelsByCategoryId(id);
        result = JSONArray.toJSONString(list);
        return result;
    }

    /**
     * 2、根据标签分类查询标签值
     */
    @ResponseBody
    @RequestMapping("/getChildrenById")
    @CacheAnnotation
    public String getChildrenById(Integer id, String status, Integer cycle
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

        List<Label> labels = labelInfoService.children(null, id, status, cycle, queryType, queryKey, type, categoryFlag, isLogAvailably);

        return JSON.toJSONString(labels);
    }

    @ResponseBody
    @RequestMapping("/getChildrenByIdAndLevel")
    @CacheAnnotation
    public String getChildrenByIdAndLevel(Integer pid, Integer level) {
        List<Map<String, Object>> list = labelInfoService.getChildrenByIdAndLevel(pid, level);
        JSONObject json = new JSONObject();
        json.put("stores", list);
        return json.toJSONString();
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/previewSignatureLabel")
    public String previewSignatureLabel(Integer cycle, LabelInfo label) {
        try {
            JSONObject result = new JSONObject();
            // 如果周期不存在，默认设置为0
            Map<String, Object> map = labelInfoService.previewSignatureLabel(cycle == null ? Integer.valueOf(0) : cycle, label);
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
        obj.put("isExist", labelInfoService.isExistLabelName(pid, labelName));
        return obj.toString();
    }

    @ResponseBody
    @RequestMapping("/isAvailabeLabel")
    @CacheAnnotation
    public String isAvailabeLabel(String ids) {
        if (null == ids || ids.isEmpty())
            throw new RuntimeException("待校验标签信息为空,请确认");
        List<Integer> idList = labelInfoService.isAvailabeLabel(ids);
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

    /**
     * 获取客户总数
     *
     * @param label
     * @return
     */
    @AuthPassport
    @RequestMapping(value = "/stat", method = RequestMethod.POST)
    public void getCustomerCount(@RequestBody String label, HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        response.setContentType("application/json; charset=utf-8");
        if (label == null || StringUtil.isEmpty(label)) {
            try {
                jsonObject.put("errorDesc", "02");
                response.getWriter().write(jsonObject.toJSONString());
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            jsonObject = groupService.previewByGroupCondition2(label);
            response.getWriter().write(jsonObject.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取客户详情列表
     *
     * @param label
     * @return
     */
    @AuthPassport
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public void getCustomerList(@RequestBody String label, HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        response.setContentType("application/json; charset=utf-8");
        if (label == null || StringUtil.isEmpty(label)) {
            try {
                jsonObject.put("errorDesc", "02");
                response.getWriter().write(jsonObject.toJSONString());
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            jsonObject = groupService.searchDetailByGroupCondition(label);
            response.getWriter().write(jsonObject.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTreeDataById(HttpServletRequest request, LabelInfo labelInfo, QueryParam params) {
        String queryType = params.getQueryType();
        String queryKey = params.getKey();
        String mineFlag = request.getParameter("mine");
        Integer categoryFlag = params.getCategoryFlag();

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
        LabelInfo label = labelInfoService.getLabelById(labelInfo.getId());
        Integer level = label.getLevel();
        if (labelInfo != null) {
            if (labelInfo.getStatus() != null) {
                map.put("status", labelInfo.getStatus());
            }
            if (null != mineFlag && mineFlag.equals("on")) {
                map.put("labelCreateUser.id", opUser().getId());
            }
            if (labelInfo.getParentCategory() != null) {
                map.put("parentCategory.id", labelInfo.getParentCategory()
                        .getId());
            }
            if (null == categoryFlag || categoryFlag == 0) {
                map.put(Constant.FILTER_KEY_PREFIX + "id", label.getId());
            }
        }
        LoginUser user = opUser();
        map.put(Constant.FILTER_KEY_PREFIX + "user", user);
        List<Map<String, Object>> list = labelInfoService.getTreeDataByMap(map,
                orLikeMap, andLikeMap);
        if (null != categoryFlag && categoryFlag == 1) {
            if (null != list && list.size() > 0 && level == 3) {
                list = (List<Map<String, Object>>) list.get(0).get("children");
            }
        }
        JSONObject json = new JSONObject();
        json.put("stores", list);

        return json.toString();
    }

}
