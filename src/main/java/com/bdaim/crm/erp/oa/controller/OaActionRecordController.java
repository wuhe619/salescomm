package com.bdaim.crm.erp.oa.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.interceptor.ClassTypeCheck;
import com.bdaim.crm.erp.oa.entity.OaActionRecord;
import com.bdaim.crm.erp.oa.service.OaActionRecordService;
import com.bdaim.crm.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * oa操作记录
 *
 * @author hmb
 */
@RequestMapping(value = "/OaRecord")
@RestController
public class OaActionRecordController extends BasicAction {

    @Resource
    private OaActionRecordService oaActionRecordService;

    /**
     * 分页查询oa工作台列表
     *
     * @param pageRequest 分页对象
     * @author hmb
     */
    @RequestMapping(value = "/getOaRecordPageList")
    @ClassTypeCheck(classType = OaActionRecord.class)
    public R getOaRecordPageList(BasePageRequest<OaActionRecord> pageRequest,OaActionRecord oaActionRecord) {
        pageRequest.setData(oaActionRecord);
//        renderJson(oaActionRecordService.getOaRecordPageList(pageRequest));
        return oaActionRecordService.getOaRecordPageList(pageRequest);
    }

    /**
     * 查询日程
     *
     * @author hmb
     */
    @RequestMapping(value = "/queryEvent")
    public R queryEvent() {
        //yyyy-mm
        String month = getPara("month");
//        renderJson(oaActionRecordService.queryEvent(month));
        return oaActionRecordService.queryEvent(month);
    }

    @RequestMapping(value = "/queryEventByDay")
    public R queryEventByDay() {
        String day = getPara("day");
//        renderJson(oaActionRecordService.queryEventByDay(day));
        return oaActionRecordService.queryEventByDay(day);
    }

    /**
     * 查询任务列表
     *
     * @author hmb
     */
    @RequestMapping(value = "/queryTask")
    public R queryTask() {
//        renderJson(oaActionRecordService.queryTask());
        return oaActionRecordService.queryTask();
    }

}
