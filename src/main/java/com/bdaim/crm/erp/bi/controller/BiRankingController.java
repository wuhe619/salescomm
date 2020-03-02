package com.bdaim.crm.erp.bi.controller;

import com.bdaim.crm.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.erp.bi.service.BiRankingService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/biRanking")
public class BiRankingController extends Controller {
    @Resource
    private BiRankingService service;

    /**
     * 合同金额排行榜
     *
     * @author zxy
     */
    @RequestMapping(value = "/contractRanKing")
    public R contractRanKing(@Para("deptId") Integer deptId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.contractRanKing(deptId,type,startTime,endTime));
        return service.contractRanKing(deptId, type, startTime, endTime);
    }

    /**
     * 回款金额排行榜
     *
     * @author zxy
     */
    @RequestMapping(value = "/receivablesRanKing")
    public R receivablesRanKing(@Para("deptId") Integer deptId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.receivablesRanKing(deptId,type,startTime,endTime));
        return service.receivablesRanKing(deptId, type, startTime, endTime);
    }

    /**
     * 签约合同排行榜
     *
     * @author zxy
     */
    @RequestMapping(value = "/contractCountRanKing")
    public R contractCountRanKing(@Para("deptId") Integer deptId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.contractCountRanKing(deptId,type,startTime,endTime));
        return service.contractCountRanKing(deptId, type, startTime, endTime);
    }

    /**
     * 产品销量排行榜
     *
     * @author zxy
     */
    @RequestMapping(value = "/productCountRanKing")
    public R productCountRanKing(@Para("deptId") Integer deptId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.productCountRanKing(deptId,type,startTime,endTime));
        return service.productCountRanKing(deptId, type, startTime, endTime);
    }

    /**
     * 新增客户数排行榜
     *
     * @author zxy
     */
    @RequestMapping(value = "/customerCountRanKing")
    public R customerCountRanKing(@Para("deptId") Integer deptId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.customerCountRanKing(deptId,type,startTime,endTime));
        return service.customerCountRanKing(deptId, type, startTime, endTime);
    }

    /**
     * 新增联系人排行榜
     *
     * @author zxy
     */
    @RequestMapping(value = "/contactsCountRanKing")
    public R contactsCountRanKing(@Para("deptId") Integer deptId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.contactsCountRanKing(deptId,type,startTime,endTime));
        return service.contactsCountRanKing(deptId, type, startTime, endTime);
    }

    /**
     * 跟进客户数排行榜
     *
     * @author zxy
     */
    @RequestMapping(value = "/customerGenjinCountRanKing")
    public R customerGenjinCountRanKing(@Para("deptId") Integer deptId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.customerGenjinCountRanKing(deptId,type,startTime,endTime));
        return service.customerGenjinCountRanKing(deptId, type, startTime, endTime);
    }

    /**
     * 跟进次数排行榜
     *
     * @author zxy
     */
    @RequestMapping(value = "/recordCountRanKing")
    public R recordCountRanKing(@Para("deptId") Integer deptId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.recordCountRanKing(deptId,type,startTime,endTime));
        return service.recordCountRanKing(deptId, type, startTime, endTime);
    }

    /**
     * 产品分类销量分析
     *
     * @author zxy
     */
    @RequestMapping(value = "/contractProductRanKing")
    public R contractProductRanKing(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.contractProductRanKing(deptId,userId,type,startTime,endTime));
        return service.contractProductRanKing(deptId, userId, type, startTime, endTime);
    }

    /**
     * 出差次数排行
     *
     * @author zxy
     */
    @RequestMapping(value = "/travelCountRanKing")
    public R travelCountRanKing(@Para("deptId") Integer deptId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.travelCountRanKing(deptId,type,startTime,endTime));
        return service.travelCountRanKing(deptId, type, startTime, endTime);
    }

    /**
     * 产品销售情况统计
     *
     * @author zxy
     */
    @RequestMapping(value = "/productSellRanKing")
    public R productSellRanKing(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.productSellRanKing(deptId,userId,type,startTime,endTime));
        return service.productSellRanKing(deptId, userId, type, startTime, endTime);
    }

    /**
     * 城市分布分析
     *
     * @author zxy
     */
    @RequestMapping(value = "/addressAnalyse")
    public R addressAnalyse() {
//        renderJson(service.addressAnalyse());
        return service.addressAnalyse();
    }

    /**
     * 客户行业分析
     *
     * @author zxy
     */
    @RequestMapping(value = "/portrait")
    public R portrait(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.portrait(deptId,userId,type,startTime,endTime));
        return service.portrait(deptId, userId, type, startTime, endTime);
    }

    /**
     * 客户级别分析
     *
     * @author zxy
     */
    @RequestMapping(value = "/portraitLevel")
    public R portraitLevel(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.portraitLevel(deptId,userId,type,startTime,endTime));
        return service.portraitLevel(deptId, userId, type, startTime, endTime);
    }

    /**
     * 客户级别分析
     *
     * @author zxy
     */
    @RequestMapping(value = "/portraitSource")
    public R portraitSource(@Para("deptId") Integer deptId, @Para("userId") Long userId, @Para("type") String type, @Para("startTime") String startTime, @Para("endTime") String endTime) {
//        renderJson(service.portraitSource(deptId,userId,type,startTime,endTime));
        return service.portraitSource(deptId, userId, type, startTime, endTime);
    }
}
