package com.bdaim.crm.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.erp.admin.entity.CrmAchievement;
import com.bdaim.crm.erp.admin.service.AdminAchievementService;

import java.util.List;

/**
 * 业绩目标设置
 * @author hmb
 */
public class AdminAchievementController extends Controller {

    @Inject
    private AdminAchievementService adminAchievementService;

    /**
     * 设置业绩目标
     * @author hmb
     */
    @Permissions("manage:crm")
    public void setAchievement(){
        String data = getRawData();
        List<CrmAchievement> crmAchievements = JSON.parseArray(data, CrmAchievement.class);
        renderJson(adminAchievementService.setAchievement(crmAchievements));
    }

    /**
     * 查询业绩目标列表
     * @param achievement 业绩目标对象
     * @author hmb
     */
    @Permissions("manage:crm")
    public void queryAchievementList(@Para("")CrmAchievement achievement){
        String userId = getPara("userId");
        Integer deptId = getParaToInt("deptId");
        renderJson(adminAchievementService.queryAchievementList(achievement,userId,deptId));
    }


}
