package com.bdaim.crm.erp.work.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.erp.work.entity.Work;
import com.bdaim.crm.erp.work.service.WorkService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.core.paragetter.Para;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author hmb
 */
@RestController
@RequestMapping(value = "/work")
public class WorkController extends BasicAction {

    @Resource
    private WorkService workService;

    /**
     * @param work 项目对象
     * @author hmb
     * 设置项目
     */
    @RequestMapping(value = "/setWork")
    public R setWork(@Para("") Work work) {
        return (workService.setWork(work));
        //return (R.ok().put("data", workService.setWork(work));
    }

    @RequestMapping(value = "/getWorkById")
    public R getWorkById() {
        String workId = getPara("workId");
        return (workService.getWorkById(workId));
        //return (R.ok().put("data", workService.getWorkById(workId)));
    }

    /**
     * @author hmb
     * 删除项目
     */
    @RequestMapping(value = "/deleteWork")
    public R deleteWork() {
        String workId = getPara("workId");
        return (workService.deleteWork(workId));
        //return (R.ok().put("data", workService.deleteWork(workId)));
    }

    /**
     * @author hmb
     * 查询项目名列表
     */
    @RequestMapping(value = "/queryWorkNameList")
    public R queryWorkNameList() {
        return (workService.queryWorkNameList());
        //return (R.ok().put("data", workService.queryWorkNameList()));
    }

    /**
     * @author hmb
     * 根据项目id查询任务板
     */
    @RequestMapping(value = "/queryTaskByWorkId")
    public R queryTaskByWorkId(@RequestBody JSONObject jsonObject) {
//        JSONObject jsonObject = JSON.parseObject(getRawData());
        return (workService.queryTaskByWorkId(jsonObject));
        //return (R.ok().put("data", workService.queryTaskByWorkId(jsonObject)));
    }

    /**
     * @author hmb
     * 根据项目id查询项目附件
     */
    @RequestMapping(value = "/queryTaskFileByWorkId")
    public R queryTaskFileByWorkId(BasePageRequest<JSONObject> pageRequest) {
        return (workService.queryTaskFileByWorkId(pageRequest));
        //return (R.ok().put("data", workService.queryTaskFileByWorkId(pageRequest)));
    }

    /**
     * @author hmb
     * 查询归档项目列表
     */
    @RequestMapping(value = "/queryArchiveWorkList")
    public R queryArchiveWorkList(BasePageRequest pageRequest) {
        return (workService.queryArchiveWorkList(pageRequest));
        //return (R.ok().put("data", workService.queryArchiveWorkList(pageRequest)));
    }

    /**
     * @author hmb
     * 项目统计
     */
    @RequestMapping(value = "/workStatistics")
    public R workStatistics() {
        String workId = getPara("workId");
        return (workService.workStatistics(workId));
        //return (R.ok().put("data", workService.workStatistics(workId)));
    }

    /**
     * @author hmb
     * 查询项目成员
     */
    @RequestMapping(value = "/queryWorkOwnerList")
    public R queryWorkOwnerList() {
        String workId = getPara("workId");
        return (workService.queryWorkOwnerList(workId));
        //return (R.ok().put("data", workService.queryWorkOwnerList(workId)));
    }

    /**
     * @author hmb
     * 修改项目任务排序
     */
    @RequestMapping(value = "/updateOrder")
    public R updateOrder(@RequestBody JSONObject jsonObject) {
//        JSONObject jsonObject = JSON.parseObject(getRawData());
        return (workService.updateOrder(jsonObject));
        //return (R.ok().put("data", workService.updateOrder(jsonObject)));
    }

    /**
     * @author hmb
     * 退出项目
     */
    @RequestMapping(value = "/leave")
    public R leave() {
        String workId = getPara("workId");
        Integer userId = BaseUtil.getUserId().intValue();
        return (workService.leave(workId, userId));
        //return (R.ok().put("data", workService.leave(workId, userId)));
    }

    /**
     * @author hmb
     * 删除项目成员
     */
    @RequestMapping(value = "/removeWorkOwnerUser")
    public R removeWorkOwnerUser() {
        String workId = getPara("workId");
        Integer userId = getInt("ownerUserId");
        return (workService.leave(workId, userId));
        //return (R.ok().put("data", workService.leave(workId, userId)));
    }

    /**
     * 查询项目管理角色列表
     *
     * @author wyq
     */
    @RequestMapping(value = "/queryRoleList")
    public R queryRoleList() {
        return (workService.queryRoleList());
        //return (R.ok().put("data", workService.queryRoleList()));
    }

    /**
     * @author wyq
     * 查询项目设置成员管理列表
     */
    @RequestMapping(value = "/queryOwnerRoleList")
    public R queryOwnerRoleList(@Para("workId") Integer workId) {
        return (workService.queryOwnerRoleList(workId));
        //return (R.ok().put("data", workService.queryOwnerRoleList(workId)));
    }

    /**
     * @author wyq
     * 保存项目角色管理设置
     */
    @RequestMapping(value = "/setOwnerRole")
    public R setOwnerRole(@RequestBody JSONObject jsonObject) {
//        JSONObject jsonObject = JSONObject.parseObject(getRawData());
        return (workService.setOwnerRole(jsonObject));
        //return (R.ok().put("data", workService.setOwnerRole(jsonObject)));
    }

    /**
     * @author hmb
     * 删除任务列表
     */
    @RequestMapping(value = "/deleteTaskList")
    public R deleteTaskList() {
        String classId = getPara("classId");
        String workId = getPara("workId");
        if (!AuthUtil.isWorkAuth(workId, "taskClass:delete")) {
            return (R.noAuth());
//            return;
        }
        return (workService.deleteTaskList(workId, classId));
        //return (R.ok().put("data", workService.deleteTaskList(workId, classId)));
    }

    /**
     * @author hmb
     * 归档已完成的任务
     */
    @RequestMapping(value = "/archiveTask")
    public R archiveTask() {
        String classId = getPara("classId");
        return (workService.archiveTask(classId));
        //return (R.ok().put("data", workService.archiveTask(classId)));
    }

    /**
     * @author hmb
     * 归档任务
     */
    @RequestMapping(value = "/archList")
    public R archList() {
        String workId = getPara("workId");
        return (workService.archList(workId));
        //return (R.ok().put("data", workService.archList(workId)));
    }

    /**
     * @author hmb
     * 移除项目成员
     */
    @RequestMapping(value = "/remove")
    public R remove(@Para("ownerUserId") Integer ownerUserId, @Para("workId") Integer workId) {
        return (workService.remove(ownerUserId, workId));
        //return (R.ok().put("data", workService.remove(ownerUserId, workId)));
    }

    /**
     * @author hmb
     * 项目class排序
     */
    @RequestMapping(value = "/updateClassOrder")
    public R updateClassOrder(JSONObject jsonObject) {
//        JSONObject jsonObject = JSONObject.parseObject(getRawData());
        return (workService.updateClassOrder(jsonObject));
        // return (R.ok().put("data", workService.updateClassOrder(jsonObject)));
    }


    /**
     * 撤销任务归档
     */
    @RequestMapping(value = "/activation")
    public R activation(@Para("taskId") Integer taskId) {
        return (workService.activation(taskId));
        //return (R.ok().put("data", workService.activation(taskId)));
    }
}
