package com.bdaim.crm.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.entity.LkCrmAdminExamineEntity;
import com.bdaim.crm.erp.admin.entity.AdminExamine;
import com.bdaim.crm.erp.admin.service.AdminExamineService;
import com.bdaim.crm.utils.R;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 审批流程
 * @author zxy
 */
@RestController
@RequestMapping("/examine")
public class AdminExamineController extends Controller {
    @Resource
    private AdminExamineService examineService;
    /**
     * 添加审批流程
     */
    @Permissions("manage:examineFlow")
    @RequestMapping(value = "/saveExamine", method = RequestMethod.POST)
    public R saveExamine(){
        JSONObject jsonObject = JSON.parseObject(getRawData());
        return (examineService.saveExamine(jsonObject));
    }
    /**
     * 查询所有未删除审批流程
     */
    @Permissions("manage:examineFlow")
    @RequestMapping(value = "/queryAllExamine", method = RequestMethod.POST)
    public R queryAllExamine(BasePageRequest<AdminExamine> basePageRequest){
        return(examineService.queryAllExamine(basePageRequest));
    }
    /**
     * 根据id查询审批流程 examineId 审批流程id
     * @author zxy
     */
    @Permissions("manage:examineFlow")
    @RequestMapping(value = "/queryExamineById", method = RequestMethod.POST)
    public R queryExamineById(){
        Integer examineId = getInt("examineId");
        return(examineService.queryExamineById(examineId));
    }
    /**
     * 停用或删除审批流程
     * examineId 审批流程id
     * status 审批状态 1启用 0禁用 2 删除
     */
    @Permissions("manage:examineFlow")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    public R updateStatus(@Para("") LkCrmAdminExamineEntity adminExamine){
        return(examineService.updateStatus(adminExamine));
    }
    /**
     * 查询当前启用审核流程步骤
     * categoryType 1 合同 2 回款
     */
    @Permissions("manage:examineFlow")
    @RequestMapping(value = "/queryExaminStep", method = RequestMethod.POST)
    public R queryExaminStep( Integer categoryType){
        //Integer categoryType = getInt("categoryType");
        return(examineService.queryExaminStep(categoryType));
    }
}
