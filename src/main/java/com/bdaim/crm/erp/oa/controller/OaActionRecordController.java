package com.bdaim.crm.erp.oa.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.erp.oa.entity.OaActionRecord;
import com.bdaim.crm.erp.oa.service.OaActionRecordService;

import javax.annotation.Resource;

/**
 * oa操作记录
 *
 * @author hmb
 */
public class OaActionRecordController extends Controller {

    @Resource
    private OaActionRecordService oaActionRecordService;

    /**
     * 分页查询oa工作台列表
     *
     * @param pageRequest 分页对象
     * @author hmb
     */
    public void getOaRecordPageList(BasePageRequest<OaActionRecord> pageRequest) {
        renderJson(oaActionRecordService.getOaRecordPageList(pageRequest));
    }

    /**
     * 查询日程
     *
     * @author hmb
     */
    public void queryEvent() {
        //yyyy-mm
        String month = getPara("month");
        renderJson(oaActionRecordService.queryEvent(month));
    }

    public void queryEventByDay() {
        String day = getPara("day");
        renderJson(oaActionRecordService.queryEventByDay(day));
    }

    /**
     * 查询任务列表
     *
     * @author hmb
     */
    public void queryTask() {
        renderJson(oaActionRecordService.queryTask());
    }

}