package com.bdaim.crm.erp.bi.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.erp.bi.service.BiService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

@RestController
@RequestMapping(value = "/bi")
public class BiController extends BasicAction {
    @Resource
    private BiService biService;

    /**
     * @author Chacker
     * 商业智能，销售漏斗
     */
    @Permissions("bi:business:read")
    @RequestMapping(value = "/funnelStatistics")
    public R funnelStatistics() {
        Date startTime = getParaToDate("startTime");
        Date endTime = getParaToDate("endTime");
        Long userId = getLong("userId");
        Integer productId = getInt("productId");
        Integer deptId = getInt("deptId");
        return biService.queryCrmBusinessStatistics(userId, deptId, productId, startTime, endTime);
    }

    /**
     * 产品销售情况统计
     *
     * @author Chacker
     * startTime 开始时间 endTime 结束时间 userId用户ID deptId部门ID
     */
    @Permissions("bi:product:read")
    @RequestMapping(value = "/productStatistics")
    public R productStatistics() {
        Date startTime = getParaToDate("startTime");
        Date endTime = getParaToDate("endTime");
        Integer userId = getInt("userId");
        Integer deptId = getInt("deptId");
        return biService.queryProductSell(startTime, endTime, userId, deptId);
    }

    /**
     * 回款统计，根据月份获取合同信息
     * userId用户ID deptId部门ID
     */
    @Permissions("bi:receivables:read")
    @RequestMapping(value = "/queryByMonth")
    public R queryByMonth() {
        String year = get("year");
        String month = get("month");
        Integer userId = getInt("userId");
        Integer deptId = getInt("deptId");
//        renderJson(biService.queryByUserIdOrYear(userId,deptId,year,month));
        return biService.queryByUserIdOrYear(userId, deptId, year, month);
    }

    /**
     * 获取商业智能业绩目标完成情况
     *
     * @author Chacker
     */
    @RequestMapping(value = "/taskCompleteStatistics")
    @Permissions("bi:achievement:read")
    @NotNullValidate(value = "year", message = "year不能为空")
    @NotNullValidate(value = "type", message = "type不能为空")
    public R taskCompleteStatistics(@Para("year") String year, @Para("type") Integer type, @Para("deptId") Integer deptId, @Para("userId") Integer userId) {
//        renderJson(biService.taskCompleteStatistics(year,type,deptId,userId));
        return biService.taskCompleteStatistics(year, type, deptId, userId);
    }
}
