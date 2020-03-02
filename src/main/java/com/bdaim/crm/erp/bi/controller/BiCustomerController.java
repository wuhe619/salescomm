package com.bdaim.crm.erp.bi.controller;

import com.bdaim.crm.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.erp.bi.service.BiCustomerService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wyq
 */
@RestController
@RequestMapping(value = "/biCustomer")
public class BiCustomerController extends Controller {
    @Resource
    BiCustomerService biCustomerService;

    /**
     * 客户总量分析柱状图
     *
     * @param deptId
     * @param userId
     * @param type
     */
    @RequestMapping(value = "/totalCustomerStats")
    @NotNullValidate(value = "deptId", message = "部门id不能为空")
    public R totalCustomerStats(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(biCustomerService.totalCustomerStats(deptId,userId,type,startTime,endTime));
        return biCustomerService.totalCustomerStats(deptId, userId, type, startTime, endTime);
    }

    /**
     * 客户总量分析表
     *
     * @author wyq
     */
    @RequestMapping(value = "/totalCustomerTable")
    @NotNullValidate(value = "deptId", message = "部门id不能为空")
    public R totalCustomerTable(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(biCustomerService.totalCustomerTable(deptId,userId,type,startTime,endTime));
        return biCustomerService.totalCustomerTable(deptId, userId, type, startTime, endTime);
    }

    /**
     * 客户跟进次数分析
     *
     * @author wyq
     */
    @RequestMapping(value = "/customerRecordStats")
    public R customerRecordStats(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(biCustomerService.customerRecordStats(deptId,userId,type,startTime,endTime));
        return biCustomerService.customerRecordStats(deptId, userId, type, startTime, endTime);
    }

    /**
     * 客户跟进次数分析表
     *
     * @author wyq
     */
    @RequestMapping(value = "/customerRecordInfo")
    public R customerRecordInfo(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(biCustomerService.customerRecordInfo(deptId,userId,type,startTime,endTime));
        return biCustomerService.customerRecordInfo(deptId, userId, type, startTime, endTime);
    }

    /**
     * 客户跟进方式分析
     *
     * @author wyq
     */
    @RequestMapping(value = "/customerRecodCategoryStats")
    public R customerRecodCategoryStats(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(biCustomerService.customerRecodCategoryStats(deptId,userId,type,startTime,endTime));
        return biCustomerService.customerRecodCategoryStats(deptId, userId, type, startTime, endTime);
    }

    /**
     * 客户转化率分析图
     *
     * @author wyq
     */
    @RequestMapping(value = "/customerConversionStats")
    public R customerConversionStats(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(biCustomerService.customerConversionStats(deptId,userId,type,startTime,endTime));
        return biCustomerService.customerConversionStats(deptId, userId, type, startTime, endTime);
    }

    /**
     * 客户转化率分析表
     *
     * @author wyq
     */
    @RequestMapping(value = "/customerConversionInfo")
    public R customerConversionInfo(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(biCustomerService.customerConversionInfo(deptId,userId,type,startTime,endTime));
        return biCustomerService.customerConversionInfo(deptId, userId, type, startTime, endTime);
    }

    /**
     * 公海客户分析图
     *
     * @author wyq
     */
    @RequestMapping(value = "/poolStats")
    public R poolStats(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(biCustomerService.poolStats(deptId,userId,type,startTime,endTime));
        return biCustomerService.poolStats(deptId, userId, type, startTime, endTime);
    }

    /**
     * 公海客户分析表
     *
     * @author wyq
     */
    @RequestMapping(value = "/poolTable")
    public R poolTable(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(biCustomerService.poolTable(deptId,userId,type,startTime,endTime));
        return biCustomerService.poolTable(deptId, userId, type, startTime, endTime);
    }

    /**
     * 员工客户成交周期图
     *
     * @author wyq
     */
    @RequestMapping(value = "/employeeCycle")
    public R employeeCycle(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(biCustomerService.employeeCycle(deptId,userId,type,startTime,endTime));
        return biCustomerService.employeeCycle(deptId, userId, type, startTime, endTime);
    }

    /**
     * 员工客户成交周期表
     *
     * @author wyq
     */
    @RequestMapping(value = "/employeeCycleInfo")
    public R employeeCycleInfo(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(biCustomerService.employeeCycleInfo(deptId,userId,type,startTime,endTime));
        return biCustomerService.employeeCycleInfo(deptId, userId, type, startTime, endTime);
    }

    /**
     * 地区成交周期图
     *
     * @author wyq
     */
    @RequestMapping(value = "/districtCycle")
    public R districtCycle(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(biCustomerService.districtCycle(deptId,userId,type,startTime,endTime));
        return biCustomerService.districtCycle(deptId, userId, type, startTime, endTime);
    }

    /**
     * 产品成交周期
     *
     * @author wyq
     */
    @RequestMapping(value = "/productCycle")
    public R productCycle(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(biCustomerService.productCycle(deptId,userId,type,startTime,endTime));
        return biCustomerService.productCycle(deptId, userId, type, startTime, endTime);
    }
}
