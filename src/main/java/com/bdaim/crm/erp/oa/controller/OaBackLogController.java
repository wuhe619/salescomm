package com.bdaim.crm.erp.oa.controller;

import com.bdaim.crm.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.bdaim.crm.erp.oa.service.OaBackLogService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wyq
 */
@RequestMapping(value = "/OaBackLog")
@RestController
public class OaBackLogController extends Controller {
    @Resource
    OaBackLogService oaBackLogService;

    /**
     * oa代办事项提醒
     */
    @RequestMapping(value = "/num")
    public R num(){
//        renderJson(oaBackLogService.backLogNum());
        return oaBackLogService.backLogNum();
    }
}
