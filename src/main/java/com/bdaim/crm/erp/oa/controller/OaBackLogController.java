package com.bdaim.crm.erp.oa.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.erp.oa.service.OaBackLogService;
import com.bdaim.crm.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wyq
 */
@RequestMapping(value = "/OaBackLog")
@RestController
public class OaBackLogController extends BasicAction {
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
