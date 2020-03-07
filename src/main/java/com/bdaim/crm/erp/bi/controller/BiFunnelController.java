package com.bdaim.crm.erp.bi.controller;

import com.bdaim.crm.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.erp.bi.service.BiFunnelService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/biFunnel")
public class BiFunnelController extends Controller {
    @Resource
    private BiFunnelService service;

    /**
     * 销售漏斗
     *
     * @author Chacker
     */
    @RequestMapping(value = "/sellFunnel")
    public R sellFunnel(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type,
                        @Para("startTime") String startTime, @Para("endTime") String endTime, @Para("typeId") Integer typeId) {
//        renderJson(service.sellFunnel(deptId,userId,type,startTime,endTime,typeId));
        return service.sellFunnel(deptId, userId, type, startTime, endTime, typeId);
    }

    /**
     * 新增商机分析图
     *
     * @author Chacker
     */
    @RequestMapping(value = "/addBusinessAnalyze")
    public R addBusinessAnalyze(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type,
                                @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.addBusinessAnalyze(deptId,userId,type,startTime,endTime));
        return service.addBusinessAnalyze(deptId, userId, type, startTime, endTime);
    }

    /**
     * 新增商机分析表
     *
     * @author Chacker
     */
    @RequestMapping(value = "/sellFunnelList")
    public R sellFunnelList(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type,
                            @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.sellFunnelList(deptId,userId,type,startTime,endTime));
        return service.sellFunnelList(deptId, userId, type, startTime, endTime);
    }

    /**
     * 商机转化率分析
     *
     * @author Chacker
     */
    @RequestMapping(value = "/win")
    public R win(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type,
                 @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.win(deptId,userId,type,startTime,endTime));
        return service.win(deptId, userId, type, startTime, endTime);
    }
}
