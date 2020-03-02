package com.bdaim.crm.erp.work.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.bdaim.crm.erp.work.service.WorkbenchService;
import com.bdaim.crm.utils.BaseUtil;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 工作台
 */
@RequestMapping(value = "/workbench")
@RestController
public class WorkbenchController extends BasicAction {

    @Resource
    private WorkbenchService workbenchService;

    /**
     * @author hmb
     * 我的任务
     */
    @RequestMapping(value = "/myTask")
    public R myTask(){
//        renderJson(workbenchService.myTask(BaseUtil.getUser().getUserId().intValue()));
//        return (R.ok().put("data", workbenchService.myTask(BaseUtil.getUser().getUserId().intValue())));
        return workbenchService.myTask(BaseUtil.getUser().getUserId().intValue());
    }

    /**
     * @author hmb
     * 任务日历
     */
    @RequestMapping(value = "/dateList")
    public R dateList(){
        String startTime = getPara("startTime");
        String endTime = getPara("endTime");
//        renderJson(workbenchService.dateList(startTime,endTime));
//        return (R.ok().put("data", workbenchService.dateList(startTime,endTime)));
        return workbenchService.dateList(startTime,endTime);
    }

    /**
     * @author hmb
     * 修改工作台任务排序
     */
    @RequestMapping(value = "/updateTop")
    public R updateTop(@RequestBody JSONObject jsonObject){
//        JSONObject jsonObject = JSON.parseObject(getRawData());
//        renderJson(workbenchService.updateTop(jsonObject));
//        return (R.ok().put("data", workbenchService.updateTop(jsonObject)));
        return workbenchService.updateTop(jsonObject);
    }
}
