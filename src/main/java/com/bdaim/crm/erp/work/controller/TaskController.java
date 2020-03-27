package com.bdaim.crm.erp.work.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.common.annotation.ClassTypeCheck;
import com.bdaim.crm.dao.LkCrmWorkDao;
import com.bdaim.crm.entity.LkCrmTaskEntity;
import com.bdaim.crm.entity.LkCrmTaskRelationEntity;
import com.bdaim.crm.entity.LkCrmWorkTaskClassEntity;
import com.bdaim.crm.erp.admin.service.LkAdminUserService;
import com.bdaim.crm.erp.oa.common.OaEnum;
import com.bdaim.crm.erp.work.entity.Task;
import com.bdaim.crm.erp.work.entity.TaskRelation;
import com.bdaim.crm.erp.work.entity.Work;
import com.bdaim.crm.erp.work.service.TaskService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.crm.utils.TagUtil;
import com.jfinal.core.paragetter.Para;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务
 */
@RestController
@RequestMapping("/task")
public class TaskController extends BasicAction {

    @Resource
    private TaskService taskService;
    @Resource
    private LkAdminUserService adminUserService;
    @Autowired
    private LkCrmWorkDao crmWorkDao;

    /**
     * @param taskClass 任务类别对象
     * @author Chacker
     * 设置任务类别
     */
    @RequestMapping(value = "/setTaskClass", method = RequestMethod.POST)
    public R setTaskClass(@Para("") LkCrmWorkTaskClassEntity taskClass) {
        return (taskService.setTaskClass(taskClass));
    }

    /**
     * @author Chacker
     * 交换任务列表排序
     */
    @RequestMapping(value = "/changeOrderTaskClass", method = RequestMethod.POST)
    public R changeOrderTaskClass() {
        String originalClassId = getPara("originalClassId");
        String targetClassId = getPara("targetClassId");
        taskService.changeOrderTaskClass(originalClassId, targetClassId);
        return (R.ok());
    }

    /**
     * @param task 任务对象
     * @author Chacker
     * 设置oa任务
     */
    @RequestMapping(value = "/setTask", method = RequestMethod.POST)
    public R setTask(LkCrmTaskEntity task) {
        if (task.getPid() != null && task.getPid() != 0) {
            boolean oaAuth = AuthUtil.isOaAuth(OaEnum.TASK_TYPE_KEY.getTypes(), task.getPid());
            if (oaAuth) {
                return (R.noAuth());
                //return;
            }
        }
        if (StrUtil.isNotEmpty(task.getOwnerUserId())) {
            task.setOwnerUserId(TagUtil.fromString(task.getOwnerUserId()));
        }
        if (task.getStartTime() != null && task.getStopTime() != null) {
            if (task.getStartTime().getTime() > task.getStopTime().getTime()) {
                return (R.error("开始时间不能大于结束时间"));
                //return;
            }
        }
        String customerIds = getPara("customerIds");
        String contactsIds = getPara("contactsIds");
        String businessIds = getPara("businessIds");
        String contractIds = getPara("contractIds");
        LkCrmTaskRelationEntity taskRelation = new LkCrmTaskRelationEntity();
        if (customerIds != null || contactsIds != null || businessIds != null || contractIds != null) {

            taskRelation.setBusinessIds(TagUtil.fromString(businessIds));
            taskRelation.setContactsIds(TagUtil.fromString(contactsIds));
            taskRelation.setContractIds(TagUtil.fromString(contractIds));
            taskRelation.setCustomerIds(TagUtil.fromString(customerIds));
        }
        return (taskService.setTask(task, taskRelation));
    }

    @RequestMapping(value = "/setWorkTask", method = RequestMethod.POST)
    public R setWorkTask(LkCrmTaskEntity task) {
        if (task.getWorkId() != null) {
            Integer isOpen = crmWorkDao.get(task.getWorkId()).getIsOpen();
            if (isOpen == 0 && !AuthUtil.isWorkAuth(task.getWorkId().toString(), "task:save")) {
                return (R.noAuth());
                //return;
            }
        }
        if (StrUtil.isNotEmpty(task.getOwnerUserId())) {
            task.setOwnerUserId(TagUtil.fromString(task.getOwnerUserId()));
        }
        if (task.getStartTime() != null && task.getStopTime() != null) {
            if (task.getStartTime().getTime() > task.getStopTime().getTime()) {
                return (R.error("开始时间不能大于结束时间"));
                //return;
            }
        }
        String customerIds = getPara("customerIds");
        String contactsIds = getPara("contactsIds");
        String businessIds = getPara("businessIds");
        String contractIds = getPara("contractIds");
        String leadsIds = getPara("leadsIds");
        LkCrmTaskRelationEntity taskRelation = new LkCrmTaskRelationEntity();
        if (customerIds != null || contactsIds != null || businessIds != null || contractIds != null|| leadsIds != null ) {

            taskRelation.setBusinessIds(TagUtil.fromString(businessIds));
            taskRelation.setContactsIds(TagUtil.fromString(contactsIds));
            taskRelation.setContractIds(TagUtil.fromString(contractIds));
            taskRelation.setCustomerIds(TagUtil.fromString(customerIds));
            taskRelation.setLeadsIds(TagUtil.fromString(leadsIds));
        }
        return (taskService.setTask(task, taskRelation));
    }


