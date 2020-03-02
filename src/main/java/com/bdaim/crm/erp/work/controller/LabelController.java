package com.bdaim.crm.erp.work.controller;

/**
 *
 */

import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.erp.work.entity.WorkTaskLabel;
import com.bdaim.crm.erp.work.service.LabelService;
import com.bdaim.crm.utils.R;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping(value = "/taskLabel")
@RestController
public class LabelController extends Controller {

    @Resource
    private LabelService labelService;

    /**
     * @author hmb
     * 设置任务标签
     * @param taskLabel 任务标签对象
     */
    @RequestMapping(value = "/setLabel")
    public R setLabel(@Para("") WorkTaskLabel taskLabel) {
//        renderJson(labelService.setLabel(taskLabel));
//        return (R.ok().put("data", labelService.setLabel(taskLabel)));
        return labelService.setLabel(taskLabel);
    }

    /**
     * @author hmb
     * 删除任务标签
     */
    @RequestMapping(value = "/deleteLabel")
    public R deleteLabel() {
        String labelId = getPara("labelId");
//        renderJson(labelService.deleteLabel(labelId));
//        return (R.ok().put("data", labelService.deleteLabel(labelId)));
        return labelService.deleteLabel(labelId);
    }

    /**
     * @author hmb
     * 任务标签列表
     */
    @RequestMapping(value = "/getLabelList")
    public R getLabelList() {
//        renderJson(labelService.getLabelList());
//        return (R.ok().put("data", labelService.getLabelList()));
        return labelService.getLabelList();
    }

    @RequestMapping(value = "/queryById")
    public R queryById(@Para("labelId") Integer labelId) {
//        renderJson(labelService.queryById(labelId));
//        return (R.ok().put("data", labelService.queryById(labelId)));
        return labelService.queryById(labelId);
    }

    /**
     * @author hmb
     * 根据用户参与的任务查询标签
     */
    @RequestMapping(value = "/getLabelListByOwn")
    public R getLabelListByOwn() {
//        renderJson(labelService.getLabelListByOwn());
//        return (R.ok().put("data", labelService.getLabelListByOwn()));
        return labelService.getLabelListByOwn();
    }

    /**
     * @author wyq
     * 标签任务列表
     */
    @RequestMapping(value = "/getTaskList")
    @NotNullValidate(value = "labelId", message = "标签不能为空")
    public R getTaskList(@Para("labelId") Integer labelId) {
//        renderJson(labelService.getTaskList(labelId));
//        return (R.ok().put("data", labelService.getTaskList(labelId)));
        return labelService.getTaskList(labelId);
    }
}
