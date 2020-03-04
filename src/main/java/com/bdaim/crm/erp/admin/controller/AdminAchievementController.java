package com.bdaim.crm.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.entity.LkCrmAchievementEntity;
import com.bdaim.crm.erp.admin.service.AdminAchievementService;
import com.bdaim.crm.utils.R;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 业绩目标设置
 *
 */
@Controller
@RequestMapping("/achievement")
public class AdminAchievementController extends BasicAction {

    @Resource
    private AdminAchievementService adminAchievementService;

    /**
     * 设置业绩目标
     *
     * @author hmb
     */
    @Permissions("manage:crm")
    @RequestMapping("/setAchievement")
    public R setAchievement(@RequestBody(required = false) String data) {
        //String data = getRawData();
        List<LkCrmAchievementEntity> crmAchievements = JSON.parseArray(data, LkCrmAchievementEntity.class);
        return (adminAchievementService.setAchievement(crmAchievements));
    }

    /**
     * 查询业绩目标列表
     *
     * @param achievement 业绩目标对象
     * @author hmb
     */
    @Permissions("manage:crm")
    @RequestMapping("/queryAchievementList")
    public R queryAchievementList( LkCrmAchievementEntity achievement,String userId, Integer deptId) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        /*String userId = getPara("userId");
        Integer deptId = getParaToInt("deptId");*/
        return (adminAchievementService.queryAchievementList(achievement, userId, deptId));
    }


}
