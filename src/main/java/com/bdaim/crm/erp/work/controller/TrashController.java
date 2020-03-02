package com.bdaim.crm.erp.work.controller;

import com.bdaim.crm.utils.R;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.erp.work.service.TrashService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 回收站
 */
@RequestMapping(value = "/workTrash")
@RestController
public class TrashController extends Controller {
    @Resource
    private TrashService trashService;

    @RequestMapping(value = "/queryList")
    public R queryList() {
//        renderJson(trashService.queryList());
        return trashService.queryList();
    }

    /**
     * 彻底删除任务
     *
     * @param taskId
     * @author wyq
     */
    @RequestMapping(value = "/deleteTask")
    public R deleteTask(@Para("taskId") Integer taskId) {
//        renderJson(trashService.deleteTask(taskId));
        return trashService.deleteTask(taskId);
    }

    /**
     * 还原任务
     *
     * @param taskId
     * @author wyq
     */
    @RequestMapping(value = "/restore")
    public R restore(@Para("taskId") Integer taskId) {
//        renderJson(trashService.restore(taskId));
        return trashService.restore(taskId);
    }
}