    /**
     * @author Chacker
     * 查询任务列表
     */
    @RequestMapping(value = "/getTaskList", method = RequestMethod.POST)
    @ClassTypeCheck(classType = BasePageRequest.class)
    public R getTaskList(BasePageRequest basePageRequest) {
        String labelId = getPara("labelId");
        String ishidden = getPara("ishidden");
        return (taskService.getTaskList(basePageRequest, labelId, ishidden));
    }


    /**
     * @author Chacker
     * 查询oa任务信息
     */
    @RequestMapping(value = "/queryTaskInfo", method = RequestMethod.POST)
    public R queryTaskInfo() {
        String taskId = getPara("taskId");
        boolean oaAuth = AuthUtil.isOaAuth(OaEnum.TASK_TYPE_KEY.getTypes(), Integer.valueOf(taskId));
        if (oaAuth) {
            return (R.noAuth());
            //return;
        }
        return (taskService.queryTaskInfo(taskId));
    }

    /**
     * @author Chacker
     * 查询项目任务详情
     */
    @RequestMapping(value = "/queryTaskById", method = RequestMethod.POST)
    public R queryTaskById() {
        String taskId = getPara("taskId");
        return (taskService.queryTaskInfo(taskId));
    }

    /**
     * 查询任务列表 oa
     */
    @ClassTypeCheck(classType = Task.class)
    @RequestMapping(value = "/queryTaskList", method = RequestMethod.POST)
    public R queryTaskList(BasePageRequest<Task> basePageRequest) {
        Integer type = getParaToInt("type");
        Integer status = getParaToInt("status");
        Integer priority = getParaToInt("priority");
        Integer date = getParaToInt("date");
        Integer mold = getParaToInt("mold");
        Long userId = getLong("userId");
        String name = getPara("search");
        List<Long> userIds = new ArrayList<>();
        if (mold == null) {
            userIds.add(BaseUtil.getUser().getUserId());
        } else if (mold == 1 && userId == null) {
            userIds = adminUserService.queryUserIdsByParentId(BaseUtil.getUser().getUserId());
        } else {
            List<Long> list = adminUserService.queryChileUserIds(BaseUtil.getUser().getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
            for (Long id : list) {
                if (id == userId) {
                    userIds.add(userId);
                }
            }
        }
        return (taskService.getTaskList(type, status, priority, date, userIds, basePageRequest, name));
    }

    /**
     * @author zxy
     * 根据任务id查询活动日志 oa
     * taskId 任务id
     */
    @RequestMapping(value = "/queryWorkTaskLog", method = RequestMethod.POST)
    public R queryWorkTaskLog() {
        Integer taskId = getParaToInt("taskId");
        boolean oaAuth = AuthUtil.isOaAuth(OaEnum.TASK_TYPE_KEY.getTypes(), taskId);
        if (oaAuth) {
            return (R.noAuth());
            //return;
        }
        return (taskService.queryWorkTaskLog(taskId));
    }

    /**
     * 根据任务id查询活动日志 work
     * taskId 任务id
     */
    @RequestMapping(value = "/queryTaskLog", method = RequestMethod.POST)
    public R queryTaskLog() {
        Integer taskId = getParaToInt("taskId");
        return (taskService.queryWorkTaskLog(taskId));
    }

    /**
     * 添加任务与业务关联
     */
    @RequestMapping(value = "/svaeTaskRelation", method = RequestMethod.POST)
    public R svaeTaskRelation(@Para("") LkCrmTaskRelationEntity taskRelation) {
        return (taskService.saveTaskRelation(taskRelation, BaseUtil.getUser().getUserId().intValue()));
    }

    /**
     * @author Chacker
     * 删除任务
     * taskId 任务id
     */
    @RequestMapping(value = "/deleteTask", method = RequestMethod.POST)
    public R deleteTask() {
        Integer taskId = getParaToInt("taskId");
        return (taskService.deleteTask(taskId));
    }

    /**
     * @author Chacker
     * crm查询关联任务
     */
    @RequestMapping(value = "/queryTaskRelation", method = RequestMethod.POST)
    public R queryTaskRelation(@Para("") BasePageRequest<TaskRelation> basePageRequest, TaskRelation taskRelation) {
        basePageRequest.setData(taskRelation);
        return (taskService.queryTaskRelation(basePageRequest));
    }

    /**
     * @param taskId
     * @author Chacker
     * 根据任务id归档任务
     */
    @RequestMapping(value = "/archiveByTaskId", method = RequestMethod.POST)
    public R archiveByTaskId(@Para("taskId") Integer taskId) {
        return (taskService.archiveByTaskId(taskId));
    }

    /**
     * 跟进记录类型设置
     */
    @RequestMapping(value = "/queryRecordOptions", method = RequestMethod.POST)
    public R queryRecordOptions() {
        return (taskService.queryRecordOptions());
    }

    /**
     * 设置任务类型
     */
    @RequestMapping(value = "/setRecordOptions", method = RequestMethod.POST)
    public R setRecordOptions(@RequestBody JSONObject jsonObject) {
        //JSONObject jsonObject = JSONObject.parseObject(getRawData());
        //JSONArray jsonArray = JSONArray.parseArray(jsonObject.getString("value"));
        //List<String> list = jsonArray.toJavaList(String.class);
        List<String> list = (List<String>) jsonObject.get("value");
        return (taskService.setRecordOptions(list));
    }
}
