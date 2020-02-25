package com.bdaim.crm.erp.crm.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.crm.erp.crm.entity.CrmReceivablesPlan;
import com.bdaim.crm.erp.crm.service.CrmReceivablesPlanService;
import com.bdaim.crm.utils.R;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 回款计划
 */
@RestController
@RequestMapping("/Crm/ReceivablesPlan")
public class CrmReceivablesPlanController extends Controller {
    @Resource
    private CrmReceivablesPlanService receivablesPlanService;

    /**
     * 添加或修改回款计划
     *
     * @author zxy
     */
    @RequestMapping(value = "/saveAndUpdate", method = RequestMethod.POST)
    public R saveAndUpdate() {
        JSONObject jsonObject = JSONObject.parseObject(getRawData());
        return (receivablesPlanService.saveAndUpdate(jsonObject));
    }

    /**
     * 根据合同id和客户id查询未使用的回款计划
     *
     * @author zxy
     */

    @RequestMapping(value = "/queryByContractAndCustomer", method = RequestMethod.POST)
    public R queryByContractAndCustomer(@Para("") CrmReceivablesPlan receivablesPlan) {
        return (receivablesPlanService.queryByContractAndCustomer(receivablesPlan));
    }

    /**
     * @author wyq
     * 删除回款计划
     */
    @RequestMapping(value = "/deleteByIds", method = RequestMethod.POST)
    public R deleteByIds(@Para("planIds") String planIds) {
        return (receivablesPlanService.deleteByIds(planIds));
    }
}
