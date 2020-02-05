package com.bdaim.crm.erp.crm.controller;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.erp.crm.entity.CrmReceivablesPlan;
import com.bdaim.crm.erp.crm.service.CrmReceivablesPlanService;

import javax.annotation.Resource;

public class CrmReceivablesPlanController extends Controller {
    @Resource
    private CrmReceivablesPlanService receivablesPlanService;
    /**
     * 添加或修改回款计划
     * @author zxy
     */
    public void saveAndUpdate(){
        JSONObject jsonObject = JSONObject.parseObject(getRawData());
        renderJson(receivablesPlanService.saveAndUpdate(jsonObject));
    }

    /**
     * 根据合同id和客户id查询未使用的回款计划
     * @author zxy
     */
    public void queryByContractAndCustomer(@Para("") CrmReceivablesPlan receivablesPlan){
        renderJson(receivablesPlanService.queryByContractAndCustomer(receivablesPlan));
    }

    /**
     * @author wyq
     * 删除回款计划
     */
    public void deleteByIds(@Para("planIds") String planIds){
        renderJson(receivablesPlanService.deleteByIds(planIds));
    }
}