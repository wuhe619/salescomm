package com.bdaim.crm.erp.work.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.erp.work.entity.Work;
import com.bdaim.crm.erp.work.service.WorkService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;

/**
 * @author hmb
 */
public class WorkController extends Controller {

    @Inject
    private WorkService workService;

    /**
     * @author hmb
     * 设置项目
     * @param work 项目对象
     */
    public void setWork(@Para("") Work work){
        renderJson(workService.setWork(work));
    }

    public void getWorkById(){
        String workId = getPara("workId");
        renderJson(workService.getWorkById(workId));
    }

    /**
     * @author hmb
     * 删除项目
     */
    public void deleteWork(){
        String workId = getPara("workId");
        renderJson(workService.deleteWork(workId));
    }
    /**
     * @author hmb
     * 查询项目名列表
     */
    public void queryWorkNameList(){
        renderJson(workService.queryWorkNameList());
    }
    /**
     * @author hmb
     * 根据项目id查询任务板
     */
    public void queryTaskByWorkId(){
        JSONObject jsonObject = JSON.parseObject(getRawData());
        renderJson(workService.queryTaskByWorkId(jsonObject));
    }

    /**
     * @author hmb
     * 根据项目id查询项目附件
     */
    public void queryTaskFileByWorkId(BasePageRequest<JSONObject> pageRequest){
        renderJson(workService.queryTaskFileByWorkId(pageRequest));
    }

    /**
     * @author hmb
     * 查询归档项目列表
     */
    public void queryArchiveWorkList(BasePageRequest pageRequest){
        renderJson(workService.queryArchiveWorkList(pageRequest));
    }

    /**
     * @author hmb
     * 项目统计
     */
    public void workStatistics(){
        String workId = getPara("workId");
        renderJson(workService.workStatistics(workId));
    }

    /**
     * @author hmb
     * 查询项目成员
     */
    public void queryWorkOwnerList(){
        String workId = getPara("workId");
        renderJson(workService.queryWorkOwnerList(workId));
    }

    /**
     * @author hmb
     * 修改项目任务排序
     */
    public void updateOrder(){
        JSONObject jsonObject = JSON.parseObject(getRawData());
        renderJson(workService.updateOrder(jsonObject));
    }

    /**
     * @author hmb
     * 退出项目
     */
    public void leave(){
        String workId = getPara("workId");
        Integer userId = BaseUtil.getUserId().intValue();
        renderJson(workService.leave(workId,userId));
    }

    /**
     * @author hmb
     * 删除项目成员
     */
    public void removeWorkOwnerUser(){
        String workId = getPara("workId");
        Integer userId = getInt("ownerUserId");
        renderJson(workService.leave(workId,userId));
    }

    /**
     * 查询项目管理角色列表
     * @author wyq
     */
    public void queryRoleList(){
        renderJson(workService.queryRoleList());
    }

    /**
     * @author wyq
     * 查询项目设置成员管理列表
     */
    public void queryOwnerRoleList(@Para("workId") Integer workId){
        renderJson(workService.queryOwnerRoleList(workId));
    }

    /**
     * @author wyq
     * 保存项目角色管理设置
     */
    public void setOwnerRole(){
        JSONObject jsonObject = JSONObject.parseObject(getRawData());
        renderJson(workService.setOwnerRole(jsonObject));
    }

    /**
     * @author hmb
     * 删除任务列表
     */
    public void deleteTaskList(){
        String classId = getPara("classId");
        String workId = getPara("workId");
        if(!AuthUtil.isWorkAuth(workId,"taskClass:delete")){
            renderJson(R.noAuth());
            return;
        }
        renderJson(workService.deleteTaskList(workId,classId));
    }

    /**
     * @author hmb
     * 归档已完成的任务
     */
    public void archiveTask(){
        String classId = getPara("classId");
        renderJson(workService.archiveTask(classId));
    }

    /**
     * @author hmb
     * 归档任务
     */
    public void archList(){
        String workId = getPara("workId");
        renderJson(workService.archList(workId));
    }

    /**
     * @author hmb
     * 移除项目成员
     */
    public void remove(@Para("ownerUserId") Integer ownerUserId, @Para("workId") Integer workId){
        renderJson(workService.remove(ownerUserId,workId));
    }

    /**
     * @author hmb
     * 项目class排序
     */
    public void updateClassOrder(){
        JSONObject jsonObject = JSONObject.parseObject(getRawData());
        renderJson(workService.updateClassOrder(jsonObject));
    }


    /**
     * 撤销任务归档
     */
    public void activation(@Para("taskId") Integer taskId){
        renderJson(workService.activation(taskId));
    }
}
