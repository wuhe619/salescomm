package com.bdaim.crm.erp.bi.controller;

import com.bdaim.crm.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.erp.bi.service.BiEmployeeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wyq
 */
@RestController
@RequestMapping(value = "/biEmployee")
public class BiEmployeeController extends Controller {
    @Resource
    BiEmployeeService biEmployeeService;

    /**
     * 合同数量分析
     *
     * @author wyq
     */
    @NotNullValidate(value = "year", message = "年份不能为空")
    @NotNullValidate(value = "deptId", message = "部门id不能为空")
    @RequestMapping(value = "/contractNumStats")
    public R contractNumStats(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("year") String year) {
//        renderJson(biEmployeeService.contractNumStats(deptId,userId,"contractNum",year));
        return biEmployeeService.contractNumStats(deptId, userId, "contractNum", year);
    }

    /**
     * 合同金额分析
     *
     * @author wyq
     */
    @RequestMapping(value = "/contractMoneyStats")
    public R contractMoneyStats(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("year") String year) {
//        renderJson(biEmployeeService.contractNumStats(deptId,userId,"contractMoney",year));
        return biEmployeeService.contractNumStats(deptId, userId, "contractMoney", year);
    }

    /**
     * 回款金额分析
     *
     * @author wyq
     */
    @RequestMapping(value = "/receivablesMoneyStats")
    public R receivablesMoneyStats(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("year") String year) {
//        renderJson(biEmployeeService.contractNumStats(deptId,userId,"receivables",year));
        return biEmployeeService.contractNumStats(deptId, userId, "receivables", year);
    }

    /**
     * 合同汇总表
     *
     * @author wyq
     */
    @RequestMapping(value = "/totalContract")
    public R totalContract(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(biEmployeeService.totalContract(deptId,userId,type,startTime,endTime));
        return biEmployeeService.totalContract(deptId, userId, type, startTime, endTime);
    }
}
