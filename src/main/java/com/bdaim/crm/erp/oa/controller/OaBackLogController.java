package com.bdaim.crm.erp.oa.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.bdaim.crm.erp.oa.service.OaBackLogService;

import javax.annotation.Resource;

/**
 * @author wyq
 */
public class OaBackLogController extends Controller {
    @Resource
    OaBackLogService oaBackLogService;

    /**
     * oa代办事项提醒
     */
    public void num(){
        renderJson(oaBackLogService.backLogNum());
    }
}