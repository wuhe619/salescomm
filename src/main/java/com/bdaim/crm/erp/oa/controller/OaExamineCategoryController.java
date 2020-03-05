package com.bdaim.crm.erp.oa.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.entity.LkCrmOaExamineCategoryEntity;
import com.bdaim.crm.entity.LkCrmOaExamineStepEntity;
import com.bdaim.crm.erp.oa.service.OaExamineCategoryService;
import com.bdaim.crm.utils.R;
import com.bdaim.crm.utils.TagUtil;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * 审批类型
 *
 * @author hmb
 */
@RestController
@RequestMapping(value = "/OaExamineCategory")
public class OaExamineCategoryController extends BasicAction {

    @Resource
    private OaExamineCategoryService oaExamineCategoryService;

    /**
     * 设置审批类型
     *
     * @author hmb
     */
    @RequestMapping(value = "/setExamineCategory")
    @Permissions("manage:oa")
    public R setExamineCategory(@RequestBody JSONObject jsonObject) {
        //JSONObject jsonObject = JSON.parseObject(getRawData());
        LkCrmOaExamineCategoryEntity oaExamineCategory = new LkCrmOaExamineCategoryEntity();
        List<LkCrmOaExamineStepEntity> oaExamineSteps = new ArrayList<>();
        oaExamineCategory.setCategoryId(jsonObject.getInteger("id"));
        oaExamineCategory.setTitle(jsonObject.getString("title"));
        oaExamineCategory.setRemarks(jsonObject.getString("remarks"));
        oaExamineCategory.setExamineType(jsonObject.getInteger("examineType"));
        if (jsonObject.getJSONArray("user_ids") != null) {
            List<Integer> list = jsonObject.getJSONArray("user_ids").toJavaList(Integer.class);
            oaExamineCategory.setUserIds(TagUtil.fromSet(new HashSet<>(list)));
        }
        if (jsonObject.getJSONArray("dept_ids") != null) {
            List<Integer> list = jsonObject.getJSONArray("dept_ids").toJavaList(Integer.class);
            oaExamineCategory.setDeptIds(TagUtil.fromSet(new HashSet<>(list)));
        }
        oaExamineCategory.setCreateTime(new Timestamp(System.currentTimeMillis()));
        JSONArray step = jsonObject.getJSONArray("step");
        for (int i = 0; i < step.size(); i++) {
            LkCrmOaExamineStepEntity oaExamineStep = new LkCrmOaExamineStepEntity();
            JSONObject jsonObject1 = step.getJSONObject(i);
            if (jsonObject1.getJSONArray("checkUserId") != null) {
                List<Integer> list = jsonObject1.getJSONArray("checkUserId").toJavaList(Integer.class);
                oaExamineStep.setCheckUserId(TagUtil.fromSet(new HashSet<>(list)));
            }
            oaExamineStep.setStepNum(i + 1);
            oaExamineStep.setStepType(jsonObject1.getInteger("stepType"));
            oaExamineSteps.add(oaExamineStep);
        }
//        renderJson(oaExamineCategoryService.setExamineCategory(oaExamineCategory, oaExamineSteps));
        return oaExamineCategoryService.setExamineCategory(oaExamineCategory, oaExamineSteps);
    }

    /**
     * 查询审批类型列表
     *
     * @param basePageRequest 分页对象
     * @author hmb
     */
    @RequestMapping(value = "/queryExamineCategoryList")
    public R queryExamineCategoryList(BasePageRequest<Void> basePageRequest) {
//        renderJson(oaExamineCategoryService.queryExamineCategoryList(basePageRequest));
        return oaExamineCategoryService.queryExamineCategoryList(basePageRequest);
    }

    /**
     * 查询审批类型列表
     *
     * @author hmb
     */
    @RequestMapping(value = "/queryAllExamineCategoryList")
    public R queryAllExamineCategoryList() {
//        renderJson(oaExamineCategoryService.queryAllExamineCategoryList());
        return oaExamineCategoryService.queryAllExamineCategoryList();
    }


    /**
     * 删除审批类型
     *
     * @author hmb
     */
    @RequestMapping(value = "/deleteExamineCategory")
    @Permissions("manage:oa")
    public R deleteExamineCategory() {
        String id = getPara("id");
//        renderJson(oaExamineCategoryService.deleteExamineCategory(id));
        return oaExamineCategoryService.deleteExamineCategory(id);
    }


    /**
     * 查询系统用户列表
     *
     * @author hmb
     */
    @RequestMapping(value = "/queryUserList")
    public R queryUserList() {
//        renderJson(oaExamineCategoryService.queryUserList());
        return oaExamineCategoryService.queryUserList();
    }

    /**
     * 查询部门
     *
     * @author hmb
     */
    @RequestMapping(value = "/queryDeptList")
    public R queryDeptList() {
//        renderJson(oaExamineCategoryService.queryDeptList());
        return oaExamineCategoryService.queryDeptList();
    }

    /**
     * 查询审批类型详情
     *
     * @author hmb
     */
    @RequestMapping(value = "/queryExamineCategoryById")
    public R queryExamineCategoryById() {
        String id = getPara("id");
//        renderJson(oaExamineCategoryService.queryExamineCategoryById(id));
        return oaExamineCategoryService.queryExamineCategoryById(id);
    }

    /**
     * 启用/禁用
     */
    @RequestMapping(value = "/updateStatus")
    @Permissions("manage:oa")
    public R updateStatus() {
        String id = getPara("id");
//        renderJson(oaExamineCategoryService.updateStatus(id));
        return oaExamineCategoryService.updateStatus(id);
    }


}
